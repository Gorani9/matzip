package com.matzip.server.domain.me;

import com.matzip.server.domain.me.model.Follow;
import com.matzip.server.domain.me.repository.FollowRepository;
import com.matzip.server.domain.user.dto.UserDto.SearchRequest;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.config.TestQueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static com.matzip.server.domain.user.model.UserProperty.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Tag("RepositoryTest")
public class FollowRepositoryTest {
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("팔로워 검색 기본 테스트: 모든 팔로워 생성시점 기준으로 오름차순 검색")
    void searchMyFollowersTest() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User follower1 = userRepository.save(new User("follower1", "password"));
        User follower2 = userRepository.save(new User("follower2", "password"));
        User follower3 = userRepository.save(new User("follower3", "password"));
        followRepository.save(new Follow(follower1, user));
        followRepository.save(new Follow(follower2, user));
        followRepository.save(new Follow(follower3, user));

        // when
        SearchRequest request = new SearchRequest("", 0, 10, null, true);
        Slice<User> users = followRepository.searchMyFollowersByUsername(request, user.getId());

        // then
        assertThat(users).containsExactly(follower1, follower2, follower3);
    }

    @Test
    @DisplayName("팔로워 검색 기본 테스트: 특정 문자열을 포함하는 유저네임을 가진 팔로워 검색")
    void searchMyFollowersTest_UsingUsername() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User follower1 = userRepository.save(new User("foo", "password"));
        User follower2 = userRepository.save(new User("follower", "password"));
        User follower3 = userRepository.save(new User("bar", "password"));
        followRepository.save(new Follow(follower1, user));
        followRepository.save(new Follow(follower2, user));
        followRepository.save(new Follow(follower3, user));

        // when
        SearchRequest request = new SearchRequest("fo", 0, 10, null, true);
        Slice<User> users = followRepository.searchMyFollowersByUsername(request, user.getId());

        // then
        assertThat(users).containsExactly(follower1, follower2);
    }

    @Test
    @DisplayName("팔로워 검색 기본 테스트: 블락되거나 탈퇴한 유저 결과에서 제외")
    void searchMyFollowersTest_UserBlockedOrDeleted() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User follower1 = userRepository.save(new User("follower1", "password"));
        User follower2 = userRepository.save(new User("follower2", "password"));
        User follower3 = userRepository.save(new User("follower3", "password"));
        followRepository.save(new Follow(follower1, user));
        followRepository.save(new Follow(follower2, user));
        followRepository.save(new Follow(follower3, user));
        follower1.block("test");
        follower3.delete();

        // when
        SearchRequest request = new SearchRequest("", 0, 10, null, true);
        Slice<User> users = followRepository.searchMyFollowersByUsername(request, user.getId());

        // then
        assertThat(users).containsExactly(follower2);
    }

    @Test
    @DisplayName("팔로워 검색 기본 테스트: 유저네임으로 내림차순 정렬")
    void searchMyFollowersTest_SortByUsername_Desc() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User follower1 = userRepository.save(new User("s-user", "password"));
        User follower2 = userRepository.save(new User("c-user", "password"));
        User follower3 = userRepository.save(new User("k-user", "password"));
        followRepository.save(new Follow(follower1, user));
        followRepository.save(new Follow(follower2, user));
        followRepository.save(new Follow(follower3, user));

        // when
        SearchRequest request = new SearchRequest("", 0, 10, USERNAME, false);
        Slice<User> users = followRepository.searchMyFollowersByUsername(request, user.getId());

        // then
        assertThat(users).containsExactly(follower1, follower3, follower2);
    }

    @Test
    @DisplayName("팔로워 검색 기본 테스트: 유저 레벨 내림차순 정렬")
    void searchMyFollowersTest_SortByLevel_Desc() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User follower1 = userRepository.save(new User("follower1", "password"));
        User follower2 = userRepository.save(new User("follower2", "password"));
        User follower3 = userRepository.save(new User("follower3", "password"));
        followRepository.save(new Follow(follower1, user));
        followRepository.save(new Follow(follower2, user));
        followRepository.save(new Follow(follower3, user));
        for (int i = 0; i < 3; i++) follower1.levelUp();
        for (int i = 0; i < 10; i++) follower2.levelUp();
        for (int i = 0; i < 2; i++) follower3.levelUp();

        // when
        SearchRequest request = new SearchRequest("", 0, 10, MATZIP_LEVEL, false);
        Slice<User> users = followRepository.searchMyFollowersByUsername(request, user.getId());

        // then
        assertThat(users).containsExactly(follower2, follower1, follower3);
    }

    @Test
    @DisplayName("팔로워 검색 기본 테스트: 팔로워 수 오름차순 정렬")
    void searchMyFollowersTest_SortByNumberOfFollowers_Asc() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User follower1 = userRepository.save(new User("follower1", "password"));
        User follower2 = userRepository.save(new User("follower2", "password"));
        User follower3 = userRepository.save(new User("follower3", "password"));
        followRepository.save(new Follow(follower1, user));
        followRepository.save(new Follow(follower2, user));
        followRepository.save(new Follow(follower3, user));
        for (int i = 0; i < 8; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), follower1));
        for (int i = 0; i < 2; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), follower2));
        for (int i = 0; i < 6; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), follower3));

        // when
        SearchRequest request = new SearchRequest("", 0, 10, NUMBER_OF_FOLLOWERS, true);
        Slice<User> users = followRepository.searchMyFollowersByUsername(request, user.getId());

        // then
        assertThat(users).containsExactly(follower2, follower3, follower1);
    }

    @Test
    @DisplayName("팔로잉 검색 기본 테스트")
    void searchMyFollowingsTest() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User followee1 = userRepository.save(new User("followee1", "password"));
        User followee2 = userRepository.save(new User("followee2", "password"));
        User followee3 = userRepository.save(new User("followee3", "password"));
        followRepository.save(new Follow(user, followee1));
        followRepository.save(new Follow(user, followee2));
        followRepository.save(new Follow(user, followee3));

        // when
        SearchRequest request = new SearchRequest("", 0, 10, null, true);
        Slice<User> users = followRepository.searchMyFollowingsByUsername(request, user.getId());

        // then
        assertThat(users).containsExactly(followee1, followee2, followee3);
    }

    @Test
    @DisplayName("팔로잉 검색 기본 테스트: 팔로워 수 내림차순 정렬")
    void searchMyFollowingsTest_SortByNumberOfFollowers_Desc() {
        // given
        User user = userRepository.save(new User("user", "password"));
        User followee1 = userRepository.save(new User("followee1", "password"));
        User followee2 = userRepository.save(new User("followee2", "password"));
        User followee3 = userRepository.save(new User("followee3", "password"));
        followRepository.save(new Follow(user, followee1));
        followRepository.save(new Follow(user, followee2));
        followRepository.save(new Follow(user, followee3));
        for (int i = 0; i < 8; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), followee1));
        for (int i = 0; i < 2; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), followee2));
        for (int i = 0; i < 6; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), "password")), followee3));

        // when
        SearchRequest request = new SearchRequest("", 0, 10, NUMBER_OF_FOLLOWERS, false);
        Slice<User> users = followRepository.searchMyFollowingsByUsername(request, user.getId());

        // then
        assertThat(users).containsExactly(followee1, followee3, followee2);
    }
}
