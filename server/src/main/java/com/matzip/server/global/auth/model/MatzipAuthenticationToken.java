package com.matzip.server.global.auth.model;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class MatzipAuthenticationToken extends AbstractAuthenticationToken {
    private final UserDetails userPrincipal;
    private Object accessToken;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    public MatzipAuthenticationToken(
            UserDetails userPrincipal,
            Object accessToken,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        super.setAuthenticated(authorities != null);
        this.userPrincipal = userPrincipal;
        this.accessToken = accessToken;
    }


    @Override
    public Object getCredentials() {
        return accessToken;
    }

    @Override
    public Object getPrincipal() {
        return userPrincipal;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        accessToken = null;
    }
}
