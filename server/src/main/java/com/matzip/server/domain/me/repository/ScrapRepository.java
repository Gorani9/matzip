package com.matzip.server.domain.me.repository;

import com.matzip.server.domain.me.model.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long>, ScrapRepositoryCustom {
    Optional<Scrap> findByUserIdAndReviewId(Long userId, Long reviewId);
}