package com.matzip.server.domain.user.api;

import com.matzip.server.domain.user.service.UserService;
import com.matzip.server.global.utils.ControllerParameters;
import com.matzip.server.global.utils.ControllerParameters.SearchUser;
import com.matzip.server.global.utils.TestParameterUtils.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.matzip.server.global.common.exception.ErrorType.BadRequest.INVALID_PARAMETER;
import static com.matzip.server.global.utils.TestParameterUtils.makeFieldList;
import static org.hamcrest.core.Is.is;
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

    @Test
    @DisplayName("Username 중복 체크 입력 파라미터 검증")
    void usernameDuplicateCheckValidation() throws Exception {
        List<Pair<Object, Integer>> usernames = Stream.of(
                Stream.of(ControllerParameters.Signup.validUsernames).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(ControllerParameters.Signup.invalidUsernames).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(usernames);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    get("/api/v1/users/exists")
                            .param("username", (String) combination.first.get(0))
                            .contentType(MediaType.APPLICATION_JSON)
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
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }


}