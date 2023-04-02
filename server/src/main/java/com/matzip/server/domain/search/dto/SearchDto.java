package com.matzip.server.domain.search.dto;

import com.matzip.server.domain.review.model.ReviewProperty;
import com.matzip.server.domain.user.model.UserProperty;

public class SearchDto {
    public record ReviewSearch(String keyword, Integer page, Integer size, ReviewProperty sort, Boolean asc) {}
    public record UserSearch(String username, Integer page, Integer size, UserProperty sort, Boolean asc) {}
}
