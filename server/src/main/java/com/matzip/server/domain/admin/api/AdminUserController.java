package com.matzip.server.domain.admin.api;

import com.matzip.server.domain.admin.dto.AdminDto;
import com.matzip.server.domain.admin.service.AdminUserService;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.user.model.UserProperty;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/users")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Slice<AdminDto.UserResponse>> searchByUsername(
            @RequestParam(value = "username", required = false) @Length(max = 30) String username,
            @RequestParam(value = "page", required = false, defaultValue ="0") @PositiveOrZero Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") @Positive @Max(100) Integer size,
            @RequestParam(value = "sort", required = false) String userProperty,
            @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc,
            @RequestParam(value = "with-blocked", required = false, defaultValue = "false") Boolean withBlocked) {

        return ResponseEntity.ok()
                .body(adminUserService.searchUsers(new AdminDto.UserSearchRequest(
                        username, page, size, UserProperty.from(userProperty), asc, withBlocked)));
    }

    @GetMapping("/{user-id}")
    public ResponseEntity<AdminDto.UserResponse> fetchUserById(@PathVariable("user-id") @Positive Long userId) {
        return ResponseEntity.ok().body(adminUserService.fetchUserById(userId));
    }

    @PatchMapping("/{user-id}")
    public ResponseEntity<AdminDto.UserResponse> patchUserById(
            @PathVariable("user-id") @Positive Long userId,
            @RequestBody AdminDto.UserPatchRequest userPatchRequest) {
        return ResponseEntity.ok().body(adminUserService.patchUserById(userId, userPatchRequest));
    }

    @PutMapping("/{user-id}/lock")
    public ResponseEntity<AdminDto.UserResponse> lockUser(@PathVariable("user-id") @Positive Long userId) {
        return ResponseEntity.ok().body(adminUserService.lockUser(userId));
    }

    @DeleteMapping("/{user-id}/lock")
    public ResponseEntity<AdminDto.UserResponse> unlockUser(@PathVariable("user-id") @Positive Long userId) {
        return ResponseEntity.ok().body(adminUserService.unlockUser(userId));
    }

    @PutMapping("/{user-id}/password")
    public ResponseEntity<AdminDto.UserResponse> changeUserPassword(
            @PathVariable("user-id") @Positive Long userId,
            @RequestBody @Valid MeDto.PasswordChangeRequest passwordChangeRequest) {
        return ResponseEntity.ok().body(adminUserService.changeUserPassword(userId, passwordChangeRequest));
    }

    @DeleteMapping("/{user-id}")
    public ResponseEntity<Object> deleteUser(@PathVariable("user-id") @Positive Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
