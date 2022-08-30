package com.matzip.server.domain.review.service;

import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.exception.ReviewChangeByAnonymousException;
import com.matzip.server.domain.review.exception.ReviewNotFoundException;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.user.exception.UsernameNotFoundException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly=true)
public class ReviewService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ImageService imageService;

    @Transactional
    public ReviewDto.Response postReview(String username, ReviewDto.PostRequest postRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        List<String> images = imageService.uploadImages(
                user.getUsername(),
                postRequest.getImages());
        return new ReviewDto.Response(user, reviewRepository.save(new Review(user, postRequest, images)));
    }

    public ReviewDto.Response getReview(String username, Long id) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return new ReviewDto.Response(
                user,
                reviewRepository.findAllById(id)
                        .orElseThrow(() -> new ReviewNotFoundException(id)));
    }

    public Page<ReviewDto.Response> searchReviews(String username, ReviewDto.SearchRequest searchRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        Sort sort = searchRequest.getAscending() ? Sort.by(searchRequest.getSortedBy()).ascending()
                                                 : Sort.by(searchRequest.getSortedBy()).descending();
        Pageable pageable = PageRequest.of(searchRequest.getPageNumber(), searchRequest.getPageSize(), sort);
        String searchType = searchRequest.getSearchType();
        String keyword = searchRequest.getKeyword();
        switch (searchType) {
            case "location":
                return reviewRepository.findAllByLocationContains(pageable, keyword)
                        .map(r -> new ReviewDto.Response(user, r));
            case "content":
                return reviewRepository.findAllByContentContains(pageable, keyword)
                        .map(r -> new ReviewDto.Response(user, r));
            default:
                return reviewRepository.findAll(pageable).map(r -> new ReviewDto.Response(user, r));
        }
    }

    @Transactional
    public ReviewDto.Response patchReview(String username, Long id, ReviewDto.PatchRequest patchRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        Review review = reviewRepository.findAllById(id).orElseThrow(() -> new ReviewNotFoundException(id));
        if (!Objects.equals(review.getUser().getId(), user.getId()) && !user.getRole().equals("ADMIN"))
            throw new ReviewChangeByAnonymousException();
        if (Optional.ofNullable(patchRequest.getContent()).isPresent())
            review.setContent(patchRequest.getContent());
        if (Optional.ofNullable(patchRequest.getNewImages()).isPresent())
            review.getImageUrls().addAll(imageService.uploadImages(user.getUsername(), patchRequest.getNewImages()));
        if (Optional.ofNullable(patchRequest.getOldUrls()).isPresent()) {
            imageService.deleteImages(patchRequest.getOldUrls());
            review.getImageUrls().removeIf(u -> patchRequest.getOldUrls().contains(u));
        }
        if (Optional.ofNullable(patchRequest.getRating()).isPresent())
            review.setRating(patchRequest.getRating());
        return new ReviewDto.Response(user, reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(User user, Long id) {
        Review review = reviewRepository.findAllById(id).orElseThrow(() -> new ReviewNotFoundException(id));
        if (!Objects.equals(review.getUser().getId(), user.getId()) && !user.getRole().equals("ADMIN"))
            throw new ReviewChangeByAnonymousException();
        reviewRepository.delete(review);
    }

    private List<LocalDateTime> getStartingTime() {
        LinkedList<LocalDateTime> localDateTimes = new LinkedList<>();
        localDateTimes.add(LocalDateTime.now().minusDays(1));
        localDateTimes.add(LocalDateTime.now().minusWeeks(1));
        localDateTimes.add(LocalDateTime.now().minusMonths(1));
        return localDateTimes;
    }

    public ReviewDto.HotResponse getHotReviews(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        List<LocalDateTime> times = getStartingTime();
        List<ReviewDto.Response> dailyHotReviews = reviewRepository.findAllByCreatedAtAfter(pageable, times.get(0))
                .stream()
                .map(r -> new ReviewDto.Response(user, r))
                .collect(Collectors.toList());
        List<ReviewDto.Response> weeklyHotReviews = reviewRepository.findAllByCreatedAtAfter(pageable, times.get(1))
                .stream()
                .map(r -> new ReviewDto.Response(user, r))
                .collect(Collectors.toList());
        List<ReviewDto.Response> monthlyHotReviews = reviewRepository.findAllByCreatedAtAfter(pageable, times.get(2))
                .stream()
                .map(r -> new ReviewDto.Response(user, r))
                .collect(Collectors.toList());
        return new ReviewDto.HotResponse(dailyHotReviews, weeklyHotReviews, monthlyHotReviews);
    }

    public ReviewDto.HallOfFameResponse getHallOfFameReviews(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        return new ReviewDto.HallOfFameResponse(reviewRepository.findAll(pageable)
                                                        .stream()
                                                        .map(r -> new ReviewDto.Response(user, r))
                                                        .collect(Collectors.toList()));
    }
}
