package com.matzip.server.global.utils;

import com.matzip.server.domain.comment.model.Comment;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.model.Heart;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.scrap.model.Scrap;
import com.matzip.server.domain.user.model.Follow;
import com.matzip.server.domain.user.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Relation {
    private final List<User> users;
    public Relation(List<User> users) {
        this.users = users;
    }
    public List<User> build() {
        return users;
    }
    static Relation withUsers(User... users) {
        return new Relation(new ArrayList<>(Arrays.asList(users)));
    }
    public Relation withReviews(ReviewConfiguration... reviews) {
        for (ReviewConfiguration reviewConfiguration : reviews) {
            User reviewer = users.stream().filter(u -> u.getUsername().equals(reviewConfiguration.username)).findFirst().orElseThrow();
            String reviewContent = reviewConfiguration.content;
            Integer rating = reviewConfiguration.rating;
            Review review = new Review(reviewer, new ReviewDto.PostRequest(reviewContent, null, rating, "somewhere"));

            if (reviewConfiguration.comments != null) {
                for (CommentConfiguration commentConfiguration : reviewConfiguration.comments) {
                    User commenter = users.stream().filter(u -> u.getUsername().equals(commentConfiguration.username)).findFirst().orElseThrow();
                    new Comment(commenter, review, commentConfiguration.content);
                }
            }

            if (reviewConfiguration.scraps != null) {
                for (String username : reviewConfiguration.scraps) {
                    User scraper = users.stream().filter(u -> u.getUsername().equals(username)).findFirst().orElseThrow();
                    new Scrap(scraper, review, "description");
                }
            }

            if (reviewConfiguration.hearts != null) {
                for (String username : reviewConfiguration.hearts) {
                    User liker = users.stream().filter(u -> u.getUsername().equals(username)).findFirst().orElseThrow();
                    new Heart(liker, review);
                }
            }
        }
        return this;
    }

    public Relation withFollows(FollowConfiguration... follows) {
        for (FollowConfiguration followConfiguration : follows) {
            User user = users.stream().filter(u -> u.getUsername().equals(followConfiguration.username)).findFirst().orElseThrow();

            if (followConfiguration.followers != null) {
                for (String followerUsername : followConfiguration.followers) {
                    User follower = users.stream().filter(u -> u.getUsername().equals(followerUsername)).findFirst().orElseThrow();
                    new Follow(follower, user);
                }
            }
        }
        return this;
    }

    static class UserConfiguration {
        String username;
        public UserConfiguration(String username) {
            this.username = username;
        }
        static UserConfiguration user(String username) {
            return new UserConfiguration(username);
        }
        public User level(int level) {
            User user = new User(username, "");
            user.setMatzipLevel(level);
            return user;
        }
    }

    static class ReviewConfiguration {
        String content, username;
        CommentConfiguration[] comments;
        String[] scraps, hearts;
        Integer rating;
        public ReviewConfiguration(String content) {
            this.content = content;
        }
        static ReviewConfiguration review(String content) {
            return new ReviewConfiguration(content);
        }
        public ReviewConfiguration rating(Integer rating) {
            this.rating = rating;
            return this;
        }
        public ReviewConfiguration by(String username) {
            this.username = username;
            return this;
        }
        public ReviewConfiguration withComments(CommentConfiguration... comments) {
            this.comments = comments;
            return this;
        }
        public ReviewConfiguration scrapedBy(String... usernames) {
            this.scraps = usernames;
            return this;
        }
        public ReviewConfiguration likedBy(String... usernames) {
            this.hearts = usernames;
            return this;
        }
    }

    static class CommentConfiguration {
        String content, username;
        public CommentConfiguration(String content) {
            this.content = content;
        }
        static CommentConfiguration comment(String content) {
            return new CommentConfiguration(content);
        }
        public CommentConfiguration by(String username) {
            this.username = username;
            return this;
        }
    }

    static class FollowConfiguration {
        String username;
        String[] followers;
        public FollowConfiguration(String username) {
            this.username = username;
        }
        static FollowConfiguration followersOf(String username) {
            return new FollowConfiguration(username);
        }
        public FollowConfiguration are(String... followers) {
            this.followers = followers;
            return this;
        }
    }
}
