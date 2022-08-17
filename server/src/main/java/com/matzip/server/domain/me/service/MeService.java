package com.matzip.server.domain.me.service;

import com.matzip.server.domain.admin.exception.DeleteAdminUserException;
import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.user.exception.UsernameNotFoundException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class MeService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ImageService imageService;

    @Transactional
    public void changePassword(MeDto.PasswordChangeRequest passwordChangeRequest) {
        User user = userRepository.findByUsername(passwordChangeRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(passwordChangeRequest.getUsername()));
        userRepository.save(user.changePassword(passwordChangeRequest, passwordEncoder));
    }

    public MeDto.Response getMe(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return new MeDto.Response(user);
    }

    @Transactional
    public void deleteMe(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        if (user.getRole().equals("ADMIN")) throw new DeleteAdminUserException();
        userRepository.delete(user);
    }

    @Transactional
    public MeDto.Response patchMe(String username, MeDto.ModifyProfileRequest modifyProfileRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        Optional<MultipartFile> profileImage = Optional.ofNullable(modifyProfileRequest.getProfileImage());
        Optional<String> profileString = Optional.ofNullable(modifyProfileRequest.getProfileString());
        profileImage.ifPresent(i -> {
            String profileImageUrl = imageService.uploadImage(username, i);
            imageService.deleteImage(username, profileImageUrl);
            user.setProfileImageUrl(profileImageUrl);
        });
        profileString.ifPresent(user::setProfileString);
        userRepository.save(user);
        return new MeDto.Response(user);
    }
}
