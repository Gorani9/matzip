package com.matzip.server.domain.user;

import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.exception.AccessBlockedOrDeletedUserException;
import com.matzip.server.domain.user.exception.UsernameAlreadyExistsException;
import com.matzip.server.domain.user.exception.UsernameNotFoundException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.domain.user.service.UserService;
import com.matzip.server.global.auth.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Tag("ServiceTest")
class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("유저네임 중복 검사")
    public void checkDuplicateUsernameTest() {
        // given
        given(userRepository.existsByUsername(any())).willReturn(false);

        // when
        UserDto.DuplicateResponse response = userService.isUsernameTakenBySomeone("username");

        // then
        assertThat(response.getExists()).isFalse();
    }

    @Test
    @DisplayName("유저 회원가입 성공")
    public void createUserTest_Success() {
        // given
        User user = new User("foo", "password");
        given(passwordEncoder.encode(any())).willReturn("password");
        given(userRepository.existsByUsername(any())).willReturn(false);
        given(userRepository.save(any())).willReturn(user);
        given(jwtProvider.generateAccessToken(any())).willReturn("token");

        // when
        UserDto.SignUpRequest request = new UserDto.SignUpRequest("foo", "password");
        UserDto.SignUpResponse response = userService.createUser(request);

        // then
        assertThat(response.getToken()).isEqualTo("token");
        assertThat(response.getResponse().getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("유저 회원가입 실패: 중복된 유저네임")
    public void createUserTest_Failure_DuplicateUsername() {
        // given
        given(passwordEncoder.encode(any())).willReturn("password");

        // when
        when(userRepository.existsByUsername(any())).thenReturn(true);
        UserDto.SignUpRequest request = new UserDto.SignUpRequest("duplicate", "password");

        // then
        assertThrows(UsernameAlreadyExistsException.class, () -> userService.createUser(request));
    }

    @Test
    @DisplayName("유저 검색")
    public void searchUserTest() {
        // given
        User user = new User("foo", "password");
        User target = new User("target", "password");
        given(userRepository.findMeById(any())).willReturn(user);
        given(userRepository.searchUsersByUsername(any())).willReturn(
                new SliceImpl<>(List.of(target), Pageable.unpaged(), true));

        // when
        UserDto.SearchRequest request = new UserDto.SearchRequest("foo", 0, 0, null, true);
        Slice<UserDto.Response> responseSlice = userService.searchUsers(1L, request);

        // then
        assertThat(responseSlice.getContent()).extracting("username").containsExactly(target.getUsername());
    }

    @Test
    @DisplayName("유저 정보 가져오기 성공")
    public void fetchUserTest_Success() {
        // given
        User user = new User("foo", "password");
        User target = new User("target", "password");
        given(userRepository.findMeById(any())).willReturn(user);
        given(userRepository.findByUsername(any())).willReturn(Optional.of(target));

        // when
        UserDto.DetailedResponse response = userService.fetchUser(1L, "target");

        // then
        assertThat(response.getUsername()).isEqualTo(target.getUsername());
    }

    @Test
    @DisplayName("유저 정보 가져오기 실패: 존재하지 않는 유저 네임")
    public void fetchUserTest_Failure_UsernameNotFound() {
        // given
        User user = new User("foo", "password");
        given(userRepository.findMeById(any())).willReturn(user);

        // when
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

        // then
        assertThrows(UsernameNotFoundException.class, () -> userService.fetchUser(1L, "target"));
    }

    @Test
    @DisplayName("유저 정보 가져오기 실패: 블락된 유저")
    public void fetchUserTest_Failure_BlockedUser() {
        // given
        User user = new User("foo", "password");
        User target = new User("target", "password");
        given(userRepository.findMeById(any())).willReturn(user);
        given(userRepository.findByUsername(any())).willReturn(Optional.of(target));

        // when
        target.block("test");

        // then
        assertThrows(AccessBlockedOrDeletedUserException.class, () -> userService.fetchUser(1L, "target"));
    }
}