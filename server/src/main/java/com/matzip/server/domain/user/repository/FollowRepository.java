package com.matzip.server.domain.user.repository;

import com.matzip.server.domain.user.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    @Modifying
    @Query("DELETE FROM Follow f WHERE f.followee.id = :userId OR f.follower.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}