package com.matzip.server.domain.review.model;

import com.matzip.server.domain.review.exception.InvalidCommentPropertyException;

import java.util.Arrays;

public enum CommentProperty {
    ;

    private final String webNaming;

    CommentProperty(String webNaming) {
        this.webNaming = webNaming;
    }

    public static CommentProperty from(String webNaming) {
        if (webNaming == null || webNaming.isBlank())
            return null;
        return Arrays.stream(values()).filter(name -> name.webNaming.equals(webNaming)).findFirst().orElseThrow(
                () -> new InvalidCommentPropertyException(webNaming)
        );
    }
}
