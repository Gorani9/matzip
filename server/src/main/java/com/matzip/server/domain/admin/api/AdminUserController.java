package com.matzip.server.domain.admin.api;

import com.matzip.server.domain.admin.dto.AdminDto;
import com.matzip.server.domain.admin.service.AdminUserService;
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

    @GetMapping("/")
    public ResponseEntity<Page<AdminDto.Response>> getUsers(
            @RequestParam @Valid @PositiveOrZero Integer pageNumber,
            @RequestParam @Valid @Positive Integer pageSize,
            @RequestParam(defaultValue = "false") Boolean withAdmin
    ) {
        return ResponseEntity.ok()
                .body(adminUserService.listUsers(
                        new AdminDto.UserListRequest(pageNumber, pageSize, withAdmin)));
    }

    @GetMapping("/username/")
    public ResponseEntity<Page<AdminDto.Response>> searchUsersByUsername(
            @RequestParam @Valid @PositiveOrZero Integer pageNumber,
            @RequestParam @Valid @Positive Integer pageSize,
            @RequestParam @Valid @NotBlank String username,
            @RequestParam(required = false) Boolean isNonLocked
    ) {
        return ResponseEntity.ok()
                .body(adminUserService.searchUsers(
                        new AdminDto.UserSearchRequest(pageNumber, pageSize, username, isNonLocked)));
    }

    @GetMapping("/{id}/")
    public ResponseEntity<AdminDto.Response> getUserById(
            @PathVariable("id") @Valid @Positive Long id
    ) {
        return ResponseEntity.ok()
                .body(adminUserService.findUserById(id));
    }

    @PatchMapping("/{id}/lock/")
    public ResponseEntity<Object> changeUserLockStatus(
            @PathVariable("id") @Valid @Positive Long id,
            @RequestParam(value = "activate", defaultValue = "false") Boolean activate
    ) {
        if (activate)
            adminUserService.lockUser(id);
        else
            adminUserService.unlockUser(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<Object> deleteUser(
            @PathVariable("id") @Valid @Positive Long id
    ) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
