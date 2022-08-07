package com.matzip.server.domain.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.auth.dto.LoginRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

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

    private final String USER1 = "foo";
    private final String USER1_PASSWD = "fooPassword1!";
    private final String USER2 = "bar";
    private final String USER2_PASSWD = "barPassword1!";

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        UserDto.SignUpRequest signUpRequest = new UserDto.SignUpRequest(USER1, USER1_PASSWD);
        userToken = mockMvc.perform(post("/api/v1/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andReturn().getResponse().getHeader("Authorization");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void signIn() throws Exception {
        LoginRequest loginRequest = new LoginRequest(USER1, USER1_PASSWD);
        mockMvc.perform(post("/api/v1/users/login/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"));
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @Transactional
    void signInFail() throws Exception {
        LoginRequest loginRequestWithWrongPassword = new LoginRequest(USER1, USER2_PASSWD);
        mockMvc.perform(post("/api/v1/users/login/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestWithWrongPassword)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Authorization"));
        assertThat(userRepository.count()).isEqualTo(1);

        LoginRequest loginRequestWithWrongUsername = new LoginRequest(USER2, USER1_PASSWD);
        mockMvc.perform(post("/api/v1/users/login/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestWithWrongUsername)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Authorization"));
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @Transactional
    void signUp() throws Exception {
        UserDto.SignUpRequest signUpRequest = new UserDto.SignUpRequest(USER2, USER2_PASSWD);
        mockMvc.perform(post("/api/v1/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"));
        assertThat(userRepository.count()).isEqualTo(2);
    }

    @Test
    @Transactional
    void duplicateUsernameSignUp() throws Exception {
        UserDto.SignUpRequest conflictSignUpRequest = new UserDto.SignUpRequest(USER1, USER1_PASSWD);
        mockMvc.perform(post("/api/v1/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictSignUpRequest)))
                .andExpect(status().isConflict());
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void checkDuplicateUsername() throws Exception {
        UserDto.DuplicateRequest duplicateRequest1 = new UserDto.DuplicateRequest(USER1);
        UserDto.DuplicateRequest duplicateRequest2 = new UserDto.DuplicateRequest(USER2);
        UserDto.DuplicateResponse duplicateResponse1 = new UserDto.DuplicateResponse(true);
        UserDto.DuplicateResponse duplicateResponse2 = new UserDto.DuplicateResponse(false);
        mockMvc.perform(get("/api/v1/users/duplicate/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest1)))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(duplicateResponse1)));
        assertThat(userRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/v1/users/duplicate/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest2)))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(duplicateResponse2)));
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void getMe() throws Exception {
        UserDto.FindRequest findRequest = new UserDto.FindRequest(USER1);
        UserDto.Response expectedResponse = new UserDto.Response(userRepository.findByUsername(USER1));
        mockMvc.perform(get("/api/v1/users/me/")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(findRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponse)));
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @Transactional
    void changePassword() throws Exception {
        String newPassword = "newFooPassword2@";
        UserDto.PasswordChangeRequest passwordChangeRequest = new UserDto.PasswordChangeRequest(newPassword);
        mockMvc.perform(put("/api/v1/users/me/password/")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
        assertThat(userRepository.count()).isEqualTo(1);

        LoginRequest loginRequest = new LoginRequest(USER1, newPassword);
        mockMvc.perform(post("/api/v1/users/login/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"));
        assertThat(userRepository.count()).isEqualTo(1);
    }

    void checkPasswordFail(String invalidPassword) throws Exception {
        UserDto.PasswordChangeRequest passwordChangeRequest = new UserDto.PasswordChangeRequest(invalidPassword);
        mockMvc.perform(put("/api/v1/users/me/password/")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isBadRequest());
        assertThat(userRepository.count()).isEqualTo(1);

        LoginRequest invalidLoginRequest = new LoginRequest(USER1, invalidPassword);
        mockMvc.perform(post("/api/v1/users/login/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isUnauthorized());
        assertThat(userRepository.count()).isEqualTo(1);

        LoginRequest validLoginRequest = new LoginRequest(USER1, USER1_PASSWD);
        mockMvc.perform(post("/api/v1/users/login/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"));
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @Transactional
    void changePasswordFail() throws Exception {
        checkPasswordFail("short");
        checkPasswordFail("long but no numeric nor special");
        checkPasswordFail("NO SMALL CASE 11!!");
        checkPasswordFail("no upper case 11!!");
        checkPasswordFail("No Numeric !!");
        checkPasswordFail("No Special 11");
    }
}