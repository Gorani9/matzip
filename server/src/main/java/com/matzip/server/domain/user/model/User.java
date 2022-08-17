package com.matzip.server.domain.user.model;

import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="user")
@RequiredArgsConstructor
@Getter
public class User extends BaseTimeEntity {
    @Column(unique=true)
    private String username;

    private String password;

    private Boolean isNonLocked = true;

    private String role = "NORMAL";

    @URL
    private String profileImageUrl;

    public User(UserDto.SignUpRequest signUpRequest, PasswordEncoder passwordEncoder) {
        this.username = signUpRequest.getUsername();
        this.password = passwordEncoder.encode(signUpRequest.getPassword());
    }

    public User changePassword(UserDto.PasswordChangeRequest passwordChangeRequest, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(passwordChangeRequest.getPassword());
        return this;
    }

    public User toAdmin() {
        this.role = "ADMIN";
        return this;
    }

    public User lock() {
        this.isNonLocked = false;
        return this;
    }

    public User unlock() {
        this.isNonLocked = true;
        return this;
    }

    public User patch(UserDto.ModifyProfileRequest modifyProfileRequest) {
        if (modifyProfileRequest.getProfileImageUrl() != null)
            this.profileImageUrl = modifyProfileRequest.getProfileImageUrl();
        return this;
    }
}
