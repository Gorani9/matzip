package com.matzip.server.global.common.exception;

import com.google.gson.Gson;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class MatzipAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException
    ) throws IOException {
        Gson gson = new Gson();
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorType.USER_NOT_ALLOWED.getErrorCode(), ErrorType.USER_NOT_ALLOWED.name()
        );
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(gson.toJson(errorResponse));
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
