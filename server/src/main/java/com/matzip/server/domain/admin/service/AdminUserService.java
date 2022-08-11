package com.matzip.server.domain.admin.service;

import com.matzip.server.domain.admin.dto.AdminDto;
import com.matzip.server.domain.admin.exception.*;
import com.matzip.server.domain.user.dto.UserDto;
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

    public Page<UserDto.Response> findUsers(AdminDto.UserSearchRequest userSearchRequest) {
        Pageable pageRequest = PageRequest.of(
                userSearchRequest.getPageNumber(),
                userSearchRequest.getPageSize(),
                Sort.by("id").ascending());
        Page<User> users;
        if (userSearchRequest.getWithAdmin())
            users = userRepository.findAll(pageRequest);
        else
            users = userRepository.findAllByRoleEquals(pageRequest, "NORMAL");
        return users.map(UserDto.Response::new);
    }

    public UserDto.Response findUserById(Long id) {
        return new UserDto.Response(userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException(id)));
    }

    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException(id));
        if (user.getIsActive())
            throw new UserAlreadyActiveException(id);
        userRepository.save(user.activate());
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException(id));
        if (!user.getIsActive())
            throw new UserAlreadyInactiveException(id);
        else if (user.getRole().equals("ADMIN"))
            throw new DeactivateAdminUserException();
        userRepository.save(user.deactivate());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException(id));
        if (user.getIsActive())
            throw new DeleteActiveUserException();
        userRepository.delete(user);
    }
}

