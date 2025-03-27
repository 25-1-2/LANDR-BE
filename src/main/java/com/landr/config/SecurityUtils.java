package com.landr.config;

import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import org.springframework.security.core.Authentication;
import com.landr.exception.ExceptionType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return (User) authentication.getPrincipal();
        }
        throw new ApiException(ExceptionType.UNAUTHORIZED_ACCESS);
    }
}
