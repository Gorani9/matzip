package com.matzip.server.domain.admin.service;

import com.matzip.server.domain.admin.dto.AdminDto;
import com.matzip.server.domain.admin.exception.UserIdNotFoundException;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class AdminUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Slice<AdminDto.UserResponse> searchUsers(AdminDto.UserSearchRequest userSearchRequest) {
        return userRepository.adminSearchUsersByUsername(userSearchRequest).map(AdminDto.UserResponse::new);
    }

    public AdminDto.UserResponse fetchUserById(Long userId) {
        return new AdminDto.UserResponse(
                userRepository.findById(userId).orElseThrow(() -> new UserIdNotFoundException(userId)));
    }

    @Transactional
    public AdminDto.UserResponse patchUserById(Long userId, AdminDto.UserPatchRequest userPatchRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserIdNotFoundException(userId));
        return new AdminDto.UserResponse(user.patchFromAdmin(userPatchRequest));
    }

    @Transactional
    public AdminDto.UserResponse lockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserIdNotFoundException(userId));
        user.block("admin");
        return new AdminDto.UserResponse(user);
    }

    @Transactional
    public AdminDto.UserResponse unlockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserIdNotFoundException(userId));
        user.unblock();
        return new AdminDto.UserResponse(user);

    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserIdNotFoundException(userId));
        user.delete();
    }

    @Transactional
    public AdminDto.UserResponse changeUserPassword(Long userId, MeDto.PasswordChangeRequest passwordChangeRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserIdNotFoundException(userId));
        user.setPassword(passwordEncoder.encode(passwordChangeRequest.getPassword()));
        return new AdminDto.UserResponse(user);
    }
}

