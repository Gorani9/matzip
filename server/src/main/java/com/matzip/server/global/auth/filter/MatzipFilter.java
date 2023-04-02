package com.matzip.server.global.auth.filter;

import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.auth.model.MatzipAuthenticationToken;
import com.matzip.server.global.auth.model.UserPrincipal;
import com.matzip.server.global.auth.service.JwtProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MatzipFilter extends BasicAuthenticationFilter {
    private final JwtProvider jwtProvider;
    private final static User anonymousUser = new User("anonymousUser", "");

    public MatzipFilter(AuthenticationManager authenticationManager, JwtProvider jwtProvider) {
        super(authenticationManager);
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        String clientIP = request.getHeader("X-Forwarded-For");
        if (clientIP == null) {
            clientIP = request.getRemoteAddr();
        }

        Authentication authentication;
        try {
            authentication = jwtProvider.getAuthentication(request.getHeader("Authorization"));
        } catch (Exception e) {
            authentication = new MatzipAuthenticationToken(new UserPrincipal(anonymousUser));
        }

        ((UserPrincipal) authentication.getPrincipal()).setUserIp(clientIP);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }
}
