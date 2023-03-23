package com.matzip.server.domain.record.service;

import com.matzip.server.domain.record.model.LoginRecord;
import com.matzip.server.domain.record.model.ReviewRecord;
import com.matzip.server.domain.record.repository.LoginRecordRepository;
import com.matzip.server.domain.record.repository.ReviewRecordRepository;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional(propagation = Propagation.REQUIRED)
@RequiredArgsConstructor
public class RecordService {
    private final LoginRecordRepository loginRecordRepository;
    private final ReviewRecordRepository reviewRecordRepository;

    private static final int POINTS_PER_REVIEW = 10;
    private static final int POINTS_PER_COMMENT = 5;
    private static final int POINTS_PER_SCRAP = 3;
    private static final int POINTS_PER_FOLLOW = 3;
    private static final int POINTS_PER_LIKE = 2;
    private static final int POINTS_PER_ACTION = 1;
    private static final int POINTS_PER_LOGIN = 1;

    private int neededPointsForLevelUp(int level) {
        return switch (level) {
            case 1 -> 7;
            case 2 -> 14;
            case 3 -> 21;
            case 4 -> 28;
            case 5 -> 38;
            case 6 -> 48;
            case 7 -> 61;
            case 8 -> 74;
            case 9 -> 87;
            default -> (int) Math.pow(level, 2);
        };
    }

    private void givePoints(User user, int points) {
        user.setMatzipPoint(user.getMatzipPoint() + points);
        while (user.getMatzipPoint() >= neededPointsForLevelUp(user.getMatzipLevel())) {
            user.setMatzipLevel(user.getMatzipLevel() + 1);
        }
    }

    private void takePoints(User user, int points) {
        user.setMatzipPoint(user.getMatzipPoint() - points);
        while (user.getMatzipPoint() < neededPointsForLevelUp(user.getMatzipLevel() - 1)) {
            user.setMatzipLevel(user.getMatzipLevel() - 1);
        }
    }

    public void signUp(User user, String token) {
        user.setMatzipPoint(3);

        LoginRecord loginRecord = new LoginRecord(user.getId(), token);

        loginRecordRepository.save(loginRecord);
    }

    public void login(User user, String token) {

        Optional<LoginRecord> recordOptional = loginRecordRepository.findById(user.getId());

        LoginRecord loginRecord;
        if (recordOptional.isPresent()) {
            loginRecord = recordOptional.get();
            if (loginRecord.getLastLoginPointGiven().isBefore(LocalDateTime.now().minusDays(1))) {
                givePoints(user, POINTS_PER_LOGIN);
                loginRecord.setLastLoginPointGiven(LocalDateTime.now());
            }
            loginRecord.setToken(token);
        } else {
            loginRecord = new LoginRecord(user.getId(), token);
            givePoints(user, POINTS_PER_LOGIN);
        }
        loginRecordRepository.save(loginRecord);
    }

    public void logout(Long userId) {
        loginRecordRepository.findById(userId).ifPresent(loginRecord -> loginRecord.setToken(null));
    }

    public void view(Review review, User user) {
        Optional<ReviewRecord> reviewRecordOptional = reviewRecordRepository
                .findById(ReviewRecord.buildKey(user.getId(), review.getId()));

        ReviewRecord reviewRecord;
        if (reviewRecordOptional.isEmpty()) {
            reviewRecord = new ReviewRecord(user.getId(), review.getId());
        } else {
            reviewRecord = reviewRecordOptional.get();
            if (reviewRecord.getLastViewedAt().isBefore(LocalDateTime.now().minusDays(1))) {
                givePoints(review.getUser(), POINTS_PER_REVIEW);
                review.setViews(review.getViews() + 1);
                reviewRecord.setLastViewedAt(LocalDateTime.now());
            }
        }
        reviewRecordRepository.save(reviewRecord);
    }

    public void postReview(User user) {
        givePoints(user, POINTS_PER_REVIEW);
    }

    public void deleteReview(User user) {
        takePoints(user, POINTS_PER_REVIEW);
    }

    public void postComment(Review review, User user) {
        ReviewRecord reviewRecord = reviewRecordRepository
                .findById(ReviewRecord.buildKey(user.getId(), review.getId()))
                .orElse(new ReviewRecord(user.getId(), review.getId()));

        givePoints(review.getUser(), POINTS_PER_COMMENT);
        givePoints(user, Math.max(0, POINTS_PER_COMMENT - reviewRecord.getCommentCount()));
        reviewRecord.setCommentCount(reviewRecord.getCommentCount() + 1);
        reviewRecordRepository.save(reviewRecord);
    }

    public void deleteComment(Review review, User user) {
        reviewRecordRepository.findById(ReviewRecord.buildKey(user.getId(), review.getId())).ifPresent(reviewRecord -> {
            takePoints(review.getUser(), POINTS_PER_ACTION);
            takePoints(user, Math.max(0, POINTS_PER_COMMENT - (reviewRecord.getCommentCount() - 1)));
            reviewRecordRepository.save(reviewRecord);
        });
    }

    public void likeReview(Review review, User user) {
        ReviewRecord reviewRecord = reviewRecordRepository
                .findById(ReviewRecord.buildKey(user.getId(), review.getId()))
                .orElse(new ReviewRecord(user.getId(), review.getId()));

        givePoints(review.getUser(), POINTS_PER_LIKE);
        givePoints(user, POINTS_PER_ACTION);
        reviewRecordRepository.save(reviewRecord);
    }

    public void deleteLike(Review review, User user) {
        reviewRecordRepository.findById(ReviewRecord.buildKey(user.getId(), review.getId())).ifPresent(reviewRecord -> {
            takePoints(review.getUser(), POINTS_PER_LIKE);
            takePoints(user, POINTS_PER_ACTION);
            reviewRecordRepository.save(reviewRecord);
        });
    }

    public void scrapReview(Review review, User user) {
        ReviewRecord reviewRecord = reviewRecordRepository
                .findById(ReviewRecord.buildKey(user.getId(), review.getId()))
                .orElse(new ReviewRecord(user.getId(), review.getId()));

        givePoints(review.getUser(), POINTS_PER_SCRAP);
        givePoints(user, POINTS_PER_ACTION);
        reviewRecordRepository.save(reviewRecord);
    }

    public void deleteScrap(Review review, User user) {
        reviewRecordRepository.findById(ReviewRecord.buildKey(user.getId(), review.getId())).ifPresent(reviewRecord -> {
            takePoints(review.getUser(), POINTS_PER_SCRAP);
            takePoints(user, POINTS_PER_ACTION);
            reviewRecordRepository.save(reviewRecord);
        });
    }

    public void followUser(User follower, User followee) {
        givePoints(follower, POINTS_PER_FOLLOW);
        givePoints(followee, POINTS_PER_ACTION);
    }

    public void unfollowUser(User follower, User followee) {
        takePoints(follower, POINTS_PER_FOLLOW);
        takePoints(followee, POINTS_PER_ACTION);
    }

}
