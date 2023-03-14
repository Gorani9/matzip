package com.matzip.server.domain.review.api;

import com.matzip.server.domain.auth.model.MatzipAuthenticationToken;
import com.matzip.server.domain.auth.model.UserPrincipal;
import com.matzip.server.domain.review.service.ReviewService;
import com.matzip.server.global.utils.ControllerParameters.Common;
import com.matzip.server.global.utils.ControllerParameters.PatchReview;
import com.matzip.server.global.utils.ControllerParameters.PostReview;
import com.matzip.server.global.utils.TestParameterUtils.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.matzip.server.global.common.exception.ErrorType.BadRequest.INVALID_PARAMETER;
import static com.matzip.server.global.common.exception.ErrorType.BadRequest.INVALID_REQUEST_BODY;
import static com.matzip.server.global.utils.TestParameterUtils.makeFieldList;
import static org.hamcrest.core.Is.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ReviewController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@DisplayName("ReviewController 테스트")
class ReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ReviewService reviewService;

    private final Authentication auth = new MatzipAuthenticationToken(new UserPrincipal(0L, "test"));

    @Test
    @DisplayName("리뷰 검색 파라미터 검증")
    void searchReviewsValidation() throws Exception {
        List<Pair<Object, Integer>> keywords = Stream.of(
                Stream.of(Common.validNullableNotBlankMax30).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidNullableNotBlankMax30).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> pages = Stream.of(
                Stream.of(Common.validPages).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidPages).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> sizes = Stream.of(
                Stream.of(Common.validSizes).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidSizes).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> sorts = Stream.of(
                Stream.of(Common.validReviewProperties).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidReviewProperties).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> asc = Stream.of(
                Stream.of(Common.validAsc).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidAsc).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(keywords, pages, sizes, sorts, asc);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    get("/api/v1/reviews")
                            .queryParam("keyword", (String) combination.first.get(0))
                            .queryParam("page", (String) combination.first.get(1))
                            .queryParam("size", (String) combination.first.get(2))
                            .queryParam("sort", (String) combination.first.get(3))
                            .queryParam("asc", (String) combination.first.get(4))
                            .with(authentication(auth))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }

    @Test
    @DisplayName("리뷰 생성 파라미터 검증")
    void postReviewValidation() throws Exception {
        List<Pair<Object, Integer>> images = Stream.of(
                Stream.of(PostReview.validImages).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(PostReview.invalidImages).map(u -> new Pair<>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> contents = Stream.of(
                Stream.of(PostReview.validContents).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(PostReview.invalidContents).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> ratings = Stream.of(
                Stream.of(PostReview.validRatings).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(PostReview.invalidRatings).map(u -> new Pair<>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> locations = Stream.of(
                Stream.of(PostReview.validLocations).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(PostReview.invalidLocations).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(images, contents, ratings, locations);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            MockMultipartHttpServletRequestBuilder request = multipart(HttpMethod.POST, "/api/v1/reviews");
            if (combination.first.get(0) != null) {
                for (MockMultipartFile file : (List<MockMultipartFile>) combination.first.get(0)) {
                    request.file(file);
                }
            }

            mockMvc.perform(
                    request.with(csrf()).with(authentication(auth))
                            .param("content", (String) combination.first.get(1))
                            .param("rating", (String) combination.first.get(2))
                            .param("location", (String) combination.first.get(3))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }

    @Test
    @DisplayName("리뷰 조회 파라미터 검증")
    void fetchReviewValidation() throws Exception {
        List<Pair<Object, Integer>> ids = Stream.of(
                Stream.of(Common.validIds).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidIds).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(ids);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    get("/api/v1/reviews/{id}", combination.first.get(0))
                            .with(authentication(auth))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }

    @Test
    @DisplayName("리뷰 수정 파라미터 검증")
    void patchReviewValidation() throws Exception {
        List<Pair<Object, Integer>> ids = Stream.of(
                Stream.of(Common.validIds).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidIds).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> images = Stream.of(
                Stream.of(PatchReview.validImages).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(PatchReview.invalidImages).map(u -> new Pair<>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> contents = Stream.of(
                Stream.of(PatchReview.validContents).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(PatchReview.invalidContents).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> oldUrls = Stream.of(
                Stream.of(PatchReview.validOldUrls).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(PatchReview.invalidOldUrls).map(u -> new Pair<>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> ratings = Stream.of(
                Stream.of(PostReview.validRatings).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(PostReview.invalidRatings).map(u -> new Pair<>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(ids, images, contents, ratings, oldUrls);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            String id = (String) combination.first.get(0);
            MockMultipartHttpServletRequestBuilder request = multipart(HttpMethod.PATCH, "/api/v1/reviews/{id}", id);
            for (MockMultipartFile file : (List<MockMultipartFile>) combination.first.get(1)) {
                request.file(file);
            }

            mockMvc.perform(
                    request.with(csrf()).with(authentication(auth))
                            .param("content", (String) combination.first.get(2))
                            .param("rating", (String) combination.first.get(3))
                            .param("oldUrls", ((List<String>) combination.first.get(4)).toArray(String[]::new))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }

    @Test
    @DisplayName("리뷰 삭제 파라미터 검증")
    void deleteReviewValidation() throws Exception {
        List<Pair<Object, Integer>> ids = Stream.of(
                Stream.of(Common.validIds).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidIds).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(ids);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    delete("/api/v1/reviews/{id}", combination.first.get(0))
                            .with(csrf()).with(authentication(auth))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }
    @Test
    @DisplayName("좋아요 파라미터 검증")
    void heartReviewValidation() throws Exception {
        List<Pair<Object, Integer>> ids = Stream.of(
                Stream.of(Common.validIds).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidIds).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(ids);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    put("/api/v1/reviews/{id}/heart", combination.first.get(0))
                            .with(authentication(auth))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }
    @Test
    @DisplayName("좋아요 취소 파라미터 검증")
    void deleteHeartReviewValidation() throws Exception {
        List<Pair<Object, Integer>> ids = Stream.of(
                Stream.of(Common.validIds).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidIds).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(ids);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    delete("/api/v1/reviews/{id}/heart", combination.first.get(0))
                            .with(authentication(auth))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }
}