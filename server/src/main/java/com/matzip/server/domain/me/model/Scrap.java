package com.matzip.server.domain.me.model;

import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name="scrap")
@NoArgsConstructor
@Getter
public class Scrap extends BaseTimeEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Review review;

    private String description;

    public Scrap(User user, Review review) {
        this.user = user;
        user.addScrap(this);
        this.review = review;
        review.addScrap(this);
    }

    public void setDescription(String description) {
        this.description = description;
    }
}