package com.matzip.server.domain.user;

import com.matzip.server.domain.admin.dto.AdminDto.UserSearchRequest;
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
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FollowRepository followRepository;

    public static final String PASSWORD = "password";

    @Test
    @DisplayName("유저 검색 기본 테스트: 생성기준 내림차순 정렬")
    public void searchUserTest_Basic_Asc() {
        // given
        User user1 = userRepository.save(new User("user1", PASSWORD));
        User user2 = userRepository.save(new User("user2", PASSWORD));
        User user3 = userRepository.save(new User("user3", PASSWORD));
        SearchRequest searchRequest = new SearchRequest("user", 0, 10, null, false);

        // when
        Slice<User> users = userRepository.searchUsersByUsername(searchRequest);

        // then
        assertThat(users.getContent()).containsExactly(user3, user2, user1);
    }

    @Test
    @DisplayName("유저 검색 기본 테스트: 생성기준 오름차순 정렬")
    public void searchUserTest_Basic_Desc() {
        // given
        User user1 = userRepository.save(new User("user1", PASSWORD));
        User user2 = userRepository.save(new User("user2", PASSWORD));
        User user3 = userRepository.save(new User("user3", PASSWORD));
        SearchRequest searchRequest = new SearchRequest("user", 0, 10, null, true);

        // when
        Slice<User> users = userRepository.searchUsersByUsername(searchRequest);

        // then
        assertThat(users.getContent()).containsExactly(user1, user2, user3);
    }

    @Test
    @DisplayName("유저 검색 기본 테스트: 삭제되거나 블락 처리된 유저 제외")
    public void searchUserTest_Basic_Exclusion() {
        // given
        User user1 = userRepository.save(new User("user1", PASSWORD));
        User user2 = userRepository.save(new User("user2", PASSWORD));
        User user3 = userRepository.save(new User("user3", PASSWORD));
        SearchRequest searchRequest = new SearchRequest("user", 0, 10, null, true);
        user2.delete();
        user3.block("test");

        // when
        Slice<User> users = userRepository.searchUsersByUsername(searchRequest);

        // then
        assertThat(users.getContent()).containsExactly(user1);
    }

    @Test
    @DisplayName("유저 검색 정렬 테스트: 유저네임 기준 오름차순 정렬")
    public void searchUserTest_Sort_Username_Asc() {
        // given
        User user1 = userRepository.save(new User("e-user", PASSWORD));
        User user2 = userRepository.save(new User("z-user", PASSWORD));
        User user3 = userRepository.save(new User("k-user", PASSWORD));
        SearchRequest searchRequest = new SearchRequest("user", 0, 10, USERNAME, true);

        // when
        Slice<User> users = userRepository.searchUsersByUsername(searchRequest);

        // then
        assertThat(users.getContent()).containsExactly(user1, user3, user2);
    }

    @Test
    @DisplayName("유저 검색 정렬 테스트: 유저네임 기준 내림차순 정렬")
    public void searchUserTest_Sort_Username_Desc() {
        // given
        User user1 = userRepository.save(new User("l-user", PASSWORD));
        User user2 = userRepository.save(new User("y-user", PASSWORD));
        User user3 = userRepository.save(new User("s-user", PASSWORD));
        SearchRequest searchRequest = new SearchRequest("user", 0, 10, USERNAME, false);

        // when
        Slice<User> users = userRepository.searchUsersByUsername(searchRequest);

        // then
        assertThat(users.getContent()).containsExactly(user2, user3, user1);
    }

    @Test
    @DisplayName("유저 검색 정렬 테스트: 레벨 기준 오름차순 정렬")
    public void searchUserTest_Sort_Level_Asc() {
        // given
        User user1 = userRepository.save(new User("user1", PASSWORD));
        User user2 = userRepository.save(new User("user2", PASSWORD));
        User user3 = userRepository.save(new User("user3", PASSWORD));
        SearchRequest searchRequest = new SearchRequest("user", 0, 10, MATZIP_LEVEL, true);
        for (int i = 0; i < 6; i++) user1.levelUp();
        for (int i = 0; i < 3; i++) user2.levelUp();
        for (int i = 0; i < 5; i++) user3.levelUp();

        // when
        Slice<User> users = userRepository.searchUsersByUsername(searchRequest);

        // then
        assertThat(users.getContent()).containsExactly(user2, user3, user1);
    }

    @Test
    @DisplayName("유저 검색 정렬 테스트: 레벨 기준 내림차순 정렬")
    public void searchUserTest_Sort_Level_Desc() {
        // given
        User user1 = userRepository.save(new User("user1", PASSWORD));
        User user2 = userRepository.save(new User("user2", PASSWORD));
        User user3 = userRepository.save(new User("user3", PASSWORD));
        SearchRequest searchRequest = new SearchRequest("user", 0, 10, MATZIP_LEVEL, false);
        for (int i = 0; i < 6; i++) user1.levelUp();
        for (int i = 0; i < 3; i++) user2.levelUp();
        for (int i = 0; i < 8; i++) user3.levelUp();

        // when
        Slice<User> users = userRepository.searchUsersByUsername(searchRequest);

        // then
        assertThat(users.getContent()).containsExactly(user3, user1, user2);
    }

    @Test
    @DisplayName("유저 검색 정렬 테스트: 팔로워 기준 오름차순 정렬")
    public void searchUserTest_Sort_Follower_Asc() {
        // given
        User user1 = userRepository.save(new User("user1", PASSWORD));
        User user2 = userRepository.save(new User("user2", PASSWORD));
        User user3 = userRepository.save(new User("user3", PASSWORD));
        SearchRequest searchRequest = new SearchRequest("user", 0, 10, NUMBER_OF_FOLLOWERS, true);
        for (int i = 0; i < 3; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), user1));
        for (int i = 0; i < 5; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), user2));
        for (int i = 0; i < 6; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), user3));

        // when
        Slice<User> users = userRepository.searchUsersByUsername(searchRequest);

        // then
        assertThat(users.getContent()).containsExactly(user1, user2, user3);
    }

    @Test
    @DisplayName("유저 검색 정렬 테스트: 팔로워 기준 내림차순 정렬")
    public void searchUserTest_Sort_Follower_Desc() {
        // given
        User user1 = userRepository.save(new User("user1", PASSWORD));
        User user2 = userRepository.save(new User("user2", PASSWORD));
        User user3 = userRepository.save(new User("user3", PASSWORD));
        SearchRequest searchRequest = new SearchRequest("user", 0, 10, NUMBER_OF_FOLLOWERS, false);
        for (int i = 0; i < 3; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), user1));
        for (int i = 0; i < 12; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), user2));
        for (int i = 0; i < 6; i++) followRepository.save(new Follow(
                userRepository.save(new User(UUID.randomUUID().toString(), PASSWORD)), user3));

        // when
        Slice<User> users = userRepository.searchUsersByUsername(searchRequest);

        // then
        assertThat(users.getContent()).containsExactly(user2, user3, user1);
    }

    @Test
    @DisplayName("관리자 유저 검색 테스트: 삭제된 유저 제외, 블락된 유저 포함")
    public void adminSearchUserTest_Basic_Exclusion() {
        // given
        User user1 = userRepository.save(new User("user1", PASSWORD));
        User user2 = userRepository.save(new User("user2", PASSWORD));
        User user3 = userRepository.save(new User("user3", PASSWORD));
        UserSearchRequest searchRequest = new UserSearchRequest("user", 0, 10, null, true, true);
        user2.delete();
        user3.block("test");

        // when
        Slice<User> users = userRepository.adminSearchUsersByUsername(searchRequest);

        // then
        assertThat(users.getContent()).containsExactly(user1, user3);
    }
}

