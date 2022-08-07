package com.matzip.server.global.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.global.auth.dto.LoginRequest;
import com.matzip.server.global.auth.jwt.JwtProvider;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MatzipAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtProvider jwtProvider;

    public MatzipAuthenticationFilter(AuthenticationManager authenticationManager, JwtProvider jwtProvider) {
        super(authenticationManager);
        setRequiresAuthenticationRequestMatcher(
                new AntPathRequestMatcher("/api/v1/users/login/", HttpMethod.POST.name())
        );
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult
    ) throws IOException {
        String token = jwtProvider.generateAccessToken(authResult);
        response.addHeader(jwtProvider.getHeader(), token);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write("{\n\"access_token:\": \"" + token + "\"\n}");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException failed
    ) throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response
    ) throws AuthenticationException {
        LoginRequest parsedRequest;
        try {
            parsedRequest = parseRequest(request);
        } catch (IOException e) {
            throw new com.matzip.server.global.common.exception.IOException();
        }
        return super.getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(
                parsedRequest.getUsername(), parsedRequest.getPassword()
        ));
    }

    private LoginRequest parseRequest(HttpServletRequest request) throws IOException {
        return new ObjectMapper().readValue(request.getReader(), LoginRequest.class);
    }
}
