package com.matzip.server.global.auth.service;

import com.matzip.server.domain.record.service.RecordService;
import com.matzip.server.domain.user.exception.UsernameAlreadyExistsException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.auth.dto.AuthDto.LoginRequest;
import com.matzip.server.global.auth.dto.AuthDto.Response;
import com.matzip.server.global.auth.dto.AuthDto.SignupRequest;
import com.matzip.server.global.auth.exception.LoginException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RecordService recordService;

    @Transactional
    public Response signup(SignupRequest request) {
        String username = request.username();
        String password = passwordEncoder.encode(request.password());

        if (userRepository.existsByUsername(username))
            throw new UsernameAlreadyExistsException(username);

        User user = userRepository.save(new User(username, password));
        String token = jwtProvider.generateToken(user.getUsername());

        recordService.signUp(user, token);

        return new Response(token);
    }

    @Transactional
    public Response login(LoginRequest request) {
        String username = request.username();
        String password = request.password();

        User user = userRepository.findByUsername(username).orElseThrow(LoginException::new);
        if (!passwordEncoder.matches(password, user.getPassword())) throw new LoginException();

        String token = jwtProvider.generateToken(user.getUsername());

        recordService.login(user, token);

        return new Response(token);
    }

    @Transactional
    public Response refresh(Long myId, String username) {
        User me = userRepository.findMeById(myId);
        String token = jwtProvider.generateToken(username);
        recordService.login(me, token);
        return new Response(token);
    }

    @Transactional
    public void logout(Long userId) {
        recordService.logout(userId);
    }
}
