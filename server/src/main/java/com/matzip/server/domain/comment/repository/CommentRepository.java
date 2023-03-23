package com.matzip.server.domain.comment.repository;

import com.matzip.server.domain.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.review WHERE c.id = :id")
    Optional<Comment> findById(Long id);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.review.id = :reviewId")
    void deleteAllByReviewId(@Param("reviewId")Long reviewId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.user.id = :userId OR c.review.id in :reviewIds")
    void deleteAllByUserIdOrReviewIds(@Param("userId") Long userId, @Param("reviewIds") List<Long> reviewIds);
}