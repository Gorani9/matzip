package com.matzip.server.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.Parameters;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.user.api.UserController;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.service.UserService;
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
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.matzip.server.ApiDocumentUtils.getDocumentRequest;
import static com.matzip.server.ApiDocumentUtils.getDocumentResponse;
import static com.matzip.server.domain.me.MeDocumentTest.getMeResponseFields;
import static com.matzip.server.domain.review.ReviewDocumentTest.getPageRequestParameters;
import static com.matzip.server.domain.review.ReviewDocumentTest.getPageResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@AutoConfigureRestDocs
@Tag("DocumentTest")
public class UserDocumentTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    public static FieldDescriptor[] getUserResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("username").type(STRING).description("유저네임"),
                fieldWithPath("profile_image_url").type(STRING).description("유저 프로필 이미지 링크").optional(),
                fieldWithPath("profile_string").type(STRING).description("유저 프로필 메시지").optional(),
                fieldWithPath("matzip_level").type(NUMBER).description("유저 레벨"),
                fieldWithPath("number_of_followers").type(NUMBER).description("유저 팔로워 수"),
                fieldWithPath("number_of_followings").type(NUMBER).description("유저 팔로잉 수"),
                fieldWithPath("is_my_following").type(BOOLEAN).description("해당 유저가 요청자의 팔로잉인지 여부"),
                fieldWithPath("is_my_follower").type(BOOLEAN).description("해당 유저가 요청자의 팔로워인지 여부"),
                fieldWithPath("is_me").type(BOOLEAN).description("해당 유저가 요청자인지 여부")
        };
    }

    @Test
    public void usernameDuplicateCheck() throws Exception {
        given(userService.isUsernameTakenBySomeone(any())).willReturn(new UserDto.DuplicateResponse(false));

        mockMvc.perform(get("/api/v1/users/exists")
                                .queryParam("username", "foo"))
                .andExpect(status().isOk())
                .andDo(document("username-duplicate-check",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestParameters(
                                        parameterWithName("username").description("중복 검사할 유저네임")),
                                responseFields(
                                        fieldWithPath("exists").type(BOOLEAN).description("해당 유저가 존재하는지 여부")
                                )
                ));
    }

    @Test
    public void userSignUp() throws Exception {
        User user = new User("foo", "1SimplePassword!");
        UserDto.SignUpRequest signUpRequest = new UserDto.SignUpRequest("foo", "1SimplePassword!");
        MeDto.Response response = new MeDto.Response(user);
        given(userService.createUser(any())).willReturn(new UserDto.SignUpResponse(response, "token"));

        mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andDo(document("user-signup",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestFields(
                                        fieldWithPath("username").type(STRING).description("유저네임"),
                                        fieldWithPath("password").type(STRING).description("비밀번호")),
                                responseFields(getMeResponseFields()).and(getUserResponseFields())
                ));
    }

    @Test
    public void fetchUser() throws Exception {
        User user = new User("foo", "1SimplePassword!");
        UserDto.DetailedResponse response = new UserDto.DetailedResponse(user, user);
        given(userService.fetchUser(any(), any())).willReturn(response);

        mockMvc.perform(get("/api/v1/users/{username}", "foo"))
                .andExpect(status().isOk())
                .andDo(document("user-fetch",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(
                                        parameterWithName("username").description("선택할 유저네임")),
                                responseFields(getUserResponseFields())
                ));
    }

    @Test
    public void searchUser() throws Exception {
        User foo = new User("foo", "1SimplePassword!");
        User bar = new User("bar", "1SimplePassword!");
        User bar2 = new User("bar2", "1SimplePassword!");
        UserDto.Response barResponse = UserDto.Response.of(bar, foo);
        UserDto.Response barResponse2 = UserDto.Response.of(bar2, foo);
        given(userService.searchUsers(any(), any())).willReturn(new SliceImpl<>(
                List.of(barResponse, barResponse2),
                PageRequest.of(0, 20),
                false
        ));

        Parameters parameters = new Parameters(0, 20);
        parameters.putParameter("username", "bar");
        mockMvc.perform(get("/api/v1/users").queryParams(parameters))
                .andExpect(status().isOk())
                .andDo(document("user-search",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestParameters(parameterWithName("username").description("검색할 유저네임"))
                                        .and(getPageRequestParameters()),
                                responseFields(getPageResponseFields())
                                        .andWithPrefix("content[].", getUserResponseFields())
                ));
    }
}
