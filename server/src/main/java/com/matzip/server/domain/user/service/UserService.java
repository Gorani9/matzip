package com.matzip.server.domain.user.service;

import com.matzip.server.domain.record.service.RecordService;
import com.matzip.server.domain.user.dto.UserDto.DetailedResponse;
import com.matzip.server.domain.user.exception.FollowMeException;
import com.matzip.server.domain.user.exception.UserNotFoundException;
import com.matzip.server.domain.user.model.Follow;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.FollowRepository;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class UserService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final RecordService recordService;

    public boolean isUsernameTakenBySomeone(String username) {
        return userRepository.existsByUsername(username);
    }

    public DetailedResponse fetchUser(Long myId, String username) {
        User me = userRepository.findMeById(myId);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

        return new DetailedResponse(user, me);
    }

    @Transactional
    public DetailedResponse followUser(Long myId, String username) {
        User me = userRepository.findMeById(myId);
        User followee = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

        if (me == followee) throw new FollowMeException();

        if (followee.getFollowers().stream().noneMatch(f -> f.getFollower() == me)) {
            followRepository.save(new Follow(me, followee));
            recordService.followUser(me, followee);
        }

        return new DetailedResponse(followee, me);
    }

    @Transactional
    public DetailedResponse unfollowUser(Long myId, String username) {
        User me = userRepository.findMeById(myId);
        User followee = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

        followRepository.findByFollowerIdAndFolloweeId(myId, followee.getId()).ifPresent(
                f -> {
                    f.delete();
                    followRepository.delete(f);
                    recordService.unfollowUser(me, followee);
                }
        );

        return new DetailedResponse(followee, me);
    }
}
