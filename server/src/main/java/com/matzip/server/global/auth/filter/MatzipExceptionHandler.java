package com.matzip.server.global.auth.filter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matzip.server.global.auth.exception.MatzipAccessDeniedException;
import com.matzip.server.global.auth.exception.UnauthorizedException;
import com.matzip.server.global.common.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Component
public class MatzipExceptionHandler implements AccessDeniedHandler, AuthenticationEntryPoint {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).create();
    private final ErrorResponse accessDenied = new ErrorResponse(new MatzipAccessDeniedException());
    private final ErrorResponse unauthorized = new ErrorResponse(new UnauthorizedException());

    private void prepareResponse(
            HttpServletRequest request, HttpServletResponse response, int status
    ) throws IOException {
        String clientIP = request.getHeader("X-Forwarded-For");
        if (clientIP == null) {
            clientIP = request.getRemoteAddr();
        }
        String token = request.getHeader("Authorization");
        if (token != null) {
            if (token.length() > 20)
                token = token.substring(0, 20) + "...";
            else if (token.isEmpty())
                token = "empty";
        }
        logger.info("{}: URI = {} {}, IP = {}, token = {}, error = {}",
                    status == SC_FORBIDDEN ? "Forbidden" : "Unauthorized",
                    request.getMethod(),
                    request.getRequestURI(),
                    clientIP,
                    token == null ? "null" : token,
                    request.getAttribute("error-response") == null ? "null" :
                    ((ErrorResponse) request.getAttribute("error-response")).getDetail());

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        if (request.getAttribute("error-response") == null) {
            response.getWriter().write(gson.toJson(status == SC_FORBIDDEN ? accessDenied : unauthorized));
        } else {
            response.getWriter().write(gson.toJson(request.getAttribute("error-response")));
        }
        response.setStatus(status);
    }

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException
    ) throws IOException {
        prepareResponse(request, response, SC_FORBIDDEN);
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        prepareResponse(request, response, SC_UNAUTHORIZED);
    }
}
