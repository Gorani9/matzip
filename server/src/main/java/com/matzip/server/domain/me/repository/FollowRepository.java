package com.matzip.server.domain.me.repository;

import com.matzip.server.domain.me.model.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    void deleteByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    Page<Follow> findAllByFolloweeId(Pageable pageable, Long followeeId);
    Page<Follow> findAllByFollowerId(Pageable pageable, Long followerId);
}