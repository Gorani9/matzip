package com.matzip.server.domain.admin.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.ExpectedStatus;
import com.matzip.server.Parameters;
import com.matzip.server.domain.admin.dto.AdminDto;
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

import java.util.List;
import java.util.Objects;

import static com.matzip.server.ExpectedStatus.BAD_REQUEST;
import static com.matzip.server.ExpectedStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AdminUserControllerTest {
    private final int pageSize = 15;
    private final int pageNumber = 0;
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

    private String signUp(String username, String password) throws Exception {
        long beforeUserCount = userRepository.count();
        UserDto.SignUpRequest signUpRequest = new UserDto.SignUpRequest(username, password);
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().is(ExpectedStatus.OK.getStatusCode()));
        long afterUserCount = userRepository.count();
        resultActions.andExpect(header().exists("Authorization"));
        assertThat(afterUserCount).isEqualTo(beforeUserCount + 1);
        signIn(username, password, "NORMAL", ExpectedStatus.OK);
        return resultActions.andReturn().getResponse().getHeader("Authorization");
    }

    private String signIn(
            String username, String password, String expectedRole, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        LoginDto.LoginRequest signUpRequest = new LoginDto.LoginRequest(username, password);
        ResultActions resultActions = mockMvc.perform(
                        post("/api/v1/users/login").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
        if (expectedStatus == ExpectedStatus.OK) {
            resultActions.andExpect(header().exists("Authorization"))
                    .andExpect(content().string(containsString(expectedRole)));
            return resultActions.andReturn().getResponse().getHeader("Authorization");
        } else {
            resultActions.andExpect(header().doesNotExist("Authorization"));
            return null;
        }
    }

    private void getUsers(
            MultiValueMap<String, String> parameters, String token, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/admin/users").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .params(parameters)).andExpect(status().is(expectedStatus.getStatusCode()));
        if (expectedStatus == ExpectedStatus.OK) {
            if (parameters.getOrDefault("withAdmin", List.of()).contains("true")) {
                resultActions.andExpect(jsonPath("$.total_elements").value(beforeUserCount));
            } else {
                Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").ascending());
                long count = userRepository.findAllByRoleEquals(pageable, "NORMAL").getTotalElements();
                resultActions.andExpect(jsonPath("$.total_elements").value(count));
            }
        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void searchUsersByUsername(
            MultiValueMap<String, String> parameters, String token, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        ResultActions resultActions = mockMvc.perform(get("/api/v1/admin/users/username").header("Authorization", token)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .params(parameters))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").ascending());
        if (expectedStatus == ExpectedStatus.OK) {
            long count;
            if (!parameters.containsKey("isNonLocked")) {
                count = userRepository.findAllByUsernameContainsIgnoreCase(pageable, parameters.getFirst("username"))
                        .getTotalElements();
            } else if (Objects.equals(parameters.getFirst("isNonLocked"), "true")) {
                count = userRepository.findAllByUsernameContainsIgnoreCaseAndIsNonLockedTrueAndRoleEquals(
                                pageable,
                                parameters.getFirst(
                                        "username"),
                                "NORMAL")
                        .getTotalElements();
            } else {
                count = userRepository.findAllByUsernameContainsIgnoreCaseAndIsNonLockedFalseAndRoleEquals(
                                pageable,
                                parameters.getFirst(
                                        "username"),
                                "NORMAL")
                        .getTotalElements();
            }
            resultActions.andExpect(jsonPath("$.total_elements").value(count));
        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void getUserById(String token, Long id, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        ResultActions resultActions = mockMvc.perform(
                        get("/api/v1/admin/users/" + id).header("Authorization", token).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        if (expectedStatus == ExpectedStatus.OK) {
            AdminDto.UserResponse userResponse = new AdminDto.UserResponse(userRepository.findById(id).orElseThrow());
            resultActions.andExpect(content().string(objectMapper.writeValueAsString(userResponse)));
        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void lockUser(String token, Long id, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        User beforeUser = userRepository.findById(id).orElse(null);
        mockMvc.perform(post("/api/v1/admin/users/" + id + "/lock").header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        if (beforeUser == null) return;
        User afterUser = userRepository.findById(id).orElseThrow();
        if (expectedStatus == ExpectedStatus.OK) {
            assertFalse(afterUser.getIsNonLocked());
        } else {
            assertThat(beforeUser.getRole()).isEqualTo(afterUser.getRole());
        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void unlockUser(String token, Long id, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        User beforeUser = userRepository.findById(id).orElse(null);
        mockMvc.perform(delete("/api/v1/admin/users/" + id + "/lock").header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        if (beforeUser == null) return;
        User afterUser = userRepository.findById(id).orElseThrow();
        if (expectedStatus == ExpectedStatus.OK) {
            assertTrue(afterUser.getIsNonLocked());
        } else {
            assertThat(beforeUser.getRole()).isEqualTo(afterUser.getRole());
        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void deleteUser(String token, Long id, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        mockMvc.perform(delete("/api/v1/admin/users/" + id).header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        long afterUserCount = userRepository.count();
        if (expectedStatus == ExpectedStatus.OK) {
            assertTrue(userRepository.findById(id).isEmpty());
            assertThat(afterUserCount).isEqualTo(beforeUserCount - 1);
        } else if (expectedStatus != ExpectedStatus.FORBIDDEN) {
            if (expectedStatus == ExpectedStatus.NOT_FOUND) assertTrue(userRepository.findById(id).isEmpty());
            else assertTrue(userRepository.findById(id).isPresent());
            assertThat(afterUserCount).isEqualTo(beforeUserCount);
        }
    }

    @Test
    void accessAdminApiTest() throws Exception {
        String normalToken = signUp("foo", "fooPassword1!");

        getUsers(new Parameters(), normalToken, ExpectedStatus.FORBIDDEN);

        getUserById(normalToken, 0L, ExpectedStatus.FORBIDDEN);
        getUserById(normalToken, 1L, ExpectedStatus.FORBIDDEN);
        getUserById(normalToken, 2L, ExpectedStatus.FORBIDDEN);
        getUserById(normalToken, 100L, ExpectedStatus.FORBIDDEN);

        lockUser(normalToken, 0L, ExpectedStatus.FORBIDDEN);
        unlockUser(normalToken, 0L, ExpectedStatus.FORBIDDEN);

        deleteUser(normalToken, 0L, ExpectedStatus.FORBIDDEN);
        deleteUser(normalToken, 1L, ExpectedStatus.FORBIDDEN);
        deleteUser(normalToken, 2L, ExpectedStatus.FORBIDDEN);
    }

    @Test
    void getUsersTest() throws Exception {
        String adminToken = signIn("admin", adminPassword, "ADMIN", ExpectedStatus.OK);

        signUp("foo1", "fooPassword1!");
        signUp("foo2", "fooPassword1!");
        signUp("foo3", "fooPassword1!");
        signUp("foo4", "fooPassword1!");
        signUp("foo5", "fooPassword1!");
        signUp("foo6", "fooPassword1!");

        Parameters parameters = new Parameters();
        getUsers(parameters, adminToken, ExpectedStatus.OK);
        parameters.putParameter("withAdmin", "false");
        getUsers(parameters, adminToken, ExpectedStatus.OK);
        parameters.putParameter("withAdmin", "true");
        getUsers(parameters, adminToken, ExpectedStatus.OK);
        parameters.putParameter("ascending", "boolean");
        getUsers(parameters, adminToken, BAD_REQUEST);
        parameters.putParameter("ascending", "false");
        getUsers(parameters, adminToken, OK);
        parameters.putParameter("ascending", "true");
        getUsers(parameters, adminToken, OK);
        parameters.putParameter("sortedBy", "username");
        getUsers(parameters, adminToken, OK);
        parameters.putParameter("sortedBy", "createdAt");
        getUsers(parameters, adminToken, OK);
        parameters.putParameter("sortedBy", "matzipLevel");
        getUsers(parameters, adminToken, OK);
        parameters.putParameter("sortedBy", "modifiedAt");
        getUsers(parameters, adminToken, BAD_REQUEST);
        parameters.putParameter("sortedBy", "id");
        getUsers(parameters, adminToken, BAD_REQUEST);
        parameters.putParameter("sortedBy", "password");
        getUsers(parameters, adminToken, BAD_REQUEST);
        parameters.putParameter("sortedBy", "role");
        getUsers(parameters, adminToken, BAD_REQUEST);
        parameters.putParameter("sortedBy", "profileString");
        getUsers(parameters, adminToken, BAD_REQUEST);
        parameters = new Parameters();
        parameters.putParameter("withAdmin", "bool");
        getUsers(parameters, adminToken, ExpectedStatus.BAD_REQUEST);
        parameters = new Parameters();
        parameters.putParameter("pageSize", "0");
        getUsers(parameters, adminToken, ExpectedStatus.BAD_REQUEST);
    }

    @Test
    void searchUsersByUsernameTest() throws Exception {
        String adminToken = signIn("admin", adminPassword, "ADMIN", ExpectedStatus.OK);

        signUp("foo1", "fooPassword1!");
        signUp("foo2", "fooPassword1!");
        signUp("foo3", "fooPassword1!");
        signUp("foo4", "fooPassword1!");
        signUp("foo5", "fooPassword1!");
        signUp("foo6", "fooPassword1!");
        signUp("foo7", "fooPassword1!");
        signUp("foo8", "fooPassword1!");

        Parameters parameters = new Parameters();
        parameters.putParameter("username", "foo");
        searchUsersByUsername(parameters, adminToken, ExpectedStatus.OK);
        parameters.putParameter("username", "true");
        searchUsersByUsername(parameters, adminToken, ExpectedStatus.OK);
        parameters.putParameter("username", "false");
        searchUsersByUsername(parameters, adminToken, ExpectedStatus.OK);
        parameters.putParameter("username", "bar");
        searchUsersByUsername(parameters, adminToken, ExpectedStatus.OK);
        parameters.putParameter("ascending", "boolean");
        searchUsersByUsername(parameters, adminToken, BAD_REQUEST);
        parameters.putParameter("ascending", "false");
        searchUsersByUsername(parameters, adminToken, OK);
        parameters.putParameter("ascending", "true");
        searchUsersByUsername(parameters, adminToken, OK);
        parameters.putParameter("sortedBy", "username");
        searchUsersByUsername(parameters, adminToken, OK);
        parameters.putParameter("sortedBy", "createdAt");
        searchUsersByUsername(parameters, adminToken, OK);
        parameters.putParameter("sortedBy", "matzipLevel");
        searchUsersByUsername(parameters, adminToken, OK);
        parameters.putParameter("sortedBy", "modifiedAt");
        searchUsersByUsername(parameters, adminToken, BAD_REQUEST);
        parameters.putParameter("sortedBy", "id");
        searchUsersByUsername(parameters, adminToken, BAD_REQUEST);
        parameters.putParameter("sortedBy", "password");
        searchUsersByUsername(parameters, adminToken, BAD_REQUEST);
        parameters.putParameter("sortedBy", "role");
        searchUsersByUsername(parameters, adminToken, BAD_REQUEST);
        parameters.putParameter("sortedBy", "profileString");
        searchUsersByUsername(parameters, adminToken, BAD_REQUEST);
        parameters = new Parameters();
        parameters.putParameter("pageNumber", "-1");
        searchUsersByUsername(parameters, adminToken, ExpectedStatus.BAD_REQUEST);
        parameters = new Parameters();
        parameters.putParameter("username", "foo");
        parameters.putParameter("pageSize", "0");
        searchUsersByUsername(parameters, adminToken, ExpectedStatus.BAD_REQUEST);
        parameters = new Parameters();
        searchUsersByUsername(parameters, adminToken, ExpectedStatus.BAD_REQUEST);
        parameters = new Parameters();
        parameters.putParameter("username", "foo");
        parameters.putParameter("isNonLocked", "bool");
        searchUsersByUsername(parameters, adminToken, ExpectedStatus.BAD_REQUEST);
    }

    @Test
    void getUserByIdTest() throws Exception {
        String adminToken = signIn("admin", adminPassword, "ADMIN", ExpectedStatus.OK);

        signUp("foo", "fooPassword1!");
        Long fooId = userRepository.findByUsername("foo").orElseThrow().getId();

        getUserById(adminToken, fooId, ExpectedStatus.OK);
        getUserById(adminToken, -1L, ExpectedStatus.BAD_REQUEST);
        getUserById(adminToken, fooId + 100, ExpectedStatus.NOT_FOUND);
    }

    @Test
    void changeUserLockStatusTest() throws Exception {
        String adminToken = signIn("admin", adminPassword, "ADMIN", ExpectedStatus.OK);

        signUp("foo", "fooPassword1!");
        Long fooId = userRepository.findByUsername("foo").orElseThrow().getId();

        lockUser(adminToken, fooId, ExpectedStatus.OK);
        signIn("foo", "fooPassword1!", "NORMAL", ExpectedStatus.UNAUTHORIZED);
        unlockUser(adminToken, fooId, ExpectedStatus.OK);
        signIn("foo", "fooPassword1!", "NORMAL", ExpectedStatus.OK);
        unlockUser(adminToken, fooId, ExpectedStatus.OK);
        signIn("foo", "fooPassword1!", "NORMAL", ExpectedStatus.OK);
        lockUser(adminToken, fooId, ExpectedStatus.OK);
        signIn("foo", "fooPassword1!", "NORMAL", ExpectedStatus.UNAUTHORIZED);
        lockUser(adminToken, fooId + 100, ExpectedStatus.NOT_FOUND);
        signIn("foo", "fooPassword1!", "NORMAL", ExpectedStatus.UNAUTHORIZED);
        unlockUser(adminToken, fooId + 100, ExpectedStatus.NOT_FOUND);
        signIn("foo", "fooPassword1!", "NORMAL", ExpectedStatus.UNAUTHORIZED);
        unlockUser(adminToken, fooId + 100, ExpectedStatus.NOT_FOUND);
        signIn("foo", "fooPassword1!", "NORMAL", ExpectedStatus.UNAUTHORIZED);
        lockUser(adminToken, fooId + 100, ExpectedStatus.NOT_FOUND);
        signIn("foo", "fooPassword1!", "NORMAL", ExpectedStatus.UNAUTHORIZED);

        Long adminId = userRepository.findByUsername("admin").orElseThrow().getId();
        lockUser(adminToken, adminId, ExpectedStatus.BAD_REQUEST);
        signIn("admin", adminPassword, "ADMIN", ExpectedStatus.OK);
    }

    @Test
    void deleteUserTest() throws Exception {
        String adminToken = signIn("admin", adminPassword, "ADMIN", ExpectedStatus.OK);

        signUp("bar", "barPassword1!");
        Long barId = userRepository.findByUsername("bar").orElseThrow().getId();

        deleteUser(adminToken, barId, ExpectedStatus.OK);
        signIn("bar", "barPassword1!", "NORMAL", ExpectedStatus.UNAUTHORIZED);
        deleteUser(adminToken, barId, ExpectedStatus.NOT_FOUND);

        Long adminId = userRepository.findByUsername("admin").orElseThrow().getId();
        deleteUser(adminToken, adminId, ExpectedStatus.BAD_REQUEST);
        signIn("admin", adminPassword, "ADMIN", ExpectedStatus.OK);
    }


}