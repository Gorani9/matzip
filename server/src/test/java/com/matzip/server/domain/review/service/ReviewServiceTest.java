package com.matzip.server.domain.review.service;

import com.matzip.server.domain.comment.repository.CommentRepository;
import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.record.service.RecordService;
import com.matzip.server.domain.review.dto.ReviewDto.PatchRequest;
import com.matzip.server.domain.review.dto.ReviewDto.PostRequest;
import com.matzip.server.domain.review.dto.ReviewDto.Response;
import com.matzip.server.domain.review.dto.ReviewDto.ScrapRequest;
import com.matzip.server.domain.review.exception.*;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.HeartRepository;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.review.repository.ScrapRepository;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.config.TestQueryDslConfig;
import com.matzip.server.global.utils.TestDataUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@ActiveProfiles("test")
@DisplayName("ReviewService 테스트")
class ReviewServiceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ScrapRepository scrapRepository;
    @Autowired
    private HeartRepository heartRepository;
    @MockBean
    private ImageService imageService;
    @MockBean
    private RecordService recordService;

    private ReviewService reviewService;
    private List<User> users;

    @PostConstruct
    void init() {
        users = TestDataUtils.testData();
        reviewService = new ReviewService(userRepository, reviewRepository, commentRepository,
                                          scrapRepository, heartRepository, imageService, recordService);

        given(imageService.uploadImages(any(), any())).willReturn(List.of("https://" + UUID.randomUUID() + ".url"));
        given(imageService.deleteImages(any())).willReturn(List.of("https://" + UUID.randomUUID() + ".url"));
    }

    @BeforeEach
    public void setUp() {
        userRepository.saveAll(users);
    }

    @Test
    @DisplayName("리뷰 작성 테스트: 정상")
    void postReviewTest() {
        // given
        User user = users.get(0);
        String content = "some content";
        String location = "restaurant";
        PostRequest request = new PostRequest(content, null, 5, location);

        // when
        Response response = reviewService.postReview(user.getId(), request);

        // then
        assertThat(response.getUser().getUsername()).isEqualTo(user.getUsername());
        assertThat(response.getContent()).isEqualTo(content);
        assertThat(response.getRestaurant()).isEqualTo(location);
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getViews()).isEqualTo(0);
    }

    @Test
    @DisplayName("리뷰 조회 테스트: 정상")
    void fetchReviewTest() {
        // given
        User user = users.get(0);
        Review review = user.getReviews().get(0);

        // when
        Response response = reviewService.fetchReview(user.getId(), review.getId());

        // then
        assertThat(response.getId()).isEqualTo(review.getId());
        assertThat(response.getContent()).isEqualTo(review.getContent());
    }

    @Test
    @DisplayName("리뷰 수정 테스트: 정상")
    void patchReviewTest() {
        // given
        User user = users.get(0);
        Review review = user.getReviews().get(0);
        review.getReviewImages().add("https://some-image.url");
        String oldContent = review.getContent();
        String newContent = "some new content";
        long beforeView = review.getViews();
        PatchRequest request = new PatchRequest(newContent, null, null, null);

        // when
        Response response = reviewService.patchReview(user.getId(), review.getId(), request);

        // then
        assertThat(response.getId()).isEqualTo(review.getId());
        assertThat(response.getContent()).isEqualTo(newContent);
        assertThat(response.getContent()).isNotEqualTo(oldContent);
        assertThat(response.getViews()).isEqualTo(beforeView);
    }

    @Test
    @DisplayName("리뷰 수정 테스트: 리뷰가 존재하지 않을 때")
    void patchReviewTest_NoReview() {
        // given
        User user = users.get(0);
        long reviewId = 100;
        PatchRequest request = new PatchRequest(null, null, null, null);

        // then
        assertThatThrownBy(() -> reviewService.patchReview(user.getId(), reviewId, request))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    @DisplayName("리뷰 수정 테스트: 내가 작성하지 않았을 때")
    void patchReviewTest_NotMyReview() {
        // given
        User user = users.get(0);
        Review review = users.get(2).getReviews().get(0);
        PatchRequest request = new PatchRequest(null, null, null, null);

        // then
        assertThatThrownBy(() -> reviewService.patchReview(user.getId(), review.getId(), request))
                .isInstanceOf(ReviewAccessDeniedException.class);
    }

    @Test
    @DisplayName("리뷰 수정 테스트: 삭제할 이미지 URL이 잘못되었을 때")
    void patchReviewTest_InvalidURL() {
        // given
        User user = users.get(0);
        Review review = user.getReviews().get(0);
        List<String> oldUrls = List.of("https://invalid-image.url");
        PatchRequest request = new PatchRequest(null, null, oldUrls, null);

        // then
        assertThatThrownBy(() -> reviewService.patchReview(user.getId(), review.getId(), request))
                .isInstanceOf(ReviewImageUrlNotFound.class);
    }

    @Test
    @DisplayName("리뷰 수정 테스트: 마지막 이미지를 삭제할 경우")
    void patchReviewTest_DeleteLastImage() {
        // given
        User user = users.get(0);
        Review review = user.getReviews().get(0);
        review.getReviewImages().add("https://valid-image.url");
        List<String> oldUrls = List.of("https://valid-image.url");
        PatchRequest request = new PatchRequest(null, null, oldUrls, null);

        // then
        assertThatThrownBy(() -> reviewService.patchReview(user.getId(), review.getId(), request))
                .isInstanceOf(DeleteLastImageException.class);
    }

    @Test
    @DisplayName("리뷰 삭제 테스트: 정상")
    void deleteReviewTest() {
        // given
        User user = users.get(0);
        Review review = user.getReviews().get(0);
        long expectedReviewCount = reviewRepository.count() - 1;
        long expectedCommentCount = commentRepository.count() - review.getComments().size();
        long expectedScrapCount = scrapRepository.count() - review.getScraps().size();
        long expectedHeartCount = heartRepository.count() - review.getHearts().size();

        // when
        reviewService.deleteReview(user.getId(), review.getId());

        // then
        assertThat(reviewRepository.findById(review.getId())).isEmpty();
        assertThat(reviewRepository.count()).isEqualTo(expectedReviewCount);
        assertThat(commentRepository.count()).isEqualTo(expectedCommentCount);
        assertThat(scrapRepository.count()).isEqualTo(expectedScrapCount);
        assertThat(heartRepository.count()).isEqualTo(expectedHeartCount);
    }

    @Test
    @DisplayName("리뷰 삭제 테스트: 리뷰가 존재하지 않을 때")
    void deleteReviewTest_NoReview() {
        // given
        User user = users.get(0);
        long reviewId = 100;

        // then
        assertThatThrownBy(() -> reviewService.deleteReview(user.getId(), reviewId))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    @DisplayName("리뷰 삭제 테스트: 내가 작성하지 않았을 때")
    void deleteReviewTest_NotMyReview() {
        // given
        User user = users.get(0);
        Review review = users.get(2).getReviews().get(0);

        // then
        assertThatThrownBy(() -> reviewService.deleteReview(user.getId(), review.getId()))
                .isInstanceOf(ReviewAccessDeniedException.class);
    }

    @Test
    @DisplayName("리뷰 좋아요 테스트: 정상")
    void heartReviewTest() {
        // given
        User user = users.get(0);
        Review review = users.get(2).getReviews().get(0);
        long beforeView = review.getViews();
        long beforeHeartCount = review.getHearts().size();

        // when
        Response response = reviewService.heartReview(user.getId(), review.getId());

        // then
        assertThat(response.getNumberOfHearts()).isEqualTo(beforeHeartCount + 1);
        assertThat(response.getViews()).isEqualTo(beforeView);
    }

    @Test
    @DisplayName("리뷰 좋아요 테스트: 리뷰가 존재하지 않을 때")
    void heartReviewTest_NoReview() {
        // given
        User user = users.get(0);
        long reviewId = 100;

        // then
        assertThatThrownBy(() -> reviewService.heartReview(user.getId(), reviewId))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    @DisplayName("리뷰 좋아요 테스트: 내가 작성했을 때")
    void heartReviewTest_MyReview() {
        // given
        User user = users.get(0);
        Review review = user.getReviews().get(0);

        // then
        assertThatThrownBy(() -> reviewService.heartReview(user.getId(), review.getId()))
                .isInstanceOf(HeartMyReviewException.class);
    }

    @Test
    @DisplayName("리뷰 좋아요 테스트: 이미 좋아요 했을 때")
    void heartReviewTest_DuplicateHeart() {
        // given
        User user = users.get(0);
        Review review = users.get(4).getReviews().get(1);

        // then
        assertThatThrownBy(() -> reviewService.heartReview(user.getId(), review.getId()))
                .isInstanceOf(DuplicateHeartException.class);
    }

    @Test
    @DisplayName("리뷰 좋아요 취소 테스트: 정상")
    void deleteHeartReviewTest() {
        // given
        User user = users.get(0);
        Review review = users.get(4).getReviews().get(1);
        long beforeView = review.getViews();
        long beforeHeartCount = review.getHearts().size();

        // when
        Response response = reviewService.deleteHeartFromReview(user.getId(), review.getId());

        // then
        assertThat(response.getNumberOfHearts()).isEqualTo(beforeHeartCount - 1);
        assertThat(response.getViews()).isEqualTo(beforeView);
    }

    @Test
    @DisplayName("리뷰 좋아요 취소 테스트: 리뷰가 존재하지 않을 때")
    void deleteHeartReviewTest_NoReview() {
        // given
        User user = users.get(0);
        long reviewId = 100;

        // then
        assertThatThrownBy(() -> reviewService.deleteHeartFromReview(user.getId(), reviewId))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    @DisplayName("리뷰 좋아요 취소 테스트: 좋아요를 하지 않았을 때")
    void deleteHeartReviewTest_NoHeart() {
        // given
        User user = users.get(0);
        Review review = users.get(2).getReviews().get(0);
        long beforeView = review.getViews();
        long beforeHeartCount = review.getHearts().size();

        // when
        Response response = reviewService.deleteHeartFromReview(user.getId(), review.getId());

        // then
        assertThat(response.getNumberOfHearts()).isEqualTo(beforeHeartCount);
        assertThat(response.getViews()).isEqualTo(beforeView);
    }

    @Test
    @DisplayName("스크랩 생성 테스트: 정상")
    void putScrapTest_New() {
        // given
        User user = userRepository.findByUsername("user-01").orElseThrow();
        Review review = users.get(2).getReviews().get(0);
        String description = "post scrap test";
        ScrapRequest request = new ScrapRequest(description);

        // when
        Response response = reviewService.putScrap(user.getId(), review.getId(), request);

        // then
        assertThat(response.getId()).isEqualTo(review.getId());
        assertThat(response.getScrapDescription()).isEqualTo(description);
    }

    @Test
    @DisplayName("스크랩 생성 테스트: 내 리뷰를 스크랩하는 경우")
    void putScrapTest_ScrapMyReview() {
        // given
        User user = userRepository.findByUsername("user-01").orElseThrow();
        Review review = user.getReviews().get(0);
        String description = "post scrap test";
        ScrapRequest request = new ScrapRequest(description);

        // then
        Assertions.assertThatThrownBy(() -> reviewService.putScrap(user.getId(), review.getId(), request))
                .isInstanceOf(ScrapMyReviewException.class);
    }

    @Test
    @DisplayName("스크랩 수정 테스트: 정상")
    void putScrapTest() {
        // given
        User user = userRepository.findByUsername("user-02").orElseThrow();
        Review review = users.get(0).getReviews().get(0);

        String beforeDescription = user.getScraps()
                .stream().filter(s -> Objects.equals(s.getReview().getId(), review.getId())).findFirst().orElseThrow()
                .getDescription();
        String description = "patch scrap test";
        ScrapRequest request = new ScrapRequest(description);

        // when
        Response response = reviewService.putScrap(user.getId(), review.getId(), request);

        // then
        assertThat(response.getScrapDescription()).isEqualTo(description);
        assertThat(response.getScrapDescription()).isNotEqualTo(beforeDescription);
    }

    @Test
    @DisplayName("스크랩 삭제 테스트: 정상")
    void deleteScrapTest() {
        // given
        User user = userRepository.findByUsername("user-02").orElseThrow();
        Review review = users.get(0).getReviews().get(0);

        // when
        reviewService.deleteScrap(user.getId(), review.getId());

        // then
        assertThat(scrapRepository.findByUserIdAndReviewId(user.getId(), review.getId())).isEmpty();
    }

    @Test
    @DisplayName("스크랩 삭제 테스트: 스크랩을 안 한 경우")
    void deleteScrapTest_NoScrap() {
        // given
        User user = userRepository.findByUsername("user-03").orElseThrow();
        Review review = users.get(0).getReviews().get(0);

        // then
        assertThatNoException().isThrownBy(() -> reviewService.deleteScrap(user.getId(), review.getId()));
    }
}