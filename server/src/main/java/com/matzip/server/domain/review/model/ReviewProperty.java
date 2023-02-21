package com.matzip.server.domain.review.model;

import java.util.Arrays;

public enum ReviewProperty {
    REVIEWER_USERNAME("username"),
    REVIEWER_MATZIP_LEVEL("level"),
    REVIEWER_NUMBER_OF_FOLLOWERS("followers"),
    NUMBER_OF_HEARTS("hearts"),
    NUMBER_OF_SCRAPS("scraps"),
    NUMBER_OF_COMMENTS("comments"),
    RATING("rating")
    ;

    private final String webNaming;

    ReviewProperty(String webNaming) {
        this.webNaming = webNaming;
    }

    public static ReviewProperty from(String webNaming) {
        if (webNaming == null || webNaming.isBlank())
            return null;
        return Arrays.stream(values()).filter(name -> name.webNaming.equals(webNaming)).findFirst().orElseThrow();
    }
}
