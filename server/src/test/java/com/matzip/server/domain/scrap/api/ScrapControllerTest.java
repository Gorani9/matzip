package com.matzip.server.domain.scrap.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matzip.server.domain.auth.model.MatzipAuthenticationToken;
import com.matzip.server.domain.auth.model.UserPrincipal;
import com.matzip.server.domain.scrap.service.ScrapService;
import com.matzip.server.global.utils.ControllerParameters.Common;
import com.matzip.server.global.utils.TestParameterUtils.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.core.Authentication;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ScrapController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@DisplayName("ScrapController 테스트")
class ScrapControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ScrapService scrapService;

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).create();
    private final Authentication auth = new MatzipAuthenticationToken(new UserPrincipal(0L, "test"));

    @Test
    @DisplayName("스크랩 생성 파라미터 검증")
    void postCommentValidation() throws Exception {
        List<Pair<Object, Integer>> reviewIds = Stream.of(
                Stream.of(Common.validIds).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidIds).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> descriptions = Stream.of(
                Stream.of(Common.validNotBlankMax100).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidNotBlankMax100).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(reviewIds, descriptions);

        List<String> fields = List.of("review_id", "description");

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;
            Map<String, Object> request = new HashMap<>();
            for (int i = 0; i < fields.size(); i++) request.put(fields.get(i), combination.first.get(i));

            mockMvc.perform(
                    post("/api/v1/scraps")
                            .contentType(APPLICATION_JSON)
                            .content(gson.toJson(request))
                            .with(csrf()).with(authentication(auth))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }

    @Test
    @DisplayName("스크랩 수정 파라미터 검증")
    void patchCommentValidation() throws Exception {
        List<Pair<Object, Integer>> reviewIds = Stream.of(
                Stream.of(Common.validIds).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidIds).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> descriptions = Stream.of(
                Stream.of(Common.validNotBlankMax100).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidNotBlankMax100).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(reviewIds, descriptions);

        List<String> fields = List.of("review_id", "description");

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;
            Map<String, Object> request = new HashMap<>();
            for (int i = 0; i < fields.size(); i++) request.put(fields.get(i), combination.first.get(i));

            mockMvc.perform(
                    patch("/api/v1/scraps/{review-id}", combination.first.get(0))
                            .contentType(APPLICATION_JSON)
                            .content(gson.toJson(request))
                            .with(csrf()).with(authentication(auth))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }

    @Test
    @DisplayName("스크랩 삭제 파라미터 검증")
    void deleteCommentValidation() throws Exception {
        List<Pair<Object, Integer>> reviewIds = Stream.of(
                Stream.of(Common.validIds).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidIds).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(reviewIds);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    delete("/api/v1/scraps/{review-id}", combination.first.get(0))
                            .with(csrf()).with(authentication(auth))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }
}