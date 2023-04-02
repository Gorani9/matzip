package com.matzip.server.domain.search.api;

import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.model.ReviewProperty;
import com.matzip.server.domain.search.dto.SearchDto.ReviewSearch;
import com.matzip.server.domain.search.dto.SearchDto.UserSearch;
import com.matzip.server.domain.search.service.SearchService;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.UserProperty;
import com.matzip.server.global.auth.model.CurrentUser;
import com.matzip.server.global.auth.model.CurrentUsername;
import com.matzip.server.global.common.logger.Logging;
import com.matzip.server.global.common.validation.NullableNotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/reviews")
    @Logging(endpoint="GET /api/v1/search/reviews")
    public ResponseEntity<Slice<ReviewDto.Response>> searchReviews(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @RequestParam(value = "keyword", required = false) @NullableNotBlank @Length(max=30) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "0") @PositiveOrZero Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") @Positive @Max(100) Integer size,
            @RequestParam(value = "sort", required = false) ReviewProperty reviewProperty,
            @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc
    ) {
        return ResponseEntity.ok(searchService.searchReviews(myId, new ReviewSearch(keyword, page, size, reviewProperty, asc)));
    }

    @GetMapping("/users")
    @Logging(endpoint="GET /api/v1/search/users")
    public ResponseEntity<Slice<UserDto.Response>> searchUsersByUsername(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @RequestParam(value = "username") @NotBlank @Length(max = 30) String username,
            @RequestParam(value = "page", required = false, defaultValue ="0") @PositiveOrZero Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") @Positive @Max(100) Integer size,
            @RequestParam(value = "sort", required = false) UserProperty userProperty,
            @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc
    ) {
        return ResponseEntity.ok(searchService.searchUsers(myId, new UserSearch(username, page, size, userProperty, asc)));
    }
}
