package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.model.ReviewProperty;
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
import static com.matzip.server.domain.review.model.QComment.comment;
import static com.matzip.server.domain.review.model.QReview.review;
import static com.matzip.server.domain.user.model.QUser.user;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<Review> searchReviewByKeyword(ReviewDto.SearchRequest searchRequest) {
        List<Review> reviews;
        Order order = searchRequest.getAsc() ? Order.ASC : Order.DESC;
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        OrderSpecifier<LocalDateTime> defaultOrder = new OrderSpecifier<>(Order.DESC, review.createdAt);

        if (searchRequest.getSort() == ReviewProperty.REVIEWER_USERNAME) {
            reviews = jpaQueryFactory
                    .select(review)
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin()
                    .where(review.content.contains(searchRequest.getKeyword()))
                    .orderBy(new OrderSpecifier<>(order, review.user.username), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (searchRequest.getSort() == ReviewProperty.REVIEWER_MATZIP_LEVEL) {
            reviews = jpaQueryFactory
                    .select(review)
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin()
                    .where(review.content.contains(searchRequest.getKeyword()))
                    .orderBy(new OrderSpecifier<>(order, review.user.matzipLevel), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else if (searchRequest.getSort() == ReviewProperty.REVIEWER_NUMBER_OF_FOLLOWERS) {
            NumberPath<Long> followers = Expressions.numberPath(Long.class, "followers");

            reviews = jpaQueryFactory
                    .select(review, follow.count().as(followers))
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin()
                    .leftJoin(user.followers, follow)
                    .groupBy(review, user)
                    .where(review.content.contains(searchRequest.getKeyword()))
                    .orderBy(new OrderSpecifier<>(order, followers), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(review)).collect(Collectors.toList());
        } else if (searchRequest.getSort() == ReviewProperty.NUMBER_OF_HEARTS) {
            NumberPath<Long> hearts = Expressions.numberPath(Long.class, "hearts");

            reviews = jpaQueryFactory
                    .select(review, heart.count().as(hearts))
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin()
                    .leftJoin(review.hearts, heart)
                    .groupBy(review, user)
                    .where(review.content.contains(searchRequest.getKeyword()))
                    .orderBy(new OrderSpecifier<>(order, hearts), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(review)).collect(Collectors.toList());
        } else if (searchRequest.getSort() == ReviewProperty.NUMBER_OF_SCRAPS) {
            NumberPath<Long> scraps = Expressions.numberPath(Long.class, "scraps");

            reviews = jpaQueryFactory
                    .select(review, scrap.count().as(scraps))
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin()
                    .leftJoin(review.scraps, scrap)
                    .groupBy(review, user)
                    .where(review.content.contains(searchRequest.getKeyword()))
                    .orderBy(new OrderSpecifier<>(order, scraps), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(review)).collect(Collectors.toList());
        } else if (searchRequest.getSort() == ReviewProperty.NUMBER_OF_COMMENTS) {
            NumberPath<Long> comments = Expressions.numberPath(Long.class, "comments");

            reviews = jpaQueryFactory
                    .select(review, comment.count().as(comments))
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin()
                    .leftJoin(review.comments, comment)
                    .groupBy(review, user)
                    .where(review.content.contains(searchRequest.getKeyword()))
                    .orderBy(new OrderSpecifier<>(order, comments), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(review)).collect(Collectors.toList());
        } else if (searchRequest.getSort() == ReviewProperty.RATING) {
            reviews = jpaQueryFactory
                    .select(review)
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin()
                    .where(review.content.contains(searchRequest.getKeyword()))
                    .orderBy(new OrderSpecifier<>(order, review.rating), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        } else {
            reviews = jpaQueryFactory
                    .select(review)
                    .from(review)
                    .leftJoin(review.user, user).fetchJoin()
                    .where(review.content.contains(searchRequest.getKeyword()))
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

    @Override
    public List<Review> fetchHotReviews(LocalDateTime from, int size) {
        Pageable pageable = PageRequest.of(0, size);
        NumberPath<Long> hearts = Expressions.numberPath(Long.class, "hearts");
        BooleanExpression after = from == null ? null : review.createdAt.after(from);

        return jpaQueryFactory
                .select(review, heart.count().as(hearts))
                .from(review)
                .leftJoin(review.user, user).fetchJoin()
                .leftJoin(review.hearts, heart).on(review.id.eq(heart.review.id))
                .groupBy(review, user)
                .where(after)
                .orderBy(new OrderSpecifier<>(Order.DESC, hearts), new OrderSpecifier<>(Order.DESC, review.createdAt))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream().map(t -> t.get(review)).collect(Collectors.toList());
    }
}
