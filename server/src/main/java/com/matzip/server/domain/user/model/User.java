package com.matzip.server.domain.user.model;

import com.matzip.server.domain.comment.model.Comment;
import com.matzip.server.domain.review.model.Heart;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.scrap.model.Scrap;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name="user")
@NoArgsConstructor
@Getter @Setter
public class User extends BaseTimeEntity {
    @OneToMany(mappedBy="followee", cascade=CascadeType.PERSIST)
    private final List<Follow> followers = new LinkedList<>();
    @OneToMany(mappedBy="follower", cascade=CascadeType.PERSIST)
    private final List<Follow> followings = new LinkedList<>();
    @OneToMany(mappedBy="user", cascade=CascadeType.PERSIST)
    private final List<Review> reviews = new LinkedList<>();
    @OneToMany(mappedBy="user", cascade=CascadeType.PERSIST)
    private final List<Comment> comments = new LinkedList<>();
    @OneToMany(mappedBy="user", cascade=CascadeType.PERSIST)
    private final List<Heart> hearts = new LinkedList<>();
    @OneToMany(mappedBy="user", cascade=CascadeType.PERSIST)
    private final List<Scrap> scraps = new LinkedList<>();
    @Column(unique=true)
    private String username;
    private String password;
    private String userImage;
    private String profileString;
    private Integer matzipLevel = 0;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void delete() {

    }
}
