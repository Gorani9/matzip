package com.matzip.server.domain.user.api;

import com.matzip.server.domain.user.dto.UserDto.DetailedResponse;
import com.matzip.server.domain.user.service.UserService;
import com.matzip.server.global.auth.model.CurrentUser;
import com.matzip.server.global.auth.model.CurrentUsername;
import com.matzip.server.global.common.logger.Logging;
import com.matzip.server.global.common.validation.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/exists")
    public ResponseEntity<Object> checkDuplicateUsername(@RequestParam @Username String username) {
        Map<String, Boolean> responseBody = Map.of("result", userService.isUsernameTakenBySomeone(username));
        return ResponseEntity.ok().body(responseBody);
    }

    @GetMapping("/{username}")
    @Logging(endpoint="GET /api/v1/users/{pathVariable}", pathVariable=true)
    public ResponseEntity<DetailedResponse> fetchUserByUsername(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("username") @Username String username
    ) {
        return ResponseEntity.ok(userService.fetchUser(myId, username));
    }

    @PutMapping("/{username}/follow")
    @Logging(endpoint="PUT /api/v1/users/{pathVariable}/follow", pathVariable=true)
    public ResponseEntity<DetailedResponse> followUserByUsername(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("username") @Username String username
    ) {
        return ResponseEntity.ok(userService.followUser(myId, username));
    }

    @DeleteMapping("/{username}/follow")
    @Logging(endpoint="DELETE /api/v1/users/{pathVariable}/follow", pathVariable=true)
    public ResponseEntity<DetailedResponse> unfollowUserByUsername(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("username") @Username String username
    ) {
        return ResponseEntity.ok(userService.unfollowUser(myId, username));
    }
}
