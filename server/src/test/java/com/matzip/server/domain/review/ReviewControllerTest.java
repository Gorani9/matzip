package com.matzip.server.domain.review;

import com.matzip.server.ExpectedStatus;
import com.matzip.server.Parameters;
import com.matzip.server.domain.review.api.ReviewController;
import com.matzip.server.domain.review.service.ReviewService;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;

import java.io.InputStream;

import static com.matzip.server.ExpectedStatus.BAD_REQUEST;
import static com.matzip.server.ExpectedStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@Tag("ControllerTest")
class ReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ReviewService reviewService;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        User user = new User("foo", "password");
        UserPrincipal userPrincipal = new UserPrincipal(user);
        authentication = new MatzipAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }

    private void searchReviews(
            MultiValueMap<String, String> parameters, ExpectedStatus expectedStatus) throws Exception {
        mockMvc.perform(get("/api/v1/reviews")
                                .with(authentication(authentication))
                                .queryParams(parameters))
                .andExpect(status().is(expectedStatus.getStatusCode()));
    }

    private void postReview(
            MultiValueMap<String, String> parameters, ExpectedStatus expectedStatus) throws Exception {
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/reviews")
                                .file(new MockMultipartFile("images", "image_name", "png", InputStream.nullInputStream()))
                                .with(authentication(authentication)).with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(parameters))
                .andExpect(status().is(expectedStatus.getStatusCode()));
    }

    private void fetchReview(String id, ExpectedStatus expectedStatus) throws Exception {
        mockMvc.perform(get("/api/v1/reviews/{review-id}", id)
                                .with(authentication(authentication)))
                .andExpect(status().is(expectedStatus.getStatusCode()));
    }

    @Test
    @DisplayName("리뷰 조회 테스트: 리뷰 아이디 경로 변수 검증")
    void fetchReviewTest() throws Exception {
        fetchReview("0", BAD_REQUEST);
        fetchReview("alphabet", BAD_REQUEST);
        fetchReview("!!", BAD_REQUEST);
        fetchReview("-1", BAD_REQUEST);
        fetchReview("1", OK);
    }

    @Test
    @DisplayName("리뷰 포스팅 테스트: 파라미터 검증")
    void postReviewTest() throws Exception {
        Parameters parameters;

        parameters = new Parameters();
        parameters.putParameter("content", "content should not be blank");
        parameters.putParameter("rating", "5");
        parameters.putParameter("location", "location string should not be blank");
        postReview(parameters, OK);

        parameters = new Parameters();
        parameters.putParameter("rating", "5");
        parameters.putParameter("location", "location string should not be blank");
        postReview(parameters, BAD_REQUEST);

        parameters = new Parameters();
        parameters.putParameter("content", "content should not be blank");
        parameters.putParameter("location", "location string should not be blank");
        postReview(parameters, BAD_REQUEST);

        parameters = new Parameters();
        parameters.putParameter("content", "content should not be blank");
        parameters.putParameter("rating", "5");
        postReview(parameters, BAD_REQUEST);

        parameters = new Parameters();
        parameters.putParameter("content", "content should not be blank");
        parameters.putParameter("location", "location string should not be blank");
        parameters.putParameter("rating", "-1");
        postReview(parameters, BAD_REQUEST);
        parameters.putParameter("rating", "0");
        postReview(parameters, OK);
        parameters.putParameter("rating", "10");
        postReview(parameters, OK);
        parameters.putParameter("rating", "11");
        postReview(parameters, BAD_REQUEST);

        parameters = new Parameters();
        String content = "content maximum length is 3000" + "!".repeat(2971);
        assertThat(content.length()).isEqualTo(3001);
        parameters.putParameter("content", content);
        parameters.putParameter("rating", "5");
        parameters.putParameter("location", "location string should not be blank");
        postReview(parameters, BAD_REQUEST);
        content = content.substring(0, 3000);
        parameters.putParameter("content", content);
        assertThat(content.length()).isEqualTo(3000);
        postReview(parameters, OK);
    }

    @Test
    @DisplayName("리뷰 검색 테스트: 파라미터 검증")
    void searchReviewsByContent() throws Exception {
        Parameters parameters;

        /* keyword length at most 100 */
        String keyword = "a".repeat(100);
        parameters = new Parameters();
        parameters.putParameter("keyword", keyword);
        searchReviews(parameters, OK);
        parameters.putParameter("keyword", keyword + "a");
        searchReviews(parameters, BAD_REQUEST);

        /* page must be positive or zero */
        parameters = new Parameters(-1, 15);
        searchReviews(parameters, BAD_REQUEST);
        parameters.putParameter("page", "0");
        searchReviews(parameters, OK);
        parameters.putParameter("page", "1");
        searchReviews(parameters, OK);

        /* size must be positive, smaller or equal to 100 */
        parameters = new Parameters(0, 0);
        searchReviews(parameters, BAD_REQUEST);
        parameters.putParameter("size", "-1");
        searchReviews(parameters, BAD_REQUEST);
        parameters.putParameter("size", "101");
        searchReviews(parameters, BAD_REQUEST);
        parameters.putParameter("size", "100");
        searchReviews(parameters, OK);

        /* asc must be either true or false or null */
        parameters = new Parameters(0, 15);
        parameters.putParameter("asc", "boolean");
        searchReviews(parameters, BAD_REQUEST);
        parameters.putParameter("asc", "null");
        searchReviews(parameters, BAD_REQUEST);
        parameters.putParameter("asc", "false");
        searchReviews(parameters, OK);
        parameters.putParameter("asc", "true");
        searchReviews(parameters, OK);
        parameters.putParameter("asc", null);
        searchReviews(parameters, OK);

        /* sort must be one of these followings: username, level, followers, hearts, scraps, comments, rating */
        parameters = new Parameters(0, 15);
        parameters.putParameter("sort", "username");
        searchReviews(parameters, OK);
        parameters.putParameter("sort", "level");
        searchReviews(parameters, OK);
        parameters.putParameter("sort", "followers");
        searchReviews(parameters, OK);
        parameters.putParameter("sort", "hearts");
        searchReviews(parameters, OK);
        parameters.putParameter("sort", "scraps");
        searchReviews(parameters, OK);
        parameters.putParameter("sort", "comments");
        searchReviews(parameters, OK);
        parameters.putParameter("sort", "rating");
        searchReviews(parameters, OK);
        parameters.putParameter("sort", "    ");
        searchReviews(parameters, OK);
        parameters.putParameter("sort", "");
        searchReviews(parameters, OK);
        parameters.putParameter("sort", null);
        searchReviews(parameters, OK);
        parameters.putParameter("sort", "createdAt");
        searchReviews(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "matzipLevel");
        searchReviews(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "modifiedAt");
        searchReviews(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "id");
        searchReviews(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "password");
        searchReviews(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "role");
        searchReviews(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "profileString");
        searchReviews(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "image");
        searchReviews(parameters, BAD_REQUEST);
    }
}