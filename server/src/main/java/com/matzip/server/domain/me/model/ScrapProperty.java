package com.matzip.server.domain.me.model;

import com.matzip.server.domain.review.exception.InvalidReviewPropertyException;

import java.util.Arrays;

public enum ScrapProperty {
    REVIEWER_USERNAME("username"),
    REVIEWER_MATZIP_LEVEL("level"),
    REVIEWER_NUMBER_OF_FOLLOWERS("followers"),
    REVIEW_NUMBER_OF_HEARTS("hearts"),
    REVIEW_NUMBER_OF_SCRAPS("scraps"),
    REVIEW_NUMBER_OF_COMMENTS("comments"),
    REVIEW_RATING("rating"),
    REVIEW_CREATED_AT("review-time")
    ;

    private final String webNaming;

    ScrapProperty(String webNaming) {
        this.webNaming = webNaming;
    }

    public static ScrapProperty from(String webNaming) {
        if (webNaming == null || webNaming.isBlank())
            return null;
        return Arrays.stream(values()).filter(name -> name.webNaming.equals(webNaming)).findFirst().orElseThrow(
                () -> new InvalidReviewPropertyException(webNaming)
        );
    }
}
