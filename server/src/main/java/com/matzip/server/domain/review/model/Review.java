package com.matzip.server.domain.review.model;

import com.matzip.server.domain.comment.model.Comment;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name="review")
@NoArgsConstructor
@Getter @Setter
public class Review extends BaseTimeEntity {
    @OneToMany(mappedBy="review", cascade=CascadeType.PERSIST)
    private final List<Comment> comments = new LinkedList<>();
    @OneToMany(mappedBy="review", cascade=CascadeType.PERSIST)
    private final List<Scrap> scraps = new LinkedList<>();
    @OneToMany(mappedBy="review", cascade=CascadeType.PERSIST)
    private final List<Heart> hearts = new LinkedList<>();
    @ElementCollection(fetch = FetchType.EAGER)
    private final List<String> reviewImages = new LinkedList<>();
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn
    private User user;
    private String content;
    private Integer rating;
    private String restaurant;
    private Long views = 0L;

    public Review(User user, ReviewDto.PostRequest postRequest) {
        this.user = user;
        this.content = postRequest.content();
        this.rating = postRequest.rating();
        this.restaurant = postRequest.restaurant();
        user.getReviews().add(this);
    }

    public void delete() {
        user.getReviews().remove(this);
    }
}