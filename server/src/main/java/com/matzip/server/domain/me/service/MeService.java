package com.matzip.server.domain.me.service;

import com.matzip.server.domain.admin.exception.DeleteAdminUserException;
import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.me.dto.ScrapDto;
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
import com.matzip.server.domain.review.exception.ReviewNotFoundException;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.CommentRepository;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.exception.UsernameAlreadyExistsException;
import com.matzip.server.domain.user.exception.UsernameNotFoundException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    public MeDto.Response changePassword(String username, MeDto.PasswordChangeRequest passwordChangeRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return new MeDto.Response(userRepository.save(user.changePassword(passwordChangeRequest, passwordEncoder)));
    }

    @Transactional
    public MeDto.Response changeUsername(String username, MeDto.UsernameChangeRequest usernameChangeRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        if (userRepository.existsByUsername(usernameChangeRequest.getUsername()))
            throw new UsernameAlreadyExistsException(usernameChangeRequest.getUsername());
        return new MeDto.Response(userRepository.save(user.changeUsername(usernameChangeRequest)));
    }

    public MeDto.Response getMe(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return new MeDto.Response(user);
    }

    @Transactional
    public void deleteMe(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        if (user.getRole().equals("ADMIN")) throw new DeleteAdminUserException();
        userRepository.delete(user);
    }

    @Transactional
    public MeDto.Response patchMe(String username, MeDto.PatchProfileRequest patchProfileRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        Optional<MultipartFile> profileImage = Optional.ofNullable(patchProfileRequest.getProfileImage());
        Optional<String> profileString = Optional.ofNullable(patchProfileRequest.getProfileString());
        profileImage.ifPresent(i -> {
            String profileImageUrl = imageService.uploadImage(user.getUsername(), i);
            imageService.deleteImage(user.getProfileImageUrl());
            user.setProfileImageUrl(profileImageUrl);
        });
        profileString.ifPresent(user::setProfileString);
        userRepository.save(user);
        return new MeDto.Response(user);
    }

    public Page<UserDto.Response> getMyFollows(User user, MeDto.FindFollowRequest findFollowRequest) {
        boolean isFollowing = findFollowRequest.getType().equals("following");
        String property = (isFollowing ? "followee_" : "follower_") + findFollowRequest.getSortedBy();
        Sort sort = findFollowRequest.getAscending() ? Sort.by(property).ascending() : Sort.by(property).descending();
        Pageable pageable = PageRequest.of(findFollowRequest.getPageNumber(), findFollowRequest.getPageSize(), sort);

        return isFollowing ?
               followRepository.findAllByFollowerId(pageable, user.getId())
                       .map(f -> new UserDto.Response(f.getFollowee())) :
               followRepository.findAllByFolloweeId(pageable, user.getId())
                       .map(f -> new UserDto.Response(f.getFollower()));
    }

    @Transactional
    public MeDto.Response followUser(String username, String followeeUsername) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        User followee = userRepository.findByUsername(followeeUsername)
                .orElseThrow(() -> new UsernameNotFoundException(followeeUsername));
        if (user.getUsername().equals(followeeUsername)) throw new FollowMeException();
        if (!followRepository.existsByFollowerIdAndFolloweeId(user.getId(), followee.getId())) {
            Follow follow = new Follow(user, followee);
            followRepository.save(follow);
        }
        return new MeDto.Response(user);
    }

    @Transactional
    public MeDto.Response unfollowUser(String username, String followeeUsername) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        User followee = userRepository.findByUsername(followeeUsername)
                .orElseThrow(() -> new UsernameNotFoundException(followeeUsername));
        followRepository.deleteByFollowerIdAndFolloweeId(user.getId(), followee.getId());
        followRepository.flush();
        return new MeDto.Response(user);

    }

    public Page<ReviewDto.Response> getMyReviews(User user, MeDto.FindReviewRequest findReviewRequest) {
        Sort sort = findReviewRequest.getAscending() ? Sort.by(findReviewRequest.getSortedBy()).ascending()
                                                     : Sort.by(findReviewRequest.getSortedBy()).descending();
        Pageable pageable = PageRequest.of(findReviewRequest.getPageNumber(), findReviewRequest.getPageSize(), sort);
        return reviewRepository.findAllByUser_Username(pageable, user.getUsername())
                .map(r -> new ReviewDto.Response(user, r));
    }

    public Page<CommentDto.Response> getMyComments(User user, MeDto.FindCommentRequest findCommentRequest) {
        Sort sort = findCommentRequest.getAscending() ? Sort.by(findCommentRequest.getSortedBy()).ascending()
                                                      : Sort.by(findCommentRequest.getSortedBy()).descending();
        Pageable pageable = PageRequest.of(findCommentRequest.getPageNumber(), findCommentRequest.getPageSize(), sort);
        return commentRepository.findAllByUser_Username(pageable, user.getUsername())
                .map(r -> new CommentDto.Response(user, r));
    }

    public Page<ScrapDto.Response> getMyScraps(User user, MeDto.FindReviewRequest findReviewRequest) {
        Sort sort = findReviewRequest.getAscending() ? Sort.by(findReviewRequest.getSortedBy()).ascending()
                                                     : Sort.by(findReviewRequest.getSortedBy()).descending();
        Pageable pageable = PageRequest.of(findReviewRequest.getPageNumber(), findReviewRequest.getPageSize(), sort);
        return scrapRepository.findAllByUser_Username(pageable, user.getUsername()).map(ScrapDto.Response::new);
    }

    @Transactional
    public void heartReview(User user, Long reviewId) {
        Review review = reviewRepository.findAllById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));
        if (review.getUser().getUsername().equals(user.getUsername()))
            throw new HeartMyReviewException();
        heartRepository.save(new Heart(user, review));
    }

    @Transactional
    public void deleteHeartFromReview(User user, Long reviewId) {
        heartRepository.deleteByUser_UsernameAndReview_Id(user.getUsername(), reviewId);
    }

    @Transactional
    public ScrapDto.Response scrapReview(String username, Long reviewId, ScrapDto.Request request) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        Review review = reviewRepository.findAllById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));
        if (review.getUser().getUsername().equals(username))
            throw new ScrapMyReviewException();
        Scrap scrap = scrapRepository.findByUser_UsernameAndReview_Id(username, review.getId())
                .orElse(new Scrap(user, review));
        scrap.setDescription(request.getDescription());
        return new ScrapDto.Response(scrapRepository.save(scrap));
    }

    @Transactional
    public void deleteMyScrap(User user, Long reviewId) {
        scrapRepository.deleteByUser_UsernameAndReview_id(user.getUsername(), reviewId);
    }
}
