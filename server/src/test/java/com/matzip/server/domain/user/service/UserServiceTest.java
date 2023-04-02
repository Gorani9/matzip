package com.matzip.server.domain.user.service;

import com.matzip.server.domain.record.service.RecordService;
import com.matzip.server.domain.user.dto.UserDto.DetailedResponse;
import com.matzip.server.domain.user.dto.UserDto.Response;
import com.matzip.server.domain.user.exception.FollowMeException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.FollowRepository;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.config.TestQueryDslConfig;
import com.matzip.server.global.utils.TestDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("UserService 테스트")
class UserServiceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FollowRepository followRepository;
    @MockBean
    private RecordService recordService;
    private UserService userService;

    @PostConstruct
    void init() {
        userService = new UserService(userRepository, followRepository, recordService);
    }

    @BeforeEach
    public void setUp() {
        userRepository.saveAll(TestDataUtils.testData());
    }

    @Test
    @DisplayName("회원 조회 테스트")
    void fetchUserTest() {
        // given
        User user = userRepository.findByUsername("user-01").orElseThrow();
        String username = "user-02";

        // when
        DetailedResponse response = userService.fetchUser(user.getId(), username);

        // then
        assertThat(response.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("회원 팔로우 테스트: 나를 팔로우할 경우")
    void followUserTest_FollowMe() {
        // given
        User user = userRepository.findByUsername("user-01").orElseThrow();
        String username = "user-01";

        // then
        assertThatThrownBy(() -> userService.followUser(user.getId(), username))
                .isInstanceOf(FollowMeException.class);
    }

    @Test
    @DisplayName("회원 팔로우 테스트: 새롭게 팔로우 하는 경우")
    void followUserTest_Basic() {
        // given
        User user = userRepository.findByUsername("user-01").orElseThrow();
        User followee = userRepository.findByUsername("user-04").orElseThrow();
        int beforeNumberOfFollowers = followee.getFollowers().size();

        // when
        Response response = userService.followUser(user.getId(), followee.getUsername());

        // then
        assertThat(response.getIsMyFollowing()).isTrue();
        assertThat(followee.getFollowers().size()).isEqualTo(beforeNumberOfFollowers + 1);
    }

    @Test
    @DisplayName("회원 팔로우 테스트: 이미 팔로우 하는 경우")
    void followUserTest_AlreadyFollowing() {
        // given
        User user = userRepository.findByUsername("user-01").orElseThrow();
        User followee = userRepository.findByUsername("user-02").orElseThrow();
        int beforeNumberOfFollowers = followee.getFollowers().size();

        // when
        Response response = userService.followUser(user.getId(), followee.getUsername());

        // then
        assertThat(response.getIsMyFollowing()).isTrue();
        assertThat(followee.getFollowers().size()).isEqualTo(beforeNumberOfFollowers);
    }

    @Test
    @DisplayName("회원 팔로우 취소 테스트: 팔로우 하고 있는 경우")
    void unfollowUserTest_Basic() {
        // given
        User user = userRepository.findByUsername("user-01").orElseThrow();
        User followee = userRepository.findByUsername("user-02").orElseThrow();
        int beforeNumberOfFollowers = followee.getFollowers().size();

        // when
        Response response = userService.unfollowUser(user.getId(), followee.getUsername());

        // then
        assertThat(response.getIsMyFollowing()).isFalse();
        assertThat(followee.getFollowers().size()).isEqualTo(beforeNumberOfFollowers - 1);
    }

    @Test
    @DisplayName("회원 팔로우 취소 테스트: 팔로우 하고 있지 않는 경우")
    void unfollowUserTest_NotFollowing() {
        // given
        User user = userRepository.findByUsername("user-01").orElseThrow();
        User followee = userRepository.findByUsername("user-04").orElseThrow();
        int beforeNumberOfFollowers = followee.getFollowers().size();

        // when
        Response response = userService.unfollowUser(user.getId(), followee.getUsername());

        // then
        assertThat(response.getIsMyFollowing()).isFalse();
        assertThat(followee.getFollowers().size()).isEqualTo(beforeNumberOfFollowers);
    }
}