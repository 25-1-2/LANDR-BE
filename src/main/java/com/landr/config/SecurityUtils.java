package com.landr.config;

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
            return (Long) authentication.getPrincipal();
        }

        throw new ApiException(ExceptionType.UNAUTHORIZED_ACCESS);
    }
}
