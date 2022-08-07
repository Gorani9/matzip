package com.matzip.server.global.auth.service;

import com.matzip.server.domain.user.exception.UserNotActiveException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.auth.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPrincipalDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException(String.format("User with username '%s' not found.", username));
        if (!user.getActive())
            throw new UserNotActiveException(user.getUsername());
        return new UserPrincipal(user);
    }
}
