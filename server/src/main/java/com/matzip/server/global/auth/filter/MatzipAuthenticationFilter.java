package com.matzip.server.global.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.auth.dto.LoginDto;
import com.matzip.server.global.auth.jwt.JwtProvider;
import com.matzip.server.global.auth.model.UserPrincipal;
import com.matzip.server.global.common.exception.ErrorResponse;
import com.matzip.server.global.common.exception.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    public MatzipAuthenticationFilter(
            AuthenticationManager authenticationManager, JwtProvider jwtProvider, ObjectMapper objectMapper) {
        super(authenticationManager);
        setRequiresAuthenticationRequestMatcher(
                new AntPathRequestMatcher("/api/v1/users/login", HttpMethod.POST.name()));
        this.jwtProvider = jwtProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException {
        User user = ((UserPrincipal) authResult.getPrincipal()).getUser();
        if (!user.getIsNonLocked()) {
            ErrorResponse errorResponse = new ErrorResponse(ErrorType.USER_LOCKED, "Current user is locked.");
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            String token = jwtProvider.generateAccessToken(authResult);
            LoginDto.LoginResponse loginResponse = new LoginDto.LoginResponse(authResult.getAuthorities().toString());
            response.addHeader(jwtProvider.getHeader(), token);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);
        logger.error("Unsuccessful Authentication: " + failed.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ErrorType.USER_ACCESS_DENIED, failed.getMessage());
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        LoginDto.LoginRequest parsedRequest;
        try {
            parsedRequest = parseRequest(request);
        } catch (IOException e) {
            throw new com.matzip.server.global.common.exception.IOException();
        }
        return super.getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(parsedRequest.getUsername(), parsedRequest.getPassword()));
    }

    private LoginDto.LoginRequest parseRequest(HttpServletRequest request) throws IOException {
        return new ObjectMapper().readValue(request.getReader(), LoginDto.LoginRequest.class);
    }
}
