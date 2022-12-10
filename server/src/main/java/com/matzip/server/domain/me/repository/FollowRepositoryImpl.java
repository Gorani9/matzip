package com.matzip.server.domain.me.repository;

import com.matzip.server.domain.me.model.Follow;
import com.matzip.server.domain.me.model.QFollow;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.QUser;
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
import static com.matzip.server.domain.user.model.UserProperty.*;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    private final QUser followee = new QUser("followee");
    private final QUser follower = new QUser("follower");

    @Override
    public Slice<User> searchMyFollowersByUsername(UserDto.SearchRequest searchRequest, Long myId) {
        return searchWithConditions(
                searchRequest.getAsc() ? Order.ASC : Order.DESC,
                searchRequest.getSort(),
                PageRequest.of(searchRequest.getPage(), searchRequest.getSize()),
                follower,
                usernameContaining(follower, searchRequest.getUsername()),
                followee.id.eq(myId),
                follower.blocked.isFalse(),
                follower.deleted.isFalse()).map(Follow::getFollower);
    }

    @Override
    public Slice<User> searchMyFollowingsByUsername(UserDto.SearchRequest searchRequest, Long myId) {
        return searchWithConditions(
                searchRequest.getAsc() ? Order.ASC : Order.DESC,
                searchRequest.getSort(),
                PageRequest.of(searchRequest.getPage(), searchRequest.getSize()),
                followee,
                usernameContaining(followee, searchRequest.getUsername()),
                follower.id.eq(myId),
                followee.blocked.isFalse(),
                followee.deleted.isFalse()).map(Follow::getFollowee);
    }

    private BooleanExpression usernameContaining(QUser qUser, String username) {
        return username == null || username.isBlank() ? null : qUser.username.contains(username);
    }

    private Slice<Follow> searchWithConditions(
            Order order, UserProperty userProperty, Pageable pageable, QUser user, BooleanExpression... conditions) {
        List<Follow> follows;
        OrderSpecifier<LocalDateTime> defaultOrder = new OrderSpecifier<>(Order.DESC, user.createdAt);

        if (userProperty == USERNAME) {
            follows = jpaQueryFactory
                    .select(follow)
                    .from(follow)
                    .leftJoin(follow.followee, followee).fetchJoin()
                    .leftJoin(follow.follower, follower).fetchJoin()
                    .leftJoin(user.userImage).fetchJoin()
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, user.username), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (userProperty == MATZIP_LEVEL) {
            follows = jpaQueryFactory
                    .select(follow)
                    .from(follow)
                    .leftJoin(follow.followee, followee).fetchJoin()
                    .leftJoin(follow.follower, follower).fetchJoin()
                    .leftJoin(user.userImage).fetchJoin()
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, user.matzipLevel), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (userProperty == NUMBER_OF_FOLLOWERS) {
            NumberPath<Long> followers = Expressions.numberPath(Long.class, "followers");
            QFollow otherFollow = new QFollow("other_follow");

            follows = jpaQueryFactory
                    .select(follow, otherFollow.count().as(followers))
                    .from(follow)
                    .leftJoin(follow.followee, followee).fetchJoin()
                    .leftJoin(follow.follower, follower).fetchJoin()
                    .leftJoin(user.followers, otherFollow)
                    .leftJoin(user.userImage).fetchJoin()
                    .groupBy(follow)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, followers), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(follow)).collect(Collectors.toList());
        } else {
            follows = jpaQueryFactory
                    .select(follow)
                    .from(follow)
                    .leftJoin(follow.followee, followee).fetchJoin()
                    .leftJoin(follow.follower, follower).fetchJoin()
                    .leftJoin(user.userImage).fetchJoin()
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, user.createdAt), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        }

        boolean hasNext = false;
        if (follows.size() > pageable.getPageSize()) {
            follows.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(follows, pageable, hasNext);
    }
}
