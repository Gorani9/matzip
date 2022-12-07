package com.matzip.server.domain.user.repository;

import com.matzip.server.domain.admin.dto.AdminDto;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.model.UserProperty;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.matzip.server.domain.me.model.QFollow.follow;
import static com.matzip.server.domain.user.model.QUser.user;
import static com.matzip.server.domain.user.model.UserProperty.*;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<User> searchUsersByUsername(UserDto.SearchRequest searchRequest) {
        return searchWithConditions(
                searchRequest.getAsc() ? Order.ASC : Order.DESC,
                searchRequest.getSort(),
                PageRequest.of(searchRequest.getPage(), searchRequest.getSize()),
                usernameContaining(searchRequest.getUsername()),
                user.blocked.isFalse(),
                user.deleted.isFalse());
    }

    @Override
    public Slice<User> adminSearchUsersByUsername(AdminDto.UserSearchRequest searchRequest) {
        return searchWithConditions(
                searchRequest.getAsc() ? Order.ASC : Order.DESC,
                searchRequest.getSort(),
                PageRequest.of(searchRequest.getPage(), searchRequest.getSize()),
                usernameContaining(searchRequest.getUsername()),
                searchRequest.getWithBlocked() ? null : user.blocked.isFalse(),
                user.deleted.isFalse());
    }

    private BooleanExpression usernameContaining(String username) {
        return username == null ? null : user.username.contains(username);
    }

    private Slice<User> searchWithConditions(
            Order order, UserProperty userProperty, Pageable pageable, BooleanExpression... conditions) {
        List<User> users;
        OrderSpecifier<LocalDateTime> defaultOrder = new OrderSpecifier<>(Order.DESC, user.createdAt);

        if (userProperty == USERNAME) {
            users = jpaQueryFactory
                    .select(user)
                    .from(user)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, user.username), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (userProperty == MATZIP_LEVEL) {
            users = jpaQueryFactory
                    .select(user)
                    .from(user)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, user.matzipLevel), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (userProperty == NUMBER_OF_FOLLOWERS) {
            NumberPath<Long> followers = Expressions.numberPath(Long.class, "followers");

            users = jpaQueryFactory
                    .select(user, user.count().as(followers))
                    .from(follow)
                    .rightJoin(follow.followee, user)
                    .groupBy(user)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, followers), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(user)).collect(Collectors.toList());
        } else {
            users = jpaQueryFactory
                    .select(user)
                    .from(user)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, user.createdAt))
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        }

        boolean hasNext = false;
        if (users.size() > pageable.getPageSize()) {
            users.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(users, pageable, hasNext);
    }
}
