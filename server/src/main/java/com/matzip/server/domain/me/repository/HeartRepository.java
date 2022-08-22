package com.matzip.server.domain.me.repository;

import com.matzip.server.domain.me.model.Heart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeartRepository extends JpaRepository<Heart, Long> {
    void deleteByUser_UsernameAndReview_Id(String username, Long reviewId);
}