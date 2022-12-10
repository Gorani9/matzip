package com.matzip.server.global.auth.jwt;

import com.google.gson.Gson;
import com.matzip.server.global.common.exception.ErrorResponse;
import com.matzip.server.global.common.exception.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class MatzipAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        Gson gson = new Gson();
        ErrorResponse errorResponse = new ErrorResponse(ErrorType.USER_AUTH_FAILED, authException.getMessage());
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(gson.toJson(errorResponse));
        response.getWriter().flush();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
