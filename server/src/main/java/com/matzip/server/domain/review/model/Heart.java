package com.matzip.server.domain.review.model;

import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name="heart")
@NoArgsConstructor
@Getter
public class Heart extends BaseTimeEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Review review;

    public Heart(User user, Review review) {
        this.user = user;
        this.review = review;
        user.getHearts().add(this);
        review.getHearts().add(this);
    }

    @Override
    public void delete() {
        user.getHearts().remove(this);
        review.getHearts().remove(this);
    }
}