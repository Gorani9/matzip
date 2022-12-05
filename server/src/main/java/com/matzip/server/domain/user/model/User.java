package com.matzip.server.domain.user.model;

import com.matzip.server.domain.admin.dto.AdminDto;
import com.matzip.server.domain.image.model.UserImage;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.me.model.Follow;
import com.matzip.server.domain.me.model.Heart;
import com.matzip.server.domain.me.model.Scrap;
import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name="user")
@NoArgsConstructor
@Getter
public class User extends BaseTimeEntity {
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
    private Boolean isNonLocked = true;
    private String role = "NORMAL";
    private String profileString;
    private Integer matzipLevel = 0;

    public User(UserDto.SignUpRequest signUpRequest, PasswordEncoder passwordEncoder) {
        this.username = signUpRequest.getUsername();
        this.password = passwordEncoder.encode(signUpRequest.getPassword());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof User && this.getId().equals(((User) obj).getId());
    }

    public User changeUsername(MeDto.UsernameChangeRequest usernameChangeRequest) {
        this.username = usernameChangeRequest.getUsername();
        return this;
    }

    public User changePassword(MeDto.PasswordChangeRequest passwordChangeRequest, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(passwordChangeRequest.getPassword());
        return this;
    }

    public User toAdmin() {
        this.role = "ADMIN";
        return this;
    }

    public User lock() {
        this.isNonLocked = false;
        return this;
    }

    public User unlock() {
        this.isNonLocked = true;
        return this;
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
        if (Boolean.TRUE.equals(userPatchRequest.getProfileString()))
            this.profileString = null;
        if (Optional.ofNullable(userPatchRequest.getMatzipLevel()).isPresent())
            this.matzipLevel = userPatchRequest.getMatzipLevel();
        return this;
    }

    public Follow addFollower(User follower) {
        Follow follow = new Follow(follower, this);
        this.followers.add(follow);
        follower.addFollowing(follow);
        return follow;
    }

    private void addFollowing(Follow follow) {
        this.followings.add(follow);
    }

    public void deleteFollower(User user) {
        this.followers.removeIf(f -> Objects.equals(f.getFollower().getId(), user.getId()));
        user.deleteFollowing(this);
    }

    private void deleteFollowing(User user) {
        this.followers.removeIf(f -> Objects.equals(f.getFollowee().getId(), user.getId()));
    }

    public boolean hasFollowing(User user) {
        return this.followings.stream().anyMatch(f -> Objects.equals(f.getFollowee().getId(), user.getId()));
    }

    public boolean hasFollower(User user) {
        return this.followers.stream().anyMatch(f -> Objects.equals(f.getFollower().getId(), user.getId()));
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
