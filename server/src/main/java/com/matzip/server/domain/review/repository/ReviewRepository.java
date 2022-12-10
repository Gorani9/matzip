package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {
    Optional<Review> findById(Long id);
}