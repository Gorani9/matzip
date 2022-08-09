package com.matzip.server.domain.admin.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class AdminDto {
    @RequiredArgsConstructor
    @Getter
    public static class UserSearchRequest {
        private final Integer pageNumber;
        private final Integer pageSize;
        private final Boolean withAdmin;
    }
}
