package com.matzip.server.domain.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.ExpectedStatus;
import com.matzip.server.Parameters;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.service.UserService;
import com.matzip.server.global.auth.model.MatzipAuthenticationToken;
import com.matzip.server.global.auth.model.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    private Authentication authentication;

    @BeforeEach
    void init() {
        User user = new User(new UserDto.SignUpRequest("foo", "password"), new BCryptPasswordEncoder());
        UserPrincipal userPrincipal = new UserPrincipal(user);
        authentication = new MatzipAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }

    private void signUp(String password, ExpectedStatus expectedStatus) throws Exception {
        UserDto.SignUpRequest signUpRequest = new UserDto.SignUpRequest("foo", password);
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

    @Test
    void signUpTest() throws Exception {
        signUp("simplePassword1!", OK);
        signUp("maximumLengthOfPasswordIs50Characters!!!!!!!!!!!!!", OK);
        signUp("short", BAD_REQUEST);
        signUp("veryVeryLongPasswordThatIsOver50Characters!!!!!!!!!", BAD_REQUEST);
        signUp("noNumeric!", BAD_REQUEST);
        signUp("noSpecial1", BAD_REQUEST);
        signUp("no_upper_case1!", BAD_REQUEST);
        signUp("NO_LOWER_CASE1!", BAD_REQUEST);
    }

    @Test
    void searchUsersByUsernameTest() throws Exception {
        Parameters parameters;

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