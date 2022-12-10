package com.matzip.server.domain.me;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.ExpectedStatus;
import com.matzip.server.domain.me.api.MeController;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.me.dto.ScrapDto;
import com.matzip.server.domain.me.service.MeService;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.auth.model.MatzipAuthenticationToken;
import com.matzip.server.global.auth.model.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.matzip.server.ExpectedStatus.BAD_REQUEST;
import static com.matzip.server.ExpectedStatus.OK;
import static com.matzip.server.domain.user.UserControllerTest.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeController.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@Tag("ControllerTest")
class MeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private MeService meService;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        User user = new User("foo", "password");
        UserPrincipal userPrincipal = new UserPrincipal(user);
        authentication = new MatzipAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }

    private void getMe(ExpectedStatus expectedStatus) throws Exception {
        mockMvc.perform(get("/api/v1/me").with(authentication(authentication)))
                .andExpect(status().is(expectedStatus.getStatusCode()));
    }

    private void patchMe(String profileString, ExpectedStatus expectedStatus) throws Exception {
        mockMvc.perform(multipart(HttpMethod.PATCH, "/api/v1/me")
                                .param("profile", profileString)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(authentication(authentication)).with(csrf()))
                .andExpect(status().is(expectedStatus.getStatusCode()));
    }

    private void changeUsername(String username, ExpectedStatus expectedStatus) throws Exception {
        MeDto.UsernameChangeRequest request = new MeDto.UsernameChangeRequest(username);
        mockMvc.perform(put("/api/v1/me/username")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(authentication(authentication)).with(csrf()))
                .andExpect(status().is(expectedStatus.getStatusCode()));
    }

    private void changePassword(String password, ExpectedStatus expectedStatus) throws Exception {
        MeDto.PasswordChangeRequest request = new MeDto.PasswordChangeRequest(password);
        mockMvc.perform(put("/api/v1/me/password")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(authentication(authentication)).with(csrf()))
                .andExpect(status().is(expectedStatus.getStatusCode()));
    }

    private void putMyScrap(Long reviewId, String description, ExpectedStatus expectedStatus) throws Exception {
        ScrapDto.PostRequest request = new ScrapDto.PostRequest(description);
        mockMvc.perform(put("/api/v1/me/scraps/{review-id}", reviewId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(authentication(authentication)).with(csrf()))
                .andExpect(status().is(expectedStatus.getStatusCode()));
    }

    @Test
    @DisplayName("내 정보 가져오기 테스트")
    public void getMeTest() throws Exception {
        getMe(OK);
    }

    @Test
    @DisplayName("내 정보 수정하기 테스트: 파라미터 검증")
    public void patchMeTest() throws Exception {
        /* profile string length at most 50 */
        String validProfileString = "!".repeat(50);
        patchMe(validProfileString, OK);
        String longProfileString = "!".repeat(51);
        patchMe(longProfileString, BAD_REQUEST);
    }

    @Test
    @DisplayName("유저네임 수정하기 테스트")
    public void changeUsernameTest() throws Exception {
        for (String validUsername : validUsernames) changeUsername(validUsername, OK);
        for (String invalidUsername : invalidUsernames) changeUsername(invalidUsername, BAD_REQUEST);
    }

    @Test
    @DisplayName("비밀번호 수정하기 테스트")
    public void changePasswordTest() throws Exception {
        for (String validPassword : validPasswords) changePassword(validPassword, OK);
        for (String invalidPassword : invalidPasswords) changePassword(invalidPassword, BAD_REQUEST);
    }

    @Test
    @DisplayName("스크랩하기 테스트")
    public void putMyScrapTest() throws Exception {
        String description = "description max length is 100" + "!".repeat(71);
        putMyScrap(1L, description, OK);
        putMyScrap(1L, description + "!", BAD_REQUEST);
    }
}