package com.matzip.server.domain.admin.service;

import com.matzip.server.domain.admin.dto.AdminDto;
import com.matzip.server.domain.admin.exception.DeleteAdminUserException;
import com.matzip.server.domain.admin.exception.LockAdminUserException;
import com.matzip.server.domain.admin.exception.UserIdNotFoundException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {
    private final UserRepository userRepository;

    public Page<AdminDto.UserResponse> listUsers(AdminDto.UserListRequest userListRequest) {
        Pageable pageRequest = PageRequest.of(
                userListRequest.getPageNumber(),
                userListRequest.getPageSize(),
                Sort.by("id").ascending());
        Page<User> users;
        if (userListRequest.getWithAdmin())
            users = userRepository.findAll(pageRequest);
        else
            users = userRepository.findAllByRoleEquals(pageRequest, "NORMAL");
        return users.map(AdminDto.UserResponse::new);
    }

    public Page<AdminDto.UserResponse> searchUsers(AdminDto.UserSearchRequest userSearchRequest) {
        PageRequest pageRequest = PageRequest.of(
                userSearchRequest.getPageNumber(),
                userSearchRequest.getPageSize(),
                Sort.by("id").ascending()
        );
        Page<User> users;
        if (userSearchRequest.getIsNonLocked() == null)
            users = userRepository
                    .findAllByUsernameContainsIgnoreCase(pageRequest, userSearchRequest.getUsername());
        else if (userSearchRequest.getIsNonLocked())
            users = userRepository
                    .findAllByUsernameContainsIgnoreCaseAndIsNonLockedTrueAndRoleEquals(pageRequest, userSearchRequest.getUsername(), "NORMAL");
        else
            users = userRepository
                    .findAllByUsernameContainsIgnoreCaseAndIsNonLockedFalseAndRoleEquals(pageRequest, userSearchRequest.getUsername(), "NORMAL");
        return users.map(AdminDto.UserResponse::new);
    }

    public AdminDto.UserResponse findUserById(Long id) {
        return new AdminDto.UserResponse(userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException(id)));
    }

    @Transactional
    public void lockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException(id));
        userRepository.save(user.lock());
    }

    @Transactional
    public void unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException(id));
        if (user.getRole().equals("ADMIN"))
            throw new LockAdminUserException();
        userRepository.save(user.unlock());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException(id));
        if (user.getRole().equals("ADMIN"))
            throw new DeleteAdminUserException();
        userRepository.delete(user);
    }
}

