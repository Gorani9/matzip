package com.matzip.server.domain.me.repository;

import com.matzip.server.domain.me.dto.ScrapDto;
import com.matzip.server.domain.me.model.QScrap;
import com.matzip.server.domain.me.model.Scrap;
import com.matzip.server.domain.me.model.ScrapProperty;
import com.matzip.server.domain.user.model.QUser;
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
import static com.matzip.server.domain.me.model.QHeart.heart;
import static com.matzip.server.domain.me.model.QScrap.scrap;
import static com.matzip.server.domain.me.model.ScrapProperty.*;
import static com.matzip.server.domain.review.model.QComment.comment;
import static com.matzip.server.domain.review.model.QReview.review;
import static com.matzip.server.domain.user.model.QUser.user;

@Repository
@RequiredArgsConstructor
public class ScrapRepositoryImpl implements ScrapRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<Scrap> searchMyScrapsByKeyword(ScrapDto.SearchRequest searchRequest, Long myId) {
        return searchWithConditions(
                searchRequest.getAsc() ? Order.ASC : Order.DESC,
                searchRequest.getSort(),
                PageRequest.of(searchRequest.getPage(), searchRequest.getSize()),
                reviewContentContaining(searchRequest.getKeyword()),
                scrap.user.id.eq(myId));
    }

    private BooleanExpression reviewContentContaining(String keyword) {
        return keyword == null ? null : review.content.contains(keyword);
    }

    private Slice<Scrap> searchWithConditions(
            Order order, ScrapProperty scrapProperty, Pageable pageable, BooleanExpression... conditions) {
        List<Scrap> scraps;
        OrderSpecifier<LocalDateTime> defaultOrder = new OrderSpecifier<>(Order.DESC, scrap.createdAt);
        QUser reviewer = new QUser("reviewer");

        if (scrapProperty == REVIEWER_USERNAME) {
            scraps = jpaQueryFactory
                    .select(scrap)
                    .from(scrap)
                    .leftJoin(scrap.review, review).fetchJoin()
                    .leftJoin(scrap.user, user).fetchJoin()
                    .leftJoin(review.user, reviewer).fetchJoin()
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, reviewer.username), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (scrapProperty == REVIEWER_MATZIP_LEVEL) {
            scraps = jpaQueryFactory
                    .select(scrap)
                    .from(scrap)
                    .leftJoin(scrap.review, review).fetchJoin()
                    .leftJoin(scrap.user, user).fetchJoin()
                    .leftJoin(review.user, reviewer).fetchJoin()
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, reviewer.matzipLevel), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (scrapProperty == REVIEWER_NUMBER_OF_FOLLOWERS) {
            NumberPath<Long> followers = Expressions.numberPath(Long.class, "followers");

            scraps = jpaQueryFactory
                    .select(scrap, follow.count().as(followers))
                    .from(scrap)
                    .leftJoin(scrap.review, review).fetchJoin()
                    .leftJoin(scrap.user, user).fetchJoin()
                    .leftJoin(review.user, reviewer).fetchJoin()
                    .leftJoin(reviewer.followers, follow)
                    .groupBy(scrap, reviewer)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, followers), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(scrap)).collect(Collectors.toList());
        } else if (scrapProperty == REVIEW_NUMBER_OF_HEARTS) {
            NumberPath<Long> hearts = Expressions.numberPath(Long.class, "hearts");

            scraps = jpaQueryFactory
                    .select(scrap, heart.count().as(hearts))
                    .from(scrap)
                    .leftJoin(scrap.review, review).fetchJoin()
                    .leftJoin(scrap.user, user).fetchJoin()
                    .leftJoin(review.user, reviewer).fetchJoin()
                    .leftJoin(review.hearts, heart)
                    .groupBy(scrap, review)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, hearts), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(scrap)).collect(Collectors.toList());
        } else if (scrapProperty == REVIEW_NUMBER_OF_SCRAPS) {
            QScrap otherScrap = new QScrap("other_scrap");
            NumberPath<Long> scrapCount = Expressions.numberPath(Long.class, "scraps");

            scraps = jpaQueryFactory
                    .select(scrap, scrap.count().as(scrapCount))
                    .from(scrap)
                    .leftJoin(scrap.review, review).fetchJoin()
                    .leftJoin(scrap.user, user).fetchJoin()
                    .leftJoin(review.user, reviewer).fetchJoin()
                    .leftJoin(review.scraps, otherScrap)
                    .groupBy(scrap, review)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, scrapCount), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(scrap)).collect(Collectors.toList());
        } else if (scrapProperty == REVIEW_NUMBER_OF_COMMENTS) {
            NumberPath<Long> comments = Expressions.numberPath(Long.class, "comments");

            scraps = jpaQueryFactory
                    .select(scrap, comment.count().as(comments))
                    .from(scrap)
                    .leftJoin(scrap.review, review).fetchJoin()
                    .leftJoin(scrap.user, user).fetchJoin()
                    .leftJoin(review.user, reviewer).fetchJoin()
                    .leftJoin(review.comments, comment)
                    .groupBy(scrap, user)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, comments), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(scrap)).collect(Collectors.toList());
        } else if (scrapProperty == REVIEW_RATING) {
            scraps = jpaQueryFactory
                    .select(scrap)
                    .from(scrap)
                    .leftJoin(scrap.review, review).fetchJoin()
                    .leftJoin(scrap.user, user).fetchJoin()
                    .leftJoin(review.user, reviewer).fetchJoin()
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, review.rating), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (scrapProperty == REVIEW_CREATED_AT) {
            scraps = jpaQueryFactory
                    .select(scrap)
                    .from(scrap)
                    .leftJoin(scrap.review, review).fetchJoin()
                    .leftJoin(scrap.user, user).fetchJoin()
                    .leftJoin(review.user, reviewer).fetchJoin()
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, review.createdAt), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else {
            scraps = jpaQueryFactory
                    .select(scrap)
                    .from(scrap)
                    .leftJoin(scrap.review, review).fetchJoin()
                    .leftJoin(scrap.user, user).fetchJoin()
                    .leftJoin(review.user, reviewer).fetchJoin()
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, scrap.createdAt))
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        }

        boolean hasNext = false;
        if (scraps.size() > pageable.getPageSize()) {
            scraps.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(scraps, pageable, hasNext);
    }
}
