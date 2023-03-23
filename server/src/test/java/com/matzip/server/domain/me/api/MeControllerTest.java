package com.matzip.server.domain.me.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matzip.server.domain.me.service.MeService;
import com.matzip.server.global.utils.ControllerParameters.Common;
import com.matzip.server.global.utils.ControllerParameters.PatchMe;
import com.matzip.server.global.utils.ControllerParameters.Signup;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;
import static com.matzip.server.global.common.exception.ErrorType.BadRequest.INVALID_REQUEST_BODY;
import static com.matzip.server.global.utils.TestParameterUtils.makeFieldList;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = MeController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@DisplayName("MeController 테스트")
class MeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MeService meService;

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).create();

    @Test
    @DisplayName("내 정보 수정 파라미터 검증")
    void patchMeValidation() throws Exception {
        List<Pair<Object, Integer>> images = Stream.of(
                Stream.of(PatchMe.validImage).map(u -> new Pair<Object, Integer>(u, null))
        ).flatMap(o -> o).collect(Collectors.toList());
        List<Pair<Object, Integer>> profiles = Stream.of(
                Stream.of(Common.validMax50).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Common.invalidMax50).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(images, profiles);

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;

            MockMultipartHttpServletRequestBuilder request = multipart(HttpMethod.PATCH, "/api/v1/me");
            Object image = combination.first.get(0);
            if (image != null) {
                request.file((MockMultipartFile) image);
            }

            mockMvc.perform(
                    request.param("profile", (String) combination.first.get(1))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second))
            ).andDo(print());
        }
    }

    @Test
    @DisplayName("내 유저네임 수정 파라미터 검증")
    void changeUsernameValidation() throws Exception {
        List<Pair<Object, Integer>> usernames = Stream.of(
                Stream.of(Signup.validUsernames).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Signup.invalidUsernames).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(usernames);

        List<String> fields = List.of("username");

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;
            Map<String, Object> request = new HashMap<>();
            for (int i = 0; i < fields.size(); i++) request.put(fields.get(i), combination.first.get(i));

            mockMvc.perform(
                    put("/api/v1/me/username")
                            .contentType(APPLICATION_JSON)
                            .content(gson.toJson(request))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second))
            ).andDo(print());
        }
    }

    @Test
    @DisplayName("내 비밀번호 수정 파라미터 검증")
    void changePasswordValidation() throws Exception {
        List<Pair<Object, Integer>> passwords = Stream.of(
                Stream.of(Signup.validPasswords).map(u -> new Pair<Object, Integer>(u, null)),
                Stream.of(Signup.invalidPasswords).map(u -> new Pair<Object, Integer>(u, INVALID_REQUEST_BODY.getCode()))
        ).flatMap(o -> o).collect(Collectors.toList());

        List<Pair<List<Object>, Pair<Integer, Integer>>> combinations = makeFieldList(passwords);

        List<String> fields = List.of("password");

        for (Pair<List<Object>, Pair<Integer, Integer>> combination : combinations) {
            Pair<Integer, Integer> result = combination.second;
            Map<String, Object> request = new HashMap<>();
            for (int i = 0; i < fields.size(); i++) request.put(fields.get(i), combination.first.get(i));

            mockMvc.perform(
                    put("/api/v1/me/password")
                            .contentType(APPLICATION_JSON)
                            .content(gson.toJson(request))
            ).andExpect(result.first == -1 ? status().isOk() : status().isBadRequest()
            ).andExpect(result.first == -1 ? status().isOk() : jsonPath("$.error_code", is(result.second))
            ).andDo(print());
        }
    }
}