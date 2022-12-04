package com.matzip.server.domain.review.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.me.model.Heart;
import com.matzip.server.domain.me.model.Scrap;
import com.matzip.server.domain.me.repository.FollowRepository;
import com.matzip.server.domain.me.repository.HeartRepository;
import com.matzip.server.domain.me.repository.ScrapRepository;
import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.CommentRepository;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.matzip.server.domain.review.model.ReviewProperty.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReviewServiceTest {
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
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ImageService imageService;

    @BeforeEach
    void createDefaultUser() {
        userService.createUser(new UserDto.SignUpRequest("foo", "password"));
    }

    @Test
    void postReviewTest() {
        // given
        given(imageService.uploadImages(any(), any())).willReturn(List.of());
        long beforeReviewCount = reviewRepository.count();
        int numberOfReviews = 100;
        String[] contents = new String[numberOfReviews];
        Integer[] ratings = new Integer[numberOfReviews];
        for (int i = 0; i < numberOfReviews; i++) {
            contents[i] = UUID.randomUUID().toString();
            ratings[i] = i % 11;
        }
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();

        // when
        for (int i = 0; i < numberOfReviews; i++) {
            reviewService.postReview(
                    fooUserId,
                    new ReviewDto.PostRequest(
                            contents[i], List.of(new MockMultipartFile("image", new byte[0])), ratings[i], "location"));
        }

        // then
        assertThat(reviewRepository.count()).isEqualTo(beforeReviewCount + numberOfReviews);
    }

    @Test
    void getReviewTest() throws JsonProcessingException {
        // given
        given(imageService.uploadImages(any(), any())).willReturn(List.of());
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();
        ReviewDto.Response response = reviewService.postReview(
                fooUserId,
                new ReviewDto.PostRequest(
                        "content", List.of(new MockMultipartFile("image", new byte[0])), 10, "location"));

        // when
        ReviewDto.Response getResponse = reviewService.getReview(fooUserId, response.getId());

        // then
        assertThat(objectMapper.writeValueAsString(getResponse)).isEqualTo(objectMapper.writeValueAsString(response));
    }

    @Test
    void patchReviewContentTest() {
        // given
        given(imageService.uploadImages(any(), any())).willReturn(List.of());
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();
        ReviewDto.Response response = reviewService.postReview(
                fooUserId,
                new ReviewDto.PostRequest(
                        "content", List.of(new MockMultipartFile("image", new byte[0])), 10, "location"));
        ReviewDto.PatchRequest patchRequest = new ReviewDto.PatchRequest(
                "patch_content", null, null, null);

        // when
        ReviewDto.Response patchResponse = reviewService.patchReview(fooUserId, response.getId(), patchRequest);

        // then
        assertThat(patchResponse.getContent()).isEqualTo("patch_content");
        assertThat(patchResponse.getContent()).isNotEqualTo(response.getContent());
        assertThat(patchResponse.getRating()).isEqualTo(response.getRating());
    }

    @Test
    void patchReviewRatingTest() {
        // given
        given(imageService.uploadImages(any(), any())).willReturn(List.of());
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();
        ReviewDto.Response response = reviewService.postReview(
                fooUserId,
                new ReviewDto.PostRequest(
                        "content", List.of(new MockMultipartFile("image", new byte[0])), 10, "location"));
        ReviewDto.PatchRequest patchRequest = new ReviewDto.PatchRequest(
                null, null, null, 1);

        // when
        ReviewDto.Response patchResponse = reviewService.patchReview(fooUserId, response.getId(), patchRequest);

        // then
        assertThat(patchResponse.getContent()).isEqualTo(response.getContent());
        assertThat(patchResponse.getRating()).isEqualTo(1);
        assertThat(patchResponse.getRating()).isNotEqualTo(response.getRating());
    }

    @Test
    void deleteReviewTest() {
        // given
        given(imageService.uploadImages(any(), any())).willReturn(List.of());
        long beforeReviewCount = reviewRepository.count();
        int numberOfReviews = 100;
        String[] contents = new String[numberOfReviews];
        Integer[] ratings = new Integer[numberOfReviews];
        Long[] ids = new Long[numberOfReviews];
        for (int i = 0; i < numberOfReviews; i++) {
            contents[i] = UUID.randomUUID().toString();
            ratings[i] = i % 11;
        }
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();
        for (int i = 0; i < numberOfReviews; i++) {
            ids[i] = reviewService.postReview(
                    fooUserId,
                    new ReviewDto.PostRequest(
                            contents[i], List.of(new MockMultipartFile("image", new byte[0])), ratings[i], "location")
            ).getId();
        }

        // when
        int deleteCount = 0;
        Random random = new Random();
        for (int i = 0; i < numberOfReviews; i++) {
            if (random.nextBoolean()) {
                reviewService.deleteReview(fooUserId, ids[i]);
                deleteCount++;
            }
        }

        // then
        assertThat(reviewRepository.count()).isEqualTo(beforeReviewCount + numberOfReviews - deleteCount);
    }

    @Test
    void searchReviewsTest() {
        // given
        int numberOfReviews = 100;
        int pageSize = 5;
        int pageOffset = 1;
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();
        String[] contents = new String[numberOfReviews];
        for (int i = 0; i < numberOfReviews; i++) {
            contents[i] = "content" + UUID.randomUUID();
            ReviewDto.PostRequest request = new ReviewDto.PostRequest(
                    contents[i], List.of(new MockMultipartFile("image", new byte[0])), 0, "location");
            reviewService.postReview(fooUserId, request);
        }

        // when
        ReviewDto.SearchRequest ascRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, null, true);
        ReviewDto.SearchRequest descRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, null, false);
        Slice<ReviewDto.Response> ascResponse = reviewService.searchReviews(fooUserId, ascRequest);
        Slice<ReviewDto.Response> descResponse = reviewService.searchReviews(fooUserId, descRequest);
        String[] expectedAscContents = new String[pageSize];
        String[] expectedDescContents = new String[pageSize];
        for (int i = 0; i < pageSize; i++) {
            expectedAscContents[i] = contents[pageOffset * pageSize + i];
            expectedDescContents[i] = contents[numberOfReviews - (pageOffset * pageSize + 1) - i];
        }

        // then
        assertThat(ascResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(ascResponse.getContent()).extracting("content").containsExactly(expectedAscContents);
        assertThat(descResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(descResponse.getContent()).extracting("content").containsExactly(expectedDescContents);
    }

    @Test
    void searchReviewsSortByReviewerUsernameTest() {
        // given
        int numberOfUsers = 100;
        int pageSize = 5;
        int pageOffset = 1;
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();
        String[] uuids = new String[numberOfUsers];
        for (int i = 0; i < numberOfUsers; i++) {
            uuids[i] = UUID.randomUUID().toString();
            userService.createUser(new UserDto.SignUpRequest("user" + uuids[i], "password"));
            ReviewDto.PostRequest request = new ReviewDto.PostRequest(
                    "content" + uuids[i], List.of(new MockMultipartFile("image", new byte[0])), 0, "location");
            User user = userRepository.findByUsername("user" + uuids[i]).orElseThrow();
            reviewService.postReview(user.getId(), request);
        }

        // when
        ReviewDto.SearchRequest ascRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, REVIEWER_USERNAME, true);
        ReviewDto.SearchRequest descRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, REVIEWER_USERNAME, false);
        Slice<ReviewDto.Response> ascResponse = reviewService.searchReviews(fooUserId, ascRequest);
        Slice<ReviewDto.Response> descResponse = reviewService.searchReviews(fooUserId, descRequest);
        String[] expectedAscContents = new String[pageSize];
        String[] expectedDescContents = new String[pageSize];
        Arrays.sort(uuids);
        for (int i = 0; i < pageSize; i++) {
            expectedAscContents[i] = "content" + uuids[pageOffset * pageSize + i];
            expectedDescContents[i] = "content" + uuids[numberOfUsers - (pageOffset * pageSize + 1) - i];
        }

        // then
        assertThat(ascResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(ascResponse.getContent()).extracting("content").containsExactly(expectedAscContents);
        assertThat(descResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(descResponse.getContent()).extracting("content").containsExactly(expectedDescContents);
    }

    @Test
    void searchReviewsSortByReviewerLevelTest() {
        // given
        int numberOfUsers = 100;
        int pageSize = 5;
        int pageOffset = 1;
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();

        /* random levels */
        List<Integer> levels = new ArrayList<>(numberOfUsers);
        for (int i = 0; i < numberOfUsers; i++) levels.add(i + 1);
        Collections.shuffle(levels);
        List<Map.Entry<String, Integer>> userLevels = new ArrayList<>();

        String[] uuids = new String[numberOfUsers];
        for (int i = 0; i < numberOfUsers; i++) {
            /* generate unique id */
            uuids[i] = UUID.randomUUID().toString();

            /* create user and set level */
            userService.createUser(new UserDto.SignUpRequest("user" + uuids[i], "password"));
            User user = userRepository.findByUsername("user" + uuids[i]).orElseThrow();
            for (int j = 0; j < levels.get(i); j++) user.levelUp();
            userLevels.add(new AbstractMap.SimpleEntry<>("content" + uuids[i], levels.get(i)));

            /* one review per user */
            ReviewDto.PostRequest request = new ReviewDto.PostRequest(
                    "content" + uuids[i], List.of(new MockMultipartFile("image", new byte[0])), 0, "location");
            reviewService.postReview(user.getId(), request);
        }

        // when
        ReviewDto.SearchRequest ascRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, REVIEWER_MATZIP_LEVEL, true);
        ReviewDto.SearchRequest descRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, REVIEWER_MATZIP_LEVEL, false);
        Slice<ReviewDto.Response> ascResponse = reviewService.searchReviews(fooUserId, ascRequest);
        Slice<ReviewDto.Response> descResponse = reviewService.searchReviews(fooUserId, descRequest);
        userLevels.sort(Map.Entry.comparingByValue());
        String[] expectedAscContents = userLevels.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(Map.Entry::getKey).toArray(String[]::new);
        userLevels.sort((o1, o2) -> -Integer.compare(o1.getValue(), o2.getValue()));
        String[] expectedDescContents = userLevels.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(Map.Entry::getKey).toArray(String[]::new);

        // then
        assertThat(ascResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(ascResponse.getContent()).extracting("content").containsExactly(expectedAscContents);
        assertThat(descResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(descResponse.getContent()).extracting("content").containsExactly(expectedDescContents);
    }

    @Test
    void searchReviewsSortByReviewerNumberOfFollowersTest() {
        // given
        int numberOfUsers = 100;
        int pageSize = 5;
        int pageOffset = 1;
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();
        List<Map.Entry<String, Integer>> followers = new ArrayList<>();
        for (int i = 0; i < numberOfUsers; i++) {
            /* generate unique id and create user */
            String uuid = UUID.randomUUID().toString();
            String username = "user" + uuid;
            userService.createUser(new UserDto.SignUpRequest(username, "password"));
            User user = userRepository.findByUsername(username).orElseThrow();

            /* giving unique number of followers */
            for (Map.Entry<String, Integer> entry : followers) {
                User following = userRepository.findByUsername("user" + entry.getKey()).orElseThrow();
                followRepository.save(following.addFollower(user));
                entry.setValue(entry.getValue() + 1);
            }

            /* one review per user */
            ReviewDto.PostRequest request = new ReviewDto.PostRequest(
                    "content" + uuid, List.of(new MockMultipartFile("image", new byte[0])), 0, "location");
            reviewService.postReview(user.getId(), request);

            followers.add(new AbstractMap.SimpleEntry<>(uuid, 0));
        }

        // when
        ReviewDto.SearchRequest ascRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, REVIEWER_NUMBER_OF_FOLLOWERS, true);
        ReviewDto.SearchRequest descRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, REVIEWER_NUMBER_OF_FOLLOWERS, false);
        Slice<ReviewDto.Response> ascResponse = reviewService.searchReviews(fooUserId, ascRequest);
        Slice<ReviewDto.Response> descResponse = reviewService.searchReviews(fooUserId, descRequest);
        followers.sort(Map.Entry.comparingByValue());
        String[] expectedAscContents = followers.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(e -> "content" + e.getKey()).toArray(String[]::new);
        followers.sort((o1, o2) -> -Integer.compare(o1.getValue(), o2.getValue()));
        String[] expectedDescContents = followers.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(e -> "content" + e.getKey()).toArray(String[]::new);

        // then
        assertThat(ascResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(ascResponse.getContent()).extracting("content").containsExactly(expectedAscContents);
        assertThat(descResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(descResponse.getContent()).extracting("content").containsExactly(expectedDescContents);
    }

    @Test
    void searchReviewsSortByNumberOfHeartsTest() {
        // given
        int numberOfUsers = 100;
        int pageSize = 5;
        int pageOffset = 1;
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();
        List<Map.Entry<Long, Integer>> hearts = new ArrayList<>();
        for (int i = 0; i < numberOfUsers; i++) {
            /* generate unique id and create user */
            String uuid = UUID.randomUUID().toString();
            String username = "user" + uuid;
            userService.createUser(new UserDto.SignUpRequest(username, "password"));
            User user = userRepository.findByUsername(username).orElseThrow();

            /* giving unique number of hearts */
            for (Map.Entry<Long, Integer> entry : hearts) {
                Review review = reviewRepository.findById(entry.getKey()).orElseThrow();
                review.addHeart(heartRepository.save(new Heart(user, review)));
                entry.setValue(entry.getValue() + 1);
            }

            /* one review per user */
            ReviewDto.PostRequest request = new ReviewDto.PostRequest(
                    "content" + uuid, List.of(new MockMultipartFile("image", new byte[0])), 0, "location");
            hearts.add(new AbstractMap.SimpleEntry<>(reviewService.postReview(user.getId(), request).getId(), 0));
        }

        // when
        ReviewDto.SearchRequest ascRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, NUMBER_OF_HEARTS, true);
        ReviewDto.SearchRequest descRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, NUMBER_OF_HEARTS, false);
        Slice<ReviewDto.Response> ascResponse = reviewService.searchReviews(fooUserId, ascRequest);
        Slice<ReviewDto.Response> descResponse = reviewService.searchReviews(fooUserId, descRequest);
        hearts.sort(Map.Entry.comparingByValue());
        Long[] expectedAscIds = hearts.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(Map.Entry::getKey).toArray(Long[]::new);
        hearts.sort((o1, o2) -> -Integer.compare(o1.getValue(), o2.getValue()));
        Long[] expectedDescIds = hearts.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(Map.Entry::getKey).toArray(Long[]::new);

        // then
        assertThat(ascResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(ascResponse.getContent()).extracting("id").containsExactly(expectedAscIds);
        assertThat(descResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(descResponse.getContent()).extracting("id").containsExactly(expectedDescIds);
    }

    @Test
    void searchReviewsSortByNumberOfScrapsTest() {
        // given
        int numberOfUsers = 100;
        int pageSize = 5;
        int pageOffset = 1;
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();
        List<Map.Entry<Long, Integer>> scraps = new ArrayList<>();
        for (int i = 0; i < numberOfUsers; i++) {
            /* generate unique id and create user */
            String uuid = UUID.randomUUID().toString();
            String username = "user" + uuid;
            userService.createUser(new UserDto.SignUpRequest(username, "password"));
            User user = userRepository.findByUsername(username).orElseThrow();

            /* giving unique number of scraps */
            for (Map.Entry<Long, Integer> entry : scraps) {
                Review review = reviewRepository.findById(entry.getKey()).orElseThrow();
                review.addScrap(scrapRepository.save(new Scrap(user, review)));
                entry.setValue(entry.getValue() + 1);
            }

            /* one review per user */
            ReviewDto.PostRequest request = new ReviewDto.PostRequest(
                    "content" + uuid, List.of(new MockMultipartFile("image", new byte[0])), 0, "location");
            scraps.add(new AbstractMap.SimpleEntry<>(reviewService.postReview(user.getId(), request).getId(), 0));
        }

        // when
        ReviewDto.SearchRequest ascRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, NUMBER_OF_SCRAPS, true);
        ReviewDto.SearchRequest descRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, NUMBER_OF_SCRAPS, false);
        Slice<ReviewDto.Response> ascResponse = reviewService.searchReviews(fooUserId, ascRequest);
        Slice<ReviewDto.Response> descResponse = reviewService.searchReviews(fooUserId, descRequest);
        scraps.sort(Map.Entry.comparingByValue());
        Long[] expectedAscIds = scraps.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(Map.Entry::getKey).toArray(Long[]::new);
        scraps.sort((o1, o2) -> -Integer.compare(o1.getValue(), o2.getValue()));
        Long[] expectedDescIds = scraps.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(Map.Entry::getKey).toArray(Long[]::new);

        // then
        assertThat(ascResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(ascResponse.getContent()).extracting("id").containsExactly(expectedAscIds);
        assertThat(descResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(descResponse.getContent()).extracting("id").containsExactly(expectedDescIds);
    }

    @Test
    void searchReviewsSortByNumberOfCommentsTest() {
        // given
        int numberOfReviews = 100;
        int pageSize = 5;
        int pageOffset = 1;
        User user = userRepository.findByUsername("foo").orElseThrow();

        /* random number of comments */
        List<Integer> comments = new ArrayList<>(numberOfReviews);
        for (int i = 0; i < numberOfReviews; i++) comments.add(i + 1);
        Collections.shuffle(comments);
        List<Map.Entry<String, Integer>> reviewComments = new ArrayList<>();

        for (int i = 0; i < numberOfReviews; i++) {
            /* generate unique id and create user */
            UUID uuid = UUID.randomUUID();
            ReviewDto.PostRequest request = new ReviewDto.PostRequest(
                    "content" + uuid, List.of(new MockMultipartFile("image", new byte[0])), 0, "location");
            Long reviewId = reviewService.postReview(user.getId(), request).getId();
            Review review = reviewRepository.findById(reviewId).orElseThrow();

            for (int j = 0; j < comments.get(i); j++) {
                review.addComment(commentRepository.save(
                        new Comment(user, review, new CommentDto.PostRequest(reviewId, "comment"))));
            }

            reviewComments.add(new AbstractMap.SimpleEntry<>("content" + uuid, comments.get(i)));
        }

        // when
        ReviewDto.SearchRequest ascRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, NUMBER_OF_COMMENTS, true);
        ReviewDto.SearchRequest descRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, NUMBER_OF_COMMENTS, false);
        Slice<ReviewDto.Response> ascResponse = reviewService.searchReviews(user.getId(), ascRequest);
        Slice<ReviewDto.Response> descResponse = reviewService.searchReviews(user.getId(), descRequest);
        reviewComments.sort(Map.Entry.comparingByValue());
        String[] expectedAscContents = reviewComments.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(Map.Entry::getKey).toArray(String[]::new);
        reviewComments.sort((o1, o2) -> -Integer.compare(o1.getValue(), o2.getValue()));
        String[] expectedDescContents = reviewComments.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(Map.Entry::getKey).toArray(String[]::new);

        // then
        assertThat(ascResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(ascResponse.getContent()).extracting("content").containsExactly(expectedAscContents);
        assertThat(descResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(descResponse.getContent()).extracting("content").containsExactly(expectedDescContents);
    }

    @Test
    void searchReviewsSortByRatingTest() {
        // given
        int numberOfReviews = 11;
        int pageSize = 5;
        int pageOffset = 1;
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();

        /* random ratings */
        List<Integer> ratings = new ArrayList<>(numberOfReviews);
        for (int i = 0; i < numberOfReviews; i++) ratings.add(i + 1);
        Collections.shuffle(ratings);
        List<Map.Entry<String, Integer>> reviewRatings = new ArrayList<>();

        for (int i = 0; i < numberOfReviews; i++) {
            UUID uuid = UUID.randomUUID();
            ReviewDto.PostRequest request = new ReviewDto.PostRequest(
                    "content" + uuid, List.of(new MockMultipartFile("image", new byte[0])),
                    ratings.get(i) % 11, "location");
            reviewService.postReview(fooUserId, request);
            reviewRatings.add(new AbstractMap.SimpleEntry<>("content" + uuid, ratings.get(i) % 11));
        }

        // when
        ReviewDto.SearchRequest ascRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, RATING, true);
        ReviewDto.SearchRequest descRequest = new ReviewDto.SearchRequest("content", pageOffset, pageSize, RATING, false);
        Slice<ReviewDto.Response> ascResponse = reviewService.searchReviews(fooUserId, ascRequest);
        Slice<ReviewDto.Response> descResponse = reviewService.searchReviews(fooUserId, descRequest);
        reviewRatings.sort(Map.Entry.comparingByValue());
        String[] expectedAscContents = reviewRatings.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(Map.Entry::getKey).toArray(String[]::new);
        reviewRatings.sort((o1, o2) -> -Integer.compare(o1.getValue(), o2.getValue()));
        String[] expectedDescContents = reviewRatings.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(Map.Entry::getKey).toArray(String[]::new);

        // then
        assertThat(ascResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(ascResponse.getContent()).extracting("content").containsExactly(expectedAscContents);
        assertThat(descResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(descResponse.getContent()).extracting("content").containsExactly(expectedDescContents);
    }
}