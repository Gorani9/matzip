package com.matzip.server.domain.user.service;

import com.matzip.server.domain.admin.exception.DeleteAdminUserException;
import com.matzip.server.domain.admin.exception.UserIdNotFoundException;
import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.exception.AdminUserAccessByNormalUserException;
import com.matzip.server.domain.user.exception.UsernameAlreadyExistsException;
import com.matzip.server.domain.user.exception.UsernameNotFoundException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.auth.jwt.JwtProvider;
import com.matzip.server.global.auth.model.MatzipAuthenticationToken;
import com.matzip.server.global.auth.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtProvider jwtProvider;

    private final ImageService imageService;

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
                userPrincipal, null, userPrincipal.getAuthorities()));
        return new UserDto.SignUpResponse(new UserDto.Response(user), token);
    }

    public UserDto.Response findUser(UserDto.FindRequest findRequest, String userRole) {
        User user = userRepository.findByUsername(findRequest.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException(findRequest.getUsername()));
        if (user.getRole().equals("ADMIN") && !userRole.equals("ADMIN"))
            throw new AdminUserAccessByNormalUserException();
        return new UserDto.Response(user);
    }

    public Page<UserDto.Response> searchUsers(UserDto.SearchRequest searchRequest) {
        PageRequest pageRequest = PageRequest.of(
                searchRequest.getPageNumber(),
                searchRequest.getPageSize(),
                Sort.by("createdAt").ascending());
        Page<User> users = userRepository
                .findAllByUsernameContainsIgnoreCaseAndIsNonLockedTrueAndRoleEquals(pageRequest, searchRequest.getUsername(), "NORMAL");
        return users.map(UserDto.Response::new);
    }

    @Transactional
    public void changePassword(UserDto.PasswordChangeRequest passwordChangeRequest) {
        User user = userRepository.findByUsername(passwordChangeRequest.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException(passwordChangeRequest.getUsername())
        );
        userRepository.save(user.changePassword(passwordChangeRequest, passwordEncoder));
    }

    public UserDto.Response getMe(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return new UserDto.Response(user);
    }

    @Transactional
    public void deleteMe(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException(id));
        if (user.getRole().equals("ADMIN"))
            throw new DeleteAdminUserException();
        userRepository.delete(user);
    }

    @Transactional
    public UserDto.Response patchMe(String username, UserDto.ModifyProfileRequest modifyProfileRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        if (modifyProfileRequest.getProfileImageUrl() != null)
            imageService.deleteImages(username, Collections.singletonList(user.getProfileImageUrl()));
        userRepository.save(user.patch(modifyProfileRequest));
        return new UserDto.Response(user);
    }
}
