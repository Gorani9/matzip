package com.matzip.server.domain.comment.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matzip.server.domain.comment.service.CommentService;
import com.matzip.server.global.utils.ControllerParameters.Common;
import com.matzip.server.global.utils.TestParameterUtils.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;
import static com.matzip.server.global.common.exception.ErrorType.BadRequest.INVALID_PARAMETER;
import static com.matzip.server.global.common.exception.ErrorType.BadRequest.INVALID_REQUEST_BODY;
import static com.matzip.server.global.utils.TestParameterUtils.makeFieldList;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = CommentController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@DisplayName("CommentController 테스트")
class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommentService commentService;

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).create();

    @Test
    @DisplayName("댓글 생성 파라미터 검증")
    void postCommentValidation() throws Exception {
        List<Pair<Object, Integer>> reviewIds = Stream.of(
                Stream.of(Common.validIds).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidIds).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> contents = Stream.of(
                Stream.of(Common.validNotBlankMax100).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidNotBlankMax100).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(reviewIds, contents);

        List<String> fields = List.of("review_id", "content");

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;
            Map<String, Object> request = new HashMap<>();
            for (int i = 0; i < fields.size(); i++) request.put(fields.get(i), combination.first.get(i));

            mockMvc.perform(
                    post("/api/v1/comments")
                            .contentType(APPLICATION_JSON)
                            .content(gson.toJson(request))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }

    @Test
    @DisplayName("댓글 수정 파라미터 검증")
    void patchCommentValidation() throws Exception {
        List<Pair<Object, Integer>> ids = Stream.of(
                Stream.of(Common.validIds).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidIds).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> contents = Stream.of(
                Stream.of(Common.validNotBlankMax100).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidNotBlankMax100).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(ids, contents);

        List<String> fields = List.of("review_id", "content");

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;
            Map<String, Object> request = new HashMap<>();
            for (int i = 0; i < fields.size(); i++) request.put(fields.get(i), combination.first.get(i));

            mockMvc.perform(
                    patch("/api/v1/comments/{id}", combination.first.get(0))
                            .contentType(APPLICATION_JSON)
                            .content(gson.toJson(request))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }

    @Test
    @DisplayName("댓글 삭제 파라미터 검증")
    void deleteCommentValidation() throws Exception {
        List<Pair<Object, Integer>> ids = Stream.of(
                Stream.of(Common.validIds).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidIds).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(ids);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    delete("/api/v1/comments/{id}", combination.first.get(0))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }
}