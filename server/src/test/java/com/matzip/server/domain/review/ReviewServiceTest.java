package com.matzip.server.domain.review;

import com.matzip.server.domain.image.exception.DeleteReviewLastImageException;
import com.matzip.server.domain.image.exception.OverloadReviewImagesException;
import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.dto.ReviewDto.PatchRequest;
import com.matzip.server.domain.review.dto.ReviewDto.PostRequest;
import com.matzip.server.domain.review.dto.ReviewDto.SearchRequest;
import com.matzip.server.domain.review.exception.AccessBlockedOrDeletedReviewException;
import com.matzip.server.domain.review.exception.ReviewChangeByAnonymousException;
import com.matzip.server.domain.review.exception.ReviewNotFoundException;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.review.service.ReviewService;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Tag("ServiceTest")
class ReviewServiceTest {
    @InjectMocks
    private ReviewService reviewService;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ImageService imageService;

    @Test
    @DisplayName("리뷰 포스팅 테스트 성공")
    public void postReviewTest_Success() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new ReviewDto.PostRequest("content", List.of(), 10, "location"));
        given(userRepository.findMeById(1L)).willReturn(user);
        given(reviewRepository.save(any())).willReturn(review);

        // when
        PostRequest request = new PostRequest("content", List.of(), 10, "location");
        ReviewDto.Response response = reviewService.postReview(1L, request);

        // then
        assertThat(response.getContent()).isEqualTo("content");
        assertThat(response.getUser()).extracting("username").isEqualTo(user.getUsername());
        assertThat(response.getRating()).isEqualTo(10);
        assertThat(response.getLocation()).isEqualTo("location");
    }

    @Test
    @DisplayName("리뷰 포스팅 테스트 실패: 리뷰 이미지 10개 초과")
    public void postReviewTest_OverloadingImages() {
        // given
        User user = new User("user", "password");
        MockMultipartFile dummy = new MockMultipartFile("dummy", new byte[0]);
        given(userRepository.findMeById(1L)).willReturn(user);

        // when
        PostRequest request = new PostRequest("content", List.of(
                dummy, dummy, dummy, dummy, dummy, dummy, dummy, dummy, dummy, dummy, dummy
        ), 10, "location");
        doThrow(OverloadReviewImagesException.class).when(imageService).uploadReviewImages(any(), any(), any());

        // then
        assertThrows(OverloadReviewImagesException.class, () -> reviewService.postReview(1L, request));
    }

    @Test
    @DisplayName("리뷰 조회 테스트 성공")
    public void fetchReviewTest_Success() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new PostRequest("content", null, 10, "location"));
        given(userRepository.findMeById(1L)).willReturn(user);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        // when
        ReviewDto.Response response = reviewService.fetchReview(1L, 1L);

        // then
        assertThat(response.getContent()).isEqualTo("content");
        assertThat(response.getRating()).isEqualTo(10);
        assertThat(response.getLocation()).isEqualTo("location");
    }

    @Test
    @DisplayName("리뷰 조회 테스트 실패: 존재하지 않는 리뷰 아이디")
    public void fetchReviewTest_ReviewNotFound() {
        // given
        User user = new User("user", "password");
        new Review(user, new PostRequest("content", null, 10, "location"));
        given(userRepository.findMeById(1L)).willReturn(user);

        // when
        when(reviewRepository.findById(100L)).thenThrow(ReviewNotFoundException.class);

        // then
        assertThrows(ReviewNotFoundException.class, () -> reviewService.fetchReview(1L, 100L));
    }

    @Test
    @DisplayName("리뷰 조회 테스트 실패: 블락되거나 삭제된 리뷰")
    public void fetchReviewTest_ReviewBlockedOrDeleted() {
        // given
        User user = new User("user", "password");
        Review review1 = new Review(user, new PostRequest("content", null, 10, "location"));
        Review review2 = new Review(user, new PostRequest("content", null, 10, "location"));
        given(userRepository.findMeById(1L)).willReturn(user);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review1));
        given(reviewRepository.findById(2L)).willReturn(Optional.of(review2));

        // when
        review1.block("test");
        review2.delete();

        // then
        assertThrows(AccessBlockedOrDeletedReviewException.class, () -> reviewService.fetchReview(1L, 1L));
        assertThrows(AccessBlockedOrDeletedReviewException.class, () -> reviewService.fetchReview(1L, 2L));
    }

    @Test
    @DisplayName("리뷰 검색 테스트")
    public void searchReviewTest() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new PostRequest("content", null, 10, "location"));
        given(userRepository.findMeById(1L)).willReturn(user);
        given(reviewRepository.searchReviewsByKeyword(any())).willReturn(new SliceImpl<>(
                List.of(review), Pageable.unpaged(), true));

        // when
        SearchRequest request = new SearchRequest("content", 0, 10, null, true);
        Slice<ReviewDto.Response> response = reviewService.searchReviews(1L, request);

        // then
        assertThat(response.getContent()).extracting("content").containsExactly(review.getContent());
        assertThat(response.getContent()).extracting("rating").containsExactly(review.getRating());
        assertThat(response.getContent()).extracting("location").containsExactly(review.getLocation());
    }

    @Test
    @DisplayName("리뷰 수정 테스트 성공")
    public void patchReviewTest_Success() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new PostRequest("content", null, 10, "location"));
        given(userRepository.findMeById(1L)).willReturn(user);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        // when
        PatchRequest request = new PatchRequest("new content", null, null, 3);
        ReviewDto.Response response = reviewService.patchReview(1L, 1L, request);

        // then
        assertThat(response.getContent()).isNotEqualTo("content");
        assertThat(response.getRating()).isNotEqualTo(10);
        assertThat(response.getContent()).isEqualTo("new content");
        assertThat(response.getRating()).isEqualTo(3);
        assertThat(response.getLocation()).isEqualTo("location");
    }

    @Test
    @DisplayName("리뷰 수정 테스트 실패: 다른 유저가 시도하는 경우")
    public void patchReviewTest_TrialByOther() {
        // given
        User user1 = new User("user1", "password");
        User user2 = new User("user2", "password");
        Review review = new Review(user1, new PostRequest("content", null, 10, "location"));
        given(userRepository.findMeById(2L)).willReturn(user2);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        // when
        PatchRequest request = new PatchRequest("new content", null, null, 3);

        // then
        assertThrows(ReviewChangeByAnonymousException.class, () -> reviewService.patchReview(2L, 1L, request));
    }

    @Test
    @DisplayName("리뷰 수정 테스트 실패: 모든 이미지를 삭제하려는 경우")
    public void patchReviewTest_NoImage() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new PostRequest("content", null, 10, "location"));
        given(userRepository.findMeById(1L)).willReturn(user);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        // when
        PatchRequest request = new PatchRequest("new content", null, List.of("old_url"), 3);
        doThrow(DeleteReviewLastImageException.class).when(imageService).deleteReviewImages(any(), any());

        // then
        assertThrows(DeleteReviewLastImageException.class, () -> reviewService.patchReview(1L, 1L, request));
    }

    @Test
    @DisplayName("리뷰 수정 테스트 실패: 리뷰 이미지 10장 초과하는 경우")
    public void patchReviewTest_OverloadingImages() {
        // given
        User user = new User("user", "password");
        MockMultipartFile dummy = new MockMultipartFile("dummy", new byte[0]);
        Review review = new Review(user, new PostRequest("content", List.of(
                dummy, dummy, dummy, dummy, dummy, dummy, dummy, dummy, dummy, dummy
        ), 10, "location"));
        given(userRepository.findMeById(1L)).willReturn(user);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        // when
        PatchRequest request = new PatchRequest("new content", List.of(dummy), List.of("old_url"), 3);
        doThrow(OverloadReviewImagesException.class).when(imageService).uploadReviewImages(any(), any(), any());

        // then
        assertThrows(OverloadReviewImagesException.class, () -> reviewService.patchReview(1L, 1L, request));
    }

    @Test
    @DisplayName("리뷰 수정 테스트 실패: 블락되거나 삭제된 리뷰")
    public void patchReviewTest_ReviewBlockedOrDeleted() {
        // given
        User user = new User("user", "password");
        Review review1 = new Review(user, new PostRequest("content", null, 10, "location"));
        Review review2 = new Review(user, new PostRequest("content", null, 10, "location"));
        given(userRepository.findMeById(1L)).willReturn(user);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review1));
        given(reviewRepository.findById(2L)).willReturn(Optional.of(review2));

        // when
        PatchRequest request = new PatchRequest("new content", null, List.of("old_url"), 3);
        review1.block("test");
        review2.delete();


        // then
        assertThrows(AccessBlockedOrDeletedReviewException.class, () -> reviewService.patchReview(1L, 1L, request));
        assertThrows(AccessBlockedOrDeletedReviewException.class, () -> reviewService.patchReview(1L, 2L, request));
    }
}