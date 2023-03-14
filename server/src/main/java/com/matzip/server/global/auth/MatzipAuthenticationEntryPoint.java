package com.matzip.server.global.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matzip.server.global.common.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

@Component
public class MatzipAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).create();
    private final ErrorResponse errorResponse = new ErrorResponse(new UnauthorizedException());

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        logger.info("Unauthorized: cookies={}, authentication={}", request.getCookies(),
                    SecurityContextHolder.getContext().getAuthentication());

        if (request.getAttribute("matzip-exception") == null) {
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(gson.toJson(errorResponse));
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
