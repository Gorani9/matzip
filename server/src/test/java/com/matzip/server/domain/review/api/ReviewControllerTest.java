package com.matzip.server.domain.review.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.ExpectedStatus;
import com.matzip.server.Parameters;
import com.matzip.server.domain.review.repository.ReviewRepository;
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
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static com.matzip.server.ExpectedStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;
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
        reviewRepository.deleteAll();
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

    private void putImageToBuilder(MockMultipartHttpServletRequestBuilder builder, String file) {
        if (Optional.ofNullable(file).isEmpty()) return;
        try (InputStream inputStream = new FileInputStream(file)) {
            File imageFile = new File(file);
            String contentType = Files.probeContentType(Path.of(file));
            builder.file(new MockMultipartFile("images", imageFile.getName(), contentType, inputStream));
        } catch (IOException e) {
            System.err.println("Error while putting into multipart " + e.getMessage());
        }
    }

    private void searchReviews(
            MultiValueMap<String, String> parameters,
            String token,
            ExpectedStatus expectedStatus,
            Integer expectedCount) throws Exception {
        long beforeUserCount = userRepository.count();
        long beforeReviewCount = reviewRepository.count();
        ResultActions resultActions = mockMvc.perform(get("/api/v1/me/follows").header("Authorization", token)
                                                              .params(parameters))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        if (expectedStatus == OK) {
            resultActions.andExpect(jsonPath("$.number_of_elements").value(expectedCount));
        }
        long afterUserCount = userRepository.count();
        long afterReviewCount = reviewRepository.count();
        assertThat(afterUserCount).isEqualTo(beforeUserCount);
        assertThat(afterReviewCount).isEqualTo(beforeReviewCount);
    }

    private void postReviews(
            String token, String filename, MultiValueMap<String, String> parameters,
            ExpectedStatus expectedStatus) throws Exception {
        long beforeReviewCount = reviewRepository.count();
        MockMultipartHttpServletRequestBuilder builder = multipart(HttpMethod.POST, "/api/v1/reviews");
        putImageToBuilder(builder, filename);
        mockMvc.perform(builder.header("Authorization", token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(parameters)).andExpect(status().is(expectedStatus.getStatusCode()));
        long afterReviewCount = reviewRepository.count();
        if (expectedStatus == OK)
            assertThat(afterReviewCount).isEqualTo(beforeReviewCount + 1);
        else
            assertThat(afterReviewCount).isEqualTo(beforeReviewCount);
    }

    private void getReview(String token, Long id, ExpectedStatus expectedStatus) throws Exception {
        long beforeReviewCount = reviewRepository.count();
        mockMvc.perform(get("api/v1/reviews/" + id).header("Authorization", token))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        long afterReviewCount = reviewRepository.count();
        assertThat(afterReviewCount).isEqualTo(beforeReviewCount);
    }

    private void patchReviews(
            String token, String filename, MultiValueMap<String, String> parameters, Long id,
            ExpectedStatus expectedStatus) throws Exception {
        long beforeReviewCount = reviewRepository.count();
        MockMultipartHttpServletRequestBuilder builder = multipart(HttpMethod.PATCH, "/api/v1/reviews/" + id);
        putImageToBuilder(builder, filename);
        mockMvc.perform(builder.header("Authorization", token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(parameters)).andExpect(status().is(expectedStatus.getStatusCode()));
        long afterReviewCount = reviewRepository.count();
        assertThat(afterReviewCount).isEqualTo(beforeReviewCount);
    }

    private void deleteReview(String token, Long id, ExpectedStatus expectedStatus) throws Exception {
        long beforeReviewCount = reviewRepository.count();
        mockMvc.perform(delete("api/v1/reviews/" + id).header("Authorization", token))
                .andExpect(status().is(expectedStatus.getStatusCode()));
        long afterReviewCount = reviewRepository.count();
        assertThat(afterReviewCount).isEqualTo(beforeReviewCount - 1);
    }

    @Test
    void searchReviewsTest() throws Exception {
        String foo = signUp("foo", "fooPassword1!");
        Parameters parameters = new Parameters();
    }
}