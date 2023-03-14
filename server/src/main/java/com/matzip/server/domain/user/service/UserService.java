package com.matzip.server.domain.user.service;

import com.matzip.server.domain.user.dto.UserDto.DetailedResponse;
import com.matzip.server.domain.user.dto.UserDto.Response;
import com.matzip.server.domain.user.dto.UserDto.SearchRequest;
import com.matzip.server.domain.user.exception.FollowMeException;
import com.matzip.server.domain.user.exception.UserNotFoundException;
import com.matzip.server.domain.user.model.Follow;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.FollowRepository;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class UserService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public DetailedResponse fetchUser(Long myId, String username) {
        User me = userRepository.findMeById(myId);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

        return new DetailedResponse(user, me);
    }

    public Slice<Response> searchUsers(Long myId, SearchRequest request) {
        User me = userRepository.findMeById(myId);
        Slice<User> users = userRepository.searchUsersByUsername(request);

        return users.map(user -> new Response(user, me));
    }

    @Transactional
    public Response followUser(Long myId, String username) {
        User me = userRepository.findMeById(myId);
        User followee = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

        if (me == followee) throw new FollowMeException();

        if (me.getFollowings().stream().noneMatch(f -> f.getFollowee() == followee))
            followRepository.save(new Follow(me, followee));

        return new Response(followee, me);
    }

    @Transactional
    public Response unfollowUser(Long myId, String username) {
        User me = userRepository.findMeById(myId);
        User followee = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

        followRepository.findByFollowerIdAndFolloweeId(myId, followee.getId()).ifPresent(
                f -> {
                    f.delete();
                    followRepository.delete(f);
                }
        );

        return new Response(followee, me);
    }
}
