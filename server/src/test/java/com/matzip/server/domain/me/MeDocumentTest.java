package com.matzip.server.domain.me;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.Parameters;
import com.matzip.server.domain.me.api.MeController;
import com.matzip.server.domain.me.dto.HeartDto;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.me.dto.ScrapDto;
import com.matzip.server.domain.me.model.Scrap;
import com.matzip.server.domain.me.service.MeService;
import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.user.dto.UserDto;
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
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.util.List;

import static com.matzip.server.ApiDocumentUtils.getDocumentRequest;
import static com.matzip.server.ApiDocumentUtils.getDocumentResponse;
import static com.matzip.server.domain.review.CommentDocumentTest.getCommentResponseFields;
import static com.matzip.server.domain.review.ReviewDocumentTest.*;
import static com.matzip.server.domain.user.UserDocumentTest.getUserResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = MeController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@AutoConfigureRestDocs
@Tag("DocumentTest")
public class MeDocumentTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private MeService meService;

    private User user;
    private Review review;
    private Comment comment;
    private Scrap scrap;
    private MeDto.Response meResponse;

    @BeforeEach
    void setUp() {
        user = new User("foo", "password");
        review =  new Review(user, new ReviewDto.PostRequest("sample_review", null, 10, "location"));
        comment = new Comment(user, review, new CommentDto.PostRequest(1L, "sample_comment"));
        scrap = new Scrap(user, review);
        scrap.setDescription("sample scrap");
        meResponse = new MeDto.Response(user);
    }

    public static FieldDescriptor[] getMeResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("created_at").type(STRING).description("리뷰 작성 일자").optional(),
                fieldWithPath("modified_at").type(STRING).description("리뷰 수정 일자").optional(),
        };
    }

    public static FieldDescriptor[] getScrapResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("created_at").type(STRING).description("스크랩 생성 일자").optional(),
                fieldWithPath("modified_at").type(STRING).description("스크랩 수정 일자").optional(),
                fieldWithPath("description").type(STRING).description("스크랩 설명")
        };
    }

    @Test
    public void getMe() throws Exception {
        given(meService.getMe(any())).willReturn(meResponse);

        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isOk())
                .andDo(document("me-fetch",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                responseFields(getMeResponseFields()).and(getUserResponseFields())
                ));
    }

    @Test
    public void patchMe() throws Exception {
        given(meService.patchMe(any(), any())).willReturn(meResponse);

        mockMvc.perform(multipart("/api/v1/me")
                                .file(new MockMultipartFile("new_image", "image_name", "", InputStream.nullInputStream()))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .param("profile_string", "sample_profile_string")
                                .with(request -> {
                                    request.setMethod("PATCH");
                                    return request;
                                }))
                .andExpect(status().isOk())
                .andDo(document("me-patch",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestParts(partWithName("new_image").description("바꿀 프로필 이미지").optional()),
                                requestParameters(parameterWithName("profile_string").description("바꿀 프로필 메시지").optional()),
                                responseFields(getMeResponseFields()).and(getUserResponseFields())
                ));
    }

    @Test
    public void deleteMe() throws Exception {
        mockMvc.perform(delete("/api/v1/me"))
                .andExpect(status().isOk())
                .andDo(document("me-delete",
                                getDocumentRequest(),
                                getDocumentResponse()
                ));
    }

    @Test
    public void changeUsername() throws Exception {
        MeDto.UsernameChangeRequest request = new MeDto.UsernameChangeRequest("new_foo");
        given(meService.changeUsername(any(), any())).willReturn(meResponse);

        mockMvc.perform(put("/api/v1/me/username")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("me-change-username",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestFields(fieldWithPath("username").type(STRING).description("바꿀 유저네임")),
                                responseFields(getMeResponseFields()).and(getUserResponseFields())
                ));
    }

    @Test
    public void changePassword() throws Exception {
        MeDto.PasswordChangeRequest request = new MeDto.PasswordChangeRequest("newPassword1!");
        given(meService.changePassword(any(), any())).willReturn(meResponse);

        mockMvc.perform(put("/api/v1/me/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("me-change-password",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestFields(fieldWithPath("password").type(STRING).description("바꿀 비밀번호")),
                                responseFields(getMeResponseFields()).and(getUserResponseFields())
                ));
    }

    @Test
    public void searchMyFollowers() throws Exception {
        UserDto.Response barResponse = UserDto.Response.of(new User("bar", "1SimplePassword!"), user);
        UserDto.Response barResponse2 = UserDto.Response.of(new User("bar2", "1SimplePassword!"), user);
        given(meService.searchMyFollowers(any(), any())).willReturn(new SliceImpl<>(
                List.of(barResponse, barResponse2), PageRequest.of(0, 20), true));

        Parameters parameters = new Parameters(0, 20);
        parameters.putParameter("username", "bar");
        mockMvc.perform(get("/api/v1/me/followers").queryParams(parameters))
                .andExpect(status().isOk())
                .andDo(document("me-search-followers",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestParameters(parameterWithName("username").description("검색할 유저네임").optional())
                                        .and(getPageRequestParameters()),
                                responseFields(getPageResponseFields())
                                        .andWithPrefix("content[].", getUserResponseFields())
                ));
    }

    @Test
    public void searchMyFollowings() throws Exception {
        UserDto.Response barResponse = UserDto.Response.of(new User("bar", "1SimplePassword!"), user);
        UserDto.Response barResponse2 = UserDto.Response.of(new User("bar2", "1SimplePassword!"), user);
        given(meService.searchMyFollowings(any(), any())).willReturn(new SliceImpl<>(
                List.of(barResponse, barResponse2), PageRequest.of(0, 20), true));

        Parameters parameters = new Parameters(0, 20);
        parameters.putParameter("username", "bar");
        mockMvc.perform(get("/api/v1/me/followings").queryParams(parameters))
                .andExpect(status().isOk())
                .andDo(document("me-search-followings",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestParameters(parameterWithName("username").description("검색할 유저네임").optional())
                                        .and(getPageRequestParameters()),
                                responseFields(getPageResponseFields())
                                        .andWithPrefix("content[].", getUserResponseFields())
                ));
    }

    @Test
    public void followAnotherUser() throws Exception {
        given(meService.followUser(any(), any())).willReturn(meResponse);

        mockMvc.perform(put("/api/v1/me/follows/{username}", "foo"))
                .andExpect(status().isOk())
                .andDo(document("me-follow-user",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(parameterWithName("username").description("팔로우할 유저의 유저네임")),
                                responseFields(getMeResponseFields()).and(getUserResponseFields())

                ));
    }

    @Test
    public void unfollowAnotherUser() throws Exception {
        given(meService.unfollowUser(any(), any())).willReturn(meResponse);

        mockMvc.perform(delete("/api/v1/me/follows/{username}", "foo"))
                .andExpect(status().isOk())
                .andDo(document("me-unfollow-user",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(parameterWithName("username").description("언팔로우할 유저의 유저네임")),
                                responseFields(getMeResponseFields()).and(getUserResponseFields())

                ));
    }

    @Test
    public void searchMyReviews() throws Exception {
        given(meService.searchMyReviews(any(), any())).willReturn(new SliceImpl<>(
                List.of(ReviewDto.Response.of(review, user)), PageRequest.of(0, 20), true));

        Parameters parameters = new Parameters(0, 20);
        parameters.putParameter("keyword", "sample");
        mockMvc.perform(get("/api/v1/me/reviews").queryParams(parameters))
                .andExpect(status().isOk())
                .andDo(document("me-search-my-reviews",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestParameters(parameterWithName("keyword").description("검색할 리뷰 내용").optional())
                                        .and(getPageRequestParameters()),
                                responseFields(getPageResponseFields())
                                        .andWithPrefix("content[].", getReviewResponseFields())
                                        .andWithPrefix("content[].user.", getUserResponseFields())
                                        .andWithPrefix("content[].comments[].", getCommentResponseFields())
                                        .andWithPrefix("content[].comments[].user.", getUserResponseFields())
                ));
    }

    @Test
    public void searchMyComments() throws Exception {
        given(meService.searchMyComments(any(), any())).willReturn(new SliceImpl<>(
                List.of(CommentDto.Response.of(comment, user)), PageRequest.of(0, 20), true));

        Parameters parameters = new Parameters(0, 20);
        parameters.putParameter("keyword", "sample");
        mockMvc.perform(get("/api/v1/me/comments").queryParams(parameters))
                .andExpect(status().isOk())
                .andDo(document("me-search-my-comments",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestParameters(parameterWithName("keyword").description("검색할 댓글 내용").optional())
                                        .and(getPageRequestParameters()),
                                responseFields(getPageResponseFields())
                                        .andWithPrefix("content[].", getCommentResponseFields())
                                        .andWithPrefix("content[].user.", getUserResponseFields())
                ));
    }

    @Test
    public void putHeartOnReview() throws Exception {
        given(meService.heartReview(any(), any())).willReturn(new HeartDto.Response(100));

        mockMvc.perform(put("/api/v1/me/hearts/{review-id}", "1"))
                .andExpect(status().isOk())
                .andDo(document("me-heart-review",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(parameterWithName("review-id").description("좋아요를 누를 리뷰 아이디")),
                                responseFields(fieldWithPath("number_of_hearts").type(NUMBER).description("해당 리뷰 좋아요 수"))
                ));
    }

    @Test
    public void deleteHeartFromReview() throws Exception {
        given(meService.deleteHeartFromReview(any(), any())).willReturn(new HeartDto.Response(50));

        mockMvc.perform(delete("/api/v1/me/hearts/{review-id}", "1"))
                .andExpect(status().isOk())
                .andDo(document("me-delete-heart-review",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(parameterWithName("review-id").description("좋아요를 취소할 리뷰 아이디")),
                                responseFields(fieldWithPath("number_of_hearts").type(NUMBER).description("해당 리뷰 좋아요 수"))
                ));
    }

    @Test
    public void searchMyScraps() throws Exception {
        given(meService.searchMyScraps(any(), any())).willReturn(new SliceImpl<>(
                List.of(new ScrapDto.Response(scrap)), PageRequest.of(0, 20), true));

        Parameters parameters = new Parameters(0, 20);
        parameters.putParameter("keyword", "sample");
        mockMvc.perform(get("/api/v1/me/scraps").queryParams(parameters))
                .andExpect(status().isOk())
                .andDo(document("me-search-my-scraps",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestParameters(parameterWithName("keyword").description("검색할 리뷰 내용").optional())
                                        .and(getPageRequestParameters()),
                                responseFields(getPageResponseFields())
                                        .andWithPrefix("content[].", getScrapResponseFields())
                                        .andWithPrefix("content[].review.", getReviewResponseFields())
                                        .andWithPrefix("content[].review.user.", getUserResponseFields())
                                        .andWithPrefix("content[].review.comments[].", getCommentResponseFields())
                                        .andWithPrefix("content[].review.comments[].user.", getUserResponseFields())
                ));
    }

    @Test
    public void putMyScrap() throws Exception {
        ScrapDto.PostRequest request = new ScrapDto.PostRequest("sample scrap");
        given(meService.scrapReview(any(), any(), any())).willReturn(new ScrapDto.Response(scrap));

        mockMvc.perform(put("/api/v1/me/scraps/{review-id}", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("me-scrap-review",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(parameterWithName("review-id").description("스크랩할 리뷰 아이디")),
                                requestFields(fieldWithPath("description").type(STRING).description("스크랩 설명")),
                                responseFields(getScrapResponseFields())
                                        .andWithPrefix("review.", getReviewResponseFields())
                                        .andWithPrefix("review.user.", getUserResponseFields())
                                        .andWithPrefix("review.comments[].", getCommentResponseFields())
                                        .andWithPrefix("review.comments[].user.", getUserResponseFields())
                ));
    }

    @Test
    public void deleteMyScrap() throws Exception {
        mockMvc.perform(delete("/api/v1/me/scraps/{review-id}", "1"))
                .andExpect(status().isOk())
                .andDo(document("me-delete-scrap",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(parameterWithName("review-id").description("스크랩 삭제할 리뷰 아이디"))
                ));
    }
}
