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
@Transactional(readOnly=true)
public class AdminUserService {
    private final UserRepository userRepository;

    public Page<AdminDto.UserResponse> listUsers(AdminDto.UserListRequest userListRequest) {
        Sort sort = userListRequest.getAscending() ?
                    Sort.by(userListRequest.getSortedBy()).ascending() :
                    Sort.by(userListRequest.getSortedBy());
        Pageable pageable = PageRequest.of(userListRequest.getPageNumber(), userListRequest.getPageSize(), sort);
        Page<User> users;
        if (userListRequest.getWithAdmin()) users = userRepository.findAll(pageable);
        else users = userRepository.findAllByRoleEquals(pageable, "NORMAL");
        return users.map(AdminDto.UserResponse::new);
    }

    public Page<AdminDto.UserResponse> searchUsers(AdminDto.UserSearchRequest userSearchRequest) {
        Sort sort = userSearchRequest.getAscending() ?
                    Sort.by(userSearchRequest.getSortedBy()).ascending() :
                    Sort.by(userSearchRequest.getSortedBy());
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
    public void lockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserIdNotFoundException(id));
        if (user.getRole().equals("ADMIN")) throw new LockAdminUserException();
        userRepository.save(user.lock());
    }

    @Transactional
    public void unlockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserIdNotFoundException(id));
        userRepository.save(user.unlock());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserIdNotFoundException(id));
        if (user.getRole().equals("ADMIN")) throw new DeleteAdminUserException();
        userRepository.delete(user);
    }
}

