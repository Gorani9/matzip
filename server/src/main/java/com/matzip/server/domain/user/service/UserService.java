package com.matzip.server.domain.user.service;

import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.exception.UsernameNotFoundException;
import com.matzip.server.domain.user.exception.UsernameAlreadyExistsException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.auth.jwt.JwtProvider;
import com.matzip.server.global.auth.model.MatzipAuthenticationToken;
import com.matzip.server.global.auth.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtProvider jwtProvider;

    public UserDto.DuplicateResponse isUsernameTakenBySomeone(UserDto.DuplicateRequest duplicateRequest) {
        return new UserDto.DuplicateResponse(userRepository.existsByUsername(duplicateRequest.getUsername()));
    }

    @Transactional
    public UserDto.SignUpResponse createUser(UserDto.SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername()))
            throw new UsernameAlreadyExistsException(signUpRequest.getUsername());
        User user = userRepository.save(new User(signUpRequest, passwordEncoder));
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String token = jwtProvider.generateAccessToken(new MatzipAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        ));
        return new UserDto.SignUpResponse(new UserDto.Response(user), token);
    }

    public UserDto.Response findUser(UserDto.FindRequest findRequest) {
        User user = userRepository.findByUsername(findRequest.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException(findRequest.getUsername())
        );
        return new UserDto.Response(user);
    }

    @Transactional
    public void changePassword(String username, UserDto.PasswordChangeRequest passwordChangeRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(username)
        );
        userRepository.save(user.changePassword(passwordChangeRequest, passwordEncoder));
    }
}
