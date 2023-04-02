package com.matzip.server.domain.record.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Getter @Setter
@RedisHash(value = "viewRecord", timeToLive = 60 * 60 * 24)
@NoArgsConstructor
public class ReviewRecord {
    @Id
    private String key;
    private LocalDateTime lastViewedAt;
    private int commentCount;

    public ReviewRecord(Long userId, Long reviewId) {
        this.key = buildKey(userId, reviewId);
        this.lastViewedAt = LocalDateTime.now();
        this.commentCount = 0;
    }

    public ReviewRecord(String key) {
        this.key = key;
        this.lastViewedAt = LocalDateTime.now();
        this.commentCount = 0;
    }

    public static String buildKey(Long userId, Long reviewId) {
        return userId + ":" + reviewId;
    }
    public static String buildKey(String userIp, Long reviewId) {
        return userIp + ":" + reviewId;
    }
}
