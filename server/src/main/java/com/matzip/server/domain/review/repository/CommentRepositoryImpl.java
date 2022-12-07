package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.model.CommentProperty;
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
import static com.matzip.server.domain.review.model.CommentProperty.USER_NUMBER_OF_FOLLOWERS;
import static com.matzip.server.domain.review.model.QComment.comment;
import static com.matzip.server.domain.review.model.QReview.review;
import static com.matzip.server.domain.user.model.QUser.user;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<Comment> searchCommentsByKeyword(CommentDto.SearchRequest searchRequest) {
        return searchWithConditions(
                searchRequest.getAsc() ? Order.ASC : Order.DESC,
                searchRequest.getSort(),
                PageRequest.of(searchRequest.getPage(), searchRequest.getSize()),
                commentContentContaining(searchRequest.getKeyword()));
    }

    @Override
    public Slice<Comment> searchMyCommentsByKeyword(CommentDto.SearchRequest searchRequest, Long myId) {
        return searchWithConditions(
                searchRequest.getAsc() ? Order.ASC : Order.DESC,
                searchRequest.getSort(),
                PageRequest.of(searchRequest.getPage(), searchRequest.getSize()),
                commentContentContaining(searchRequest.getKeyword()),
                user.id.eq(myId));
    }

    private BooleanExpression commentContentContaining(String keyword) {
        return keyword == null ? null : comment.content.contains(keyword);
    }

    private Slice<Comment> searchWithConditions(
            Order order, CommentProperty commentProperty, Pageable pageable, BooleanExpression... conditions) {
        List<Comment> comments;
        OrderSpecifier<LocalDateTime> defaultOrder = new OrderSpecifier<>(Order.DESC, review.createdAt);

        if (commentProperty == USER_NUMBER_OF_FOLLOWERS) {
            NumberPath<Long> followers = Expressions.numberPath(Long.class, "followers");

            comments = jpaQueryFactory
                    .select(comment, follow.count().as(followers))
                    .from(comment)
                    .leftJoin(comment.user, user).fetchJoin()
                    .leftJoin(user.followers, follow)
                    .groupBy(comment, user)
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, followers), defaultOrder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch()
                    .stream().map(t -> t.get(comment)).collect(Collectors.toList());
        } else {
            comments = jpaQueryFactory
                    .select(comment)
                    .from(comment)
                    .leftJoin(comment.user, user).fetchJoin()
                    .where(conditions)
                    .orderBy(new OrderSpecifier<>(order, comment.createdAt))
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1)
                    .fetch();
        }

        boolean hasNext = false;
        if (comments.size() > pageable.getPageSize()) {
            comments.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(comments, pageable, hasNext);
    }
}
