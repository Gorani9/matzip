package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.model.ReviewProperty;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
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

import static com.matzip.server.domain.me.model.QFollow.follow;
import static com.matzip.server.domain.me.model.QHeart.heart;
import static com.matzip.server.domain.me.model.QScrap.scrap;
import static com.matzip.server.domain.review.model.QComment.comment;
import static com.matzip.server.domain.review.model.QReview.review;
import static com.matzip.server.domain.review.model.ReviewProperty.*;
import static com.matzip.server.domain.user.model.QUser.user;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<Review> searchReviewsByKeyword(ReviewDto.SearchRequest searchRequest) {
        return searchWithConditions(
                searchRequest.getAsc() ? Order.ASC : Order.DESC,
                searchRequest.getSort(),
                PageRequest.of(searchRequest.getPage(), searchRequest.getSize()),
                reviewContentContaining(searchRequest.getKeyword()),
                review.blocked.isFalse(),
                review.deleted.isFalse());
    }

    @Override
    public Slice<Review> searchMyReviewsByKeyword(ReviewDto.SearchRequest searchRequest, Long myId) {
        return searchWithConditions(
                searchRequest.getAsc() ? Order.ASC : Order.DESC,
                searchRequest.getSort(),
                PageRequest.of(searchRequest.getPage(), searchRequest.getSize()),
                reviewContentContaining(searchRequest.getKeyword()),
                user.id.eq(myId),
                review.blocked.isFalse(),
                review.deleted.isFalse());
    }

    @Override
    public List<Review> fetchHotReviews(LocalDateTime from, int size) {
        return searchWithConditions(
                Order.DESC,
                NUMBER_OF_HEARTS,
                PageRequest.of(0, size),
                from == null ? null : review.createdAt.after(from),
                review.blocked.isFalse(),
                review.deleted.isFalse()).getContent();
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
                    .leftJoin(review.user, user).fetchJoin().leftJoin(user.userImage).fetchJoin()
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, review.user.username), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (reviewProperty == REVIEWER_MATZIP_LEVEL) {
            reviews = jpaQueryFactory
                    .select(review)
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin().leftJoin(user.userImage).fetchJoin()
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, review.user.matzipLevel), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (reviewProperty == ReviewProperty.REVIEWER_NUMBER_OF_FOLLOWERS) {
            NumberPath<Long> followers = Expressions.numberPath(Long.class, "followers");

            reviews = jpaQueryFactory
                    .select(review, follow.count().as(followers))
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin().leftJoin(user.userImage).fetchJoin()
                    .leftJoin(user.followers, follow)
                    .groupBy(review)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, followers), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(review)).collect(Collectors.toList());
        } else if (reviewProperty == NUMBER_OF_HEARTS) {
            NumberPath<Long> hearts = Expressions.numberPath(Long.class, "hearts");

            reviews = jpaQueryFactory
                    .select(review, heart.count().as(hearts))
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin().leftJoin(user.userImage).fetchJoin()
                    .leftJoin(review.hearts, heart)
                    .groupBy(review)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, hearts), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(review)).collect(Collectors.toList());
        } else if (reviewProperty == NUMBER_OF_SCRAPS) {
            NumberPath<Long> scraps = Expressions.numberPath(Long.class, "scraps");

            reviews = jpaQueryFactory
                    .select(review, scrap.count().as(scraps))
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin().leftJoin(user.userImage).fetchJoin()
                    .leftJoin(review.scraps, scrap)
                    .groupBy(review)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, scraps), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(review)).collect(Collectors.toList());
        } else if (reviewProperty == NUMBER_OF_COMMENTS) {
            NumberPath<Integer> commentCount = Expressions.numberPath(Integer.class, "comments");
            NumberExpression<Integer> validCommentCount = new CaseBuilder()
                    .when(comment.deleted.isFalse()).then(1)
                    .otherwise(0);

            reviews = jpaQueryFactory
                    .select(review, validCommentCount.sum().as(commentCount))
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin().leftJoin(user.userImage).fetchJoin()
                    .leftJoin(review.comments, comment)
                    .groupBy(review)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, commentCount), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(review)).collect(Collectors.toList());
        } else if (reviewProperty == RATING) {
            reviews = jpaQueryFactory
                    .select(review)
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin().leftJoin(user.userImage).fetchJoin()
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, review.rating), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else {
            reviews = jpaQueryFactory
                    .select(review)
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin().leftJoin(user.userImage).fetchJoin()
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
