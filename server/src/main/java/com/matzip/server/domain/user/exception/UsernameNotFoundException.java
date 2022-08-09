package com.matzip.server.domain.user.exception;

public class UsernameNotFoundException extends UserNotFoundException {
    public UsernameNotFoundException(String username) {
        super("username " + username);
    }
}
