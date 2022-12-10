package com.matzip.server.domain.me.service;

import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.me.dto.HeartDto;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.me.dto.ScrapDto;
import com.matzip.server.domain.me.exception.DuplicateHeartException;
import com.matzip.server.domain.me.exception.FollowMeException;
import com.matzip.server.domain.me.exception.HeartMyReviewException;
import com.matzip.server.domain.me.exception.ScrapMyReviewException;
import com.matzip.server.domain.me.model.Follow;
import com.matzip.server.domain.me.model.Heart;
import com.matzip.server.domain.me.model.Scrap;
import com.matzip.server.domain.me.repository.FollowRepository;
import com.matzip.server.domain.me.repository.HeartRepository;
import com.matzip.server.domain.me.repository.ScrapRepository;
import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.exception.AccessBlockedOrDeletedReviewException;
import com.matzip.server.domain.review.exception.ReviewNotFoundException;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.CommentRepository;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.exception.AccessBlockedOrDeletedUserException;
import com.matzip.server.domain.user.exception.UsernameAlreadyExistsException;
import com.matzip.server.domain.user.exception.UsernameNotFoundException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class MeService {
    private final UserRepository userRepository;

    private final FollowRepository followRepository;

    private final ReviewRepository reviewRepository;

    private final CommentRepository commentRepository;

    private final HeartRepository heartRepository;

    private final ScrapRepository scrapRepository;

    private final PasswordEncoder passwordEncoder;

    private final ImageService imageService;

    @Transactional
    public MeDto.Response changePassword(Long myId, MeDto.PasswordChangeRequest passwordChangeRequest) {
        User me = userRepository.findMeById(myId);
        me.setPassword(passwordEncoder.encode(passwordChangeRequest.getPassword()));
        return new MeDto.Response(me);
    }

    @Transactional
    public MeDto.Response changeUsername(Long myId, MeDto.UsernameChangeRequest usernameChangeRequest) {
        User me = userRepository.findMeById(myId);
        String username = usernameChangeRequest.getUsername();
        if (userRepository.existsByUsername(username)) throw new UsernameAlreadyExistsException(username);
        me.setUsername(username);
        return new MeDto.Response(me);
    }

    public MeDto.Response getMe(Long myId) {
        return new MeDto.Response(userRepository.findMeById(myId));
    }

    @Transactional
    public void deleteMe(Long myId) {
        User me = userRepository.findMeById(myId);
        followRepository.deleteAll(me.getFollowers());
        heartRepository.deleteAll(me.getHearts());
        scrapRepository.deleteAll(me.getScraps());
        me.delete();
    }

    @Transactional
    public MeDto.Response patchMe(Long myId, MeDto.PatchProfileRequest patchProfileRequest) {
        User me = userRepository.findMeById(myId);
        Optional.ofNullable(patchProfileRequest.getImage()).ifPresent(i -> imageService.uploadUserImage(me, i));
        Optional.ofNullable(patchProfileRequest.getProfile()).ifPresent(me::setProfileString);
        return new MeDto.Response(me);
    }

    public Slice<UserDto.Response> searchMyFollowers(Long myId, UserDto.SearchRequest searchRequest) {
        User me = userRepository.findMeById(myId);
        return followRepository.searchMyFollowersByUsername(searchRequest, myId).map(u -> UserDto.Response.of(u, me));
    }

    public Slice<UserDto.Response> searchMyFollowings(Long myId, UserDto.SearchRequest searchRequest) {
        User me = userRepository.findMeById(myId);
        return followRepository.searchMyFollowingsByUsername(searchRequest, myId).map(u -> UserDto.Response.of(u, me));
    }

    @Transactional
    public MeDto.Response followUser(Long myId, String followeeUsername) {
        User me = userRepository.findMeById(myId);
        User followee = userRepository.findByUsername(followeeUsername)
                .orElseThrow(() -> new UsernameNotFoundException(followeeUsername));
        if (followee.isBlocked() || followee.isDeleted()) throw new AccessBlockedOrDeletedUserException(followeeUsername);
        if (me == followee) throw new FollowMeException();
        if (!me.isFollowing(followee)) followRepository.save(new Follow(me, followee));
        return new MeDto.Response(me);
    }

    @Transactional
    public MeDto.Response unfollowUser(Long myId, String followeeUsername) {
        User me = userRepository.findMeById(myId);
        User followee = userRepository.findByUsername(followeeUsername)
                .orElseThrow(() -> new UsernameNotFoundException(followeeUsername));
        if (followee.isBlocked() || followee.isDeleted()) throw new AccessBlockedOrDeletedUserException(followeeUsername);
        followRepository.findByFollowerIdAndFolloweeId(myId, followee.getId()).ifPresent(
                f -> {
                    me.deleteFollowing(f);
                    followee.deleteFollower(f);
                    followRepository.delete(f);
                }
        );
        return new MeDto.Response(me);

    }

    public Slice<ReviewDto.Response> searchMyReviews(Long myId, ReviewDto.SearchRequest searchRequest) {
        User me = userRepository.findMeById(myId);
        return reviewRepository.searchMyReviewsByKeyword(searchRequest, myId).map(r -> ReviewDto.Response.of(r, me));
    }

    public Slice<CommentDto.Response> searchMyComments(Long myId, CommentDto.SearchRequest searchRequest) {
        User me = userRepository.findMeById(myId);
        return commentRepository.searchMyCommentsByKeyword(searchRequest, myId).map(c -> new CommentDto.Response(c, me));
    }

    public Slice<ScrapDto.Response> searchMyScraps(Long myId, ScrapDto.SearchRequest searchRequest) {
        return scrapRepository.searchMyScrapsByKeyword(searchRequest, myId).map(ScrapDto.Response::new);
    }

    @Transactional
    public HeartDto.Response heartReview(Long myId, Long reviewId) {
        User me = userRepository.findMeById(myId);
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));
        if (review.isBlocked() || review.isDeleted()) throw new AccessBlockedOrDeletedReviewException(reviewId);
        if (review.getUser() == me)
            throw new HeartMyReviewException();
        if (heartRepository.existsByUserIdAndReviewId(myId, reviewId))
            throw new DuplicateHeartException();
        heartRepository.save(new Heart(me, review));
        return new HeartDto.Response(review.getHearts().size());
    }

    @Transactional
    public HeartDto.Response deleteHeartFromReview(Long myId, Long reviewId) {
        User me = userRepository.findMeById(myId);
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));
        if (review.isBlocked() || review.isDeleted()) throw new AccessBlockedOrDeletedReviewException(reviewId);
        heartRepository.findByUserIdAndReviewId(myId, reviewId).ifPresent(
                h -> {
                    me.deleteHeart(h);
                    review.deleteHeart(h);
                    heartRepository.delete(h);
                }
        );
        return new HeartDto.Response(review.getHearts().size());
    }

    @Transactional
    public ScrapDto.Response scrapReview(Long myId, Long reviewId, ScrapDto.PostRequest postRequest) {
        User me = userRepository.findMeById(myId);
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));
        if (review.isBlocked() || review.isDeleted()) throw new AccessBlockedOrDeletedReviewException(reviewId);
        if (review.getUser() == me)
            throw new ScrapMyReviewException();
        Scrap scrap = scrapRepository.findByUserIdAndReviewId(myId, reviewId)
                .orElse(scrapRepository.save(new Scrap(me, review)));
        scrap.setDescription(postRequest.getDescription());
        return new ScrapDto.Response(scrap);
    }

    @Transactional
    public void deleteMyScrap(Long myId, Long reviewId) {
        User me = userRepository.findMeById(myId);
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));
        if (review.isBlocked() || review.isDeleted()) throw new AccessBlockedOrDeletedReviewException(reviewId);
        scrapRepository.findByUserIdAndReviewId(myId, reviewId).ifPresent(
                s -> {
                    me.deleteScrap(s);
                    review.deleteScrap(s);
                    scrapRepository.delete(s);
                }
        );
    }
}
