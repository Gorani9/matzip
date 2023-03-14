package com.matzip.server.domain.me.api;

import com.matzip.server.domain.auth.model.CurrentUser;
import com.matzip.server.domain.auth.model.CurrentUsername;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.me.dto.MeDto.PasswordChangeRequest;
import com.matzip.server.domain.me.dto.MeDto.PatchRequest;
import com.matzip.server.domain.me.dto.MeDto.UsernameChangeRequest;
import com.matzip.server.domain.me.service.MeService;
import com.matzip.server.domain.user.dto.UserDto.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me")
public class MeController {
    private final MeService meService;

    @GetMapping
    public ResponseEntity<Response> getMe(
            @CurrentUser Long myId,
            @CurrentUsername String user
    ) {
        log.info("[{}(id={})] GET /api/v1/me", user, myId);
        return ResponseEntity.ok(meService.getMe(myId));
    }

    @GetMapping("/detail")
    public ResponseEntity<MeDto.Response> getMeDetail(
            @CurrentUser Long myId,
            @CurrentUsername String user
    ) {
        log.info("[{}(id={})] GET /api/v1/me/detail", user, myId);
        return ResponseEntity.ok(meService.getMeDetail(myId));
    }

    @PatchMapping(consumes={"multipart/form-data"})
    public ResponseEntity<Response> patchMe(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @ModelAttribute @Valid PatchRequest request
    ) {
        log.info("""
                         [{}(id={})] PATCH /api/v1/me
                         \t image = {}
                         \t profile = {}""", user, myId, request.image() == null, request.profile());
        return ResponseEntity.ok(meService.patchMe(myId, request));
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteMe(
            @CurrentUser Long myId,
            @CurrentUsername String user
    ) {
        log.info("[{}(id={})] DELETE /api/v1/me", user, myId);
        meService.deleteMe(myId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/username")
    public ResponseEntity<Response> changeUsername(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @RequestBody @Valid UsernameChangeRequest request
    ) {
        log.info("[{}(id={})] PUT /api/v1/me/username: username = {}", user, myId, request.username());
        return ResponseEntity.ok(meService.changeUsername(myId, request));
    }

    @PutMapping("/password")
    public ResponseEntity<Object> changePassword(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @RequestBody @Valid PasswordChangeRequest request
    ) {
        log.info("[{}(id={})] PUT /api/v1/me/password", user, myId);
        meService.changePassword(myId, request);
        return ResponseEntity.ok().build();
    }
}
