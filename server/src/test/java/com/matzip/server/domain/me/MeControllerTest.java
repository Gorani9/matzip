package com.matzip.server.domain.me;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.ExpectedStatus;
import com.matzip.server.Parameters;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.me.repository.FollowRepository;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.auth.dto.LoginDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static com.matzip.server.ExpectedStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Value("${admin-password}")
    private String adminPassword;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        followRepository.deleteAll();
    }

    private String signUp(String username, String password) throws Exception {
        long beforeUserCount = userRepository.count();
        UserDto.SignUpRequest signUpRequest = new UserDto.SignUpRequest(username, password);
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().is(OK.getStatusCode()));
        long afterUserCount = userRepository.count();
        resultActions.andExpect(header().exists("Authorization"));
        assertThat(afterUserCount).isEqualTo(beforeUserCount + 1);
        signIn(username, password, OK);
        return resultActions.andReturn().getResponse().getHeader("Authorization");
    }

    public String signIn(String username, String password, ExpectedStatus expectedStatus) throws Exception {
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

    private void getMe(String token) throws Exception {
        long beforeUserCount = userRepository.count();
        mockMvc.perform(get("/api/v1/me").header("Authorization", token).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void changeUsername(
            String username, String password, String newUsername, ExpectedStatus expectedStatus) throws Exception {
        String token = signIn(username, password, OK);
        long beforeUserCount = userRepository.count();
        MeDto.UsernameChangeRequest usernameChangeRequest = new MeDto.UsernameChangeRequest(newUsername);
        mockMvc.perform(put("/api/v1/me/username").header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(usernameChangeRequest)))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        if (expectedStatus == OK) {
            signIn(username, password, ExpectedStatus.UNAUTHORIZED);
            signIn(newUsername, password, OK);
        } else if (!username.equals(newUsername)) {
            signIn(username, password, OK);
            signIn(newUsername, password, ExpectedStatus.UNAUTHORIZED);
        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void changePassword(
            String username, String oldPassword, String newPassword, ExpectedStatus expectedStatus) throws Exception {
        String token = signIn(username, oldPassword, OK);
        long beforeUserCount = userRepository.count();
        MeDto.PasswordChangeRequest passwordChangeRequest = new MeDto.PasswordChangeRequest(newPassword);
        mockMvc.perform(put("/api/v1/me/password").header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        if (expectedStatus == OK) {
            signIn(username, oldPassword, ExpectedStatus.UNAUTHORIZED);
            signIn(username, newPassword, OK);
        } else {
            signIn(username, oldPassword, OK);
            signIn(username, newPassword, ExpectedStatus.UNAUTHORIZED);
        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void deleteMe(String token, String username, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        mockMvc.perform(delete("/api/v1/me").header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        long afterUserCount = userRepository.count();
        if (expectedStatus == OK) {
            assertTrue(userRepository.findByUsername(username).isEmpty());
            assertThat(afterUserCount).isEqualTo(beforeUserCount - 1);
        } else {
            assertTrue(userRepository.findByUsername(username).isPresent());
            assertThat(afterUserCount).isEqualTo(beforeUserCount);
        }
    }

    private void patchMe(
            String token, String filename, String rawProfileString, ExpectedStatus expectedStatus) throws Exception {
        Optional<String> file = Optional.ofNullable(filename);
        Optional<String> profileString = Optional.ofNullable(rawProfileString);
        long beforeUserCount = userRepository.count();
        MockMultipartHttpServletRequestBuilder builder = multipart(HttpMethod.PATCH, "/api/v1/me");
        User user = userRepository.findByUsername("foo").orElseThrow();
        if (file.isPresent()) {
            try (InputStream inputStream = new FileInputStream(file.get())) {
                File imageFile = new File(file.get());
                String contentType = Files.probeContentType(Path.of(file.get()));
                builder.file(new MockMultipartFile("profileImage", imageFile.getName(), contentType, inputStream));
            } catch (IOException e) {
                System.err.println("Error while putting into multipart " + e.getMessage());
            }
        }
        profileString.ifPresent(s -> builder.file("profileString", s.getBytes()));
        String content = mockMvc.perform(builder.header("Authorization", token)
                                                 .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is(expectedStatus.getStatusCode()))
                .andReturn()
                .getResponse()
                .getContentAsString();
//        if (expectedStatus == OK) {
//            String profileImageUrlFromResponse = JsonPath.read(content, "$.profile_image_url");
//            String profileStringFromResponse = JsonPath.read(content, "$.profile_string");
//            file.ifPresentOrElse(
//                    f -> assertThat(f).isNotEqualTo(profileImageUrlFromResponse),
//                    () -> assertThat(user.getProfileImageUrl()).isEqualTo(profileImageUrlFromResponse));
//            profileString.ifPresentOrElse(
//                    s -> assertThat(s).isEqualTo(profileStringFromResponse),
//                    () -> assertThat(user.getProfileString()).isEqualTo(profileStringFromResponse));
//        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void getMyFollows(
            MultiValueMap<String, String> parameters,
            String token,
            ExpectedStatus expectedStatus,
            Integer expectedCount) throws Exception {
        long beforeUserCount = userRepository.count();
        long beforeFollowCount = followRepository.count();
        ResultActions resultActions = mockMvc.perform(get("/api/v1/me/follows").header("Authorization", token)
                                                              .queryParams(parameters))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        if (expectedStatus == OK) {
            resultActions.andExpect(jsonPath("$.number_of_elements").value(expectedCount));
        }
        long afterUserCount = userRepository.count();
        long afterFollowCount = followRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
        assertThat(afterFollowCount).isEqualTo(beforeFollowCount);
    }

    private void followAnotherUser(
            String token, String username, String followeeUsername,
            ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        mockMvc.perform(put("/api/v1/me/follows/" + followeeUsername)
                                .header("Authorization", token))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        if (expectedStatus == OK) {
            User follower = userRepository.findByUsername(username).orElseThrow();
            User followee = userRepository.findByUsername(followeeUsername).orElseThrow();
            assertTrue(followRepository.existsByFollowerIdAndFolloweeId(follower.getId(), followee.getId()));
        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void unfollowAnotherUser(
            String token, String username, String followeeUsername,
            ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        mockMvc.perform(delete("/api/v1/me/follows/" + followeeUsername)
                                .header("Authorization", token))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        if (expectedStatus == OK) {
            User follower = userRepository.findByUsername(username).orElseThrow();
            User followee = userRepository.findByUsername(followeeUsername).orElseThrow();
            assertFalse(followRepository.existsByFollowerIdAndFolloweeId(follower.getId(), followee.getId()));
        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    @Test
    void getMeTest() throws Exception {
        String token = signUp("foo", "fooPassword1!");
        getMe(token);

        token = signUp("bar", "barPassword1!");
        getMe(token);
    }

    @Test
    void changeUsernameTest() throws Exception {
        signUp("foo", "fooPassword1!");
        signUp("bar", "barPassword1!");

        changeUsername("foo", "fooPassword1!", "", BAD_REQUEST);
        changeUsername("foo", "fooPassword1!", "veryLongUsernameOf_31characters", BAD_REQUEST);
        changeUsername("foo", "fooPassword1!", "foo", CONFLICT);
        changeUsername("foo", "fooPassword1!", "special!!", BAD_REQUEST);
        changeUsername("foo", "fooPassword1!", "-special!@#$", BAD_REQUEST);
        changeUsername("foo", "fooPassword1!", "bar", CONFLICT);

        changeUsername("foo", "fooPassword1!", ".__Upto30chars._______________", OK);
        changeUsername("bar", "barPassword1!", "foo", OK);
    }

    @Test
    void changePasswordTest() throws Exception {
        signUp("foo", "fooPassword1!");
        signUp("bar", "barPassword1!");

        changePassword("foo", "fooPassword1!", "short", BAD_REQUEST);
        changePassword(
                "foo",
                "fooPassword1!",
                "veryVeryLongPasswordThatIsOver50Characters!!!!!!!!!",
                BAD_REQUEST);
        changePassword("foo", "fooPassword1!", "noNumeric!", BAD_REQUEST);
        changePassword("foo", "fooPassword1!", "noSpecial1", BAD_REQUEST);
        changePassword("foo", "fooPassword1!", "no_upper_case1!", BAD_REQUEST);
        changePassword("foo", "fooPassword1!", "NO_LOWER_CASE1!", BAD_REQUEST);

        changePassword("foo", "fooPassword1!", "maximumLengthOfPasswordIs50Characters!!!!!!!!!!!!!", OK);
        changePassword("bar", "barPassword1!", "newPassword1!", OK);
    }

    @Test
    void deleteMeTest() throws Exception {
        String token = signUp("foo", "fooPassword1!");
        String adminToken = signIn("admin", adminPassword, OK);

        deleteMe(token, "foo", OK);
        signIn("foo", "fooPassword1!", ExpectedStatus.UNAUTHORIZED);
        deleteMe(adminToken, "admin", BAD_REQUEST);
        signIn("admin", adminPassword, OK);
    }

    @Test
    void patchMeTest() throws Exception {
        String token = signUp("foo", "fooPassword1!");

        String dir = "src/test/java/com/matzip/server/domain/user/api/";
        String testImage = dir + "test-image.jpeg";
        String profileString = "profile String TEST!!";
        String longProfileString = "profile String that is exactly 50 characters!!!!!!";
        String veryLongProfileString = "profile String that is over 50 characters!!!!!!!!!!";

        patchMe(token, testImage, profileString, OK);
        patchMe(token, null, longProfileString, OK);
        patchMe(token, testImage, null, OK);
        patchMe(token, null, null, OK);
        patchMe(token, null, veryLongProfileString, BAD_REQUEST);
    }

    @Test
    void getMyFollowsTest() throws Exception {
        String foo = signUp("foo", "fooPassword1!");
        Parameters parameters = new Parameters();

        getMyFollows(parameters, foo, OK, 0);
        parameters.putParameter("type", "follow");
        getMyFollows(parameters, foo, BAD_REQUEST, 0);
        parameters.putParameter("type", "follower");
        getMyFollows(parameters, foo, OK, 0);
        parameters.putParameter("type", "following");
        getMyFollows(parameters, foo, OK, 0);
        parameters.putParameter("ascending", "boolean");
        getMyFollows(parameters, foo, BAD_REQUEST, 0);
        parameters.putParameter("ascending", "false");
        getMyFollows(parameters, foo, OK, 0);
        parameters.putParameter("ascending", "true");
        getMyFollows(parameters, foo, OK, 0);
        parameters.putParameter("sortedBy", "username");
        getMyFollows(parameters, foo, OK, 0);
        parameters.putParameter("sortedBy", "createdAt");
        getMyFollows(parameters, foo, OK, 0);
        parameters.putParameter("sortedBy", "matzipLevel");
        getMyFollows(parameters, foo, OK, 0);
        parameters.putParameter("sortedBy", "modifiedAt");
        getMyFollows(parameters, foo, BAD_REQUEST, 0);
        parameters.putParameter("sortedBy", "id");
        getMyFollows(parameters, foo, BAD_REQUEST, 0);
        parameters.putParameter("sortedBy", "password");
        getMyFollows(parameters, foo, BAD_REQUEST, 0);
        parameters.putParameter("sortedBy", "role");
        getMyFollows(parameters, foo, BAD_REQUEST, 0);
        parameters.putParameter("sortedBy", "profileString");
        getMyFollows(parameters, foo, BAD_REQUEST, 0);
    }

    @Test
    void followTest() throws Exception {
        String foo = signUp("foo", "fooPassword1!");
        String bar = signUp("bar", "barPassword1!");
        Parameters follower = new Parameters().putParameter("type", "follower");
        Parameters following = new Parameters().putParameter("type", "following");

        followAnotherUser(foo, "foo", "foo", BAD_REQUEST);
        getMyFollows(following, foo, OK, 0);
        getMyFollows(follower, foo, OK, 0);
        getMyFollows(following, bar, OK, 0);
        getMyFollows(follower, bar, OK, 0);
        followAnotherUser(foo, "foo", "nobody", NOT_FOUND);
        getMyFollows(following, foo, OK, 0);
        getMyFollows(follower, foo, OK, 0);
        getMyFollows(following, bar, OK, 0);
        getMyFollows(follower, bar, OK, 0);
        followAnotherUser(foo, "foo", "bar", OK);
        getMyFollows(following, foo, OK, 1);
        getMyFollows(follower, foo, OK, 0);
        getMyFollows(following, bar, OK, 0);
        getMyFollows(follower, bar, OK, 1);
        followAnotherUser(foo, "foo", "bar", OK);
        getMyFollows(following, foo, OK, 1);
        getMyFollows(follower, foo, OK, 0);
        getMyFollows(following, bar, OK, 0);
        getMyFollows(follower, bar, OK, 1);
        followAnotherUser(bar, "bar", "bar", BAD_REQUEST);
        getMyFollows(following, foo, OK, 1);
        getMyFollows(follower, foo, OK, 0);
        getMyFollows(following, bar, OK, 0);
        getMyFollows(follower, bar, OK, 1);
        followAnotherUser(bar, "bar", "foo", OK);
        getMyFollows(following, foo, OK, 1);
        getMyFollows(follower, foo, OK, 1);
        getMyFollows(following, bar, OK, 1);
        getMyFollows(follower, bar, OK, 1);
        followAnotherUser(bar, "bar", "foo", OK);
        getMyFollows(following, foo, OK, 1);
        getMyFollows(follower, foo, OK, 1);
        getMyFollows(following, bar, OK, 1);
        getMyFollows(follower, bar, OK, 1);
        unfollowAnotherUser(foo, "foo", "foo", OK);
        getMyFollows(following, foo, OK, 1);
        getMyFollows(follower, foo, OK, 1);
        getMyFollows(following, bar, OK, 1);
        getMyFollows(follower, bar, OK, 1);
        unfollowAnotherUser(foo, "foo", "nobody", NOT_FOUND);
        getMyFollows(following, foo, OK, 1);
        getMyFollows(follower, foo, OK, 1);
        getMyFollows(following, bar, OK, 1);
        getMyFollows(follower, bar, OK, 1);
        unfollowAnotherUser(foo, "foo", "bar", OK);
        getMyFollows(following, foo, OK, 0);
        getMyFollows(follower, foo, OK, 1);
        getMyFollows(following, bar, OK, 1);
        getMyFollows(follower, bar, OK, 0);
        unfollowAnotherUser(foo, "foo", "bar", OK);
        getMyFollows(following, foo, OK, 0);
        getMyFollows(follower, foo, OK, 1);
        getMyFollows(following, bar, OK, 1);
        getMyFollows(follower, bar, OK, 0);
        unfollowAnotherUser(bar, "bar", "bar", OK);
        getMyFollows(following, foo, OK, 0);
        getMyFollows(follower, foo, OK, 1);
        getMyFollows(following, bar, OK, 1);
        getMyFollows(follower, bar, OK, 0);
        unfollowAnotherUser(bar, "bar", "foo", OK);
        getMyFollows(following, foo, OK, 0);
        getMyFollows(follower, foo, OK, 0);
        getMyFollows(following, bar, OK, 0);
        getMyFollows(follower, bar, OK, 0);
        unfollowAnotherUser(bar, "bar", "foo", OK);
        getMyFollows(following, foo, OK, 0);
        getMyFollows(follower, foo, OK, 0);
        getMyFollows(following, bar, OK, 0);
        getMyFollows(follower, bar, OK, 0);
    }
}