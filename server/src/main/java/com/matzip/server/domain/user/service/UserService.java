package com.matzip.server.domain.user.service;

import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.exception.UserNotFoundException;
import com.matzip.server.domain.user.exception.UsernameAlreadyExistsException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
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

    public UserDto.DuplicateResponse isUsernameTakenBySomeone(UserDto.DuplicateRequest duplicateRequest) {
        return new UserDto.DuplicateResponse(userRepository.existsByUsername(duplicateRequest.getUsername()));
    }

    @Transactional
    public UserDto.Response createUser(UserDto.SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername()))
            throw new UsernameAlreadyExistsException(signUpRequest.getUsername());
        User newUser = userRepository.save(new User(signUpRequest, passwordEncoder));
        return new UserDto.Response(newUser);
    }

    public UserDto.Response findUser(UserDto.FindRequest findRequest) {
        User user = userRepository.findByUsername(findRequest.getUsername());
        if (user == null)
            throw new UserNotFoundException(findRequest.getUsername());
        return new UserDto.Response(user);
    }

    @Transactional
    public void changePassword(String username, UserDto.PasswordChangeRequest passwordChangeRequest) {
        User user = userRepository.findByUsername(username);
        user.changePassword(passwordChangeRequest, passwordEncoder);
        userRepository.save(user);
    }
}
