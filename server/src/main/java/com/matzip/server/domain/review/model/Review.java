package com.matzip.server.domain.review.model;

import com.matzip.server.domain.me.model.Heart;
import com.matzip.server.domain.image.model.Image;
import com.matzip.server.domain.me.model.Scrap;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name="review")
@NoArgsConstructor
@Getter
public class Review extends BaseTimeEntity {
    @OneToMany(mappedBy="review", orphanRemoval=true, cascade=CascadeType.ALL)
    private final List<Comment> comments = new ArrayList<>();
    @OneToMany(mappedBy="review", orphanRemoval=true, cascade=CascadeType.ALL)
    private final List<Scrap> scraps = new ArrayList<>();
    @OneToMany(mappedBy="review", orphanRemoval=true, cascade=CascadeType.ALL)
    private final List<Heart> hearts = new ArrayList<>();
    @OneToMany(mappedBy = "review", orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<Image> images = new ArrayList<>();
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn
    private User user;
    @NotBlank
    private String content;
    @Range(min=0, max=10)
    private Integer rating;
    @NotBlank
    private String location;

    public Review(User user, ReviewDto.PostRequest postRequest, List<String> imageUrls) {
        this.user = user;
        user.addReview(this);
        this.content = postRequest.getContent();
        this.images.addAll(imageUrls.stream().map(url -> new Image(this, url)).collect(Collectors.toList()));
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

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public void deleteComment(Long commentId) {
        this.comments.removeIf(c -> c.getId().equals(commentId));
    }

    public void addScrap(Scrap scrap) {
        this.scraps.add(scrap);
    }

    public void deleteScrap(Long scrapId) {
        this.scraps.removeIf(s -> s.getId().equals(scrapId));
    }

    public void addHeart(Heart heart) {
        this.hearts.add(heart);
    }

    public void deleteHeart(Long heartId) {
        this.hearts.removeIf(h -> h.getId().equals(heartId));
    }

    public void addImages(List<Image> images) {
        this.images.addAll(images);
    }

    public void deleteImages(List<String> urls) {
        this.images.removeIf(i -> urls.contains(i.getImageUrl()));
    }
}