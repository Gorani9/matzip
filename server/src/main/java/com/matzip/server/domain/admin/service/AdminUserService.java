package com.matzip.server.domain.admin.service;

import com.matzip.server.domain.admin.dto.AdminDto;
import com.matzip.server.domain.admin.exception.AdminUserStatusChangeException;
import com.matzip.server.domain.admin.exception.DeleteAdminUserException;
import com.matzip.server.domain.admin.exception.UserIdNotFoundException;
import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class AdminUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    public Page<AdminDto.UserResponse> listUsers(AdminDto.UserListRequest userListRequest) {
        Sort sort = userListRequest.getAscending() ?
                    Sort.by(userListRequest.getSortedBy()).ascending() :
                    Sort.by(userListRequest.getSortedBy()).descending();
        Pageable pageable = PageRequest.of(userListRequest.getPageNumber(), userListRequest.getPageSize(), sort);
        Page<User> users;
        if (userListRequest.getWithAdmin()) users = userRepository.findAll(pageable);
        else users = userRepository.findAllByRoleEquals(pageable, "NORMAL");
        return users.map(AdminDto.UserResponse::new);
    }

    public Page<AdminDto.UserResponse> searchUsers(AdminDto.UserSearchRequest userSearchRequest) {
        Sort sort = userSearchRequest.getAscending() ?
                    Sort.by(userSearchRequest.getSortedBy()).ascending() :
                    Sort.by(userSearchRequest.getSortedBy()).descending();
        Pageable pageable = PageRequest.of(userSearchRequest.getPageNumber(), userSearchRequest.getPageSize(), sort);
        Page<User> users;
        if (userSearchRequest.getIsNonLocked() == null)
            users = userRepository.findAllByUsernameContainsIgnoreCase(pageable, userSearchRequest.getUsername());
        else if (userSearchRequest.getIsNonLocked())
            users = userRepository.findAllByUsernameContainsIgnoreCaseAndIsNonLockedTrueAndRoleEquals(
                    pageable,
                    userSearchRequest.getUsername(),
                    "NORMAL");
        else users = userRepository.findAllByUsernameContainsIgnoreCaseAndIsNonLockedFalseAndRoleEquals(
                    pageable,
                    userSearchRequest.getUsername(),
                    "NORMAL");
        return users.map(AdminDto.UserResponse::new);
    }

    public AdminDto.UserResponse findUserById(Long id) {
        return new AdminDto.UserResponse(
                userRepository.findById(id).orElseThrow(() -> new UserIdNotFoundException(id)));
    }

    @Transactional
    public AdminDto.UserResponse patchUserById(Long id, AdminDto.UserPatchRequest userPatchRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserIdNotFoundException(id));
        if (user.getRole().equals("ADMIN")) throw new AdminUserStatusChangeException();
        if (Optional.ofNullable(userPatchRequest.getProfileImageUrl()).isPresent())
            imageService.deleteImage(user.getProfileImageUrl());
        return new AdminDto.UserResponse(userRepository.save(user.patchFromAdmin(userPatchRequest)));
    }

    @Transactional
    public AdminDto.UserResponse lockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserIdNotFoundException(id));
        if (user.getRole().equals("ADMIN")) throw new AdminUserStatusChangeException();
        return new AdminDto.UserResponse(userRepository.save(user.lock()));
    }

    @Transactional
    public AdminDto.UserResponse unlockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserIdNotFoundException(id));
        return new AdminDto.UserResponse(userRepository.save(user.unlock()));

    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserIdNotFoundException(id));
        if (user.getRole().equals("ADMIN")) throw new DeleteAdminUserException();
        userRepository.delete(user);
    }

    @Transactional
    public AdminDto.UserResponse changeUserPassword(Long id, MeDto.PasswordChangeRequest passwordChangeRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserIdNotFoundException(id));
        if (user.getRole().equals("ADMIN")) throw new AdminUserStatusChangeException();
        return new AdminDto.UserResponse(userRepository.save(user.changePassword(
                passwordChangeRequest,
                passwordEncoder)));
    }
}

