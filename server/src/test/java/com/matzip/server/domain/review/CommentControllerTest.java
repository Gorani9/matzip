package com.matzip.server.domain.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.ExpectedStatus;
import com.matzip.server.Parameters;
import com.matzip.server.domain.review.api.CommentController;
import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.service.CommentService;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.auth.model.MatzipAuthenticationToken;
import com.matzip.server.global.auth.model.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;

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

    private void searchComments(
            MultiValueMap<String, String> parameters, ExpectedStatus expectedStatus) throws Exception {
        mockMvc.perform(get("/api/v1/comments")
                                .with(authentication(authentication))
                                .queryParams(parameters))
                .andExpect(status().is(expectedStatus.getStatusCode()));
    }

    private void postComment(String content, ExpectedStatus expectedStatus) throws Exception {
        CommentDto.PostRequest request = new CommentDto.PostRequest(1L, content);
        mockMvc.perform(post("/api/v1/comments")
                                .with(authentication(authentication)).with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.getStatusCode()));
    }

    @Test
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
    void searchCommentsTest() throws Exception {
        Parameters parameters;

        /* page must be positive or zero */
        parameters = new Parameters(-1, 15);
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("page", "0");
        searchComments(parameters, OK);
        parameters.putParameter("page", "1");
        searchComments(parameters, OK);

        /* size must be positive, smaller or equal to 100 */
        parameters = new Parameters(0, 0);
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("size", "-1");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("size", "101");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("size", "100");
        searchComments(parameters, OK);

        /* asc must be either true or false or null */
        parameters = new Parameters(0, 15);
        parameters.putParameter("asc", "boolean");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("asc", "null");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("asc", "false");
        searchComments(parameters, OK);
        parameters.putParameter("asc", "true");
        searchComments(parameters, OK);
        parameters.putParameter("asc", null);
        searchComments(parameters, OK);

        /* sort must be one of these followings: follower */
        parameters = new Parameters(0, 15);
        parameters.putParameter("sort", "followers");
        searchComments(parameters, OK);
        parameters.putParameter("sort", "    ");
        searchComments(parameters, OK);
        parameters.putParameter("sort", "");
        searchComments(parameters, OK);
        parameters.putParameter("sort", null);
        searchComments(parameters, OK);
        parameters.putParameter("sort", "username");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "level");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "hearts");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "scraps");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "comments");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "rating");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "createdAt");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "matzipLevel");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "modifiedAt");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "id");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "password");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "role");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "profileString");
        searchComments(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "image");
        searchComments(parameters, BAD_REQUEST);
    }
}