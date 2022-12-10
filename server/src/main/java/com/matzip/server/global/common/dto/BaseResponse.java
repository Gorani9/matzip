package com.matzip.server.global.common.dto;

import lombok.Getter;

@Getter
public abstract class BaseResponse {
    private final boolean normal;
    protected BaseResponse(boolean normal) {
        this.normal = normal;
    }

}
