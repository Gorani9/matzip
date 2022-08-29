package com.matzip.server.domain.user.api;

import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.service.UserService;
import com.matzip.server.domain.user.validation.UserProperty;
import com.matzip.server.domain.user.validation.Username;
import com.matzip.server.global.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto.Response> signUp(@RequestBody @Valid UserDto.SignUpRequest signUpRequest) {
        UserDto.SignUpResponse signUpResponse = userService.createUser(signUpRequest);
        return ResponseEntity.ok()
                .header("Authorization", signUpResponse.getToken())
                .body(signUpResponse.getResponse());
    }

    @GetMapping("/exists")
    public ResponseEntity<UserDto.DuplicateResponse> checkDuplicateUsername(
            @RequestParam @Valid @Username String username) {
        return ResponseEntity.ok().body(userService.isUsernameTakenBySomeone(username));
    }

    @GetMapping("/username")
    public ResponseEntity<Page<UserDto.Response>> searchUsersByUsername(
            @CurrentUser User user,
            @RequestParam(defaultValue="0") @Valid @PositiveOrZero Integer pageNumber,
            @RequestParam(defaultValue="15") @Valid @Positive Integer pageSize,
            @RequestParam(defaultValue="createdAt") @Valid @UserProperty String sortedBy,
            @RequestParam(defaultValue="false") Boolean ascending,
            @RequestParam @Valid @NotBlank String username) {
        return ResponseEntity.ok()
                .body(userService.searchUsers(
                        user.getId(),
                        new UserDto.SearchRequest(pageNumber,
                                                  pageSize,
                                                  sortedBy,
                                                  ascending,
                                                  username)));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto.Response> getUserByUsername(
            @CurrentUser User user, @PathVariable("username") @Valid @NotBlank String username) {
        return ResponseEntity.ok().body(userService.findUser(username, user.getUsername()));
    }
}
