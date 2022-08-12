package com.matzip.server.domain.image.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;

public class ImageDto {
    @RequiredArgsConstructor
    @Getter
    public static class UploadRequest {
        private final String username;
        private final List<MultipartFile> images;
    }

    @RequiredArgsConstructor
    @Getter
    public static class DeleteRequest {
        @NotNull
        private final List<String> urls;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Response {
        private final List<String> urls;
    }
}
