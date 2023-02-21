package com.matzip.server.domain.me.service;

import com.matzip.server.domain.comment.repository.CommentRepository;
import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.me.dto.MeDto.PasswordChangeRequest;
import com.matzip.server.domain.me.dto.MeDto.PatchRequest;
import com.matzip.server.domain.me.dto.MeDto.UsernameChangeRequest;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.HeartRepository;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.scrap.repository.ScrapRepository;
import com.matzip.server.domain.user.dto.UserDto.Response;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.FollowRepository;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.config.TestQueryDslConfig;
import com.matzip.server.global.utils.TestDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@ActiveProfiles("test")
@DisplayName("MeService 테스트")
class MeServiceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ScrapRepository scrapRepository;
    @Autowired
    private HeartRepository heartRepository;
    @Autowired
    private FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    @MockBean
    private ImageService imageService;

    private MeService meService;
    private List<User> users;

    @PostConstruct
    void init() {
        users = TestDataUtils.testData();
        meService = new MeService(userRepository, reviewRepository, commentRepository, scrapRepository,
                                  heartRepository, followRepository, passwordEncoder, imageService);

        given(imageService.uploadImage(any(), any())).willReturn("https://" + UUID.randomUUID() + ".url");
        given(imageService.deleteImage(any())).willReturn("https://" + UUID.randomUUID() + ".url");
    }

    @BeforeEach
    public void setUp() {
        userRepository.saveAll(users);
    }

    @Test
    @DisplayName("비밀번호 수정 테스트")
    void changePasswordTest() {
        // given
        User user = users.get(0);
        String oldPassword = user.getPassword();
        String newPassword = "newPassword";
        PasswordChangeRequest request = new PasswordChangeRequest(newPassword);

        // when
        meService.changePassword(user.getId(), request);

        // then
        assertThat(user.getPassword()).isNotEqualTo(oldPassword);
        assertThat(passwordEncoder.matches(newPassword, user.getPassword())).isTrue();
    }

    @Test
    @DisplayName("유저네임 수정 테스트")
    void changeUsernameTest() {
        // given
        User user = users.get(0);
        String oldUsername = user.getUsername();
        String newUsername = "newUsername";
        UsernameChangeRequest request = new UsernameChangeRequest(newUsername);

        // when
        Response response = meService.changeUsername(user.getId(), request);

        // then
        assertThat(response.getUsername()).isEqualTo(newUsername);
        assertThat(response.getUsername()).isNotEqualTo(oldUsername);
    }

    @Test
    @DisplayName("회원 탈퇴 테스트")
    void deleteMeTest() {
        // given
        User user = users.get(0);
        List<Long> reviewIds = user.getReviews().stream().map(Review::getId).toList();
        long expectedUserCount = userRepository.count() - 1;
        long expectedReviewCount = reviewRepository.count() - user.getReviews().size();
        long expectedCommentCount = commentRepository.count()
                                    - user.getReviews().stream().map(r -> r.getComments().size()).reduce(Integer::sum).orElseThrow()
                                    - user.getComments().stream().filter(c -> !reviewIds.contains(c.getReview().getId())).count();
        long expectedScrapCount = scrapRepository.count()
                                  - user.getReviews().stream().map(r -> r.getScraps().size()).reduce(Integer::sum).orElseThrow()
                                  - user.getScraps().stream().filter(s -> !reviewIds.contains(s.getReview().getId())).count();
        long expectedHeartCount = heartRepository.count()
                                  - user.getReviews().stream().map(r -> r.getHearts().size()).reduce(Integer::sum).orElseThrow()
                                  - user.getHearts().stream().filter(h -> !reviewIds.contains(h.getReview().getId())).count();
        long expectedFollowCount = followRepository.count() - (user.getFollowers().size() + user.getFollowings().size());

        // when
        meService.deleteMe(user.getId());

        // then
        assertThat(userRepository.count()).isEqualTo(expectedUserCount);
        assertThat(reviewRepository.count()).isEqualTo(expectedReviewCount);
        assertThat(commentRepository.count()).isEqualTo(expectedCommentCount);
        assertThat(scrapRepository.count()).isEqualTo(expectedScrapCount);
        assertThat(heartRepository.count()).isEqualTo(expectedHeartCount);
        assertThat(followRepository.count()).isEqualTo(expectedFollowCount);
    }

    @Test
    @DisplayName("회원 정보 수정 테스트")
    void patchMeTest() {
        // given
        User user = users.get(0);
        String oldProfile = user.getProfileString();
        String newProfile = "new profile";
        PatchRequest request = new PatchRequest(null, newProfile);

        // when
        Response response = meService.patchMe(user.getId(), request);

        // then
        assertThat(response.getProfileString()).isEqualTo(newProfile);
        assertThat(response.getProfileString()).isNotEqualTo(oldProfile);
    }
}