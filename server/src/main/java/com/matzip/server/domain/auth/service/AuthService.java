package com.matzip.server.domain.auth.service;

import com.matzip.server.domain.auth.dto.AuthDto.LoginRequest;
import com.matzip.server.domain.auth.dto.AuthDto.SignupRequest;
import com.matzip.server.domain.auth.exception.LoginException;
import com.matzip.server.domain.auth.exception.UsernameAlreadyExistsException;
import com.matzip.server.domain.auth.model.MatzipAuthenticationToken;
import com.matzip.server.domain.auth.model.UserPrincipal;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequest request) {
        String username = request.username();
        String password = passwordEncoder.encode(request.password());

        if (userRepository.existsByUsername(username))
            throw new UsernameAlreadyExistsException(username);

        User user = userRepository.save(new User(username, password));
        MatzipAuthenticationToken authentication = new MatzipAuthenticationToken(new UserPrincipal(user.getId(), username));

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        RequestContextHolder.currentRequestAttributes()
                .setAttribute("SPRING_SECURITY_CONTEXT", context, RequestAttributes.SCOPE_SESSION);
    }

    public void login(LoginRequest request) {
        String username = request.username();
        String password = request.password();

        User user = userRepository.findByUsername(username).orElseThrow(LoginException::new);
        if (!passwordEncoder.matches(password, user.getPassword())) throw new LoginException();

        MatzipAuthenticationToken authentication = new MatzipAuthenticationToken(new UserPrincipal(user.getId(), username));

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        RequestContextHolder.currentRequestAttributes()
                .setAttribute("SPRING_SECURITY_CONTEXT", context, RequestAttributes.SCOPE_SESSION);
    }

    public boolean isUsernameTakenBySomeone(String username) {
        return userRepository.existsByUsername(username);
    }
}
