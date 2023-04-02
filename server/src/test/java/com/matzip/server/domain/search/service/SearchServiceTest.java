package com.matzip.server.domain.search.service;

import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.search.dto.SearchDto.UserSearch;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
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

import javax.annotation.PostConstruct;

import static com.matzip.server.domain.user.model.UserProperty.USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("SearchService 테스트")
public class SearchServiceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    private SearchService searchService;

    @PostConstruct
    void init() {
        searchService = new SearchService(userRepository, reviewRepository);
    }

    @BeforeEach
    public void setUp() {
        userRepository.saveAll(TestDataUtils.testData());
    }


    @Test
    @DisplayName("회원 검색 테스트")
    void searchUserTest() {
        // given
        User user = userRepository.findByUsername("user-01").orElseThrow();
        String username = "user";
        UserSearch request = new UserSearch(username, 0, 5, USERNAME, true);

        // when
        Slice<UserDto.Response> responses = searchService.searchUsers(user.getId(), request);

        // then
        assertThat(responses.getNumberOfElements()).isEqualTo(5);
        assertThat(responses.getContent()).extracting("username")
                .containsExactlyInAnyOrder("user-01", "user-02", "user-03", "user-04", "user-05");
    }
}
