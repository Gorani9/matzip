package com.matzip.server.domain.user.service;

import com.matzip.server.domain.me.repository.FollowRepository;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.exception.UsernameAlreadyExistsException;
import com.matzip.server.domain.user.exception.UsernameNotFoundException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.matzip.server.domain.user.model.UserProperty.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("ServiceTest")
class UserServiceTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void createDefaultUser() {
        userService.createUser(new UserDto.SignUpRequest("foo", "password"));
    }

    @Test
    void createUserTest_OK() {
        // given
        long beforeUserCount = userRepository.count();
        int numberOfUsers = 100;
        String[] usernames = new String[numberOfUsers];
        for (int i = 0; i < numberOfUsers; i++) {
            usernames[i] = "user" + UUID.randomUUID();
        }

        // when
        for (int i = 0; i < numberOfUsers; i++) {
            userService.createUser(new UserDto.SignUpRequest(usernames[i], "password"));
        }

        // then
        assertThat(userRepository.count()).isEqualTo(beforeUserCount + numberOfUsers);
    }

    @Test
    void createUserTest_CONFLICT() {
        // given
        userService.createUser(new UserDto.SignUpRequest("user", "password"));

        // when
        UserDto.SignUpRequest signUpRequest = new UserDto.SignUpRequest("user", "password");

        // then
        assertThrowsExactly(UsernameAlreadyExistsException.class, () -> userService.createUser(signUpRequest));
    }

    @Test
    void searchUsersTest() {
        // given
        int numberOfUsers = 100;
        int pageSize = 5;
        int pageOffset = 1;
        String[] usernames = new String[numberOfUsers];
        for (int i = 0; i < numberOfUsers; i++) {
            usernames[i] = "user" + UUID.randomUUID();
            userService.createUser(new UserDto.SignUpRequest(usernames[i], "password"));
        }
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();

        // when
        UserDto.SearchRequest ascRequest = new UserDto.SearchRequest("user", pageOffset, pageSize, null, true);
        UserDto.SearchRequest descRequest = new UserDto.SearchRequest("user", pageOffset, pageSize, null, false);
        Slice<UserDto.Response> ascResponse = userService.searchUsers(fooUserId, ascRequest);
        Slice<UserDto.Response> descResponse = userService.searchUsers(fooUserId, descRequest);
        String[] expectedAscUsernames = new String[pageSize];
        String[] expectedDescUsernames = new String[pageSize];
        for (int i = 0; i < pageSize; i++) {
            expectedAscUsernames[i] = usernames[pageOffset * pageSize + i];
            expectedDescUsernames[i] = usernames[numberOfUsers - (pageOffset * pageSize + 1) - i];
        }

        // then
        assertThat(ascResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(ascResponse.getContent()).extracting("username").containsExactly(expectedAscUsernames);
        assertThat(descResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(descResponse.getContent()).extracting("username").containsExactly(expectedDescUsernames);
    }

    @Test
    void searchUsersSortByUsernameTest() {
        // given
        int numberOfUsers = 100;
        int pageSize = 5;
        int pageOffset = 1;
        String[] usernames = new String[numberOfUsers];
        for (int i = 0; i < numberOfUsers; i++) {
            usernames[i] = "user" + UUID.randomUUID();
            userService.createUser(new UserDto.SignUpRequest(usernames[i], "password"));
        }
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();

        // when
        UserDto.SearchRequest ascRequest = new UserDto.SearchRequest("user", pageOffset, pageSize, USERNAME, true);
        UserDto.SearchRequest descRequest = new UserDto.SearchRequest("user", pageOffset, pageSize, USERNAME, false);
        Slice<UserDto.Response> ascResponse = userService.searchUsers(fooUserId, ascRequest);
        Slice<UserDto.Response> descResponse = userService.searchUsers(fooUserId, descRequest);
        String[] expectedAscUsernames = new String[pageSize];
        String[] expectedDescUsernames = new String[pageSize];
        Arrays.sort(usernames);
        for (int i = 0; i < pageSize; i++) {
            expectedAscUsernames[i] = usernames[pageOffset * pageSize + i];
            expectedDescUsernames[i] = usernames[numberOfUsers - (pageOffset * pageSize + 1) - i];
        }

        // then
        assertThat(ascResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(ascResponse.getContent()).extracting("username").containsExactly(expectedAscUsernames);
        assertThat(descResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(descResponse.getContent()).extracting("username").containsExactly(expectedDescUsernames);
    }

    @Test
    void searchUsersSortByMatzipLevelTest() {
        // given
        int numberOfUsers = 100;
        int pageSize = 5;
        int pageOffset = 1;
        List<Integer> levels = new ArrayList<>(numberOfUsers);
        for (int i = 0; i < numberOfUsers; i++) levels.add(i + 1);
        Collections.shuffle(levels);
        List<Map.Entry<String, Integer>> userLevels = new ArrayList<>();
        for (int i = 0; i < numberOfUsers; i++) {
            String username = "user" + UUID.randomUUID();
            userService.createUser(new UserDto.SignUpRequest(username, "password"));
            User user = userRepository.findByUsername(username).orElseThrow();
            for (int j = 0; j < levels.get(i); j++) user.levelUp();
            userLevels.add(new AbstractMap.SimpleEntry<>(username, levels.get(i)));
        }
        UserDto.SearchRequest ascRequest = new UserDto.SearchRequest("user", pageOffset, pageSize, MATZIP_LEVEL, true);
        UserDto.SearchRequest descRequest = new UserDto.SearchRequest("user", pageOffset, pageSize, MATZIP_LEVEL, false);
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();

        // when
        Slice<UserDto.Response> ascResponse = userService.searchUsers(fooUserId, ascRequest);
        Slice<UserDto.Response> descResponse = userService.searchUsers(fooUserId, descRequest);
        userLevels.sort(Map.Entry.comparingByValue());
        String[] expectedAscUsernames = userLevels.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(Map.Entry::getKey).toArray(String[]::new);
        userLevels.sort((o1, o2) -> -Integer.compare(o1.getValue(), o2.getValue()));
        String[] expectedDescUsernames = userLevels.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(Map.Entry::getKey).toArray(String[]::new);

        // then
        assertThat(ascResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(ascResponse.getContent()).extracting("username").containsExactly(expectedAscUsernames);
        assertThat(descResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(descResponse.getContent()).extracting("username").containsExactly(expectedDescUsernames);
    }

    @Test
    void searchUsersSortByNumberOfFollowersTest() {
        // given
        int numberOfUsers = 100;
        int pageSize = 5;
        int pageOffset = 1;
        List<Map.Entry<String, Integer>> followers = new ArrayList<>();
        for (int i = 0; i < numberOfUsers; i++) {
            String uuid = UUID.randomUUID().toString();
            String username = "user" + uuid;
            userService.createUser(new UserDto.SignUpRequest(username, "password"));
            User user = userRepository.findByUsername(username).orElseThrow();

            /* giving unique number of followers */
            for (Map.Entry<String, Integer> entry : followers) {
                User following = userRepository.findByUsername(entry.getKey()).orElseThrow();
                followRepository.save(following.addFollower(user));
                entry.setValue(entry.getValue() + 1);
            }

            followers.add(new AbstractMap.SimpleEntry<>(username, 0));
        }
        UserDto.SearchRequest ascRequest = new UserDto.SearchRequest("user", pageOffset, pageSize, NUMBER_OF_FOLLOWERS, true);
        UserDto.SearchRequest descRequest = new UserDto.SearchRequest("user", pageOffset, pageSize, NUMBER_OF_FOLLOWERS, false);
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();

        // when
        Slice<UserDto.Response> ascResponse = userService.searchUsers(fooUserId, ascRequest);
        Slice<UserDto.Response> descResponse = userService.searchUsers(fooUserId, descRequest);
        followers.sort(Map.Entry.comparingByValue());
        String[] expectedAscUsernames = followers.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(Map.Entry::getKey).toArray(String[]::new);
        followers.sort((o1, o2) -> -Integer.compare(o1.getValue(), o2.getValue()));
        String[] expectedDescUsernames = followers.stream()
                .skip(pageOffset * pageSize).limit(pageSize).map(Map.Entry::getKey).toArray(String[]::new);

        // then
        assertThat(ascResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(ascResponse.getContent()).extracting("username").containsExactly(expectedAscUsernames);
        assertThat(descResponse.getNumberOfElements()).isEqualTo(pageSize);
        assertThat(descResponse.getContent()).extracting("username").containsExactly(expectedDescUsernames);
    }

    @Test
    void getUserTest() {
        // given
        userService.createUser(new UserDto.SignUpRequest("bar", "password"));
        Long fooUserId = userRepository.findByUsername("foo").orElseThrow().getId();

        // when
        UserDto.Response barResponse = userService.fetchUser(fooUserId, "bar");

        // then
        assertThat(barResponse.getUsername()).isEqualTo("bar");
        assertThrowsExactly(UsernameNotFoundException.class, () -> userService.fetchUser(fooUserId, "not_existing_user"));
    }
}