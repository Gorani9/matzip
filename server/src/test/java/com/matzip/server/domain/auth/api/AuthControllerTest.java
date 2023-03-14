package com.matzip.server.domain.auth.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matzip.server.domain.auth.service.AuthService;
import com.matzip.server.global.utils.ControllerParameters.Login;
import com.matzip.server.global.utils.ControllerParameters.Signup;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@DisplayName("AuthController 테스트")
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).create();

    @Test
    @DisplayName("회원가입 입력 파라미터 검증")
    void signupValidation() throws Exception {
        List<Pair<Object, Integer>> usernames = Stream.of(
                Stream.of(Signup.validUsernames).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Signup.invalidUsernames).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<Object, Integer>> passwords = Stream.of(
                Stream.of(Signup.validPasswords).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Signup.invalidPasswords).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<String> fields = List.of("username", "password");

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(usernames, passwords);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;
            Map<String, Object> request = new HashMap<>();
            for (int i = 0; i < fields.size(); i++) request.put(fields.get(i), combination.first.get(i));

            mockMvc.perform(
                    post("/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
                            .with(csrf())
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }

    @Test
    @DisplayName("Username 중복 체크 입력 파라미터 검증")
    void usernameDuplicateCheckValidation() throws Exception {
        List<Pair<Object, Integer>> usernames = Stream.of(
                Stream.of(Signup.validUsernames).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Signup.invalidUsernames).map(u -> new Pair<Object, Integer>(u, INVALID_PARAMETER.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(usernames);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            mockMvc.perform(
                    get("/signup/exists")
                            .param("username", (String) combination.first.get(0))
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf())
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }

    @Test
    @DisplayName("로그인 입력 파라미터 검증")
    void loginValidation() throws Exception {
        List<Pair<Object, Integer>> usernames = Stream.of(
                Stream.of(Login.validUsernames).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Login.invalidUsernames).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<Object, Integer>> passwords = Stream.of(
                Stream.of(Login.validPasswords).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Login.invalidPasswords).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<String> fields = List.of("username", "password");

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(usernames, passwords);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;
            Map<String, Object> request = new HashMap<>();
            for (int i = 0; i < fields.size(); i++) request.put(fields.get(i), combination.first.get(i));

            mockMvc.perform(
                    post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
                            .with(csrf())
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second)));
        }
    }
}