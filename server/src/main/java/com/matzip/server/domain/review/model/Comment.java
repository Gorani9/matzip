package com.matzip.server.domain.review.model;

import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.model.BaseLazyDeletedTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name="comment")
@NoArgsConstructor
@Getter
@Setter
public class Comment extends BaseLazyDeletedTimeEntity {
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
        user.addComment(this);
        this.review = review;
        review.addComment(this);
        this.content = content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}