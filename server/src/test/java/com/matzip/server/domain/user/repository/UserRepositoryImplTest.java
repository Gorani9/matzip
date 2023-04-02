package com.matzip.server.domain.user.repository;

import com.matzip.server.domain.search.dto.SearchDto.UserSearch;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.config.TestQueryDslConfig;
import com.matzip.server.global.utils.TestDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import static com.matzip.server.domain.user.model.UserProperty.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("UserRepository 테스트")
class UserRepositoryImplTest {
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository.saveAll(TestDataUtils.testData());
    }

    @Test
    @DisplayName("페이징 테스트: 다음 페이지가 있는 경우")
    void pagingHasNextTest() {
        // given
        UserSearch request = new UserSearch("user", 0, 4, null, false);

        // when
        Slice<User> users = userRepository.searchUsersByUsername(request);

        // then
        assertThat(users.getNumberOfElements()).isEqualTo(4);
        assertThat(users.hasNext()).isEqualTo(true);
    }

    @Test
    @DisplayName("페이징 테스트: 마지막 페이지인 경우")
    void pagingTest2() {
        // given
        UserSearch request = new UserSearch("user", 0, 5, null, false);

        // when
        Slice<User> users = userRepository.searchUsersByUsername(request);

        // then
        assertThat(users.getNumberOfElements()).isEqualTo(5);
        assertThat(users.hasNext()).isEqualTo(false);
    }

    @Test
    @DisplayName("검색 테스트")
    void searchingTest() {
        // given
        UserSearch request = new UserSearch("01", 0, 5, null, false);

        // when
        Slice<User> users = userRepository.searchUsersByUsername(request);

        // then
        assertThat(users.getNumberOfElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("정렬 테스트: Username 기준")
    void sortingByUsernameTest() {
        // given
        UserSearch request = new UserSearch("user", 0, 5, USERNAME, true);

        // when
        Slice<User> users = userRepository.searchUsersByUsername(request);

        // then
        assertThat(users.getNumberOfElements()).isEqualTo(5);
        assertThat(users.getContent()).extracting("username")
                .containsExactly("user-01", "user-02", "user-03", "user-04", "user-05");
    }

    @Test
    @DisplayName("정렬 테스트: Level 기준")
    void sortingByLevelTest() {
        // given
        UserSearch request = new UserSearch("user", 0, 5, MATZIP_LEVEL, true);

        // when
        Slice<User> users = userRepository.searchUsersByUsername(request);

        // then
        assertThat(users.getNumberOfElements()).isEqualTo(5);
        assertThat(users.getContent()).extracting("username")
                .containsExactly("user-04", "user-01", "user-05", "user-03", "user-02");
    }

    @Test
    @DisplayName("정렬 테스트: 팔로워 수 기준")
    void sortingByFollowersTest() {
        // given
        UserSearch request = new UserSearch("user", 0, 5, NUMBER_OF_FOLLOWERS, true);

        // when
        Slice<User> users = userRepository.searchUsersByUsername(request);

        // then
        assertThat(users.getNumberOfElements()).isEqualTo(5);
        assertThat(users.getContent()).extracting("username")
                .containsExactly("user-04", "user-03", "user-02", "user-01", "user-05");
    }

    @Test
    @DisplayName("정렬 테스트: 가입 일자 기준")
    void sortingByCreatedAtTest() {
        // given
        UserSearch request = new UserSearch("user", 0, 5, null, true);

        // when
        Slice<User> users = userRepository.searchUsersByUsername(request);

        // then
        assertThat(users.getNumberOfElements()).isEqualTo(5);
        assertThat(users.getContent()).extracting("username")
                .containsExactly("user-01", "user-02", "user-03", "user-04", "user-05");
    }
}