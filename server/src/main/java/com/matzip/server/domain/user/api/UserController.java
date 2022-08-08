package com.matzip.server.domain.user.api;

import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.service.UserService;
import com.matzip.server.global.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/")
    public ResponseEntity<UserDto.Response> signUp(@RequestBody @Valid UserDto.SignUpRequest signUpRequest) {
        UserDto.SignUpResponse signUpResponse = userService.createUser(signUpRequest);
        return ResponseEntity.ok()
                .header("Authorization", signUpResponse.getToken())
                .body(signUpResponse.getResponse());
    }

    @GetMapping("/exists/")
    public ResponseEntity<UserDto.DuplicateResponse> checkDuplicateUsername(
            @RequestParam @Valid @NotBlank String username
    ) {
        return ResponseEntity.ok(userService.isUsernameTakenBySomeone(new UserDto.DuplicateRequest(username)));
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
