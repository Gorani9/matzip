package com.matzip.server.domain.user.model;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum UserProperty {
    USERNAME("username"),
    MATZIP_LEVEL("level"),
    NUMBER_OF_FOLLOWERS("followers")
    ;

    private final String webNaming;

    public static UserProperty from(String webNaming) {
        if (webNaming == null || webNaming.isBlank())
            return null;
        return Arrays.stream(values()).filter(name -> name.webNaming.equals(webNaming)).findFirst().orElseThrow();
    }
}
