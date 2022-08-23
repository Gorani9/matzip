package com.matzip.server.global.auth.jwt;

import com.google.gson.Gson;
import com.matzip.server.global.common.exception.ErrorResponse;
import com.matzip.server.global.common.exception.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class MatzipAccessDeniedHandler implements AccessDeniedHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException
    ) throws IOException {
        logger.error("Handle servlet response when access denied: " + accessDeniedException.getMessage());
        Gson gson = new Gson();
        ErrorResponse errorResponse = new ErrorResponse(ErrorType.USER_ACCESS_DENIED, accessDeniedException.getMessage());
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(gson.toJson(errorResponse));
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
