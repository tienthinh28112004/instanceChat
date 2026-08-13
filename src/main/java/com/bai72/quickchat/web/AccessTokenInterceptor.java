package com.bai72.quickchat.web;

import com.bai72.quickchat.exception.ApiException;
import com.bai72.quickchat.model.UserAccount;
import com.bai72.quickchat.store.UserStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AccessTokenInterceptor implements HandlerInterceptor {
    public static final String CURRENT_USER = "currentUser";

    private final UserStore userStore;

    public AccessTokenInterceptor(UserStore userStore) {
        this.userStore = userStore;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        if (token == null || token.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Access token is required");
        }

        UserAccount user = userStore.findByToken(token).orElse(null);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Access token is missing or expired");
        }

        request.setAttribute(CURRENT_USER, user);
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String custom = request.getHeader("Access-Token");
        if (custom != null && !custom.isBlank()) {
            return custom.trim();
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || header.isBlank()) {
            return null;
        }
        if (header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return header.substring(7).trim();
        }
        return header.trim();
    }
}
