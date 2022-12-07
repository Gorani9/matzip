package com.matzip.server.domain.image.model;

import com.matzip.server.domain.user.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "user_image")
@NoArgsConstructor
@Getter
public class UserImage extends BaseImageEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User user;

    public UserImage(User user, String imageUrl) {
        super(imageUrl);
        this.user = user;
        user.setUserImage(this);
    }
}
