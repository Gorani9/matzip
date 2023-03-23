package com.matzip.server.domain.review.model;

import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name="scrap")
@NoArgsConstructor
@Getter @Setter
public class Scrap extends BaseTimeEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Review review;

    private String description;

    public Scrap(User user, Review review, String description) {
        this.user = user;
        this.review = review;
        this.description = description;
        user.getScraps().add(this);
        review.getScraps().add(this);
    }

    @Override
    public void delete() {
        user.getScraps().remove(this);
        review.getScraps().remove(this);
    }
}