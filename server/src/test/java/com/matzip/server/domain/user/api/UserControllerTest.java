package com.matzip.server.domain.user.api;

import com.matzip.server.domain.auth.model.MatzipAuthenticationToken;
import com.matzip.server.domain.auth.model.UserPrincipal;
import com.matzip.server.domain.user.service.UserService;
import com.matzip.server.global.utils.ControllerParameters.Common;
import com.matzip.server.global.utils.ControllerParameters.Login;
import com.matzip.server.global.utils.ControllerParameters.SearchUser;
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.matzip.server.global.common.exception.ErrorType.BadRequest.INVALID_PARAMETER;
import static com.matzip.server.global.utils.TestParameterUtils.makeFieldList;
import static org.hamcrest.core.Is.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@DisplayName("UserController 테스트")
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    private final Authentication auth = new MatzipAuthenticationToken(new UserPrincipal(0L, "test"));

    @Test
    @DisplayName("회원 검색 파라미터 검증")
    void searchByUsernameValidation() throws Exception {
        List<Pair<Object, Integer>> usernames = Stream.of(
                Stream.of(Login.validUsernames).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Login.invalidUsernames).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
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
                Stream.of(Common.validUserProperties).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidUserProperties).map(u -> new Pair<Object, Integer>(u,
                                                                                         INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> asc = Stream.of(
                Stream.of(Common.validAsc).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidAsc).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(usernames, pages, sizes, sorts, asc);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    get("/api/v1/users")
                            .queryParam("username", (String) combination.first.get(0))
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
    @DisplayName("회원 정보 조회 파라미터 검증")
    void fetchByUsernameValidation() throws Exception {
        List<Pair<Object, Integer>> usernames = Stream.of(
                Stream.of(SearchUser.validUsernames).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(SearchUser.invalidUsernames).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(usernames);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    get("/api/v1/users/{username}", combination.first.get(0))
                            .with(authentication(auth))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }

    @Test
    @DisplayName("회원 팔로우 파라미터 검증")
    void followByUsernameValidation() throws Exception {
        List<Pair<Object, Integer>> usernames = Stream.of(
                Stream.of(SearchUser.validUsernames).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(SearchUser.invalidUsernames).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(usernames);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    put("/api/v1/users/{username}/follow", combination.first.get(0))
                            .with(csrf())
                            .with(authentication(auth))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }

    @Test
    @DisplayName("회원 팔로우 취소 파라미터 검증")
    void unfollowByUsernameValidation() throws Exception {
        List<Pair<Object, Integer>> usernames = Stream.of(
                Stream.of(SearchUser.validUsernames).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(SearchUser.invalidUsernames).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(usernames);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    delete("/api/v1/users/{username}/follow", combination.first.get(0))
                            .with(csrf())
                            .with(authentication(auth))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }


}