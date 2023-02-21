package com.matzip.server.domain.auth.model;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public class MatzipAuthenticationToken extends AbstractAuthenticationToken {
    private final UserDetails userPrincipal;

    public MatzipAuthenticationToken(UserDetails userPrincipal) {
        super(userPrincipal.getAuthorities());
        super.setAuthenticated(true);
        this.userPrincipal = userPrincipal;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userPrincipal;
    }
}
