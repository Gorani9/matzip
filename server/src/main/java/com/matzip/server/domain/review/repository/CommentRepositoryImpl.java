package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.model.Comment;
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
import static com.matzip.server.domain.review.model.CommentProperty.USER_NUMBER_OF_FOLLOWERS;
import static com.matzip.server.domain.review.model.QComment.comment;
import static com.matzip.server.domain.review.model.QReview.review;
import static com.matzip.server.domain.user.model.QUser.user;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<Comment> searchCommentByKeyword(CommentDto.SearchRequest searchRequest) {
        List<Comment> comments;
        Order order = searchRequest.getAsc() ? Order.ASC : Order.DESC;
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        OrderSpecifier<LocalDateTime> defaultOrder = new OrderSpecifier<>(Order.DESC, review.createdAt);

        if (searchRequest.getSort() == USER_NUMBER_OF_FOLLOWERS) {
            NumberPath<Long> followers = Expressions.numberPath(Long.class, "followers");

            comments = jpaQueryFactory
                    .select(comment, follow.count().as(followers))
                    .from(comment)
                    .leftJoin(comment.user, user).fetchJoin()
                    .leftJoin(user.followers, follow)
                    .groupBy(comment, user)
                    .where(comment.content.contains(searchRequest.getKeyword()))
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
                    .where(comment.content.contains(searchRequest.getKeyword()))
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
