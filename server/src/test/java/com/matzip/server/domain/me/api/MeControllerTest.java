package com.matzip.server.domain.me.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.matzip.server.ExpectedStatus;
import com.matzip.server.domain.me.dto.MeDto;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        signIn(username, password, ExpectedStatus.OK);
        return resultActions.andReturn().getResponse().getHeader("Authorization");
    }

    private String signIn(String username, String password, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        LoginDto.LoginRequest signUpRequest = new LoginDto.LoginRequest(username, password);
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users/login").contentType(MediaType.APPLICATION_JSON)
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

    private void getMe(String token, String username) throws Exception {
        long beforeUserCount = userRepository.count();
        MeDto.Response response = new MeDto.Response(userRepository.findByUsername(username).orElseThrow());
        mockMvc.perform(get("/api/v1/me").header("Authorization", token).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    private void changePassword(
            String username, String oldPassword, String newPassword, ExpectedStatus expectedStatus) throws Exception {
        String token = signIn(username, oldPassword, ExpectedStatus.OK);
        long beforeUserCount = userRepository.count();
        MeDto.PasswordChangeRequest passwordChangeRequest = new MeDto.PasswordChangeRequest(newPassword);
        mockMvc.perform(put("/api/v1/me/password").header("Authorization", token)
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

    private void deleteMe(String token, String username, ExpectedStatus expectedStatus) throws Exception {
        long beforeUserCount = userRepository.count();
        mockMvc.perform(delete("/api/v1/me").header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        long afterUserCount = userRepository.count();
        if (expectedStatus == ExpectedStatus.OK) {
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
        MeDto.Response responseBeforePatch = new MeDto.Response(userRepository.findByUsername("foo").orElseThrow());
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
        if (expectedStatus == ExpectedStatus.OK) {
            String profileImageUrlFromResponse = JsonPath.read(content, "$.profile_image_url");
            String profileStringFromResponse = JsonPath.read(content, "$.profile_string");
            file.ifPresentOrElse(
                    f -> assertThat(f).isNotEqualTo(profileImageUrlFromResponse),
                    () -> assertThat(responseBeforePatch.getProfileImageUrl()).isEqualTo(
                            profileImageUrlFromResponse));
            profileString.ifPresentOrElse(
                    s -> assertThat(s).isEqualTo(profileStringFromResponse),
                    () -> assertThat(responseBeforePatch.getProfileString()).isEqualTo(
                            profileStringFromResponse));
        }
        long afterUserCount = userRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
    }

    @Test
    void getMeTest() throws Exception {
        String token = signUp("foo", "fooPassword1!");
        getMe(token, "foo");

        token = signUp("bar", "barPassword1!");
        getMe(token, "bar");
    }

    @Test
    void changePasswordTest() throws Exception {
        signUp("foo", "fooPassword1!");
        signUp("bar", "barPassword1!");

        changePassword("foo", "fooPassword1!", "short", ExpectedStatus.BAD_REQUEST);
        changePassword(
                "foo",
                "fooPassword1!",
                "veryVeryLongPasswordThatIsOver50Characters!!!!!!!!!",
                ExpectedStatus.BAD_REQUEST);
        changePassword("foo", "fooPassword1!", "noNumeric!", ExpectedStatus.BAD_REQUEST);
        changePassword("foo", "fooPassword1!", "noSpecial1", ExpectedStatus.BAD_REQUEST);
        changePassword("foo", "fooPassword1!", "no_upper_case1!", ExpectedStatus.BAD_REQUEST);
        changePassword("foo", "fooPassword1!", "NO_LOWER_CASE1!", ExpectedStatus.BAD_REQUEST);

        changePassword("foo", "fooPassword1!", "maximumLengthOfPasswordIs50Characters!!!!!!!!!!!!!", ExpectedStatus.OK);
        changePassword("bar", "barPassword1!", "newPassword1!", ExpectedStatus.OK);
    }

    @Test
    void deleteMeTest() throws Exception {
        String token = signUp("foo", "fooPassword1!");
        String adminToken = signIn("admin", adminPassword, ExpectedStatus.OK);

        deleteMe(token, "foo", ExpectedStatus.OK);
        signIn("foo", "fooPassword1!", ExpectedStatus.UNAUTHORIZED);
        deleteMe(adminToken, "admin", ExpectedStatus.BAD_REQUEST);
        signIn("admin", adminPassword, ExpectedStatus.OK);
    }

    @Test
    void patchMeTest() throws Exception {
        String token = signUp("foo", "fooPassword1!");

        String dir = "src/test/java/com/matzip/server/domain/user/api/";
        String testImage = dir + "test-image.jpeg";
        String profileString = "profile String TEST!!";
        String longProfileString = "profile String that is exactly 50 characters!!!!!!";
        String veryLongProfileString = "profile String that is over 50 characters!!!!!!!!!!";

        patchMe(token, testImage, profileString, ExpectedStatus.OK);
        patchMe(token, null, longProfileString, ExpectedStatus.OK);
        patchMe(token, testImage, null, ExpectedStatus.OK);
        patchMe(token, null, null, ExpectedStatus.OK);
        patchMe(token, null, veryLongProfileString, ExpectedStatus.BAD_REQUEST);
    }
}