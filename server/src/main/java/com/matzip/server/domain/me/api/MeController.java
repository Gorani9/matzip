package com.matzip.server.domain.me.api;

import com.matzip.server.domain.me.dto.MeDto.*;
import com.matzip.server.domain.me.service.MeService;
import com.matzip.server.global.auth.model.CurrentUser;
import com.matzip.server.global.auth.model.CurrentUsername;
import com.matzip.server.global.common.logger.Logging;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me")
public class MeController {
    private final MeService meService;

    @GetMapping
    @Logging(endpoint="GET /api/v1/me")
    public ResponseEntity<Response> getMe(
            @CurrentUser Long myId,
            @CurrentUsername String user
    ) {
        return ResponseEntity.ok(meService.getMe(myId));
    }

    @PatchMapping(consumes={"multipart/form-data"})
    @Logging(endpoint="PATCH /api/v1/me")
    public ResponseEntity<Response> patchMe(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @ModelAttribute @Valid PatchRequest request
    ) {
        return ResponseEntity.ok(meService.patchMe(myId, request));
    }

    @DeleteMapping
    @Logging(endpoint="DELETE /api/v1/me")
    public ResponseEntity<Object> deleteMe(
            @CurrentUser Long myId,
            @CurrentUsername String user
    ) {
        meService.deleteMe(myId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/username")
    @Logging(endpoint="PUT /api/v1/me/username")
    public ResponseEntity<UsernameResponse> changeUsername(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @RequestBody @Valid UsernameChangeRequest request
    ) {
        return ResponseEntity.ok(meService.changeUsername(myId, request));
    }

    @PutMapping("/password")
    @Logging(endpoint="PUT /api/v1/me/password", hideRequestBody = true)
    public ResponseEntity<Object> changePassword(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @RequestBody @Valid PasswordChangeRequest request
    ) {
        meService.changePassword(myId, request);
        return ResponseEntity.ok().build();
    }
}
