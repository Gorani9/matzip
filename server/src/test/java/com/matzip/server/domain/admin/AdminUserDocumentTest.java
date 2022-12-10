package com.matzip.server.domain.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.Parameters;
import com.matzip.server.domain.admin.api.AdminUserController;
import com.matzip.server.domain.admin.dto.AdminDto;
import com.matzip.server.domain.admin.service.AdminUserService;
import com.matzip.server.domain.me.dto.MeDto;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.matzip.server.ApiDocumentUtils.getDocumentRequest;
import static com.matzip.server.ApiDocumentUtils.getDocumentResponse;
import static com.matzip.server.domain.DocumentFields.*;
import static com.matzip.server.domain.me.MeDocumentTest.getMeResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AdminUserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@AutoConfigureRestDocs
@Tag("DocumentTest")
public class AdminUserDocumentTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AdminUserService adminUserService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("foo", "password");
    }

    @Test
    public void searchByUsername() throws Exception {
        given(adminUserService.searchUsers(any())).willReturn(new SliceImpl<>(
                List.of(new AdminDto.UserResponse(user)), PageRequest.of(0, 20), true));

        Parameters parameters = new Parameters(0, 20);
        parameters.putParameter("username", "foo");
        mockMvc.perform(get("/admin/api/v1/users").queryParams(parameters))
                .andExpect(status().isOk())
                .andDo(document("admin-user-search",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestParameters(parameterWithName("username").description("검색할 유저네임").optional())
                                        .and(getPageRequestParameters())
                                        .and(parameterWithName("with-locked").description("차단된 유저 포함 여부").optional()),
                                responseFields(getPageResponseFields())
                                        .andWithPrefix("content[].", getNormalResponseField())
                                        .andWithPrefix("content[].", getAdminUserResponseFields())
                                        .andWithPrefix("content[].", getMeResponseFields())
                                        .andWithPrefix("content[].", getUserResponseFields())
                ));
    }

    @Test
    public void fetchUserById() throws Exception {
        given(adminUserService.fetchUserById(any())).willReturn(new AdminDto.UserResponse(user));

        mockMvc.perform(get("/admin/api/v1/users/{user-id}", "1"))
                .andExpect(status().isOk())
                .andDo(document("admin-user-fetch",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(parameterWithName("user-id").description("선택할 유저 아이디")),
                                responseFields(getNormalResponseField()).and(getAdminUserResponseFields()).and(getMeResponseFields()).and(getUserResponseFields())
                ));
    }

    @Test
    public void patchUserById() throws Exception {
        AdminDto.UserPatchRequest request = new AdminDto.UserPatchRequest(true, true, true, 10);
        given(adminUserService.patchUserById(any(), any())).willReturn(new AdminDto.UserResponse(user));

        mockMvc.perform(patch("/admin/api/v1/users/{user-id}", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("admin-user-patch",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(parameterWithName("user-id").description("선택할 유저 아이디")),
                                requestFields(
                                        fieldWithPath("username").type(BOOLEAN).description("유저네임 초기화 여부").optional(),
                                        fieldWithPath("profile_image_url").type(BOOLEAN).description("프로필 이미지 초기화 여부").optional(),
                                        fieldWithPath("profile_string").type(BOOLEAN).description("프로필 메시지 초기화 여부").optional(),
                                        fieldWithPath("matzip_level").type(NUMBER).description("설정할 유저 레벨").optional()),
                                responseFields(getNormalResponseField()).and(getAdminUserResponseFields()).and(getMeResponseFields()).and(getUserResponseFields())
                ));
    }

    @Test
    public void lockUser() throws Exception {
        given(adminUserService.lockUser(any())).willReturn(new AdminDto.UserResponse(user));

        mockMvc.perform(put("/admin/api/v1/users/{user-id}/lock", "1"))
                .andExpect(status().isOk())
                .andDo(document("admin-lock-user",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(parameterWithName("user-id").description("선택할 유저 아이디")),
                                responseFields(getNormalResponseField()).and(getAdminUserResponseFields()).and(getMeResponseFields()).and(getUserResponseFields())
                ));
    }

    @Test
    public void unlockUser() throws Exception {
        given(adminUserService.unlockUser(any())).willReturn(new AdminDto.UserResponse(user));

        mockMvc.perform(delete("/admin/api/v1/users/{user-id}/lock", "1"))
                .andExpect(status().isOk())
                .andDo(document("admin-unlock-user",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(parameterWithName("user-id").description("선택할 유저 아이디")),
                                responseFields(getNormalResponseField()).and(getAdminUserResponseFields()).and(getMeResponseFields()).and(getUserResponseFields())
                ));
    }

    @Test
    public void changeUserPassword() throws Exception {
        MeDto.PasswordChangeRequest request = new MeDto.PasswordChangeRequest("newPassword1!");
        given(adminUserService.changeUserPassword(any(), any())).willReturn(new AdminDto.UserResponse(user));

        mockMvc.perform(put("/admin/api/v1/users/{user-id}/password", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("admin-user-change-password",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(parameterWithName("user-id").description("선택할 유저 아이디")),
                                requestFields(fieldWithPath("password").type(STRING).description("바꿀 비밀번호")),
                                responseFields(getNormalResponseField()).and(getAdminUserResponseFields()).and(getMeResponseFields()).and(getUserResponseFields())
                ));
    }

    @Test
    public void deleteUser() throws Exception {
        mockMvc.perform(delete("/admin/api/v1/users/{user-id}", "1"))
                .andExpect(status().isOk())
                .andDo(document("admin-user-delete",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                pathParameters(parameterWithName("user-id").description("선택할 유저 아이디"))
                ));
    }
}
