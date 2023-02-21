package com.matzip.server.admin.model;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class AdminAuthenticationToken extends AbstractAuthenticationToken {
    public AdminAuthenticationToken() {
        super(List.of(new SimpleGrantedAuthority("ADMIN")));
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
