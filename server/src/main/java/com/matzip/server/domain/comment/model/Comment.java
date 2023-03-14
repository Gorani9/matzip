package com.matzip.server.domain.comment.model;

import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name="comment")
@NoArgsConstructor
@Getter @Setter
public class Comment extends BaseTimeEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Review review;

    @NotBlank
    private String content;

    public Comment(User user, Review review, String content) {
        this.user = user;
        this.review = review;
        this.content = content;
        user.getComments().add(this);
        review.getComments().add(this);
    }

    @Override
    public void delete() {
        user.getComments().remove(this);
        review.getComments().remove(this);
    }
}