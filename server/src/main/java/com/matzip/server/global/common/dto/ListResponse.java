package com.matzip.server.global.common.dto;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class ListResponse<T> {
    private final List<T> data;
    private final int count;

    public ListResponse(Stream<T> data) {
        this.data = data.collect(Collectors.toList());
        this.count = this.data.size();
    }
}
