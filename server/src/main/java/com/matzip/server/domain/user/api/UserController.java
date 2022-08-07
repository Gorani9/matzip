package com.matzip.server.domain.user.api;

import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.service.UserService;
import com.matzip.server.global.auth.CurrentUser;
import com.matzip.server.global.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @PostMapping("/")
    public ResponseEntity<UserDto.Response> signUp(@RequestBody @Valid UserDto.SignUpRequest signUpRequest) {
        UserDto.Response response = userService.createUser(signUpRequest);
        return ResponseEntity.ok()
                .header("Authorization", jwtProvider.generateAccessToken(response.getUsername()))
                .body(response);
    }

    @GetMapping("/duplicate/")
    public ResponseEntity<UserDto.DuplicateResponse> checkDuplicateUsername(
            @RequestBody @Valid UserDto.DuplicateRequest duplicateRequest
    ) {
        return ResponseEntity.ok(userService.isUsernameTakenBySomeone(duplicateRequest));
    }

    @GetMapping("/me/")
    public ResponseEntity<UserDto.Response> getMe(@CurrentUser User user) {
        return ResponseEntity.ok(userService.findUser(new UserDto.FindRequest(user.getUsername())));
    }

    @PutMapping("/me/password/")
    public ResponseEntity<Object> changePassword(
            @CurrentUser User user,
            @RequestBody @Valid UserDto.PasswordChangeRequest passwordChangeRequest
    ) {
        userService.changePassword(user.getUsername(), passwordChangeRequest);
        return ResponseEntity.ok().build();
    }
}
