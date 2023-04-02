package com.matzip.server.global.auth.api;

import com.matzip.server.global.auth.dto.AuthDto;
import com.matzip.server.global.auth.dto.AuthDto.Response;
import com.matzip.server.global.auth.model.CurrentUser;
import com.matzip.server.global.auth.model.CurrentUsername;
import com.matzip.server.global.auth.service.AuthService;
import com.matzip.server.global.common.logger.Logging;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @Logging(endpoint="POST /api/v1/auth/signup", hideRequestBody = true)
    public ResponseEntity<Response> signup(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @RequestBody @Valid AuthDto.SignupRequest request
    ) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    @Logging(endpoint="POST /api/v1/auth/login", hideRequestBody = true)
    public ResponseEntity<Response> login(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @RequestBody @Valid AuthDto.LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @Logging(endpoint="POST /api/v1/auth/logout")
    public ResponseEntity<Object> logout(
            @CurrentUser Long myId,
            @CurrentUsername String user
    ) {
        authService.logout(myId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/refresh")
    @Logging(endpoint="GET /api/v1/auth/refresh")
    public ResponseEntity<Response> isLoggedIn(
            @CurrentUser Long myId,
            @CurrentUsername String user
    ) {
        return ResponseEntity.ok(authService.refresh(myId, user));
    }
}
