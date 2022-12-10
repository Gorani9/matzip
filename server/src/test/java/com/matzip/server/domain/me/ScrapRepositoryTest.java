package com.matzip.server.domain.me;

import com.matzip.server.domain.me.dto.ScrapDto;
import com.matzip.server.domain.me.model.Follow;
import com.matzip.server.domain.me.model.Heart;
import com.matzip.server.domain.me.model.Scrap;
import com.matzip.server.domain.me.repository.FollowRepository;
import com.matzip.server.domain.me.repository.HeartRepository;
import com.matzip.server.domain.me.repository.ScrapRepository;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.CommentRepository;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.config.TestQueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static com.matzip.server.domain.me.model.ScrapProperty.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Tag("RepositoryTest")
public class ScrapRepositoryTest {
    @Autowired
    private ScrapRepository scrapRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private HeartRepository heartRepository;
    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("내 스크랩 검색 기본 테스트: 스크랩 생성시점 기준 오름차순 정렬")
    void searchMyScrapsTest() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User reviewer = userRepository.save(new User("reviewer", "password"));
        Review review1 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content1", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content2", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content3", null, 10, "location")));
        Scrap scrap1 = scrapRepository.save(new Scrap(user, review1));
        Scrap scrap2 = scrapRepository.save(new Scrap(user, review2));
        Scrap scrap3 = scrapRepository.save(new Scrap(user, review3));

        // when
        ScrapDto.SearchRequest request = new ScrapDto.SearchRequest("", 0, 10, null, true);
        Slice<Scrap> scraps = scrapRepository.searchMyScrapsByKeyword(request, user.getId());

        // then
        assertThat(scraps).containsExactly(scrap1, scrap2, scrap3);
    }

    @Test
    @DisplayName("내 스크랩 검색 기본 테스트: 특정 내용을 포함하는 리뷰 스크랩 생성시점 기준 내림차순 정렬")
    void searchMyScrapsTest_UsingContent() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User reviewer = userRepository.save(new User("reviewer", "password"));
        Review review1 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("review 1 content", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("example", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("test", null, 10, "location")));
        Scrap scrap1 = scrapRepository.save(new Scrap(user, review1));
        Scrap scrap2 = scrapRepository.save(new Scrap(user, review2));
        Scrap scrap3 = scrapRepository.save(new Scrap(user, review3));

        // when
        ScrapDto.SearchRequest request = new ScrapDto.SearchRequest("te", 0, 10, null, false);
        Slice<Scrap> scraps = scrapRepository.searchMyScrapsByKeyword(request, user.getId());

        // then
        assertThat(scraps).containsExactly(scrap3, scrap1);
    }

    @Test
    @DisplayName("내 스크랩 검색 기본 테스트: 블락 처리된 리뷰 검색 결과에서 제외")
    void searchMyScrapsTest_ReviewBlockedOrDeleted() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User reviewer = userRepository.save(new User("reviewer", "password"));
        Review review1 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content1", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content2", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content3", null, 10, "location")));
        Scrap scrap1 = scrapRepository.save(new Scrap(user, review1));
        Scrap scrap2 = scrapRepository.save(new Scrap(user, review2));
        Scrap scrap3 = scrapRepository.save(new Scrap(user, review3));
        review2.block("test");
        review3.delete();

        // when
        ScrapDto.SearchRequest request = new ScrapDto.SearchRequest("", 0, 10, null, false);
        Slice<Scrap> scraps = scrapRepository.searchMyScrapsByKeyword(request, user.getId());

        // then
        assertThat(scraps).containsExactly(scrap3, scrap1);
    }

    @Test
    @DisplayName("내 스크랩 검색 기본 테스트: 리뷰 작성자 유저네임 기준 오름차순 정렬")
    void searchMyScrapsTest_SortByReviewerUsername_Asc() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User reviewer1 = userRepository.save(new User("u-user", "password"));
        Review review1 = reviewRepository.save(
                new Review(reviewer1, new ReviewDto.PostRequest("content1", null, 10, "location")));
        User reviewer2 = userRepository.save(new User("e-user", "password"));
        Review review2 = reviewRepository.save(
                new Review(reviewer2, new ReviewDto.PostRequest("content2", null, 10, "location")));
        User reviewer3 = userRepository.save(new User("n-user", "password"));
        Review review3 = reviewRepository.save(
                new Review(reviewer3, new ReviewDto.PostRequest("content3", null, 10, "location")));
        Scrap scrap1 = scrapRepository.save(new Scrap(user, review1));
        Scrap scrap2 = scrapRepository.save(new Scrap(user, review2));
        Scrap scrap3 = scrapRepository.save(new Scrap(user, review3));

        // when
        ScrapDto.SearchRequest request = new ScrapDto.SearchRequest("", 0, 10, REVIEWER_USERNAME, true);
        Slice<Scrap> scraps = scrapRepository.searchMyScrapsByKeyword(request, user.getId());

        // then
        assertThat(scraps).containsExactly(scrap2, scrap3, scrap1);
    }

    @Test
    @DisplayName("내 스크랩 검색 기본 테스트: 리뷰 작성자 레벨 기준 내림차순 정렬")
    void searchMyScrapsTest_SortByReviewerLevel_Desc() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User reviewer1 = userRepository.save(new User("reviewer1", "password"));
        Review review1 = reviewRepository.save(
                new Review(reviewer1, new ReviewDto.PostRequest("content1", null, 10, "location")));
        User reviewer2 = userRepository.save(new User("reviewer2", "password"));
        Review review2 = reviewRepository.save(
                new Review(reviewer2, new ReviewDto.PostRequest("content2", null, 10, "location")));
        User reviewer3 = userRepository.save(new User("reviewer3", "password"));
        Review review3 = reviewRepository.save(
                new Review(reviewer3, new ReviewDto.PostRequest("content3", null, 10, "location")));
        Scrap scrap1 = scrapRepository.save(new Scrap(user, review1));
        Scrap scrap2 = scrapRepository.save(new Scrap(user, review2));
        Scrap scrap3 = scrapRepository.save(new Scrap(user, review3));
        for (int i = 0 ; i < 8; i++) reviewer1.levelUp();
        for (int i = 0 ; i < 3; i++) reviewer2.levelUp();
        for (int i = 0 ; i < 5; i++) reviewer3.levelUp();

        // when
        ScrapDto.SearchRequest request = new ScrapDto.SearchRequest("", 0, 10, REVIEWER_MATZIP_LEVEL, false);
        Slice<Scrap> scraps = scrapRepository.searchMyScrapsByKeyword(request, user.getId());

        // then
        assertThat(scraps).containsExactly(scrap1, scrap3, scrap2);
    }

    @Test
    @DisplayName("내 스크랩 검색 기본 테스트: 리뷰 작성자 팔로워 수 기준 오름차순 정렬")
    void searchMyScrapsTest_SortByReviewerNumberOfFollowers_Asc() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User reviewer1 = userRepository.save(new User("reviewer1", "password"));
        Review review1 = reviewRepository.save(
                new Review(reviewer1, new ReviewDto.PostRequest("content1", null, 10, "location")));
        User reviewer2 = userRepository.save(new User("reviewer2", "password"));
        Review review2 = reviewRepository.save(
                new Review(reviewer2, new ReviewDto.PostRequest("content2", null, 10, "location")));
        User reviewer3 = userRepository.save(new User("reviewer3", "password"));
        Review review3 = reviewRepository.save(
                new Review(reviewer3, new ReviewDto.PostRequest("content3", null, 10, "location")));
        Scrap scrap1 = scrapRepository.save(new Scrap(user, review1));
        Scrap scrap2 = scrapRepository.save(new Scrap(user, review2));
        Scrap scrap3 = scrapRepository.save(new Scrap(user, review3));
        for (int i = 0; i < 10; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), reviewer1));
        for (int i = 0; i < 5; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), reviewer2));
        for (int i = 0; i < 9; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), reviewer3));

        // when
        ScrapDto.SearchRequest request = new ScrapDto.SearchRequest("", 0, 10, REVIEWER_NUMBER_OF_FOLLOWERS, true);
        Slice<Scrap> scraps = scrapRepository.searchMyScrapsByKeyword(request, user.getId());

        // then
        assertThat(scraps).containsExactly(scrap2, scrap3, scrap1);
    }

    @Test
    @DisplayName("내 스크랩 검색 기본 테스트: 리뷰 좋아요 수 기준 내림차순 정렬")
    void searchMyScrapsTest_SortByReviewNumberOfHearts_Desc() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User reviewer = userRepository.save(new User("reviewer", "password"));
        Review review1 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content1", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content2", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content3", null, 10, "location")));
        Scrap scrap1 = scrapRepository.save(new Scrap(user, review1));
        Scrap scrap2 = scrapRepository.save(new Scrap(user, review2));
        Scrap scrap3 = scrapRepository.save(new Scrap(user, review3));
        for (int i = 0; i < 7; i++) heartRepository.save(new Heart(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), review1));
        for (int i = 0; i < 1; i++) heartRepository.save(new Heart(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), review2));
        for (int i = 0; i < 4; i++) heartRepository.save(new Heart(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), review3));

        // when
        ScrapDto.SearchRequest request = new ScrapDto.SearchRequest("", 0, 10, REVIEW_NUMBER_OF_HEARTS, false);
        Slice<Scrap> scraps = scrapRepository.searchMyScrapsByKeyword(request, user.getId());

        // then
        assertThat(scraps).containsExactly(scrap1, scrap3, scrap2);
    }

    @Test
    @DisplayName("내 스크랩 검색 기본 테스트: 리뷰 스크랩 수 기준 오름차순 정렬")
    void searchMyScrapsTest_SortByReviewNumberOfScraps_Asc() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User reviewer = userRepository.save(new User("reviewer", "password"));
        Review review1 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content1", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content2", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content3", null, 10, "location")));
        Scrap scrap1 = scrapRepository.save(new Scrap(user, review1));
        Scrap scrap2 = scrapRepository.save(new Scrap(user, review2));
        Scrap scrap3 = scrapRepository.save(new Scrap(user, review3));
        for (int i = 0; i < 15; i++) scrapRepository.save(new Scrap(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), review1));
        for (int i = 0; i < 12; i++) scrapRepository.save(new Scrap(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), review2));
        for (int i = 0; i < 7; i++) scrapRepository.save(new Scrap(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), review3));

        // when
        ScrapDto.SearchRequest request = new ScrapDto.SearchRequest("", 0, 10, REVIEW_NUMBER_OF_SCRAPS, true);
        Slice<Scrap> scraps = scrapRepository.searchMyScrapsByKeyword(request, user.getId());

        // then
        assertThat(scraps).containsExactly(scrap3, scrap2, scrap1);
    }

    @Test
    @DisplayName("내 스크랩 검색 기본 테스트: 리뷰 댓글 수 기준 내림차순 정렬")
    void searchMyScrapsTest_SortByReviewNumberOfComments_Desc() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User reviewer = userRepository.save(new User("reviewer", "password"));
        Review review1 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content1", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content2", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content3", null, 10, "location")));
        Scrap scrap1 = scrapRepository.save(new Scrap(user, review1));
        Scrap scrap2 = scrapRepository.save(new Scrap(user, review2));
        Scrap scrap3 = scrapRepository.save(new Scrap(user, review3));
        for (int i = 0; i < 5; i++) commentRepository.save(new Comment(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), review1, "comment"));
        for (int i = 0; i < 15; i++) commentRepository.save(new Comment(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), review2, "comment"));
        for (int i = 0; i < 11; i++) commentRepository.save(new Comment(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), review3, "comment"));

        // when
        ScrapDto.SearchRequest request = new ScrapDto.SearchRequest("", 0, 10, REVIEW_NUMBER_OF_COMMENTS, false);
        Slice<Scrap> scraps = scrapRepository.searchMyScrapsByKeyword(request, user.getId());

        // then
        assertThat(scraps).containsExactly(scrap2, scrap3, scrap1);
    }

    @Test
    @DisplayName("내 스크랩 검색 기본 테스트: 리뷰 댓글 수 기준 내림차순 정렬 - 삭제된 댓글 고려")
    void searchMyScrapsTest_SortByReviewNumberOfComments_ConsideringDeletion_Desc() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User reviewer = userRepository.save(new User("reviewer", "password"));
        Review review1 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content1", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content2", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content3", null, 10, "location")));
        Scrap scrap1 = scrapRepository.save(new Scrap(user, review1));
        Scrap scrap2 = scrapRepository.save(new Scrap(user, review2));
        Scrap scrap3 = scrapRepository.save(new Scrap(user, review3));
        for (int i = 0; i < 5; i++) commentRepository.save(new Comment(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), review1, "comment"));
        for (int i = 0; i < 15; i++) commentRepository.save(new Comment(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), review2, "comment"));
        for (int i = 0; i < 11; i++) commentRepository.save(new Comment(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), review3, "comment")).delete();

        // when
        ScrapDto.SearchRequest request = new ScrapDto.SearchRequest("", 0, 10, REVIEW_NUMBER_OF_COMMENTS, false);
        Slice<Scrap> scraps = scrapRepository.searchMyScrapsByKeyword(request, user.getId());

        // then
        assertThat(scraps).containsExactly(scrap2, scrap1, scrap3);
    }

    @Test
    @DisplayName("내 스크랩 검색 기본 테스트: 리뷰 별점 기준 오름차순 정렬")
    void searchMyScrapsTest_SortByReviewRating_Asc() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User reviewer = userRepository.save(new User("reviewer", "password"));
        Review review1 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content1", null, 3, "location")));
        Review review2 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content2", null, 1, "location")));
        Review review3 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content3", null, 8, "location")));
        Scrap scrap1 = scrapRepository.save(new Scrap(user, review1));
        Scrap scrap2 = scrapRepository.save(new Scrap(user, review2));
        Scrap scrap3 = scrapRepository.save(new Scrap(user, review3));

        // when
        ScrapDto.SearchRequest request = new ScrapDto.SearchRequest("", 0, 10, REVIEW_RATING, true);
        Slice<Scrap> scraps = scrapRepository.searchMyScrapsByKeyword(request, user.getId());

        // then
        assertThat(scraps).containsExactly(scrap2, scrap1, scrap3);
    }

    @Test
    @DisplayName("내 스크랩 검색 기본 테스트: 리뷰 생성 기준 내림차순 정렬")
    void searchMyScrapsTest_SortByReviewCreatedAt_Desc() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User reviewer = userRepository.save(new User("reviewer", "password"));
        Review review1 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content1", null, 3, "location")));
        Review review2 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content2", null, 1, "location")));
        Review review3 = reviewRepository.save(
                new Review(reviewer, new ReviewDto.PostRequest("content3", null, 8, "location")));
        Scrap scrap1 = scrapRepository.save(new Scrap(user, review1));
        Scrap scrap2 = scrapRepository.save(new Scrap(user, review2));
        Scrap scrap3 = scrapRepository.save(new Scrap(user, review3));

        // when
        ScrapDto.SearchRequest request = new ScrapDto.SearchRequest("", 0, 10, REVIEW_CREATED_AT, false);
        Slice<Scrap> scraps = scrapRepository.searchMyScrapsByKeyword(request, user.getId());

        // then
        assertThat(scraps).containsExactly(scrap3, scrap2, scrap1);
    }
}
