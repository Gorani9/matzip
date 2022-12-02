package com.matzip.server.domain.admin.api;

import com.matzip.server.domain.admin.dto.AdminDto;
import com.matzip.server.domain.admin.service.AdminUserService;
import com.matzip.server.domain.me.dto.MeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<AdminDto.UserResponse>> getUsers(
            @RequestParam(defaultValue="0") @Valid @PositiveOrZero Integer pageNumber,
            @RequestParam(defaultValue="15") @Valid @Positive Integer pageSize,
            @RequestParam(defaultValue="createdAt") @Valid String sortedBy,
            @RequestParam(defaultValue="false") Boolean ascending,
            @RequestParam(defaultValue="false") Boolean withAdmin) {
        return ResponseEntity.ok()
                .body(adminUserService.listUsers(new AdminDto.UserListRequest(
                        pageNumber,
                        pageSize,
                        sortedBy,
                        ascending,
                        withAdmin)));
    }

    @GetMapping("/username")
    public ResponseEntity<Page<AdminDto.UserResponse>> searchUsersByUsername(
            @RequestParam(defaultValue="0") @Valid @PositiveOrZero Integer pageNumber,
            @RequestParam(defaultValue="15") @Valid @Positive Integer pageSize,
            @RequestParam(defaultValue="createdAt") @Valid String sortedBy,
            @RequestParam(defaultValue="false") Boolean ascending,
            @RequestParam @Valid @NotBlank String username,
            @RequestParam(required=false) Boolean isNonLocked) {
        return ResponseEntity.ok().body(adminUserService.searchUsers(
                new AdminDto.UserSearchRequest(pageNumber, pageSize, sortedBy, ascending, username, isNonLocked)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminDto.UserResponse> getUserById(@PathVariable("id") @Valid @Positive Long id) {
        return ResponseEntity.ok().body(adminUserService.findUserById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AdminDto.UserResponse> patchUserById(
            @PathVariable("id") @Valid @Positive Long id,
            @RequestBody AdminDto.UserPatchRequest userPatchRequest) {
        return ResponseEntity.ok().body(adminUserService.patchUserById(id, userPatchRequest));
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<AdminDto.UserResponse> lockUser(@PathVariable("id") @Valid @Positive Long id) {
        return ResponseEntity.ok().body(adminUserService.lockUser(id));
    }

    @DeleteMapping("/{id}/lock")
    public ResponseEntity<AdminDto.UserResponse> unlockUser(@PathVariable("id") @Valid @Positive Long id) {
        return ResponseEntity.ok().body(adminUserService.unlockUser(id));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<AdminDto.UserResponse> changeUserPassword(
            @PathVariable("id") @Valid @Positive Long id,
            @RequestBody @Valid MeDto.PasswordChangeRequest passwordChangeRequest) {
        return ResponseEntity.ok().body(adminUserService.changeUserPassword(id, passwordChangeRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable("id") @Valid @Positive Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
