package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.model.CommentProperty;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.matzip.server.domain.review.model.QComment.comment;
import static com.matzip.server.domain.review.model.QReview.review;
import static com.matzip.server.domain.user.model.QUser.user;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<Comment> searchMyCommentsByKeyword(CommentDto.SearchRequest searchRequest, Long myId) {
        return searchWithConditions(
                searchRequest.getAsc() ? Order.ASC : Order.DESC,
                searchRequest.getSort(),
                PageRequest.of(searchRequest.getPage(), searchRequest.getSize()),
                commentContentContaining(searchRequest.getKeyword()),
                user.id.eq(myId),
                comment.blocked.isFalse(),
                comment.deleted.isFalse());
    }

    private BooleanExpression commentContentContaining(String keyword) {
        return keyword == null || keyword.isBlank() ? null : comment.content.contains(keyword);
    }

    private Slice<Comment> searchWithConditions(
            Order order, CommentProperty commentProperty, Pageable pageable, BooleanExpression... conditions) {
        List<Comment> comments;
        OrderSpecifier<LocalDateTime> defaultOrder = new OrderSpecifier<>(Order.DESC, review.createdAt);

        comments = jpaQueryFactory
                .select(comment)
                .from(comment)
                .leftJoin(comment.user, user).fetchJoin().leftJoin(user.userImage).fetchJoin()
                .where(conditions)
                .orderBy(new OrderSpecifier<>(order, comment.createdAt))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (comments.size() > pageable.getPageSize()) {
            comments.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(comments, pageable, hasNext);
    }
}
