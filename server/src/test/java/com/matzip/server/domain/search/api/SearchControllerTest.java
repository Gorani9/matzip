package com.matzip.server.domain.search.api;

import com.matzip.server.domain.search.service.SearchService;
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.matzip.server.global.common.exception.ErrorType.BadRequest.INVALID_PARAMETER;
import static com.matzip.server.global.utils.ControllerParameters.Common.*;
import static com.matzip.server.global.utils.ControllerParameters.Login.invalidUsernames;
import static com.matzip.server.global.utils.ControllerParameters.Login.validUsernames;
import static com.matzip.server.global.utils.TestParameterUtils.makeFieldList;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = SearchController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@DisplayName("SearchController 테스트")
class SearchControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SearchService searchService;

    @Test
    @DisplayName("리뷰 검색 파라미터 검증")
    void searchReviewsValidation() throws Exception {
        List<Pair<Object, Integer>> keywords = Stream.of(
                Stream.of(validNullableNotBlankMax30).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(invalidNullableNotBlankMax30).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> pages = Stream.of(
                Stream.of(validPages).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(invalidPages).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> sizes = Stream.of(
                Stream.of(validSizes).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(invalidSizes).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> sorts = Stream.of(
                Stream.of(validReviewProperties).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(invalidReviewProperties).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> asc = Stream.of(
                Stream.of(validAsc).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(invalidAsc).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(keywords, pages, sizes, sorts, asc);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    get("/api/v1/search/reviews")
                            .queryParam("keyword", (String) combination.first.get(0))
                            .queryParam("page", (String) combination.first.get(1))
                            .queryParam("size", (String) combination.first.get(2))
                            .queryParam("sort", (String) combination.first.get(3))
                            .queryParam("asc", (String) combination.first.get(4))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }

    @Test
    @DisplayName("회원 검색 파라미터 검증")
    void searchByUsernameValidation() throws Exception {
        List<Pair<Object, Integer>> usernames = Stream.of(
                Stream.of(validUsernames).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(invalidUsernames).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> pages = Stream.of(
                Stream.of(validPages).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(invalidPages).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> sizes = Stream.of(
                Stream.of(validSizes).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(invalidSizes).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> sorts = Stream.of(
                Stream.of(validUserProperties).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(invalidUserProperties).map(u -> new Pair<Object, Integer>(u,
                                                                                    INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> asc = Stream.of(
                Stream.of(validAsc).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(invalidAsc).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(usernames, pages, sizes, sorts, asc);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    get("/api/v1/search/users")
                            .queryParam("username", (String) combination.first.get(0))
                            .queryParam("page", (String) combination.first.get(1))
                            .queryParam("size", (String) combination.first.get(2))
                            .queryParam("sort", (String) combination.first.get(3))
                            .queryParam("asc", (String) combination.first.get(4))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }
}