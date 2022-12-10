package com.matzip.server.domain;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;

import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

public class DocumentFields {
    public static FieldDescriptor[] getNormalResponseField() {
        return new FieldDescriptor[]{
                fieldWithPath("normal").type(BOOLEAN).description("정상 RESPONSE 여부")
        };
    }

    public static ParameterDescriptor[] getPageRequestParameters() {
        return new ParameterDescriptor[]{
                parameterWithName("page").description("페이지 번호").optional(),
                parameterWithName("size").description("페이지 크기").optional(),
                parameterWithName("sort").description("정렬 기준").optional(),
                parameterWithName("asc").description("오름차순 여부").optional()
        };
    }

    public static FieldDescriptor[] getPageResponseFields() {
        return new FieldDescriptor[]{
                subsectionWithPath("pageable").type(OBJECT).description(""),
                fieldWithPath("number_of_elements").type(NUMBER).description("요소 개수"),
                fieldWithPath("size").type(NUMBER).description("페이지 크기"),
                fieldWithPath("number").type(NUMBER).description("페이지 번호"),
                subsectionWithPath("sort").type(OBJECT).description("정렬 여부"),
                fieldWithPath("first").type(BOOLEAN).description("첫번째 페이지 여부"),
                fieldWithPath("last").type(BOOLEAN).description("마지막 페이지 여부"),
                fieldWithPath("empty").type(BOOLEAN).description("비어있는지 여부"),
                };
    }

    public static FieldDescriptor[] getUserResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("username").type(STRING).description("유저네임"),
                fieldWithPath("profile_image_url").type(STRING).description("유저 프로필 이미지 링크").optional(),
                fieldWithPath("profile_string").type(STRING).description("유저 프로필 메시지").optional(),
                fieldWithPath("matzip_level").type(NUMBER).description("유저 레벨"),
                fieldWithPath("is_my_following").type(BOOLEAN).description("해당 유저가 요청자의 팔로잉인지 여부"),
                fieldWithPath("is_my_follower").type(BOOLEAN).description("해당 유저가 요청자의 팔로워인지 여부"),
                fieldWithPath("is_me").type(BOOLEAN).description("해당 유저가 요청자인지 여부")
        };
    }

    public static FieldDescriptor[] getUserDetailedResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("followers").type(ARRAY).description("유저 팔로워"),
                fieldWithPath("number_of_followers").type(NUMBER).description("유저 팔로워 수"),
                fieldWithPath("followings").type(ARRAY).description("유저 팔로잉"),
                fieldWithPath("number_of_followings").type(NUMBER).description("유저 팔로잉 수"),
                fieldWithPath("reviews").type(ARRAY).description("유저가 작성한 리뷰")
        };
    }

    public static FieldDescriptor[] getAdminUserResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").type(NUMBER).description("유저 아이디").optional(),
                fieldWithPath("is_non_locked").type(BOOLEAN).description("유저 차단 여부"),
                };
    }

    public static FieldDescriptor[] getReviewResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").type(NUMBER).description("리뷰 아이디").optional(),
                fieldWithPath("created_at").type(STRING).description("리뷰 작성 일자").optional(),
                fieldWithPath("modified_at").type(STRING).description("리뷰 수정 일자").optional(),
                fieldWithPath("content").type(STRING).description("리뷰 내용"),
                fieldWithPath("image_url").type(STRING).description("리뷰 사진 URL").optional(),
                fieldWithPath("rating").type(NUMBER).description("리뷰 별점"),
                fieldWithPath("location").type(STRING).description("가게 위치"),
                fieldWithPath("is_deletable").type(BOOLEAN).description("해당 리뷰를 삭제할 수 있는지 여부"),
                fieldWithPath("is_hearted").type(BOOLEAN).description("해당 리뷰를 좋아요 했는지 여부"),
                fieldWithPath("is_scraped").type(BOOLEAN).description("해당 리뷰를 스크랩 했는지 여부")
        };
    }

    public static FieldDescriptor[] getReviewDetailedResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("image_urls").type(ARRAY).description("리뷰 사진 URL").optional(),
                fieldWithPath("comments").type(VARIES).description("리뷰 댓글"),
                fieldWithPath("number_of_scraps").type(NUMBER).description("스크랩 수"),
                fieldWithPath("number_of_hearts").type(NUMBER).description("좋아요 수"),
                };
    }

}
