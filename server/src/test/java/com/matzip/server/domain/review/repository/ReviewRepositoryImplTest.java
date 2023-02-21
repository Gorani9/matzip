package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.dto.ReviewDto.SearchRequest;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.config.TestQueryDslConfig;
import com.matzip.server.global.utils.TestDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.matzip.server.domain.review.model.ReviewProperty.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@ActiveProfiles("test")
@DisplayName("UserRepository 테스트")
class ReviewRepositoryImplTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    public void setUp() {
        userRepository.saveAll(TestDataUtils.testData());
    }

    @Test
    @DisplayName("페이징 테스트: 다음 페이지가 있는 경우")
    void pagingHasNextTest() {
        // given
        SearchRequest request = new SearchRequest("review", 0, 5, null, false);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getNumberOfElements()).isEqualTo(5);
        assertThat(reviews.hasNext()).isEqualTo(true);
    }

    @Test
    @DisplayName("페이징 테스트: 마지막 페이지인 경우")
    void pagingTest2() {
        // given
        SearchRequest request = new SearchRequest("review", 0, 6, null, false);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getNumberOfElements()).isEqualTo(6);
        assertThat(reviews.hasNext()).isEqualTo(false);
    }

    @Test
    @DisplayName("검색 테스트")
    void searchingTest() {
        // given
        SearchRequest request = new SearchRequest("01", 0, 6, null, false);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getNumberOfElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("정렬 테스트: Username 기준")
    void sortingByUsernameTest() {
        // given
        SearchRequest request = new SearchRequest("review", 0, 6, REVIEWER_USERNAME, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getNumberOfElements()).isEqualTo(6);
        assertThat(reviews.getContent()).extracting("content")
                .containsExactly("review-02", "review-01", "review-03", "review-06", "review-05", "review-04");
    }

    @Test
    @DisplayName("정렬 테스트: Level 기준")
    void sortingByLevelTest() {
        // given
        SearchRequest request = new SearchRequest("review", 0, 6, REVIEWER_MATZIP_LEVEL, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getNumberOfElements()).isEqualTo(6);
        assertThat(reviews.getContent()).extracting("content")
                .containsExactly("review-06", "review-02", "review-01", "review-05", "review-04", "review-03");
    }

    @Test
    @DisplayName("정렬 테스트: 팔로워 수 기준")
    void sortingByFollowersTest() {
        // given
        SearchRequest request = new SearchRequest("review", 0, 6, REVIEWER_NUMBER_OF_FOLLOWERS, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getNumberOfElements()).isEqualTo(6);
        assertThat(reviews.getContent()).extracting("content")
                .containsExactly("review-06", "review-03", "review-02", "review-01", "review-05", "review-04");
    }

    @Test
    @DisplayName("정렬 테스트: 좋아요 수 기준")
    void sortingByHeartsTest() {
        // given
        SearchRequest request = new SearchRequest("review", 0, 6, NUMBER_OF_HEARTS, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getNumberOfElements()).isEqualTo(6);
        assertThat(reviews.getContent()).extracting("content")
                .containsExactly("review-04", "review-03", "review-02", "review-06", "review-01", "review-05");
    }

    @Test
    @DisplayName("정렬 테스트: 스크랩 수 기준")
    void sortingByScrapsTest() {
        // given
        SearchRequest request = new SearchRequest("review", 0, 6, NUMBER_OF_SCRAPS, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getNumberOfElements()).isEqualTo(6);
        assertThat(reviews.getContent()).extracting("content")
                .containsExactly("review-04", "review-06", "review-02", "review-05", "review-03", "review-01");
    }

    @Test
    @DisplayName("정렬 테스트: 댓글 수 기준")
    void sortingByCommentsTest() {
        // given
        SearchRequest request = new SearchRequest("review", 0, 6, NUMBER_OF_COMMENTS, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getNumberOfElements()).isEqualTo(6);
        assertThat(reviews.getContent()).extracting("content")
                .containsExactly("review-05", "review-02", "review-04", "review-03", "review-01", "review-06");
    }

    @Test
    @DisplayName("정렬 테스트: 별점 기준")
    void sortingByRatingsTest() {
        // given
        SearchRequest request = new SearchRequest("review", 0, 6, RATING, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getNumberOfElements()).isEqualTo(6);
        assertThat(reviews.getContent()).extracting("content")
                .containsExactly("review-05", "review-04", "review-02", "review-03", "review-01", "review-06");
    }

    @Test
    @DisplayName("정렬 테스트: 작성 일자 기준")
    void sortingByCreatedAtTest() {
        // given
        SearchRequest request = new SearchRequest("review", 0, 6, null, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getNumberOfElements()).isEqualTo(6);
        assertThat(reviews.getContent()).extracting("content")
                .containsExactly("review-01", "review-02", "review-03", "review-06", "review-04", "review-05");
    }

    @Test
    @DisplayName("인기 리뷰 테스트")
    void hotReviewsTest() {
        // when
        List<Review> reviews = reviewRepository.fetchHotReviews();

        // then
        assertThat(reviews.size()).isEqualTo(6);
        assertThat(reviews).extracting("content")
                .containsExactly("review-05", "review-06", "review-01", "review-02", "review-04", "review-03");
    }
}