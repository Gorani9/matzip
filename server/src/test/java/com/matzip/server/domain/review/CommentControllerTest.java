package com.matzip.server.domain.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.ExpectedStatus;
import com.matzip.server.domain.review.api.CommentController;
import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.service.CommentService;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.auth.model.MatzipAuthenticationToken;
import com.matzip.server.global.auth.model.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.matzip.server.ExpectedStatus.BAD_REQUEST;
import static com.matzip.server.ExpectedStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@Tag("ControllerTest")
class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CommentService commentService;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        User user = new User("foo", "password");
        UserPrincipal userPrincipal = new UserPrincipal(user);
        authentication = new MatzipAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }

    private void postComment(String content, ExpectedStatus expectedStatus) throws Exception {
        CommentDto.PostRequest request = new CommentDto.PostRequest(1L, content);
        mockMvc.perform(post("/api/v1/comments")
                                .with(authentication(authentication)).with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.getStatusCode()));
    }

    private void fetchComment(String id, ExpectedStatus expectedStatus) throws Exception {
        mockMvc.perform(get("/api/v1/comments/{comment-id}", id).with(authentication(authentication)))
                .andExpect(status().is(expectedStatus.getStatusCode()));
    }

    @Test
    @DisplayName("댓글 작성 테스트: request 검증")
    void postCommentTest() throws Exception {
        postComment("content", OK);

        String content = "content maximum length is 100" + "!".repeat(72);
        assertThat(content.length()).isEqualTo(101);
        postComment(content, BAD_REQUEST);
        content = content.substring(0, 100);
        assertThat(content.length()).isEqualTo(100);
        postComment(content, OK);
    }

    @Test
    @DisplayName("댓글 조회 테스트: 댓글 아이디 경로 변수 검증")
    void fetchCommentTest() throws Exception {
        fetchComment("0", BAD_REQUEST);
        fetchComment("alphabet", BAD_REQUEST);
        fetchComment("!!", BAD_REQUEST);
        fetchComment("-1", BAD_REQUEST);
        fetchComment("1", OK);
    }
}