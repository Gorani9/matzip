package com.matzip.server.domain.user.api;

import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.dto.UserDto.DetailedResponse;
import com.matzip.server.domain.user.dto.UserDto.DuplicateResponse;
import com.matzip.server.domain.user.dto.UserDto.Response;
import com.matzip.server.domain.user.dto.UserDto.SearchRequest;
import com.matzip.server.domain.user.model.UserProperty;
import com.matzip.server.domain.user.service.UserService;
import com.matzip.server.domain.user.validation.Username;
import com.matzip.server.global.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
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
    public ResponseEntity<MeDto.Response> signUp(@RequestBody @Valid UserDto.SignUpRequest signUpRequest) {
        UserDto.SignUpResponse signUpResponse = userService.createUser(signUpRequest);
        return ResponseEntity.ok()
                .header("Authorization", signUpResponse.getToken())
                .body(signUpResponse.getResponse());
    }

    @GetMapping("/exists")
    public ResponseEntity<DuplicateResponse> checkDuplicateUsername(
            @RequestParam @Username String username) {
        return ResponseEntity.ok().body(userService.isUsernameTakenBySomeone(username));
    }

    @GetMapping
    public ResponseEntity<Slice<Response>> searchUsersByUsername(
            @CurrentUser Long myId,
            @RequestParam("username") @NotBlank @Length(max = 30) String username,
            @RequestParam(value = "page", required = false, defaultValue ="0") @PositiveOrZero Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") @Positive @Max(100) Integer size,
            @RequestParam(value = "sort", required = false) String userProperty,
            @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc) {
        return ResponseEntity.ok().body(userService.searchUsers(
                myId, new SearchRequest(username, page, size, UserProperty.from(userProperty), asc)));
    }

    @GetMapping("/{username}")
    public ResponseEntity<DetailedResponse> fetchUserByUsername(
            @CurrentUser Long myId, @PathVariable("username") @Username String username) {
        return ResponseEntity.ok().body(userService.fetchUser(myId, username));
    }
}
