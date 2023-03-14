package com.matzip.server.domain.auth.api;

import com.matzip.server.domain.auth.dto.AuthDto.LoginRequest;
import com.matzip.server.domain.auth.dto.AuthDto.SignupRequest;
import com.matzip.server.domain.auth.service.AuthService;
import com.matzip.server.domain.auth.validation.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@RequestBody @Valid SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/signup/exists")
    public ResponseEntity<Object> checkDuplicateUsername(
            @RequestParam @Username String username
    ) {
        Map<String, Boolean> responseBody = Map.of("result", authService.isUsernameTakenBySomeone(username));
        return ResponseEntity.ok().body(responseBody);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid LoginRequest request) {
        authService.login(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login")
    public ResponseEntity<Object> isLoggedIn() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        return ResponseEntity.ok().build();
    }
}
