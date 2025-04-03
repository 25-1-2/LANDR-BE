package com.landr.config;

import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import org.springframework.security.core.Authentication;
import com.landr.exception.ExceptionType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getId(); // User 객체에서 ID를 가져옴
            }
        }
        throw new ApiException(ExceptionType.UNAUTHORIZED_ACCESS);
    }
}
