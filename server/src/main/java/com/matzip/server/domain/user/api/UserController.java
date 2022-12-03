package com.matzip.server.domain.user.api;

import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.UserProperty;
import com.matzip.server.domain.user.service.UserService;
import com.matzip.server.domain.user.validation.Username;
import com.matzip.server.global.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
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

    @GetMapping
    public ResponseEntity<Slice<UserDto.Response>> searchUsersByUsername(
            @CurrentUser Long myId,
            @RequestParam("username") @NotBlank String username,
            @RequestParam(value = "page", required = false, defaultValue ="0") @PositiveOrZero Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") @Positive @Max(100) Integer size,
            @RequestParam(value = "sort", required = false) String userProperty,
            @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc) {
        return ResponseEntity.ok().body(userService.searchUsers(myId, new UserDto.SearchRequest(
                username, page, size, UserProperty.from(userProperty), asc)));
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDto.Response> fetchUserByUsername(
            @CurrentUser Long myId, @PathVariable("username") @Valid @NotBlank String username) {
        return ResponseEntity.ok().body(userService.fetchUser(myId, username));
    }
}
