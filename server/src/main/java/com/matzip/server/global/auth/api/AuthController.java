package com.matzip.server.global.auth.api;

import com.matzip.server.global.auth.dto.AuthDto;
import com.matzip.server.global.auth.dto.AuthDto.Response;
import com.matzip.server.global.auth.model.CurrentUser;
import com.matzip.server.global.auth.model.CurrentUsername;
import com.matzip.server.global.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Response> signup(@RequestBody @Valid AuthDto.SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<Response> login(@RequestBody @Valid AuthDto.LoginRequest request) {
        log.info("[{}(id={})] POST /api/v1/auth/login", request.username(), "???");
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(
            @CurrentUser Long myId,
            @CurrentUsername String user
    ) {
        log.info("[{}(id={})] POST /api/v1/auth/logout", user, myId);
        authService.logout(myId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/refresh")
    public ResponseEntity<Response> isLoggedIn(
            @CurrentUser Long myId,
            @CurrentUsername String user
    ) {
        log.info("[{}(id={})] GET /api/v1/auth/refresh", user, myId);
        return ResponseEntity.ok(authService.refresh(myId, user));
    }
}
