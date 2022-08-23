package com.matzip.server.domain.review.model;

import com.matzip.server.domain.me.model.Heart;
import com.matzip.server.domain.me.model.Scrap;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name="review")
@NoArgsConstructor
@Getter
public class Review extends BaseTimeEntity {
    @OneToMany(mappedBy="review", orphanRemoval=true, cascade=CascadeType.ALL)
    private final List<Comment> comments = List.of();
    @OneToMany(mappedBy="review", orphanRemoval=true, cascade=CascadeType.ALL)
    private final List<Scrap> scraps = List.of();
    @OneToMany(mappedBy="review", orphanRemoval=true, cascade=CascadeType.ALL)
    private final List<Heart> hearts = List.of();
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn
    private User user;
    @NotBlank
    private String content;
    @ElementCollection
    private List<String> imageUrls;
    @Range(min=0, max=10)
    private Integer rating;
    @NotBlank
    private String location;

    public Review(User user, ReviewDto.PostRequest postRequest, List<String> imageUrls) {
        this.user = user;
        this.content = postRequest.getContent();
        this.imageUrls = imageUrls;
        this.rating = postRequest.getRating();
        this.location = postRequest.getLocation();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Review && this.getId().equals(((Review) obj).getId());
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}