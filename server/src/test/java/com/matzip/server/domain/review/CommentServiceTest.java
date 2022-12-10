package com.matzip.server.domain.review;

import com.matzip.server.domain.review.dto.CommentDto.PostRequest;
import com.matzip.server.domain.review.dto.CommentDto.PutRequest;
import com.matzip.server.domain.review.dto.CommentDto.Response;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.exception.AccessBlockedOrDeletedReviewException;
import com.matzip.server.domain.review.exception.CommentChangeByAnonymousException;
import com.matzip.server.domain.review.exception.CommentNotFoundException;
import com.matzip.server.domain.review.exception.ReviewNotFoundException;
import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.CommentRepository;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.review.service.CommentService;
import com.matzip.server.domain.user.exception.AccessBlockedOrDeletedCommentException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Tag("ServiceTest")
public class CommentServiceTest {
    @InjectMocks
    private CommentService commentService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @Test
    @DisplayName("댓글 작성 테스트 성공")
    public void postCommentTest_Success() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new ReviewDto.PostRequest("content", null, 10, "location"));
        Comment comment = new Comment(user, review, "comment");
        given(userRepository.findMeById(1L)).willReturn(user);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        PostRequest request = new PostRequest(1L, "comment");
        Response response = commentService.postComment(1L, request);

        // then
        assertThat(response.getContent()).isEqualTo("comment");
        assertThat(response.getUser()).extracting("username").isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("댓글 작성 테스트 실패: 존재하지 않는 리뷰 아이디")
    public void postCommentTest_ReviewNotFound() {
        // given
        User user = new User("user", "password");
        given(userRepository.findMeById(1L)).willReturn(user);
        given(reviewRepository.findById(1L)).willReturn(Optional.empty());

        // when
        PostRequest request = new PostRequest(1L, "comment");

        // then
        assertThrows(ReviewNotFoundException.class, () -> commentService.postComment(1L, request));
    }

    @Test
    @DisplayName("댓글 작성 테스트 실패: 블락되거나 삭제된 리뷰에 댓글 달 경우")
    public void postCommentTest_ReviewBlockedOrDeleted() {
        // given
        User user = new User("user", "password");
        Review review1 = new Review(user, new ReviewDto.PostRequest("content", null, 10, "location"));
        Review review2 = new Review(user, new ReviewDto.PostRequest("content", null, 10, "location"));
        given(userRepository.findMeById(1L)).willReturn(user);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review1));
        given(reviewRepository.findById(2L)).willReturn(Optional.of(review2));

        // when
        PostRequest request1 = new PostRequest(1L, "comment");
        PostRequest request2 = new PostRequest(2L, "comment");
        review1.block("test");
        review2.delete();

        // then
        assertThrows(AccessBlockedOrDeletedReviewException.class, () -> commentService.postComment(1L, request1));
        assertThrows(AccessBlockedOrDeletedReviewException.class, () -> commentService.postComment(1L, request2));
    }

    @Test
    @DisplayName("댓글 조회 테스트 성공")
    public void fetchCommentTest_Success() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new ReviewDto.PostRequest("content", null, 10, "location"));
        Comment comment = new Comment(user, review, "comment");
        given(userRepository.findMeById(1L)).willReturn(user);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when
        Response response = commentService.fetchComment(1L, 1L);

        // then
        assertThat(response.getContent()).isEqualTo("comment");
        assertThat(response.getUser()).extracting("username").isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("댓글 조회 테스트 실패: 존재하지 않는 댓글 아이디")
    public void fetchCommentTest_CommentNotFound() {
        // given
        User user = new User("user", "password");
        given(userRepository.findMeById(1L)).willReturn(user);
        given(commentRepository.findById(any())).willReturn(Optional.empty());

        // then
        assertThrows(CommentNotFoundException.class, () -> commentService.fetchComment(1L, 1L));
    }

    @Test
    @DisplayName("댓글 조회 테스트 실패: 삭제되거나 블락된 댓글 조회할 경우")
    public void fetchCommentTest_CommentBlockedOrDeleted() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new ReviewDto.PostRequest("content", null, 10, "location"));
        Comment comment1 = new Comment(user, review, "comment");
        Comment comment2 = new Comment(user, review, "comment");
        given(userRepository.findMeById(1L)).willReturn(user);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment1));
        given(commentRepository.findById(2L)).willReturn(Optional.of(comment2));

        // when
        comment1.block("test");
        comment2.delete();

        // then
        assertThrows(AccessBlockedOrDeletedCommentException.class, () -> commentService.fetchComment(1L, 1L));
        assertThrows(AccessBlockedOrDeletedCommentException.class, () -> commentService.fetchComment(1L, 2L));
    }

    @Test
    @DisplayName("댓글 수정 테스트 성공")
    public void putCommentTest_Success() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new ReviewDto.PostRequest("content", null, 10, "location"));
        Comment comment = new Comment(user, review, "comment");
        given(userRepository.findMeById(1L)).willReturn(user);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when
        Response response = commentService.putComment(1L, 1L, new PutRequest("new comment"));

        // then
        assertThat(response.getContent()).isNotEqualTo("comment");
        assertThat(response.getContent()).isEqualTo("new comment");
    }

    @Test
    @DisplayName("댓글 수정 테스트 실패: 존재하지 않는 댓글 아이디")
    public void putCommentTest_CommentNotFound() {
        // given
        User user = new User("user", "password");
        given(userRepository.findMeById(1L)).willReturn(user);

        // when
        when(commentRepository.findById(any())).thenReturn(Optional.empty());

        // then
        assertThrows(CommentNotFoundException.class,
                     () -> commentService.putComment(1L, 1L, new PutRequest("new comment")));
    }

    @Test
    @DisplayName("댓글 수정 테스트 실패: 삭제되거나 블락된 댓글")
    public void putCommentTest_CommentBlockedOrDeleted() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new ReviewDto.PostRequest("content", null, 10, "location"));
        Comment comment1 = new Comment(user, review, "comment");
        Comment comment2 = new Comment(user, review, "comment");
        given(userRepository.findMeById(1L)).willReturn(user);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment1));
        given(commentRepository.findById(2L)).willReturn(Optional.of(comment2));

        // when
        comment1.block("test");
        comment2.delete();

        // then
        assertThrows(AccessBlockedOrDeletedCommentException.class,
                     () -> commentService.putComment(1L, 1L, new PutRequest("new comment")));
        assertThrows(AccessBlockedOrDeletedCommentException.class,
                     () -> commentService.putComment(1L, 2L, new PutRequest("new comment")));
    }

    @Test
    @DisplayName("댓글 수정 테스트 실패: 작성자가 아닌 다른 유저가 시도하는 경우")
    public void putCommentTest_AccessByOthers() {
        // given
        User user1 = new User("user1", "password");
        User user2 = new User("user2", "password");
        Review review = new Review(user1, new ReviewDto.PostRequest("content", null, 10, "location"));
        Comment comment = new Comment(user1, review, "comment");
        given(userRepository.findMeById(2L)).willReturn(user2);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // then
        assertThrows(CommentChangeByAnonymousException.class,
                     () -> commentService.putComment(2L, 1L, new PutRequest("new comment")));
    }

    @Test
    @DisplayName("댓글 삭제 테스트 성공")
    public void deleteCommentTest_Success() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new ReviewDto.PostRequest("content", null, 10, "location"));
        Comment comment = new Comment(user, review, "comment");
        given(userRepository.findMeById(1L)).willReturn(user);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when
        commentService.deleteComment(1L, 1L);

        // then
        assertThat(comment.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("댓글 삭제 테스트 실패: 작성자가 아닌 다른 유저가 시도하는 경우")
    public void deleteCommentTest_AccessByOthers() {
        // given
        User user1 = new User("user1", "password");
        User user2 = new User("user2", "password");
        Review review = new Review(user1, new ReviewDto.PostRequest("content", null, 10, "location"));
        Comment comment = new Comment(user1, review, "comment");
        given(userRepository.findMeById(2L)).willReturn(user2);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // then
        assertThrows(CommentChangeByAnonymousException.class, () -> commentService.deleteComment(2L, 1L));
    }

    @Test
    @DisplayName("댓글 삭제 테스트 실패: 삭제되거나 블락된 댓글")
    public void deleteCommentTest_CommentBlockedOrDeleted() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new ReviewDto.PostRequest("content", null, 10, "location"));
        Comment comment1 = new Comment(user, review, "comment");
        Comment comment2 = new Comment(user, review, "comment");
        given(userRepository.findMeById(1L)).willReturn(user);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment1));
        given(commentRepository.findById(2L)).willReturn(Optional.of(comment2));

        // when
        comment1.block("test");
        comment2.delete();

        // then
        assertThrows(AccessBlockedOrDeletedCommentException.class, () -> commentService.deleteComment(1L, 1L));
        assertThrows(AccessBlockedOrDeletedCommentException.class, () -> commentService.deleteComment(1L, 2L));
    }
}
