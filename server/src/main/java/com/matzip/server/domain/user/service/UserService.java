package com.matzip.server.domain.user.service;

import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.user.dto.UserDto.*;
import com.matzip.server.domain.user.exception.AccessBlockedUserException;
import com.matzip.server.domain.user.exception.UsernameAlreadyExistsException;
import com.matzip.server.domain.user.exception.UsernameNotFoundException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.auth.jwt.JwtProvider;
import com.matzip.server.global.auth.model.MatzipAuthenticationToken;
import com.matzip.server.global.auth.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtProvider jwtProvider;

    public DuplicateResponse isUsernameTakenBySomeone(String username) {
        return new DuplicateResponse(userRepository.existsByUsername(username));
    }

    @Transactional
    public SignUpResponse createUser(SignUpRequest signUpRequest) {
        String username = signUpRequest.getUsername();
        String password = passwordEncoder.encode(signUpRequest.getPassword());
        if (userRepository.existsByUsername(username))
            throw new UsernameAlreadyExistsException(username);
        User user = userRepository.save(new User(username, password));
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String token = jwtProvider.generateAccessToken(new MatzipAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()));
        return new SignUpResponse(new MeDto.Response(user), token);
    }

    public DetailedResponse fetchUser(Long myId, String username) {
        User me = userRepository.findMeById(myId);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        if (user.isBlocked()) throw new AccessBlockedUserException(username);
        return new DetailedResponse(user, me);
    }

    public Slice<Response> searchUsers(Long myId, SearchRequest searchRequest) {
        User me = userRepository.findMeById(myId);
        Slice<User> users = userRepository.searchUsersByUsername(searchRequest);
        return users.map(user -> Response.of(user, me));
    }
}
