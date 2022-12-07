package com.matzip.server.domain.image.model;

import com.matzip.server.domain.review.model.Review;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "review_image")
@NoArgsConstructor
@Getter
public class ReviewImage extends BaseImageEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Review review;

    public ReviewImage(Review review, String imageUrl) {
        super(imageUrl);
        this.review = review;
        review.addImage(this);
    }
}
