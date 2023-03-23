package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.model.Heart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HeartRepository extends JpaRepository<Heart, Long> {
    Optional<Heart> findByUserIdAndReviewId(Long userId, Long reviewId);

    @Modifying
    @Query("DELETE FROM Heart h WHERE h.review.id = :reviewId")
    void deleteAllByReviewId(@Param("reviewId") Long reviewId);

    @Modifying
    @Query("DELETE FROM Heart h WHERE h.user.id = :userId OR h.review.id in :reviewIds")
    void deleteAllByUserIdOrReviewIds(@Param("userId") Long userId, @Param("reviewIds") List<Long> reviewIds);
}