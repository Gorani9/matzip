package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.model.ReviewProperty;
import com.matzip.server.domain.search.dto.SearchDto.ReviewSearch;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.*;
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

import static com.matzip.server.domain.comment.model.QComment.comment;
import static com.matzip.server.domain.review.model.QHeart.heart;
import static com.matzip.server.domain.review.model.QReview.review;
import static com.matzip.server.domain.review.model.QScrap.scrap;
import static com.matzip.server.domain.review.model.ReviewProperty.*;
import static com.matzip.server.domain.user.model.QFollow.follow;
import static com.matzip.server.domain.user.model.QUser.user;
import static com.querydsl.core.types.dsl.Expressions.numberPath;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<Review> searchReviewsByKeyword(ReviewSearch searchRequest) {
        return searchWithConditions(
                searchRequest.asc() ? Order.ASC : Order.DESC,
                searchRequest.sort(),
                PageRequest.of(searchRequest.page(), searchRequest.size()),
                reviewContentContaining(searchRequest.keyword()));
    }

    @Override
    public Slice<Review> searchReviewsByKeywordUsingFullText(ReviewSearch searchRequest) {
        return searchWithConditions(
                searchRequest.asc() ? Order.ASC : Order.DESC,
                searchRequest.sort(),
                PageRequest.of(searchRequest.page(), searchRequest.size()),
                reviewContentMatchAgainst(searchRequest.keyword()));
    }

    private BooleanExpression reviewContentMatchAgainst(String keyword) {
        NumberTemplate<Double> score = Expressions.numberTemplate(
                Double.class, "function('match',{0},{1})", review.content, keyword + "*");
        return score.gt(0);
    }

    private BooleanExpression reviewContentContaining(String keyword) {
        return keyword == null || keyword.isBlank() ? null : review.content.contains(keyword);
    }

    private Slice<Review> searchWithConditions(
            Order order, ReviewProperty reviewProperty, Pageable pageable, BooleanExpression... conditions) {
        List<Review> reviews;
        OrderSpecifier<LocalDateTime> defaultOrder = new OrderSpecifier<>(Order.DESC, review.createdAt);

        if (reviewProperty == REVIEWER_USERNAME) {
            reviews = jpaQueryFactory
                    .select(review)
                    .from(review)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, review.user.username), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (reviewProperty == REVIEWER_MATZIP_LEVEL) {
            reviews = jpaQueryFactory
                    .select(review)
                    .from(review)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, review.user.matzipLevel), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (reviewProperty == ReviewProperty.REVIEWER_NUMBER_OF_FOLLOWERS) {
            NumberPath<Long> followers = numberPath(Long.class, "followers");

            reviews = jpaQueryFactory
                    .select(review, follow.count().as(followers))
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin()
                    .leftJoin(user.followers, follow)
                    .groupBy(review)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, followers), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(review)).collect(Collectors.toList());
        } else if (reviewProperty == NUMBER_OF_HEARTS) {
            NumberPath<Long> hearts = numberPath(Long.class, "hearts");

            reviews = jpaQueryFactory
                    .select(review, heart.count().as(hearts))
                    .from(review)
                    .leftJoin(review.hearts, heart)
                    .groupBy(review)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, hearts), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(review)).collect(Collectors.toList());
        } else if (reviewProperty == NUMBER_OF_SCRAPS) {
            NumberPath<Long> scraps = numberPath(Long.class, "scraps");

            reviews = jpaQueryFactory
                    .select(review, scrap.count().as(scraps))
                    .from(review)
                    .leftJoin(review.scraps, scrap)
                    .groupBy(review)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, scraps), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(review)).collect(Collectors.toList());
        } else if (reviewProperty == NUMBER_OF_COMMENTS) {
            NumberPath<Long> comments = numberPath(Long.class, "comments");

            reviews = jpaQueryFactory
                    .select(review, comment.count().as(comments))
                    .from(review)
                    .leftJoin(review.comments, comment)
                    .groupBy(review)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, comments), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(review)).collect(Collectors.toList());
        } else if (reviewProperty == RATING) {
            reviews = jpaQueryFactory
                    .select(review)
                    .from(review)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, review.rating), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else {
            reviews = jpaQueryFactory
                    .select(review)
                    .from(review)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, review.createdAt))
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        }

        boolean hasNext = false;
        if (reviews.size() > pageable.getPageSize()) {
            reviews.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(reviews, pageable, hasNext);
    }
}
