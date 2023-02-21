package com.matzip.server.domain.me.service;

import com.matzip.server.domain.auth.exception.UsernameAlreadyExistsException;
import com.matzip.server.domain.comment.repository.CommentRepository;
import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.me.dto.MeDto.PatchRequest;
import com.matzip.server.domain.review.repository.HeartRepository;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.scrap.repository.ScrapRepository;
import com.matzip.server.domain.user.dto.UserDto.Response;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.FollowRepository;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class MeService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final ScrapRepository scrapRepository;
    private final HeartRepository heartRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    @Transactional
    public void changePassword(Long myId, MeDto.PasswordChangeRequest passwordChangeRequest) {
        User me = userRepository.findMeById(myId);

        me.setPassword(passwordEncoder.encode(passwordChangeRequest.password()));
    }

    @Transactional
    public Response changeUsername(Long myId, MeDto.UsernameChangeRequest usernameChangeRequest) {
        User me = userRepository.findMeById(myId);
        String username = usernameChangeRequest.username();
        if (userRepository.existsByUsername(username)) throw new UsernameAlreadyExistsException(username);

        me.setUsername(username);
        return new Response(me, me);
    }

    public Response getMe(Long myId) {
        User me = userRepository.findMeById(myId);
        return new Response(me, me);
    }

    public MeDto.Response getMeDetail(Long myId) {
        User me = userRepository.findMeById(myId);
        return new MeDto.Response(me);
    }

    @Transactional
    public void deleteMe(Long myId) {
        User me = userRepository.findMeById(myId);

        if (me.getUserImage() != null) imageService.deleteImage(me.getUserImage());

        List<Long> reviewIds = me.getReviews().stream().map(BaseTimeEntity::getId).toList();


        heartRepository.deleteAllByUserIdOrReviewIds(myId, reviewIds);
        scrapRepository.deleteAllByUserIdOrReviewIds(myId, reviewIds);
        commentRepository.deleteAllByUserIdOrReviewIds(myId, reviewIds);
        reviewRepository.deleteAllByUserIdOrReviewIds(myId, reviewIds);
        followRepository.deleteAllByUserId(myId);

        me.delete();
        userRepository.delete(me);
        SecurityContextHolder.clearContext();
    }

    @Transactional
    public Response patchMe(Long myId, PatchRequest request) {
        User me = userRepository.findMeById(myId);

        if (request.image() != null) me.setUserImage(imageService.uploadImage(me.getUsername(), request.image()));
        if (request.profile() != null) me.setProfileString(request.profile());

        return new Response(me, me);
    }
}
