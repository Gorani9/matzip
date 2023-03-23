package com.matzip.server.domain.record.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Getter @Setter
@RedisHash(value = "loginRecord", timeToLive = 60 * 60 * 24)
@NoArgsConstructor
public class LoginRecord {
    @Id
    private Long userId;
    private String token;
    private LocalDateTime lastLoginPointGiven;

    public LoginRecord(Long userId, String token) {
        this.userId = userId;
        this.token = token;
        this.lastLoginPointGiven = LocalDateTime.now();
    }
}
