package com.matzip.server.domain.image.model;

import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "user_image")
@NoArgsConstructor
@Getter
public class UserImage extends BaseTimeEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User user;

    @NotBlank
    @URL
    private String imageUrl;

    public UserImage(User user, String imageUrl) {
        this.user = user;
        user.setUserImage(this);
        this.imageUrl = imageUrl;
    }
}
