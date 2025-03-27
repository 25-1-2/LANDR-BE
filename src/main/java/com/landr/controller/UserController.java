package com.landr.controller;

import com.landr.config.JwtTokenProvider;
import com.landr.controller.dto.LoginRequest;
import com.landr.controller.dto.LoginResponse;
import com.landr.controller.dto.UserResponse;
import com.landr.domain.user.User;
import com.landr.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.findOrCreateUser(request.getEmail(), request.getName());
        String token = jwtTokenProvider.createToken(user.getId());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserProfile(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getName()));
    }
}