package com.matzip.server.domain.me.model;

import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="scrap")
@NoArgsConstructor
@Getter
public class Scrap extends BaseTimeEntity {
    @ManyToOne
    @JoinColumn
    private User user;

    @ManyToOne
    @JoinColumn
    private Review review;

    private String description;

    public Scrap(User user, Review review) {
        this.user = user;
        this.review = review;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Scrap && this.user.equals(((Scrap) obj).user) && this.review.equals(((Scrap) obj).review);
    }

    public void setDescription(String description) {
        this.description = description;
    }
}