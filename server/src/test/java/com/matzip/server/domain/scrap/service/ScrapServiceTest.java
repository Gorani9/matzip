package com.matzip.server.domain.scrap.service;

import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.scrap.dto.ScrapDto.PatchRequest;
import com.matzip.server.domain.scrap.dto.ScrapDto.PostRequest;
import com.matzip.server.domain.scrap.dto.ScrapDto.Response;
import com.matzip.server.domain.scrap.exception.DuplicateScrapException;
import com.matzip.server.domain.scrap.exception.ScrapMyReviewException;
import com.matzip.server.domain.scrap.exception.ScrapNotFoundException;
import com.matzip.server.domain.scrap.repository.ScrapRepository;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.config.TestQueryDslConfig;
import com.matzip.server.global.utils.TestDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@ActiveProfiles("test")
@DisplayName("ScrapService 테스트")
class ScrapServiceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ScrapRepository scrapRepository;

    private ScrapService scrapService;
    private List<User> users;

    @PostConstruct
    void init() {
        users = TestDataUtils.testData();
        scrapService = new ScrapService(userRepository, reviewRepository, scrapRepository);
    }

    @BeforeEach
    public void setUp() {
        userRepository.saveAll(users);
    }

    @Test
    @DisplayName("스크랩 생성 테스트: 정상")
    void postScrapTest() {
        // given
        User user = userRepository.findByUsername("user-01").orElseThrow();
        Review review = users.get(2).getReviews().get(0);
        String description = "post scrap test";
        PostRequest request = new PostRequest(review.getId(), description);

        // when
        Response response = scrapService.postScrap(user.getId(), request);

        // then
        assertThat(response.getReview().getId()).isEqualTo(review.getId());
        assertThat(response.getDescription()).isEqualTo(description);
    }

    @Test
    @DisplayName("스크랩 생성 테스트: 내 리뷰를 스크랩하는 경우")
    void postScrapTest_ScrapMyReview() {
        // given
        User user = userRepository.findByUsername("user-01").orElseThrow();
        Review review = user.getReviews().get(0);
        String description = "post scrap test";
        PostRequest request = new PostRequest(review.getId(), description);

        // then
        assertThatThrownBy(() -> scrapService.postScrap(user.getId(), request))
                .isInstanceOf(ScrapMyReviewException.class);
    }

    @Test
    @DisplayName("스크랩 생성 테스트: 이미 스크랩했을 경우")
    void postScrapTest_DuplicateScrap() {
        // given
        User user = userRepository.findByUsername("user-02").orElseThrow();
        Review review = users.get(0).getReviews().get(0);
        String description = "post scrap test";
        PostRequest request = new PostRequest(review.getId(), description);

        // then
        assertThatThrownBy(() -> scrapService.postScrap(user.getId(), request))
                .isInstanceOf(DuplicateScrapException.class);
    }

    @Test
    @DisplayName("스크랩 수정 테스트: 정상")
    void patchScrapTest() {
        // given
        User user = userRepository.findByUsername("user-02").orElseThrow();
        Review review = users.get(0).getReviews().get(0);

        String beforeDescription = user.getScraps()
                .stream().filter(s -> Objects.equals(s.getReview().getId(), review.getId())).findFirst().orElseThrow()
                .getDescription();
        String description = "patch scrap test";
        PatchRequest request = new PatchRequest(description);

        // when
        Response response = scrapService.patchScrap(user.getId(), review.getId(), request);

        // then
        assertThat(response.getDescription()).isEqualTo(description);
        assertThat(response.getDescription()).isNotEqualTo(beforeDescription);
    }

    @Test
    @DisplayName("스크랩 수정 테스트: 스크랩을 안 한 경우")
    void patchScrapTest_NoScrap() {
        // given
        User user = userRepository.findByUsername("user-03").orElseThrow();
        Review review = users.get(0).getReviews().get(0);
        String description = "patch scrap test";
        PatchRequest request = new PatchRequest(description);

        // then
        assertThatThrownBy(() -> scrapService.patchScrap(user.getId(), review.getId(), request))
                .isInstanceOf(ScrapNotFoundException.class);
    }

    @Test
    @DisplayName("스크랩 삭제 테스트: 정상")
    void deleteScrapTest() {
        // given
        User user = userRepository.findByUsername("user-02").orElseThrow();
        Review review = users.get(0).getReviews().get(0);

        // when
        scrapService.deleteScrap(user.getId(), review.getId());

        // then
        assertThat(scrapRepository.existsByUserIdAndReviewId(user.getId(), review.getId())).isFalse();
    }

    @Test
    @DisplayName("스크랩 삭제 테스트: 스크랩을 안 한 경우")
    void deleteScrapTest_NoScrap() {
        // given
        User user = userRepository.findByUsername("user-03").orElseThrow();
        Review review = users.get(0).getReviews().get(0);

        // then
        assertThatThrownBy(() -> scrapService.deleteScrap(user.getId(), review.getId()))
                .isInstanceOf(ScrapNotFoundException.class);
    }
}