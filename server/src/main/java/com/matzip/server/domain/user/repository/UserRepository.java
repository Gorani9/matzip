package com.matzip.server.domain.user.repository;

import com.matzip.server.domain.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Page<User> findAll(Pageable pageable);

    Page<User> findAllByRoleEquals(Pageable pageable, String role);

    Page<User> findAllByUsernameContainsIgnoreCase(Pageable pageable, String username);

    Page<User> findAllByUsernameContainsIgnoreCaseAndIsNonLockedTrueAndRoleEquals(Pageable pageable, String username, String role);

    Page<User> findAllByUsernameContainsIgnoreCaseAndIsNonLockedFalseAndRoleEquals(Pageable pageable, String username, String role);

}