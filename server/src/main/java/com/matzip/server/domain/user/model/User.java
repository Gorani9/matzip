package com.matzip.server.domain.user.model;

import com.matzip.server.domain.admin.dto.AdminDto;
import com.matzip.server.domain.image.model.UserImage;
import com.matzip.server.domain.me.model.Follow;
import com.matzip.server.domain.me.model.Heart;
import com.matzip.server.domain.me.model.Scrap;
import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.global.common.model.BaseLazyDeletedTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name="user")
@NoArgsConstructor
@Getter
public class User extends BaseLazyDeletedTimeEntity {
    @OneToMany(mappedBy="followee", cascade=CascadeType.ALL, orphanRemoval=true)
    private final List<Follow> followers = new ArrayList<>();
    @OneToMany(mappedBy="follower", cascade=CascadeType.ALL, orphanRemoval=true)
    private final List<Follow> followings = new ArrayList<>();
    @OneToMany(mappedBy="user", cascade=CascadeType.ALL, orphanRemoval=true)
    private final List<Review> reviews = new ArrayList<>();
    @OneToMany(mappedBy="user", cascade=CascadeType.ALL, orphanRemoval=true)
    private final List<Comment> comments = new ArrayList<>();
    @OneToMany(mappedBy="user", cascade=CascadeType.ALL, orphanRemoval=true)
    private final List<Heart> hearts = new ArrayList<>();
    @OneToMany(mappedBy="user", cascade=CascadeType.ALL, orphanRemoval=true)
    private final List<Scrap> scraps = new ArrayList<>();
    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
    @JoinColumn
    private UserImage userImage;
    @Column(unique=true)
    private String username;
    private String password;
    private String profileString;
    private Integer matzipLevel = 0;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User levelUp() {
        this.matzipLevel++;
        return this;
    }

    public void setUserImage(UserImage userImage) {
        this.userImage = userImage;
    }

    public void setProfileString(String profileString) {
        this.profileString = profileString;
    }

    public User patchFromAdmin(AdminDto.UserPatchRequest userPatchRequest) {
        if (Boolean.TRUE.equals(userPatchRequest.getUsername()))
            this.username = "random-user-" + LocalDateTime.now() + "-" + UUID.randomUUID();
        if (Boolean.TRUE.equals(userPatchRequest.getProfileImageUrl()) && userImage != null)
            this.userImage.delete();
        if (Boolean.TRUE.equals(userPatchRequest.getProfileString()))
            this.profileString = null;
        if (Optional.ofNullable(userPatchRequest.getMatzipLevel()).isPresent())
            this.matzipLevel = userPatchRequest.getMatzipLevel();
        return this;
    }

    public void addFollower(Follow follower) {
        this.followers.add(follower);
    }

    public void deleteFollower(Follow follower) {
        this.followers.remove(follower);
    }

    public void addFollowing(Follow following) {
        this.followings.add(following);
    }

    public void deleteFollowing(Follow following) {
        this.followers.remove(following);
    }

    public boolean isFollowing(User user) {
        return this.followings.stream().anyMatch(f -> f.getFollowee() == user);
    }

    public boolean isFollowedBy(User user) {
        return this.followers.stream().anyMatch(f -> f.getFollower() == user);
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

    public void deleteReview(Review review) {
        this.reviews.remove(review);
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public void deleteComment(Comment comment) {
        this.comments.remove(comment);
    }

    public void addScrap(Scrap scrap) {
        this.scraps.add(scrap);
    }

    public void deleteScrap(Scrap scrap) {
        this.scraps.remove(scrap);
    }

    public void addHeart(Heart heart) {
        this.hearts.add(heart);
    }

    public void deleteHeart(Heart heart) {
        this.hearts.remove(heart);
    }
}
