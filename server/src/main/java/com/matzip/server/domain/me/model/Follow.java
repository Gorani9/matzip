package com.matzip.server.domain.me.model;

import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name="follow")
@NoArgsConstructor
@Getter
public class Follow extends BaseTimeEntity {
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn
    private User follower;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn
    private User followee;

    public Follow(User follower, User followee) {
        this.follower = follower;
        this.followee = followee;
    }
}
