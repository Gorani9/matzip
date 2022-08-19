package com.matzip.server.domain.user.model;

import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.me.model.Follow;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="user")
@NoArgsConstructor
@Getter
public class User extends BaseTimeEntity {
    @Column(unique=true)
    private String username;

    private String password;

    private Boolean isNonLocked = true;

    private String role = "NORMAL";

    @URL
    private String profileImageUrl;

    private String profileString;

    private Integer matzipLevel = 0;

    @OneToMany(mappedBy = "followee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followers;

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followings;

    public User(UserDto.SignUpRequest signUpRequest, PasswordEncoder passwordEncoder) {
        this.username = signUpRequest.getUsername();
        this.password = passwordEncoder.encode(signUpRequest.getPassword());
    }

    public User changeUsername(MeDto.UsernameChangeRequest usernameChangeRequest) {
        this.username = usernameChangeRequest.getUsername();
        return this;
    }

    public User changePassword(MeDto.PasswordChangeRequest passwordChangeRequest, PasswordEncoder passwordEncoder) {
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

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setProfileString(String profileString) {
        this.profileString = profileString;
    }

    public void levelUp() {
        this.matzipLevel++;
    }
}
