package com.matzip.server.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.ExpectedStatus;
import com.matzip.server.Parameters;
import com.matzip.server.domain.user.api.UserController;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.service.UserService;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;

import static com.matzip.server.ExpectedStatus.BAD_REQUEST;
import static com.matzip.server.ExpectedStatus.OK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
@Tag("ControllerTest")
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;
    private Authentication authentication;

    @BeforeEach
    void init() {
        User user = new User("foo", "password");
        UserPrincipal userPrincipal = new UserPrincipal(user);
        authentication = new MatzipAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }

    private void signUp(String username, String password, ExpectedStatus expectedStatus) throws Exception {
        UserDto.SignUpRequest signUpRequest = new UserDto.SignUpRequest(username, password);
        given(userService.createUser(any())).willReturn(new UserDto.SignUpResponse(null, null));
        mockMvc.perform(post("/api/v1/users")
                                .with(authentication(authentication)).with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().is(expectedStatus.getStatusCode()));
    }

    private void searchUsersByUsername(
            MultiValueMap<String, String> parameters, ExpectedStatus expectedStatus) throws Exception {
        mockMvc.perform(get("/api/v1/users")
                                .with(authentication(authentication))
                                .queryParams(parameters))
                .andExpect(status().is(expectedStatus.getStatusCode()));
    }

    public static String[] validUsernames = new String[]{
            "maxLengthOfUsernameIs.30.chars",
            "underscore_username",
            "1111",
            "using_dot.is_allowed",
            ".start_with_dot_is_allowed"
    };

    public static String[] invalidUsernames = new String[]{
            "",
            "maxLengthOfUsername.Is.30.chars",
            "specialForbidden!",
            "space Forbidden",
            "end_with_dot_is_not_allowed.",
            "double_dot.._is_not_allowed",
            "admin",
            "Admin",
            "ADMIN"
    };

    public static String[] validPasswords = new String[]{
            "simplePassword1!",
            "maximumLengthOfPasswordIs50Characters!!!!!!!!!!!!!"
    };

    public static String[] invalidPasswords = new String[]{
            "short",
            "veryVeryLongPasswordThatIsOver50Characters!!!!!!!!!",
            "noNumeric!",
            "noSpecial1",
            "no_upper_case1!",
            "NO_LOWER_CASE1!"
    };

    @Test
    @DisplayName("유저 회원가입 테스트: 유저네임, 비밀번호 검증")
    void signUpTest() throws Exception {
        signUp("foo","simplePassword1!", OK);

        /* validate username */
        for (String validUsername : validUsernames) signUp(validUsername, validPasswords[0], OK);
        for (String invalidUsername : invalidUsernames) signUp(invalidUsername, validPasswords[0], BAD_REQUEST);

        /* validate password */
        for (String validPassword : validPasswords) signUp(validUsernames[0], validPassword, OK);
        for (String invalidPassword : invalidPasswords) signUp(validUsernames[0], invalidPassword, BAD_REQUEST);
    }

    @Test
    @DisplayName("유저 검색 테스트: 파라미터 검증")
    void searchUsersByUsernameTest() throws Exception {
        Parameters parameters;

        /* username length at most 30 */
        parameters = new Parameters();
        String username = "a".repeat(30);
        parameters.putParameter("username", username);
        searchUsersByUsername(parameters, OK);
        parameters.putParameter("username", username + "a");
        searchUsersByUsername(parameters, BAD_REQUEST);

        /* username must be included */
        parameters = new Parameters(0, 15);
        searchUsersByUsername(parameters, BAD_REQUEST);
        parameters.putParameter("username", "foo");
        searchUsersByUsername(parameters, OK);

        /* username must not be blank */
        parameters = new Parameters(0, 15);
        parameters.putParameter("username", "");
        searchUsersByUsername(parameters, BAD_REQUEST);
        parameters.putParameter("username", "      ");
        searchUsersByUsername(parameters, BAD_REQUEST);

        /* page must be positive or zero */
        parameters = new Parameters(-1, 15);
        parameters.putParameter("username", "foo");
        searchUsersByUsername(parameters, BAD_REQUEST);
        parameters.putParameter("page", "0");
        searchUsersByUsername(parameters, OK);
        parameters.putParameter("page", "1");
        searchUsersByUsername(parameters, OK);

        /* size must be positive, smaller or equal to 100 */
        parameters = new Parameters(0, 0);
        parameters.putParameter("username", "foo");
        searchUsersByUsername(parameters, BAD_REQUEST);
        parameters.putParameter("size", "-1");
        searchUsersByUsername(parameters, BAD_REQUEST);
        parameters.putParameter("size", "101");
        searchUsersByUsername(parameters, BAD_REQUEST);
        parameters.putParameter("size", "100");
        searchUsersByUsername(parameters, OK);

        /* asc must be either true or false or null */
        parameters = new Parameters(0, 15);
        parameters.putParameter("username", "foo");
        parameters.putParameter("asc", "boolean");
        searchUsersByUsername(parameters, BAD_REQUEST);
        parameters.putParameter("asc", "null");
        searchUsersByUsername(parameters, BAD_REQUEST);
        parameters.putParameter("asc", "false");
        searchUsersByUsername(parameters, OK);
        parameters.putParameter("asc", "true");
        searchUsersByUsername(parameters, OK);
        parameters.putParameter("asc", null);
        searchUsersByUsername(parameters, OK);

        /* sort must be username or matzip-level or number-of-followers or null */
        parameters = new Parameters(0, 15);
        parameters.putParameter("username", "foo");
        parameters.putParameter("sort", "username");
        searchUsersByUsername(parameters, OK);
        parameters.putParameter("sort", "level");
        searchUsersByUsername(parameters, OK);
        parameters.putParameter("sort", "followers");
        searchUsersByUsername(parameters, OK);
        parameters.putParameter("sort", "    ");
        searchUsersByUsername(parameters, OK);
        parameters.putParameter("sort", "");
        searchUsersByUsername(parameters, OK);
        parameters.putParameter("sort", null);
        searchUsersByUsername(parameters, OK);
        parameters.putParameter("sort", "createdAt");
        searchUsersByUsername(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "matzipLevel");
        searchUsersByUsername(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "modifiedAt");
        searchUsersByUsername(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "id");
        searchUsersByUsername(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "password");
        searchUsersByUsername(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "role");
        searchUsersByUsername(parameters, BAD_REQUEST);
        parameters.putParameter("sort", "profileString");
        searchUsersByUsername(parameters, BAD_REQUEST);
    }
}