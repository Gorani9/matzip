package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {
    Optional<Review> findById(Long id);

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.comments WHERE r.id = :id")
    Optional<Review> findByIdFetchJoinComments(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.user.id = :userId OR r.id in :reviewIds")
    void deleteAllByUserIdOrReviewIds(@Param("userId") Long userId, @Param("reviewIds") List<Long> reviewIds);
}