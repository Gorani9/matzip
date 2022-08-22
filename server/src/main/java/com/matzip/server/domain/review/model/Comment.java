package com.matzip.server.domain.review.model;

import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name="comment")
@NoArgsConstructor
@Getter
@Setter
public class Comment extends BaseTimeEntity {
    @ManyToOne
    @JoinColumn
    private User user;

    @ManyToOne
    @JoinColumn
    private Review review;

    @NotBlank
    private String content;

    public Comment(User user, Review review, CommentDto.PostRequest postRequest) {
        this.user = user;
        this.review = review;
        this.content = postRequest.getContent();
    }

    public void setContent(String content) {
        this.content = content;
    }
}