package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.model.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    Optional<Scrap> findByUserIdAndReviewId(Long userId, Long reviewId);

    @Modifying
    @Query("DELETE FROM Scrap s WHERE s.review.id = :reviewId")
    void deleteAllByReviewId(@Param("reviewId") Long reviewId);

    @Modifying
    @Query("DELETE FROM Scrap s WHERE s.user.id = :userId OR s.review.id in :reviewIds")
    void deleteAllByUserIdOrReviewIds(@Param("userId") Long userId, @Param("reviewIds") List<Long> reviewIds);
}