package com.matzip.server.domain.user.service;

import com.matzip.server.domain.admin.exception.UserIdNotFoundException;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public UserDto.DuplicateResponse isUsernameTakenBySomeone(String username) {
        return new UserDto.DuplicateResponse(userRepository.existsByUsername(username));
    }

    @Transactional
    public UserDto.SignUpResponse createUser(UserDto.SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername()))
            throw new UsernameAlreadyExistsException(signUpRequest.getUsername());
        User user = userRepository.save(new User(signUpRequest, passwordEncoder));
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String token = jwtProvider.generateAccessToken(new MatzipAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()));
        return new UserDto.SignUpResponse(new UserDto.Response(user, user), token);
    }

    public UserDto.Response findUser(String findUsername, String username) {
        User user = userRepository.findByUsername(findUsername).orElseThrow(() -> new UsernameNotFoundException(username));
        User findUser = userRepository.findByUsername(findUsername).orElseThrow(() -> new UsernameNotFoundException(findUsername));
        if (findUser.getRole().equals("ADMIN") && !user.getRole().equals("ADMIN"))
            throw new AdminUserAccessByNormalUserException();
        return new UserDto.Response(findUser, user);
    }

    public Page<UserDto.Response> searchUsers(Long userId, UserDto.SearchRequest searchRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserIdNotFoundException(userId));
        Sort sort = searchRequest.getAscending() ?
                    Sort.by(searchRequest.getSortedBy()).ascending() :
                    Sort.by(searchRequest.getSortedBy()).descending();
        Pageable pageable = PageRequest.of(searchRequest.getPageNumber(), searchRequest.getPageSize(), sort);
        Page<User> users = userRepository.findAllByUsernameContainsIgnoreCaseAndIsNonLockedTrueAndRoleEquals(
                pageable,
                searchRequest.getUsername(),
                "NORMAL");
        return users.map(u -> new UserDto.Response(u, user));
    }
}
