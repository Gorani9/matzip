package com.matzip.server.domain.image.api;

import com.amazonaws.services.s3.AmazonS3Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.ExpectedStatus;
import com.matzip.server.domain.image.dto.ImageDto;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.repository.UserRepository;
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
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ImageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AmazonS3Client amazonS3Client;

    @Autowired
    private UserRepository userRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private String signUp() throws Exception {
        UserDto.SignUpRequest signUpRequest = new UserDto.SignUpRequest("foo", "fooPassword1!");
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users")
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().is(ExpectedStatus.OK.getStatusCode()));
        resultActions.andExpect(header().exists("Authorization"));
        return resultActions.andReturn().getResponse().getHeader("Authorization");
    }

    private String getKeyFromUrl(String url) {
        int slashIndex = url.lastIndexOf('/');
        return url.substring(slashIndex + 1);
    }

    private boolean existsInS3(String url) {
        return amazonS3Client.doesObjectExist(bucketName, getKeyFromUrl(url));
    }

    private List<String> uploadImages(String token, List<String> files) throws Exception {
        MockMultipartHttpServletRequestBuilder builder = multipart(HttpMethod.POST, "/api/v1/images");
        for (String file : files) {
            try (InputStream inputStream = new FileInputStream(file)) {
                File imageFile = new File(file);
                String contentType = Files.probeContentType(Path.of(file));
                builder.file(new MockMultipartFile("images", imageFile.getName(), contentType, inputStream));
            } catch (IOException e) {
                System.err.println("Error while putting into multipart " + e.getMessage());
            }
        }
        String responseString = mockMvc.perform(builder
                                                        .header("Authorization", token)
                                                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is(ExpectedStatus.OK.getStatusCode()))
                .andReturn().getResponse().getContentAsString();
        ImageDto.Response response = objectMapper.readValue(responseString, ImageDto.Response.class);
        for (String url : response.getUrls()) {
            assertTrue(existsInS3(url));
        }
        return response.getUrls();
    }

    private void deleteImages(String token, List<String> urls) throws Exception {
        ImageDto.DeleteRequest deleteRequest = new ImageDto.DeleteRequest(urls);
        mockMvc.perform(delete("/api/v1/images")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isOk());
        for (String url : urls) {
            assertFalse(existsInS3(url));
        }
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void imageTest() throws Exception {
        String token = signUp();

        List<String> stringList = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            String dir = "src/test/java/com/matzip/server/domain/image/api/";
            String testImage = dir + "test-image.jpeg";
            stringList.add(testImage);
        }

        List<String> urls = uploadImages(token, stringList);
        deleteImages(token, urls);
    }
}