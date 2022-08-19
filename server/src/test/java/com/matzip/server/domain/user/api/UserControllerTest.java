package com.matzip.server.domain.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.ExpectedStatus;
import com.matzip.server.Parameters;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.auth.dto.LoginDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.MultiValueMap;

import static com.matzip.server.ExpectedStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Value("${admin-password}")
    private String adminPassword;

    @BeforeEach
    void setUp() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            UserDto.SignUpRequest signUpRequest = new UserDto.SignUpRequest("admin", adminPassword);
            User user = new User(signUpRequest, passwordEncoder);
            userRepository.save(user.toAdmin());
        }
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private String signUp(String username, String password, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        UserDto.SignUpRequest signUpRequest = new UserDto.SignUpRequest(username, password);
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        long afterUserCount = userRepository.count();
        if (expectedStatus == OK) {
            resultActions.andExpect(header().exists("Authorization"));
            assertThat(afterUserCount).isEqualTo(beforeUserCount + 1);
            signIn(username, password, OK);
            return resultActions.andReturn().getResponse().getHeader("Authorization");
        } else {
            resultActions.andExpect(header().doesNotExist("Authorization"));
            assertThat(afterUserCount).isEqualTo(beforeUserCount);
            if (expectedStatus == CONFLICT) signIn(username, password, OK);
            else signIn(username, password, UNAUTHORIZED);
            return null;
        }
    }

    private String signIn(String username, String password, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        LoginDto.LoginRequest signUpRequest = new LoginDto.LoginRequest(username, password);
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users/login").contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
        if (expectedStatus == OK) {
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
        mockMvc.perform(get("/api/v1/users/exists").contentType(MediaType.APPLICATION_JSON).param("username", username))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(duplicateResponse)));
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void searchUsersByUsername(
            MultiValueMap<String, String> parameters, String token, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        ResultActions resultActions = mockMvc.perform(get("/api/v1/users/username").header("Authorization", token)
                                                              .params(parameters))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        if (expectedStatus == OK) {
            int pageNumber = 0;
            int pageSize = 15;
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").ascending());
            long count = userRepository.findAllByUsernameContainsIgnoreCaseAndIsNonLockedTrueAndRoleEquals(
                            pageable,
                            parameters.getFirst(
                                    "username"),
                            "NORMAL")
                    .getTotalElements();
            resultActions.andExpect(jsonPath("$.total_elements").value(count));
        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void getUserByUsername(String token, String username, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        mockMvc.perform(get("/api/v1/users/username/" + username).header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    @Test
    void signInTest() throws Exception {
        String token;

        token = signUp("foo", "fooPassword1!", OK);
        assertThat(token).isNotNull();

        token = signIn("foo", "fooPassword1!", OK);
        assertThat(token).isNotNull();

        token = signIn("foo", "fooPassword", UNAUTHORIZED);
        assertThat(token).isNull();

        token = signIn("fo", "fooPassword1!", UNAUTHORIZED);
        assertThat(token).isNull();
    }

    @Test
    void signUpTest() throws Exception {
        String token;

        token = signUp("foo", "fooPassword1!", OK);
        assertThat(token).isNotNull();

        token = signUp("foo2", "maximumLengthOfPasswordIs50Characters!!!!!!!!!!!!!", OK);
        assertThat(token).isNotNull();

        token = signUp("foo", "fooPassword1!", CONFLICT);
        assertThat(token).isNull();

        token = signUp("bar", "short", BAD_REQUEST);
        assertThat(token).isNull();

        token = signUp("bar", "veryVeryLongPasswordThatIsOver50Characters!!!!!!!!!", BAD_REQUEST);
        assertThat(token).isNull();

        token = signUp("bar", "noNumeric!", BAD_REQUEST);
        assertThat(token).isNull();

        token = signUp("bar", "noSpecial1", BAD_REQUEST);
        assertThat(token).isNull();

        token = signUp("bar", "no_upper_case1!", BAD_REQUEST);
        assertThat(token).isNull();

        token = signUp("bar", "NO_LOWER_CASE1!", BAD_REQUEST);
        assertThat(token).isNull();
    }

    @Test
    void checkDuplicateUsernameTest() throws Exception {
        signUp("foo", "fooPassword1!", OK);
        signUp("bar", "barPassword1!", OK);

        checkDuplicateUsername("foo", true);
        checkDuplicateUsername("foo2", false);
        checkDuplicateUsername("bar", true);
        checkDuplicateUsername("ba", false);
    }

    @Test
    void getUserByUsernameTest() throws Exception {
        String token = signUp("foo", "fooPassword1!", OK);
        signUp("bar", "barPassword1!", OK);

        getUserByUsername(token, "foo", OK);
        getUserByUsername(token, "bar", OK);
        getUserByUsername(token, "not_existing_user", NOT_FOUND);
        getUserByUsername(token, "not_found", NOT_FOUND);
    }

    @Test
    void searchUsersByUsernameTest() throws Exception {
        String token = signUp("foo", "fooPassword1!", OK);

        signUp("foo1", "fooPassword1!", OK);
        signUp("foo2", "fooPassword1!", OK);
        signUp("foo3", "fooPassword1!", OK);
        signUp("foo4", "fooPassword1!", OK);
        signUp("foo5", "fooPassword1!", OK);
        signUp("foo6", "fooPassword1!", OK);
        signUp("foo7", "fooPassword1!", OK);
        signUp("foo8", "fooPassword1!", OK);

        Parameters parameters = new Parameters();
        parameters.putParameter("username", "foo");
        searchUsersByUsername(parameters, token, OK);
        parameters.putParameter("username", "bar");
        searchUsersByUsername(parameters, token, OK);
        parameters.putParameter("ascending", "boolean");
        searchUsersByUsername(parameters, token, BAD_REQUEST);
        parameters.putParameter("ascending", "false");
        searchUsersByUsername(parameters, token, OK);
        parameters.putParameter("ascending", "true");
        searchUsersByUsername(parameters, token, OK);
        parameters.putParameter("sortedBy", "username");
        searchUsersByUsername(parameters, token, OK);
        parameters.putParameter("sortedBy", "createdAt");
        searchUsersByUsername(parameters, token, OK);
        parameters.putParameter("sortedBy", "matzipLevel");
        searchUsersByUsername(parameters, token, OK);
        parameters.putParameter("sortedBy", "modifiedAt");
        searchUsersByUsername(parameters, token, BAD_REQUEST);
        parameters.putParameter("sortedBy", "id");
        searchUsersByUsername(parameters, token, BAD_REQUEST);
        parameters.putParameter("sortedBy", "password");
        searchUsersByUsername(parameters, token, BAD_REQUEST);
        parameters.putParameter("sortedBy", "role");
        searchUsersByUsername(parameters, token, BAD_REQUEST);
        parameters.putParameter("sortedBy", "profileString");
        searchUsersByUsername(parameters, token, BAD_REQUEST);
        parameters = new Parameters();
        parameters.putParameter("pageNumber", "-1");
        searchUsersByUsername(parameters, token, BAD_REQUEST);
        parameters = new Parameters();
        parameters.putParameter("pageSize", "0");
        searchUsersByUsername(parameters, token, BAD_REQUEST);
    }
}