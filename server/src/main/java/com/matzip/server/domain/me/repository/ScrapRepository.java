package com.matzip.server.domain.me.repository;

import com.matzip.server.domain.me.model.Scrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    Optional<Scrap> findByUser_UsernameAndReview_Id(String username, Long reviewId);

    void deleteByUser_UsernameAndReview_id(String username, Long reviewId);

    Page<Scrap> findAllByUser_Username(Pageable pageable, String username);
}