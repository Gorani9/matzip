package com.matzip.server.domain.user.model;

import com.matzip.server.domain.user.exception.InvalidUserPropertyException;

import java.util.Arrays;

public enum UserProperty {
    USERNAME("username"),
    MATZIP_LEVEL("level"),
    NUMBER_OF_FOLLOWERS("followers");

    private final String webNaming;

    UserProperty(String webNaming) {
        this.webNaming = webNaming;
    }

    public static UserProperty from(String webNaming) {
        if (webNaming == null || webNaming.isBlank())
            return null;
        return Arrays.stream(values()).filter(name -> name.webNaming.equals(webNaming)).findFirst().orElseThrow(
                () -> new InvalidUserPropertyException(webNaming)
        );
    }
}
