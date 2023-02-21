package com.matzip.server.domain.review.model;

import com.matzip.server.domain.comment.model.Comment;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.scrap.model.Scrap;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
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
    @ElementCollection(fetch = FetchType.LAZY)
    private final List<String> reviewImages = new LinkedList<>();
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn
    private User user;
    @NotBlank
    private String content;
    @Range(min=0, max=10)
    private Integer rating;
    @NotBlank
    private String location;
    private Long views = 0L;

    public Review(User user, ReviewDto.PostRequest postRequest) {
        this.user = user;
        this.content = postRequest.content();
        this.rating = postRequest.rating();
        this.location = postRequest.location();
        user.getReviews().add(this);
    }

    public void delete() {
        user.getReviews().remove(this);
    }
}