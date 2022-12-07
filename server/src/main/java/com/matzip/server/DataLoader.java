package com.matzip.server;

import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class DataLoader implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin-password}")
    private String adminPassword;

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByUsername("qwer").isEmpty())
            userRepository.save(new User("qwer", passwordEncoder.encode(adminPassword)));
    }
}
