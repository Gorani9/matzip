package com.matzip.server.admin.api;

import com.matzip.server.admin.service.AdminService;
import com.matzip.server.domain.auth.dto.AuthDto.LoginRequest;
import com.matzip.server.domain.auth.validation.Username;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/login")
    ResponseEntity<Object> adminLogin(@RequestBody LoginRequest request) {
        log.info("POST /admin/api/v1/login");
        adminService.login(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{username}")
    ResponseEntity<Object> deleteUser(
            @PathVariable("username") @Username String username
    ) {
        log.info("DELETE /admin/api/v1/users/{}", username);
        adminService.deleteUser(username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reviews/{id}")
    ResponseEntity<Object> deleteReview(
            @PathVariable("id") @Positive Long reviewId
    ) {
        log.info("DELETE /admin/api/v1/reviews/{}", reviewId);
        adminService.deleteReview(reviewId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{id}")
    ResponseEntity<Object> deleteComment(
            @PathVariable("id") @Positive Long commentId
    ) {
        log.info("DELETE /admin/api/v1/comments/{}", commentId);
        adminService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}
