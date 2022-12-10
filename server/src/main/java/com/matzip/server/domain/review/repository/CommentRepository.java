package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
}