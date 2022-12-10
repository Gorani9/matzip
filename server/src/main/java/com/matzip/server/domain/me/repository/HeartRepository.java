package com.matzip.server.domain.me.repository;

import com.matzip.server.domain.me.model.Heart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HeartRepository extends JpaRepository<Heart, Long> {
    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);
    Optional<Heart> findByUserIdAndReviewId(Long userId, Long reviewId);
}