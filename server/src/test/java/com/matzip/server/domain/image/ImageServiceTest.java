package com.matzip.server.domain.image;

import com.amazonaws.services.s3.AmazonS3Client;
import com.matzip.server.domain.image.exception.DeleteReviewLastImageException;
import com.matzip.server.domain.image.exception.OverloadReviewImagesException;
import com.matzip.server.domain.image.model.ReviewImage;
import com.matzip.server.domain.image.repository.ReviewImageRepository;
import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.review.dto.ReviewDto.PostRequest;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Tag("ServiceTest")
public class ImageServiceTest {
    @InjectMocks
    private ImageService imageService;
    @Mock
    private AmazonS3Client amazonS3Client;
    @Mock
    private ReviewImageRepository reviewImageRepository;

    private URL url1;
    private URL url2;
    private MultipartFile dummy;

    @BeforeEach
    void setUp() {
        try {
            url1 = new URL("http://sample_image_url1");
            url2 = new URL("http://sample_image_url2");
        } catch (MalformedURLException ignored) {}
        dummy = new MockMultipartFile("dummy.png", "dummy.png", "image/png", new byte[0]);
    }

    @Test
    @DisplayName("리뷰 이미지 업로드 테스트 성공")
    void uploadReviewImagesTest_Success() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new PostRequest("content", List.of(), 9, "location"));

        // when
        List<MultipartFile> images = List.of(dummy, dummy);
        when(amazonS3Client.getUrl(any(), any())).thenReturn(url1);

        // then
        assertDoesNotThrow(() -> imageService.uploadReviewImages(user, review, images));
    }

    @Test
    @DisplayName("리뷰 이미지 업로드 테스트 실패: 새로운 이미지 업로드 10개 초과")
    void uploadReviewImagesTest_OverloadingImagesNew() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new PostRequest("content", List.of(), 9, "location"));

        // when
        List<MultipartFile> images = List.of(dummy, dummy, dummy, dummy, dummy, dummy, dummy, dummy, dummy, dummy, dummy);

        // then
        assertThrows(OverloadReviewImagesException.class, () -> imageService.uploadReviewImages(user, review, images));
    }

    @Test
    @DisplayName("리뷰 이미지 업로드 테스트 실패: 총 이미지 업로드 10개 초과")
    void uploadReviewImagesTest_OverloadingImagesTotal() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new PostRequest("content", List.of(), 9, "location"));
        for (int i = 0; i < 8; i++) new ReviewImage(review, url1.toString());

        // when
        List<MultipartFile> images = List.of(dummy, dummy, dummy);

        // then
        assertThrows(OverloadReviewImagesException.class, () -> imageService.uploadReviewImages(user, review, images));
    }

    @Test
    @DisplayName("리뷰 이미지 삭제 테스트 성공")
    void deleteReviewImagesTest_Success() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new PostRequest("content", List.of(), 9, "location"));
        new ReviewImage(review, url1.toString());
        new ReviewImage(review, url2.toString());

        // when
        List<String> urls = List.of(url1.toString());

        // then
        assertDoesNotThrow(() -> imageService.deleteReviewImages(review, urls));
    }

    @Test
    @DisplayName("리뷰 이미지 삭제 테스트 실패: 모든 이미지 삭제")
    void deleteReviewImagesTest_NoReviewImage() {
        // given
        User user = new User("user", "password");
        Review review = new Review(user, new PostRequest("content", List.of(), 9, "location"));
        new ReviewImage(review, url1.toString());

        // when
        List<String> urls = List.of(url1.toString());

        // then
        assertThrows(DeleteReviewLastImageException.class, () -> imageService.deleteReviewImages(review, urls));
    }
}
