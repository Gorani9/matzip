package com.matzip.server.global.utils;

import com.matzip.server.domain.user.model.User;

import java.util.List;

import static com.matzip.server.global.utils.Relation.CommentConfiguration.comment;
import static com.matzip.server.global.utils.Relation.FollowConfiguration.followersOf;
import static com.matzip.server.global.utils.Relation.ReviewConfiguration.review;
import static com.matzip.server.global.utils.Relation.UserConfiguration.user;

public class TestDataUtils {
    public static List<User> testData() {
        return Relation
                .withUsers(
                        user("user-01").level(3),
                        user("user-02").level(16),
                        user("user-03").level(8),
                        user("user-04").level(3),
                        user("user-05").level(5))
                .withReviews(
                        review("review-01").rating(9).by("user-01")
                                .withComments(
                                        comment("comment-01").by("user-02"),
                                        comment("comment-02").by("user-04"),
                                        comment("comment-03").by("user-04"),
                                        comment("comment-04").by("user-01"),
                                        comment("comment-05").by("user-05"),
                                        comment("comment-06").by("user-01"),
                                        comment("comment-07").by("user-02"),
                                        comment("comment-08").by("user-01"),
                                        comment("comment-09").by("user-02"))
                                .likedBy("user-02", "user-05")
                                .scrapedBy("user-02"),
                        review("review-02").rating(4).by("user-01")
                                .likedBy("user-03"),
                        review("review-03").rating(7).by("user-03")
                                .withComments(
                                        comment("comment-10").by("user-04"),
                                        comment("comment-11").by("user-01"),
                                        comment("comment-12").by("user-04"),
                                        comment("comment-13").by("user-01"),
                                        comment("comment-14").by("user-04"))
                                .scrapedBy("user-05"),
                        review("review-04").rating(2).by("user-05")
                                .withComments(
                                        comment("comment-15").by("user-01"),
                                        comment("comment-16").by("user-05"),
                                        comment("comment-17").by("user-01")),
                        review("review-05").rating(1).by("user-05")
                                .likedBy("user-01", "user-02", "user-03", "user-04")
                                .scrapedBy("user-01"),
                        review("review-06").rating(10).by("user-04")
                                .withComments(
                                        comment("comment-18").by("user-01"),
                                        comment("comment-19").by("user-03"),
                                        comment("comment-20").by("user-01"),
                                        comment("comment-21").by("user-05"),
                                        comment("comment-22").by("user-03"),
                                        comment("comment-23").by("user-04"),
                                        comment("comment-24").by("user-03"),
                                        comment("comment-25").by("user-05"),
                                        comment("comment-26").by("user-02"),
                                        comment("comment-27").by("user-05"),
                                        comment("comment-28").by("user-03"))
                                .likedBy("user-01", "user-03"))
                .withFollows(
                        followersOf("user-01").are("user-02", "user-04", "user-05"),
                        followersOf("user-02").are("user-01", "user-03", "user-04"),
                        followersOf("user-03").are("user-01", "user-02"),
                        followersOf("user-04").are("user-02"),
                        followersOf("user-05").are("user-01", "user-02", "user-03", "user-04"))
                .build();
    }
}
