package com.matzip.server.domain.me.api;

import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.me.service.MeService;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/me")
public class MeController {
    private final MeService meService;

    @GetMapping
    public ResponseEntity<MeDto.Response> getMe(@CurrentUser User user) {
        return ResponseEntity.ok().body(meService.getMe(user.getUsername()));
    }

    @PatchMapping
    public ResponseEntity<MeDto.Response> patchMe(
            @CurrentUser User user,
            @RequestPart(required=false) MultipartFile profileImage,
            @RequestPart(required=false) @Valid @Length(max=50) String profileString) {
        return ResponseEntity.ok()
                .body(meService.patchMe(
                        user.getUsername(),
                        new MeDto.ModifyProfileRequest(profileImage, profileString)));
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteMe(@CurrentUser User user) {
        meService.deleteMe(user.getUsername());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Object> changePassword(
            @CurrentUser User user, @RequestBody @Valid MeDto.PasswordChangeRequest passwordChangeRequest) {
        passwordChangeRequest.setUsername(user.getUsername());
        meService.changePassword(passwordChangeRequest);
        return ResponseEntity.ok().build();
    }
}
