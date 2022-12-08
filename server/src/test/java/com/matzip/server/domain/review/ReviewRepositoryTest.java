package com.matzip.server.domain.review;

import com.matzip.server.domain.me.model.Follow;
import com.matzip.server.domain.me.model.Heart;
import com.matzip.server.domain.me.model.Scrap;
import com.matzip.server.domain.me.repository.FollowRepository;
import com.matzip.server.domain.me.repository.HeartRepository;
import com.matzip.server.domain.me.repository.ScrapRepository;
import com.matzip.server.domain.review.dto.ReviewDto.PostRequest;
import com.matzip.server.domain.review.dto.ReviewDto.SearchRequest;
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

import static com.matzip.server.domain.review.model.ReviewProperty.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Tag("RepositoryTest")
public class ReviewRepositoryTest {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private HeartRepository heartRepository;
    @Autowired
    private ScrapRepository scrapRepository;
    @Autowired
    private CommentRepository commentRepository;

    private final String PASSWORD = "password";

    @Test
    @DisplayName("리뷰 검색 기본 테스트: 생성기준 내림차순 정렬")
    public void searchReviewsTest_Basic_Asc() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user, new PostRequest("content1", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(user, new PostRequest("content2", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(user, new PostRequest("content3", null, 10, "location")));
        SearchRequest request = new SearchRequest("content", 0, 5, null, false);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review3, review2, review1);
    }

    @Test
    @DisplayName("리뷰 검색 기본 테스트: 생성기준 오름차순 정렬")
    public void searchReviewsTest_Basic_Desc() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user, new PostRequest("content1", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(user, new PostRequest("content2", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(user, new PostRequest("content3", null, 10, "location")));
        SearchRequest request = new SearchRequest("content", 0, 5, null, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review1, review2, review3);
    }

    @Test
    @DisplayName("리뷰 검색 기본 테스트: 삭제되거나 블락 처리된 리뷰 제외")
    public void searchReviewsTest_Basic_Exclusion() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user, new PostRequest("content1", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(user, new PostRequest("content2", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(user, new PostRequest("content3", null, 10, "location")));
        SearchRequest request = new SearchRequest("content", 0, 5, null, true);
        review2.delete();
        review3.block("test");

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review1);
    }

    @Test
    @DisplayName("리뷰 검색 기본 테스트: 페이징")
    public void searchReviewsTest_Basic_Paging() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review[] reviews = new Review[10];
        for (int i = 0; i < 10; i++) reviews[i] = reviewRepository.save(
                new Review(user, new PostRequest("content" + i, null, 10, "location")));
        SearchRequest request = new SearchRequest("content", 1, 3, null, true);

        // when
        Slice<Review> searchedReviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(searchedReviews.getContent()).containsExactly(reviews[3], reviews[4], reviews[5]);
        assertThat(searchedReviews.hasNext()).isTrue();
        assertThat(searchedReviews.isFirst()).isFalse();
        assertThat(searchedReviews.isLast()).isFalse();
    }

    @Test
    @DisplayName("리뷰 검색 기본 테스트: 한글 검색")
    public void searchReviewsTest_Basic_Korean() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review = reviewRepository.save(
                new Review(user, new PostRequest("한글 리뷰 내용", null, 10, "location")));
        SearchRequest request = new SearchRequest("한", 0, 3, null, true);

        // when
        Slice<Review> searchedReviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(searchedReviews.getContent()).containsExactly(review);
    }

    @Test
    @DisplayName("리뷰 검색 정렬 테스트: 작성자 유저네임 기준 오름차순 정렬")
    public void searchReviewsTest_Sort_Username_Asc() {
        // given
        Review review1 = reviewRepository.save(
                new Review(userRepository.save(new User("e-user", PASSWORD)),
                           new PostRequest("content", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(userRepository.save(new User("z-user", PASSWORD)),
                           new PostRequest("content", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(userRepository.save(new User("k-user", PASSWORD)),
                           new PostRequest("content", null, 10, "location")));
        SearchRequest request = new SearchRequest("content", 0, 10, REVIEWER_USERNAME, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review1, review3, review2);
    }

    @Test
    @DisplayName("리뷰 검색 정렬 테스트: 작성자 유저네임 기준 내림차순 정렬")
    public void searchReviewsTest_Sort_Username_Desc() {
        // given
        Review review1 = reviewRepository.save(
                new Review(userRepository.save(new User("l-user", PASSWORD)),
                           new PostRequest("content", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(userRepository.save(new User("y-user", PASSWORD)),
                           new PostRequest("content", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(userRepository.save(new User("s-user", PASSWORD)),
                           new PostRequest("content", null, 10, "location")));
        SearchRequest request = new SearchRequest("content", 0, 10, REVIEWER_USERNAME, false);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review2, review3, review1);
    }

    @Test
    @DisplayName("리뷰 검색 정렬 테스트: 작성자 레벨 기준 오름차순 정렬")
    public void searchReviewsTest_Sort_Level_Asc() {
        // given
        User user1 = userRepository.save(new User("user1", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user1, new PostRequest("content", null, 10, "location")));
        User user2 = userRepository.save(new User("user2", PASSWORD));
        Review review2 = reviewRepository.save(
                new Review(user2, new PostRequest("content", null, 10, "location")));
        User user3 = userRepository.save(new User("user3", PASSWORD));
        Review review3 = reviewRepository.save(
                new Review(user3, new PostRequest("content", null, 10, "location")));
        SearchRequest request = new SearchRequest("content", 0, 10, REVIEWER_MATZIP_LEVEL, true);
        for (int i = 0; i < 6; i++) user1.levelUp();
        for (int i = 0; i < 3; i++) user2.levelUp();
        for (int i = 0; i < 5; i++) user3.levelUp();

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review2, review3, review1);
    }

    @Test
    @DisplayName("리뷰 검색 정렬 테스트: 작성자 레벨 기준 내림차순 정렬")
    public void searchReviewsTest_Sort_Level_Desc() {
        // given
        User user1 = userRepository.save(new User("user1", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user1, new PostRequest("content", null, 10, "location")));
        User user2 = userRepository.save(new User("user2", PASSWORD));
        Review review2 = reviewRepository.save(
                new Review(user2, new PostRequest("content", null, 10, "location")));
        User user3 = userRepository.save(new User("user3", PASSWORD));
        Review review3 = reviewRepository.save(
                new Review(user3, new PostRequest("content", null, 10, "location")));
        SearchRequest request = new SearchRequest("content", 0, 10, REVIEWER_MATZIP_LEVEL, false);
        for (int i = 0; i < 6; i++) user1.levelUp();
        for (int i = 0; i < 3; i++) user2.levelUp();
        for (int i = 0; i < 8; i++) user3.levelUp();

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review3, review1, review2);
    }

    @Test
    @DisplayName("리뷰 검색 정렬 테스트: 작성자 팔로워수 기준 오름차순 정렬")
    public void searchReviewsTest_Sort_Follower_Asc() {
        // given
        User user1 = userRepository.save(new User("user1", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user1, new PostRequest("content", null, 10, "location")));
        User user2 = userRepository.save(new User("user2", PASSWORD));
        Review review2 = reviewRepository.save(
                new Review(user2, new PostRequest("content", null, 10, "location")));
        User user3 = userRepository.save(new User("user3", PASSWORD));
        Review review3 = reviewRepository.save(
                new Review(user3, new PostRequest("content", null, 10, "location")));
        for (int i = 0; i < 3; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), user1));
        for (int i = 0; i < 5; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), user2));
        for (int i = 0; i < 6; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), user3));
        SearchRequest request = new SearchRequest("content", 0, 10, REVIEWER_NUMBER_OF_FOLLOWERS, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review1, review2, review3);
    }

    @Test
    @DisplayName("리뷰 검색 정렬 테스트: 작성자 팔로워수 기준 내림차순 정렬")
    public void searchReviewsTest_Sort_Follower_Desc() {
        // given
        User user1 = userRepository.save(new User("user1", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user1, new PostRequest("content", null, 10, "location")));
        User user2 = userRepository.save(new User("user2", PASSWORD));
        Review review2 = reviewRepository.save(
                new Review(user2, new PostRequest("content", null, 10, "location")));
        User user3 = userRepository.save(new User("user3", PASSWORD));
        Review review3 = reviewRepository.save(
                new Review(user3, new PostRequest("content", null, 10, "location")));
        for (int i = 0; i < 3; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), user1));
        for (int i = 0; i < 12; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), user2));
        for (int i = 0; i < 6; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), user3));
        SearchRequest request = new SearchRequest("content", 0, 10, REVIEWER_NUMBER_OF_FOLLOWERS, false);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review2, review3, review1);
    }

    @Test
    @DisplayName("리뷰 검색 정렬 테스트: 리뷰 좋아요수 기준 오름차순 정렬")
    public void searchReviewsTest_Sort_Hearts_Asc() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        for (int i = 0; i < 7; i++) heartRepository.save(new Heart(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review1));
        for (int i = 0; i < 1; i++) heartRepository.save(new Heart(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review2));
        for (int i = 0; i < 13; i++) heartRepository.save(new Heart(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review3));
        SearchRequest request = new SearchRequest("content", 0, 10, NUMBER_OF_HEARTS, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review2, review1, review3);
    }

    @Test
    @DisplayName("리뷰 검색 정렬 테스트: 리뷰 좋아요수 기준 내림차순 정렬")
    public void searchReviewsTest_Sort_Hearts_Desc() {
// given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        for (int i = 0; i < 9; i++) heartRepository.save(new Heart(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review1));
        for (int i = 0; i < 4; i++) heartRepository.save(new Heart(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review2));
        for (int i = 0; i < 17; i++) heartRepository.save(new Heart(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review3));
        SearchRequest request = new SearchRequest("content", 0, 10, NUMBER_OF_HEARTS, false);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review3, review1, review2);
    }

    @Test
    @DisplayName("리뷰 검색 정렬 테스트: 리뷰 스크랩수 기준 오름차순 정렬")
    public void searchReviewsTest_Sort_Scraps_Asc() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        for (int i = 0; i < 18; i++) scrapRepository.save(new Scrap(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review1));
        for (int i = 0; i < 11; i++) scrapRepository.save(new Scrap(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review2));
        for (int i = 0; i < 5; i++) scrapRepository.save(new Scrap(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review3));
        SearchRequest request = new SearchRequest("content", 0, 10, NUMBER_OF_SCRAPS, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review3, review2, review1);
    }

    @Test
    @DisplayName("리뷰 검색 정렬 테스트: 리뷰 스크랩수 기준 내림차순 정렬")
    public void searchReviewsTest_Sort_Scraps_Desc() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        for (int i = 0; i < 8; i++) scrapRepository.save(new Scrap(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review1));
        for (int i = 0; i < 13; i++) scrapRepository.save(new Scrap(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review2));
        for (int i = 0; i < 6; i++) scrapRepository.save(new Scrap(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review3));
        SearchRequest request = new SearchRequest("content", 0, 10, NUMBER_OF_SCRAPS, false);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review2, review1, review3);
    }

    @Test
    @DisplayName("리뷰 검색 정렬 테스트: 리뷰 댓글수 기준 오름차순 정렬")
    public void searchReviewsTest_Sort_Comments_Asc() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        for (int i = 0; i < 8; i++) commentRepository.save(new Comment(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review1, "comment"));
        for (int i = 0; i < 10; i++) commentRepository.save(new Comment(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review2, "comment"));
        for (int i = 0; i < 7; i++) commentRepository.save(new Comment(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review3, "comment"));
        SearchRequest request = new SearchRequest("content", 0, 10, NUMBER_OF_COMMENTS, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review3, review1, review2);
    }

    @Test
    @DisplayName("리뷰 검색 정렬 테스트: 리뷰 댓글수 기준 내림차순 정렬")
    public void searchReviewsTest_Sort_Comments_Desc() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        Review review2 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        for (int i = 0; i < 9; i++) commentRepository.save(new Comment(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review1, "comment"));
        for (int i = 0; i < 3; i++) commentRepository.save(new Comment(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review2, "comment"));
        for (int i = 0; i < 4; i++) commentRepository.save(new Comment(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), review3, "comment"));
        SearchRequest request = new SearchRequest("content", 0, 10, NUMBER_OF_COMMENTS, false);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review1, review3, review2);
    }

    @Test
    @DisplayName("리뷰 검색 정렬 테스트: 리뷰 별점 기준 오름차순 정렬")
    public void searchReviewsTest_Sort_Rating_Asc() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 7, "location")));
        Review review2 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 8, "location")));
        Review review3 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        SearchRequest request = new SearchRequest("content", 0, 10, RATING, true);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review1, review2, review3);
    }

    @Test
    @DisplayName("리뷰 검색 정렬 테스트: 리뷰 별점 기준 내림차순 정렬")
    public void searchReviewsTest_Sort_Rating_Desc() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 5, "location")));
        Review review2 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 10, "location")));
        Review review3 = reviewRepository.save(
                new Review(user, new PostRequest("content", null, 9, "location")));
        SearchRequest request = new SearchRequest("content", 0, 10, RATING, false);

        // when
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);

        // then
        assertThat(reviews.getContent()).containsExactly(review2, review3, review1);
    }

    @Test
    @DisplayName("자기가 작성한 리뷰 검색 테스트")
    public void searchMyReviewsTest() {
        // given
        User user1 = userRepository.save(new User("user1", PASSWORD));
        Review review1 = reviewRepository.save(
                new Review(user1, new PostRequest("content", null, 5, "location")));
        Review review2 = reviewRepository.save(
                new Review(user1, new PostRequest("content", null, 10, "location")));
        User user2 = userRepository.save(new User("user2", PASSWORD));
        Review review3 = reviewRepository.save(
                new Review(user2, new PostRequest("content", null, 9, "location")));
        SearchRequest request = new SearchRequest("content", 0, 10, null, true);

        // when
        Slice<Review> reviews = reviewRepository.searchMyReviewsByKeyword(request, user1.getId());

        // then
        assertThat(reviews.getContent()).containsExactly(review1, review2);
    }
}
