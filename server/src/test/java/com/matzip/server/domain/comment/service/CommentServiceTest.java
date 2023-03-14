package com.matzip.server.domain.comment.service;

import com.matzip.server.domain.comment.dto.CommentDto.PatchRequest;
import com.matzip.server.domain.comment.dto.CommentDto.PostRequest;
import com.matzip.server.domain.comment.dto.CommentDto.Response;
import com.matzip.server.domain.comment.exception.CommentAccessDeniedException;
import com.matzip.server.domain.comment.exception.CommentNotFoundException;
import com.matzip.server.domain.comment.model.Comment;
import com.matzip.server.domain.comment.repository.CommentRepository;
import com.matzip.server.domain.review.exception.ReviewNotFoundException;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.ReviewRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@ActiveProfiles("test")
@DisplayName("CommentService 테스트")
class CommentServiceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private CommentRepository commentRepository;

    private CommentService commentService;
    private List<User> users;

    @PostConstruct
    void init() {
        users = TestDataUtils.testData();
        commentService = new CommentService(userRepository, reviewRepository, commentRepository);
    }

    @BeforeEach
    public void setUp() {
        userRepository.saveAll(users);
    }

    @Test
    @DisplayName("댓글 생성 테스트: 정상")
    void postCommentTest() {
        // given
        User user = users.get(0);
        Review review = user.getReviews().get(0);
        String content = "some comment";
        PostRequest request = new PostRequest(review.getId(), content);

        // when
        Response response = commentService.postComment(user.getId(), request);

        // then
        assertThat(response.getReviewId()).isEqualTo(review.getId());
        assertThat(response.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("댓글 생성 테스트: 리뷰가 존재하지 않는 경우")
    void postCommentTest_NoReview() {
        // given
        User user = users.get(0);
        long reviewId = 100;
        String content = "some comment";
        PostRequest request = new PostRequest(reviewId, content);

        // then
        assertThatThrownBy(() -> commentService.postComment(user.getId(), request))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    @DisplayName("댓글 수정 테스트: 정상")
    void patchCommentTest() {
        // given
        User user = users.get(0);
        Comment comment = user.getComments().get(0);
        String oldContent = comment.getContent();
        String newContent = "patch comment";
        PatchRequest request = new PatchRequest(newContent);

        // when
        Response response = commentService.patchComment(user.getId(), comment.getId(), request);

        // then
        assertThat(response.getContent()).isEqualTo(newContent);
        assertThat(response.getContent()).isNotEqualTo(oldContent);
    }

    @Test
    @DisplayName("댓글 수정 테스트: 댓글이 존재하지 않는 경우")
    void patchCommentTest_NoComment() {
        // given
        User user = users.get(0);
        long commentId = 100;
        String content = "some comment";
        PatchRequest request = new PatchRequest(content);

        // then
        assertThatThrownBy(() -> commentService.patchComment(user.getId(), commentId, request))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @DisplayName("댓글 수정 테스트: 내 댓글이 아닌 경우")
    void patchCommentTest_NotMyComment() {
        // given
        User user = users.get(0);
        Comment comment = users.get(1).getComments().get(0);
        String content = "some comment";
        PatchRequest request = new PatchRequest(content);

        // then
        assertThatThrownBy(() -> commentService.patchComment(user.getId(), comment.getId(), request))
                .isInstanceOf(CommentAccessDeniedException.class);
    }

    @Test
    @DisplayName("댓글 삭제 테스트: 정상")
    void deleteCommentTest() {
        // given
        User user = users.get(0);
        Comment comment = user.getComments().get(0);
        long beforeCount = commentRepository.count();

        // when
        commentService.deleteComment(user.getId(), comment.getId());

        // then
        assertThat(commentRepository.findById(comment.getId())).isEmpty();
        assertThat(commentRepository.count()).isEqualTo(beforeCount - 1);
    }

    @Test
    @DisplayName("댓글 삭제 테스트: 댓글이 존재하지 않는 경우")
    void deleteCommentTest_NoComment() {
        // given
        User user = users.get(0);
        long commentId = 100;

        // then
        assertThatThrownBy(() -> commentService.deleteComment(user.getId(), commentId))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @DisplayName("댓글 삭제 테스트: 내 댓글이 아닌 경우")
    void deleteCommentTest_NotMyComment() {
        // given
        User user = users.get(0);
        Comment comment = users.get(1).getComments().get(0);

        // then
        assertThatThrownBy(() -> commentService.deleteComment(user.getId(), comment.getId()))
                .isInstanceOf(CommentAccessDeniedException.class);
    }

}