package com.matzip.server.domain.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.domain.review.api.CommentController;
import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.service.CommentService;
import com.matzip.server.domain.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.matzip.server.ApiDocumentUtils.getDocumentRequest;
import static com.matzip.server.ApiDocumentUtils.getDocumentResponse;
import static com.matzip.server.domain.user.UserDocumentTest.getUserResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = CommentController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@AutoConfigureRestDocs
@Tag("DocumentTest")
public class CommentDocumentTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CommentService commentService;

    private User user;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user = new User("foo", "password");
        Review review = new Review(user, new ReviewDto.PostRequest("sample_review", null, 10, "location"));
        comment = new Comment(user, review, "sample_comment");
    }

    public static FieldDescriptor[] getCommentResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").type(NUMBER).description("댓글 아이디").optional(),
                fieldWithPath("created_at").type(STRING).description("리뷰 작성 일자").optional(),
                fieldWithPath("modified_at").type(STRING).description("리뷰 수정 일자").optional(),
                fieldWithPath("review_id").type(NUMBER).description("리뷰 아이디").optional(),
                fieldWithPath("content").type(STRING).description("댓글 내용"),
                fieldWithPath("deletable").type(BOOLEAN).description("댓글 삭제 가능 여부"),
        };
    }

    @Test
    public void postComment() throws Exception {
        given(commentService.postComment(any(), any())).willReturn(new CommentDto.Response(comment, user));

        CommentDto.PostRequest request = new CommentDto.PostRequest(1L, "sample_comment");
        mockMvc.perform(post("/api/v1/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("comment-post",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestFields(
                                        fieldWithPath("review_id").type(NUMBER).description("댓글이 달리는 리뷰 아이디"),
                                        fieldWithPath("content").type(STRING).description("댓글 내용")),
                                responseFields(getCommentResponseFields())
                                        .andWithPrefix("user.", getUserResponseFields())
                ));
    }

    @Test
    public void getComment() throws Exception {
        given(commentService.fetchComment(any(), any())).willReturn(new CommentDto.Response(comment, user));

        mockMvc.perform(get("/api/v1/comments/{comment-id}", "1"))
                .andExpect(status().isOk())
                .andDo(document("comment-post",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(
                                        parameterWithName("comment-id").description("선택할 댓글 아이디")),
                                responseFields(getCommentResponseFields())
                                        .andWithPrefix("user.", getUserResponseFields())
                ));
    }

    @Test
    public void putComment() throws Exception {
        given(commentService.putComment(any(), any(), any())).willReturn(new CommentDto.Response(comment, user));

        CommentDto.PutRequest request = new CommentDto.PutRequest("sample_comment");
        mockMvc.perform(put("/api/v1/comments/{comment-id}", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("comment-put",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(
                                        parameterWithName("comment-id").description("수정할 댓글 아이디")),
                                requestFields(
                                        fieldWithPath("content").type(STRING).description("수정할 댓글 내용")),
                                responseFields(getCommentResponseFields())
                                        .andWithPrefix("user.", getUserResponseFields())
                ));
    }

    @Test
    public void deleteComment() throws Exception {
        mockMvc.perform(delete("/api/v1/comments/{comment-id}", "1"))
                .andExpect(status().isOk())
                .andDo(document("comment-delete",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(
                                        parameterWithName("comment-id").description("삭제할 댓글 아이디"))
                ));
    }
}
