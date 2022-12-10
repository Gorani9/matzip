package com.matzip.server.domain.review;

import com.matzip.server.Parameters;
import com.matzip.server.domain.review.api.ReviewController;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.service.ReviewService;
import com.matzip.server.domain.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.util.List;

import static com.matzip.server.ApiDocumentUtils.getDocumentRequest;
import static com.matzip.server.ApiDocumentUtils.getDocumentResponse;
import static com.matzip.server.domain.DocumentFields.*;
import static com.matzip.server.domain.review.CommentDocumentTest.getCommentResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ReviewController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@AutoConfigureRestDocs
@Tag("DocumentTest")
public class ReviewDocumentTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ReviewService reviewService;

    private User user;
    private Review review;

    @BeforeEach
    void setUp() {
        user = new User("foo", "password");
        review =  new Review(user, new ReviewDto.PostRequest("sample_review", null, 10, "location"));
        new Comment(user, review, "sample_comment");
    }
    @Test
    public void searchReviews() throws Exception {
        given(reviewService.searchReviews(any(), any())).willReturn(new SliceImpl<>(
                List.of(ReviewDto.Response.of(review, user)), PageRequest.of(0, 20), true));

        Parameters parameters = new Parameters(0, 20);
        parameters.putParameter("keyword", "sample");
        mockMvc.perform(get("/api/v1/reviews").queryParams(parameters))
                .andExpect(status().isOk())
                .andDo(document("review-search",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestParameters(parameterWithName("keyword").description("검색할 리뷰 내용").optional())
                                        .and(getPageRequestParameters()),
                                responseFields(getPageResponseFields())
                                        .andWithPrefix("content[].", getNormalResponseField())
                                        .andWithPrefix("content[].", getReviewResponseFields())
                                        .andWithPrefix("content[].user.", getNormalResponseField())
                                        .andWithPrefix("content[].user.", getUserResponseFields())
                ));
    }

    @Test
    public void getReview() throws Exception {
        given(reviewService.fetchReview(any(), any())).willReturn(new ReviewDto.DetailedResponse(review, user));

        mockMvc.perform(get("/api/v1/reviews/{review-id}", "1"))
                .andExpect(status().isOk())
                .andDo(document("review-fetch",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(
                                        parameterWithName("review-id").description("선택할 리뷰 아이디")),
                                responseFields(getNormalResponseField())
                                        .and(getReviewResponseFields())
                                        .and(getReviewDetailedResponseFields())
                                        .andWithPrefix("user.", getNormalResponseField())
                                        .andWithPrefix("user.", getUserResponseFields())
                                        .andWithPrefix("comments[].", getCommentResponseFields())
                                        .andWithPrefix("comments[].user.", getNormalResponseField())
                                        .andWithPrefix("comments[].user.", getUserResponseFields())
                ));
    }

    @Test
    public void postReview() throws Exception {
        given(reviewService.postReview(any(), any())).willReturn(new ReviewDto.DetailedResponse(review, user));

        mockMvc.perform(multipart("/api/v1/reviews")
                                .file(new MockMultipartFile("images", "image_name", "", InputStream.nullInputStream()))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .param("content", "sample_review")
                                .param("rating", "10")
                                .param("location", "location"))
                .andExpect(status().isOk())
                .andDo(document("review-post",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestParts(partWithName("images").description("리뷰 이미지")),
                                requestParameters(
                                        parameterWithName("content").description("리뷰 내용"),
                                        parameterWithName("rating").description("별점"),
                                        parameterWithName("location").description("위치")),
                                responseFields(getNormalResponseField())
                                        .and(getReviewResponseFields())
                                        .and(getReviewDetailedResponseFields())
                                        .andWithPrefix("user.", getNormalResponseField())
                                        .andWithPrefix("user.", getUserResponseFields())
                                        .andWithPrefix("comments[].", getCommentResponseFields())
                                        .andWithPrefix("comments[].user.", getNormalResponseField())
                                        .andWithPrefix("comments[].user.", getUserResponseFields())
                ));
    }

    @Test
    public void patchReview() throws Exception {
        given(reviewService.patchReview(any(), any(), any())).willReturn(new ReviewDto.DetailedResponse(review, user));

        mockMvc.perform(multipart("/api/v1/reviews/{review-id}", "1")
                                .file(new MockMultipartFile("new_images", "image_name", "", InputStream.nullInputStream()))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .param("content", "sample_content")
                                .param("rating", "10")
                                .param("old_urls", "image1", "image2")
                                .with(request -> {
                                    request.setMethod("PATCH");
                                    return request;
                                }))
                .andExpect(status().isOk())
                .andDo(document("review-patch",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(parameterWithName("review-id").description("선택할 리뷰 아이디")),
                                requestParts(partWithName("new_images").description("추가할 리뷰 이미지")),
                                requestParameters(
                                        parameterWithName("content").description("리뷰 내용"),
                                        parameterWithName("rating").description("별점"),
                                        parameterWithName("old_urls").description("삭제할 리뷰 이미지 URL")),
                                responseFields(getNormalResponseField())
                                        .and(getReviewResponseFields())
                                        .and(getReviewDetailedResponseFields())
                                        .andWithPrefix("user.", getNormalResponseField())
                                        .andWithPrefix("user.", getUserResponseFields())
                                        .andWithPrefix("comments[].", getCommentResponseFields())
                                        .andWithPrefix("comments[].user.", getUserResponseFields())
                                        .andWithPrefix("comments[].user.", getNormalResponseField())
                ));
    }

    @Test
    public void deleteReview() throws Exception {
        mockMvc.perform(delete("/api/v1/reviews/{review-id}", "1"))
                .andExpect(status().isOk())
                .andDo(document("review-delete",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(parameterWithName("review-id").description("선택할 리뷰 아이디"))
                ));
    }

    @Test
    public void getHotReviews() throws Exception {
        List<ReviewDto.Response> reviews = List.of(ReviewDto.Response.of(review, user));
        given(reviewService.getHotReviews(any())).willReturn(new ReviewDto.HotResponse(reviews, reviews, reviews));

        mockMvc.perform(get("/api/v1/reviews/hot"))
                .andExpect(status().isOk())
                .andDo(document("review-hot",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                responseFields(
                                        subsectionWithPath("daily_hot_reviews").type(ARRAY).description("일별 인기 리뷰"),
                                        subsectionWithPath("weekly_hot_reviews").type(ARRAY).description("주별 인기 리뷰"),
                                        subsectionWithPath("monthly_hot_reviews").type(ARRAY).description("월별 인기 리뷰"))
                ));
    }

    @Test
    public void getHallOfFameReviews() throws Exception {
        List<ReviewDto.Response> reviews = List.of(ReviewDto.Response.of(review, user));
        given(reviewService.getHallOfFameReviews(any())).willReturn(new ReviewDto.HallOfFameResponse(reviews));

        mockMvc.perform(get("/api/v1/reviews/hall-of-fame"))
                .andExpect(status().isOk())
                .andDo(document("review-hall-of-fame",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                responseFields(subsectionWithPath("hall_of_fame_reviews").type(ARRAY).description("명예의 전당"))
                ));
    }
}
