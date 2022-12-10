package com.matzip.server.domain.image.repository;

import com.matzip.server.domain.image.model.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
}
