package com.matzip.server.domain.search.service;

import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.search.dto.SearchDto.ReviewSearch;
import com.matzip.server.domain.search.dto.SearchDto.UserSearch;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public Slice<ReviewDto.Response> searchReviews(Long myId, ReviewSearch request) {
        User me = userRepository.findMeById(myId);
        Slice<Review> reviews = reviewRepository.searchReviewsByKeywordUsingFullText(request);
        return reviews.map(r -> new ReviewDto.Response(r, me));
    }

    public Slice<UserDto.Response> searchUsers(Long myId, UserSearch request) {
        User me = userRepository.findMeById(myId);
        Slice<User> users = userRepository.searchUsersByUsername(request);

        return users.map(user -> new UserDto.Response(user, me));
    }
}
