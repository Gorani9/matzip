package com.matzip.server.domain.image.model;

import com.matzip.server.domain.review.model.Review;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "review_image")
@NoArgsConstructor
@Getter
public class ReviewImage extends BaseTimeEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Review review;

    @NotBlank
    @URL
    private String imageUrl;

    public ReviewImage(Review review, String imageUrl) {
        this.review = review;
        review.addImage(this);
        this.imageUrl = imageUrl;
    }
}
