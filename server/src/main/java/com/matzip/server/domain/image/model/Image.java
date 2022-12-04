package com.matzip.server.domain.image.model;

import com.matzip.server.domain.review.model.Review;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "image")
@NoArgsConstructor
@Getter
public class Image extends BaseTimeEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Review review;

    @NotBlank
    private String imageUrl;

    public Image(Review review, String imageUrl) {
        this.review = review;
        this.imageUrl = imageUrl;
    }
}
