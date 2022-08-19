package com.matzip.server.domain.me.service;

import com.matzip.server.domain.admin.exception.DeleteAdminUserException;
import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.me.exception.FollowMeException;
import com.matzip.server.domain.me.model.Follow;
import com.matzip.server.domain.me.repository.FollowRepository;
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
    public MeDto.Response patchMe(String username, MeDto.ModifyProfileRequest modifyProfileRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        Optional<MultipartFile> profileImage = Optional.ofNullable(modifyProfileRequest.getProfileImage());
        Optional<String> profileString = Optional.ofNullable(modifyProfileRequest.getProfileString());
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
}
