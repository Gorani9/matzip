package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.model.Comment;
import org.springframework.data.domain.Slice;

public interface CommentRepositoryCustom {
    Slice<Comment> searchMyCommentsByKeyword(CommentDto.SearchRequest searchRequest, Long myId);
}
