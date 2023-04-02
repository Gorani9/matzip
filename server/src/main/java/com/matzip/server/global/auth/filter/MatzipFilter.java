package com.matzip.server.global.auth.filter;

import com.matzip.server.global.auth.exception.InvalidJwtException;
import com.matzip.server.global.auth.model.UserPrincipal;
import com.matzip.server.global.auth.service.JwtProvider;
import com.matzip.server.global.common.dto.ErrorResponse;
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

        try {
            Authentication authentication = jwtProvider.getAuthentication(request.getHeader("Authorization"));
            ((UserPrincipal) authentication.getPrincipal()).setUserIp(clientIP);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (InvalidJwtException e) {
            request.setAttribute("error-response", new ErrorResponse(e));
        } catch (Exception ignored) {
        }

        chain.doFilter(request, response);
    }
}
