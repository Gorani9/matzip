package com.matzip.server.domain.image.api;

import com.matzip.server.domain.image.dto.ImageDto;
import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/")
    ResponseEntity<ImageDto.Response> uploadImages(
            @CurrentUser User user,
            @RequestPart List<MultipartFile> images
    ) {
        return ResponseEntity.ok()
                .body(imageService.uploadImages(new ImageDto.UploadRequest(user.getUsername(), images)));
    }

    @DeleteMapping("/")
    ResponseEntity<Object> deleteImages(
            @CurrentUser User user,
            @RequestBody @Valid ImageDto.DeleteRequest deleteRequest) {
        imageService.deleteImages(user.getUsername(), deleteRequest.getUrls());
        return ResponseEntity.ok().build();
    }
}
