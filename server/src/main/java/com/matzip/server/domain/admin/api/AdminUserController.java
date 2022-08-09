package com.matzip.server.domain.admin.api;

import com.matzip.server.domain.admin.dto.AdminDto;
import com.matzip.server.domain.admin.service.AdminUserService;
import com.matzip.server.domain.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping("/")
    public ResponseEntity<Page<UserDto.Response>> getUsers(
            @RequestParam @Valid @PositiveOrZero Integer pageNumber,
            @RequestParam @Valid @PositiveOrZero Integer pageSize,
            @RequestParam(defaultValue = "false") Boolean withAdmin
    ) {
        return ResponseEntity.ok()
                .body(adminUserService.findUsers(
                        new AdminDto.UserSearchRequest(pageNumber, pageSize, withAdmin)));
    }

    @GetMapping("/{id}/")
    public ResponseEntity<UserDto.Response> getUserById(
            @PathVariable("id") @Valid @PositiveOrZero Long id
    ) {
        return ResponseEntity.ok()
                .body(adminUserService.findUserById(id));
    }

    @PatchMapping("/{id}/status/")
    public ResponseEntity<Object> changeUserStatus(
            @PathVariable("id") @Valid @PositiveOrZero Long id,
            @RequestParam("activate") @Valid @NotNull Boolean activate
    ) {
        if (activate)
            adminUserService.activateUser(id);
        else
            adminUserService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<Object> deleteUser(
            @PathVariable("id") @Valid @PositiveOrZero Long id
    ) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
