package com.matzip.server.domain.review;

import com.matzip.server.domain.review.dto.CommentDto.SearchRequest;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Tag("RepositoryTest")
public class CommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    private final String PASSWORD = "password";

    @Test
    @DisplayName("자기가 작성한 댓글 검색 기본 테스트: 생성기준 내림차순 정렬")
    public void searchUserTest_Basic_Asc() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review = reviewRepository.save(new Review(
                user, new ReviewDto.PostRequest("content", null, 10, "location")));
        Comment comment1 = commentRepository.save(new Comment(user, review, "comment1"));
        Comment comment2 = commentRepository.save(new Comment(user, review, "comment2"));
        Comment comment3 = commentRepository.save(new Comment(user, review, "comment3"));
        SearchRequest request = new SearchRequest("comment", 0, 10, null, false);

        // when
        Slice<Comment> comments = commentRepository.searchMyCommentsByKeyword(request, user.getId());

        // then
        assertThat(comments.getContent()).containsExactly(comment3, comment2, comment1);
    }

    @Test
    @DisplayName("내가 작성한 댓글 검색 기본 테스트: 생성기준 오름차순 정렬")
    public void searchUserTest_Basic_Desc() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review = reviewRepository.save(new Review(
                user, new ReviewDto.PostRequest("content", null, 10, "location")));
        Comment comment1 = commentRepository.save(new Comment(user, review, "comment1"));
        Comment comment2 = commentRepository.save(new Comment(user, review, "comment2"));
        Comment comment3 = commentRepository.save(new Comment(user, review, "comment3"));
        SearchRequest request = new SearchRequest("comment", 0, 10, null, true);

        // when
        Slice<Comment> comments = commentRepository.searchMyCommentsByKeyword(request, user.getId());

        // then
        assertThat(comments.getContent()).containsExactly(comment1, comment2, comment3);
    }

    @Test
    @DisplayName("내가 작성한 댓글 검색 기본 테스트: 삭제되거나 블락 처리된 댓글 제외")
    public void searchUserTest_Basic_Exclusion() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review = reviewRepository.save(new Review(
                user, new ReviewDto.PostRequest("content", null, 10, "location")));
        Comment comment1 = commentRepository.save(new Comment(user, review, "comment1"));
        Comment comment2 = commentRepository.save(new Comment(user, review, "comment2"));
        Comment comment3 = commentRepository.save(new Comment(user, review, "comment3"));
        SearchRequest request = new SearchRequest("comment", 0, 10, null, true);
        comment2.delete();
        comment3.block("test");

        // when
        Slice<Comment> comments = commentRepository.searchMyCommentsByKeyword(request, user.getId());

        // then
        assertThat(comments.getContent()).containsExactly(comment1);
    }

    @Test
    @DisplayName("내가 작성한 댓글 검색 기본 테스트: 페이징")
    public void searchUserTest_Basic_Paging() {
        // given
        User user = userRepository.save(new User("user", PASSWORD));
        Review review = reviewRepository.save(new Review(
                user, new ReviewDto.PostRequest("content", null, 10, "location")));
        Comment[] comments = new Comment[10];
        for (int i = 0; i < 10; i++) comments[i] = commentRepository.save(new Comment(user, review, "comment" + i));
        SearchRequest request = new SearchRequest("comment", 1, 3, null, true);

        // when
        Slice<Comment> searchedComments = commentRepository.searchMyCommentsByKeyword(request, user.getId());

        // then
        assertThat(searchedComments.getContent()).containsExactly(comments[3], comments[4], comments[5]);
        assertThat(searchedComments.hasNext()).isTrue();
        assertThat(searchedComments.isFirst()).isFalse();
        assertThat(searchedComments.isLast()).isFalse();
    }
}
