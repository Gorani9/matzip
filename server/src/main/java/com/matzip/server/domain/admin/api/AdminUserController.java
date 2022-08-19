package com.matzip.server.domain.admin.api;

import com.matzip.server.domain.admin.dto.AdminDto;
import com.matzip.server.domain.admin.service.AdminUserService;
import com.matzip.server.domain.user.validation.UserProperty;
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
            @RequestParam(defaultValue="createdAt") @Valid @UserProperty String sortedBy,
            @RequestParam(defaultValue="true") Boolean ascending,
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
            @RequestParam(defaultValue="createdAt") @Valid @UserProperty String sortedBy,
            @RequestParam(defaultValue="true") Boolean ascending,
            @RequestParam @Valid @NotBlank String username,
            @RequestParam(required=false) Boolean isNonLocked) {
        return ResponseEntity.ok().body(adminUserService.searchUsers(
                new AdminDto.UserSearchRequest(pageNumber, pageSize, sortedBy, ascending, username, isNonLocked)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminDto.UserResponse> getUserById(@PathVariable("id") @Valid @Positive Long id) {
        return ResponseEntity.ok().body(adminUserService.findUserById(id));
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<Object> lockUser(@PathVariable("id") @Valid @Positive Long id) {
        adminUserService.lockUser(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/lock")
    public ResponseEntity<Object> unlockUser(@PathVariable("id") @Valid @Positive Long id) {
        adminUserService.unlockUser(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable("id") @Valid @Positive Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
