package com.matzip.server.domain.user.repository;

import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
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
    public Slice<User> searchUnlockedNormalUserByUsername(UserDto.SearchRequest searchRequest) {
        List<User> users;
        Order order = searchRequest.getAsc() ? Order.ASC : Order.DESC;
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        OrderSpecifier<LocalDateTime> defaultOrder = new OrderSpecifier<>(Order.DESC, user.createdAt);

        if (searchRequest.getSort() == USERNAME) {
            users = jpaQueryFactory
                    .select(user)
                    .from(user)
                    .where(user.username.contains(searchRequest.getUsername()))
                    .orderBy(new OrderSpecifier<>(order, user.username), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (searchRequest.getSort() == MATZIP_LEVEL) {
            users = jpaQueryFactory
                    .select(user)
                    .from(user)
                    .where(user.username.contains(searchRequest.getUsername()))
                    .orderBy(new OrderSpecifier<>(order, user.matzipLevel), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (searchRequest.getSort() == NUMBER_OF_FOLLOWERS) {
            NumberPath<Long> followers = Expressions.numberPath(Long.class, "followers");

            users = jpaQueryFactory
                    .select(user, user.count().as(followers))
                    .from(follow)
                    .rightJoin(follow.followee, user).on(follow.followee.id.eq(user.id))
                    .groupBy(user)
                    .where(user.username.contains(searchRequest.getUsername()))
                    .orderBy(new OrderSpecifier<>(order, followers), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(user)).collect(Collectors.toList());
        } else {
            users = jpaQueryFactory
                    .select(user)
                    .from(user)
                    .where(user.username.contains(searchRequest.getUsername()))
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
