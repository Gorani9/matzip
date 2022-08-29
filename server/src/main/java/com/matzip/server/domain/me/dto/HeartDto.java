package com.matzip.server.domain.me.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class HeartDto {
    @RequiredArgsConstructor
    @Getter
    public static class Response {
        private final Integer numberOfHearts;
    }
}
