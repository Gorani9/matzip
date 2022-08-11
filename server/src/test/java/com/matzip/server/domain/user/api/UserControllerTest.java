package com.matzip.server.domain.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.ExpectedStatus;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.auth.dto.LoginDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private String signUp(String username, String password, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        UserDto.SignUpRequest signUpRequest = new UserDto.SignUpRequest(username, password);
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        long afterUserCount = userRepository.count();
        if (expectedStatus == ExpectedStatus.OK) {
            resultActions.andExpect(header().exists("Authorization"));
            assertThat(afterUserCount).isEqualTo(beforeUserCount + 1);
            signIn(username, password, ExpectedStatus.OK);
            return resultActions.andReturn().getResponse().getHeader("Authorization");
        } else {
            resultActions.andExpect(header().doesNotExist("Authorization"));
            assertThat(afterUserCount).isEqualTo(beforeUserCount);
            if (expectedStatus == ExpectedStatus.CONFLICT)
                signIn(username, password, ExpectedStatus.OK);
            else
                signIn(username, password, ExpectedStatus.UNAUTHORIZED);
            return null;
        }
    }

    private String signIn(String username, String password, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        LoginDto.LoginRequest signUpRequest = new LoginDto.LoginRequest(username, password);
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users/login/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
        if (expectedStatus == ExpectedStatus.OK) {
            resultActions.andExpect(header().exists("Authorization"));
            return resultActions.andReturn().getResponse().getHeader("Authorization");
        } else {
            resultActions.andExpect(header().doesNotExist("Authorization"));
            return null;
        }
    }

    private void checkDuplicateUsername(String username, Boolean exists) throws Exception {
        long beforeUserCount = userRepository.count();
        UserDto.DuplicateResponse duplicateResponse = new UserDto.DuplicateResponse(exists);
        mockMvc.perform(get("/api/v1/users/exists/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(duplicateResponse)));
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void searchUserByUsername(
            MultiValueMap<String, String> parameters, String token, ExpectedStatus expectedStatus
    ) throws Exception {
        long beforeUserCount = userRepository.count();
        ResultActions resultActions = mockMvc.perform(get("/api/v1/users/username/")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .params(parameters))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        if (expectedStatus == ExpectedStatus.OK) {
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").ascending());
            long count = userRepository
                    .findAllByUsernameContainsIgnoreCase(pageable, parameters.getFirst("username"))
                    .getTotalElements();
            resultActions.andExpect(jsonPath("$.total_elements").value(count));
        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void getUserByUsername(String token, String username, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        ResultActions resultActions = mockMvc.perform(get("/api/v1/users/username/" + username + "/")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        if (expectedStatus == ExpectedStatus.OK) {
            UserDto.Response response = new UserDto.Response(userRepository.findByUsername(username).orElseThrow());
            resultActions.andExpect(content().string(objectMapper.writeValueAsString(response)));
        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void getMe(String token, String username) throws Exception {
        long beforeUserCount = userRepository.count();
        UserDto.Response response = new UserDto.Response(userRepository.findByUsername(username).orElseThrow());
        mockMvc.perform(get("/api/v1/users/me/")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void changePassword(
            String username, String oldPassword, String newPassword, ExpectedStatus expectedStatus
    ) throws Exception {
        String token = signIn(username, oldPassword, ExpectedStatus.OK);
        long beforeUserCount = userRepository.count();
        UserDto.PasswordChangeRequest passwordChangeRequest = new UserDto.PasswordChangeRequest(newPassword);
        mockMvc.perform(put("/api/v1/users/me/password/")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        if (expectedStatus == ExpectedStatus.OK) {
            signIn(username, oldPassword, ExpectedStatus.UNAUTHORIZED);
            signIn(username, newPassword, ExpectedStatus.OK);
        } else {
            signIn(username, oldPassword, ExpectedStatus.OK);
            signIn(username, newPassword, ExpectedStatus.UNAUTHORIZED);
        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private final int pageSize = 15;
    private final int pageNumber = 0;

    private MultiValueMap<String, String> pageParameters() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("pageNumber", Collections.singletonList(String.valueOf(pageNumber)));
        parameters.put("pageSize", Collections.singletonList(String.valueOf(pageSize)));
        return parameters;
    }

    @Test
    void signInTest() throws Exception {
        String token;

        token = signUp("foo", "fooPassword1!", ExpectedStatus.OK);
        assertThat(token).isNotNull();

        token = signIn("foo", "fooPassword1!", ExpectedStatus.OK);
        assertThat(token).isNotNull();

        token = signIn("foo", "fooPassword", ExpectedStatus.UNAUTHORIZED);
        assertThat(token).isNull();

        token = signIn("fo", "fooPassword1!", ExpectedStatus.UNAUTHORIZED);
        assertThat(token).isNull();
    }

    @Test
    void signUpTest() throws Exception {
        String token;

        token = signUp("foo", "fooPassword1!", ExpectedStatus.OK);
        assertThat(token).isNotNull();

        token = signUp("foo2",
                "maximumLengthOfPasswordIs50Characters!!!!!!!!!!!!!",
                ExpectedStatus.OK);
        assertThat(token).isNotNull();

        token = signUp("foo", "fooPassword1!", ExpectedStatus.CONFLICT);
        assertThat(token).isNull();

        token = signUp("bar", "short", ExpectedStatus.BAD_REQUEST);
        assertThat(token).isNull();

        token = signUp("bar",
                "veryVeryLongPasswordThatIsOver50Characters!!!!!!!!!",
                ExpectedStatus.BAD_REQUEST);
        assertThat(token).isNull();

        token = signUp("bar", "noNumeric!", ExpectedStatus.BAD_REQUEST);
        assertThat(token).isNull();

        token = signUp("bar", "noSpecial1", ExpectedStatus.BAD_REQUEST);
        assertThat(token).isNull();

        token = signUp("bar", "no_upper_case1!", ExpectedStatus.BAD_REQUEST);
        assertThat(token).isNull();

        token = signUp("bar", "NO_LOWER_CASE1!", ExpectedStatus.BAD_REQUEST);
        assertThat(token).isNull();
    }

    @Test
    void checkDuplicateUsernameTest() throws Exception {
        signUp("foo", "fooPassword1!", ExpectedStatus.OK);
        signUp("bar", "barPassword1!", ExpectedStatus.OK);

        checkDuplicateUsername("foo", true);
        checkDuplicateUsername("foo2", false);
        checkDuplicateUsername("bar", true);
        checkDuplicateUsername("ba", false);
    }

    @Test
    void getUserByUsernameTest() throws Exception {
        String token = signUp("foo", "fooPassword1!", ExpectedStatus.OK);
        signUp("bar", "barPassword1!", ExpectedStatus.OK);

        getUserByUsername(token, "foo", ExpectedStatus.OK);
        getUserByUsername(token, "bar", ExpectedStatus.OK);
        getUserByUsername(token, "not_existing_user", ExpectedStatus.NOT_FOUND);
        getUserByUsername(token, "not_found", ExpectedStatus.NOT_FOUND);
    }

    @Test
    void searchUserByUsernameTest() throws Exception {
        String token = signUp("foo", "fooPassword1!", ExpectedStatus.OK);

        signUp("foo1", "fooPassword1!", ExpectedStatus.OK);
        signUp("foo2", "fooPassword1!", ExpectedStatus.OK);
        signUp("foo3", "fooPassword1!", ExpectedStatus.OK);
        signUp("foo4", "fooPassword1!", ExpectedStatus.OK);
        signUp("foo5", "fooPassword1!", ExpectedStatus.OK);
        signUp("foo6", "fooPassword1!", ExpectedStatus.OK);
        signUp("foo7", "fooPassword1!", ExpectedStatus.OK);
        signUp("foo8", "fooPassword1!", ExpectedStatus.OK);

        MultiValueMap<String, String> parameters = pageParameters();
        parameters.put("username", Collections.singletonList("foo"));
        searchUserByUsername(parameters, token, ExpectedStatus.OK);
        parameters.put("username", Collections.singletonList("bar"));
        searchUserByUsername(parameters, token, ExpectedStatus.OK);

        parameters.put("pageNumber", Collections.singletonList("-1"));
        searchUserByUsername(parameters, token, ExpectedStatus.BAD_REQUEST);
        parameters = pageParameters();
        parameters.put("pageSize", Collections.singletonList("0"));
        searchUserByUsername(parameters, token, ExpectedStatus.BAD_REQUEST);
    }

    @Test
    void getMeTest() throws Exception {
        String token = signUp("foo", "fooPassword1!", ExpectedStatus.OK);
        getMe(token, "foo");

        token = signUp("bar", "barPassword1!", ExpectedStatus.OK);
        getMe(token, "bar");
    }

    @Test
    void changePasswordTest() throws Exception {
        signUp("foo", "fooPassword1!", ExpectedStatus.OK);
        signUp("bar", "barPassword1!", ExpectedStatus.OK);

        changePassword("foo", "fooPassword1!", "short", ExpectedStatus.BAD_REQUEST);
        changePassword("foo", "fooPassword1!",
                "veryVeryLongPasswordThatIsOver50Characters!!!!!!!!!", ExpectedStatus.BAD_REQUEST);
        changePassword("foo", "fooPassword1!", "noNumeric!", ExpectedStatus.BAD_REQUEST);
        changePassword("foo", "fooPassword1!", "noSpecial1", ExpectedStatus.BAD_REQUEST);
        changePassword("foo", "fooPassword1!", "no_upper_case1!", ExpectedStatus.BAD_REQUEST);
        changePassword("foo", "fooPassword1!", "NO_LOWER_CASE1!", ExpectedStatus.BAD_REQUEST);

        changePassword("foo", "fooPassword1!",
                "maximumLengthOfPasswordIs50Characters!!!!!!!!!!!!!", ExpectedStatus.OK);
        changePassword("bar", "barPassword1!", "newPassword1!", ExpectedStatus.OK);
    }
}