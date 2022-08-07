package com.matzip.server.domain.user.model;

import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "user")
@RequiredArgsConstructor
@Getter
public class User extends BaseTimeEntity {
    @Column(unique = true)
    private String username;

    private String password;

    private Boolean active;

    private String role;

    public User(UserDto.SignUpRequest signUpRequest, PasswordEncoder passwordEncoder) {
        this.username = signUpRequest.getUsername();
        this.password = passwordEncoder.encode(signUpRequest.getPassword());
        this.active = true;
    }

    public void changePassword(UserDto.PasswordChangeRequest passwordChangeRequest, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(passwordChangeRequest.getPassword());
    }
}
