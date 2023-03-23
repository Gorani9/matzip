package com.matzip.server.domain.user.api;

import com.matzip.server.domain.user.dto.UserDto.DetailedResponse;
import com.matzip.server.domain.user.dto.UserDto.Response;
import com.matzip.server.domain.user.dto.UserDto.SearchRequest;
import com.matzip.server.domain.user.model.UserProperty;
import com.matzip.server.domain.user.service.UserService;
import com.matzip.server.global.auth.model.CurrentUser;
import com.matzip.server.global.auth.model.CurrentUsername;
import com.matzip.server.global.common.validation.Username;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/exists")
    public ResponseEntity<Object> checkDuplicateUsername(
            @RequestParam @Username String username
    ) {
        Map<String, Boolean> responseBody = Map.of("result", userService.isUsernameTakenBySomeone(username));
        return ResponseEntity.ok().body(responseBody);
    }

    @GetMapping
    public ResponseEntity<Slice<Response>> searchUsersByUsername(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @RequestParam(value = "username") @NotBlank @Length(max = 30) String username,
            @RequestParam(value = "page", required = false, defaultValue ="0") @PositiveOrZero Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") @Positive @Max(100) Integer size,
            @RequestParam(value = "sort", required = false) UserProperty userProperty,
            @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc
    ) {
        log.info("""
                         [{}(id={})] GET /api/v1/users?
                         \t username = {}
                         \t page = {}
                         \t size = {}
                         \t userProperty = {}
                         \t asc = {}""",
                 user, myId, username, page, size, userProperty, asc);
        return ResponseEntity.ok(userService.searchUsers(myId, new SearchRequest(username, page, size, userProperty, asc)));
    }

    @GetMapping("/{username}")
    public ResponseEntity<DetailedResponse> fetchUserByUsername(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("username") @Username String username
    ) {
        log.info("[{}(id={})] GET /api/v1/users/{} BY {}", user, myId, username, user);
        return ResponseEntity.ok(userService.fetchUser(myId, username));
    }

    @PutMapping("/{username}/follow")
    public ResponseEntity<DetailedResponse> followUserByUsername(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("username") @Username String username
    ) {
        log.info("[{}(id={})] GET /api/v1/users/{}/follow", user, myId, username);
        return ResponseEntity.ok(userService.followUser(myId, username));
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<DetailedResponse> unfollowUserByUsername(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("username") @Username String username
    ) {
        log.info("[{}(id={})] DELETE /api/v1/users/{}/follow", user, myId, username);
        return ResponseEntity.ok(userService.unfollowUser(myId, username));
    }
}
