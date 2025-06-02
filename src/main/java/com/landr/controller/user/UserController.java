package com.landr.controller.user;

import com.landr.config.JwtTokenProvider;
import com.landr.controller.user.dto.EditNameRequest;
import com.landr.controller.user.dto.LoginRequest;
import com.landr.controller.user.dto.LoginResponse;
import com.landr.controller.user.dto.UserResponse;
import com.landr.domain.user.User;
import com.landr.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "User 관련 API")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "사용자 로그인")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        User user = userService.findOrCreateUser(request);
        String token = jwtTokenProvider.createToken(user.getId());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @Operation(summary = "현재 사용자 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserProfile(
            @AuthenticationPrincipal User user
    ) {
        UserResponse response = new UserResponse(user.getId(), user.getEmail(), user.getName());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용자 이름 수정")
    @PatchMapping("/me/name")
    public ResponseEntity<UserResponse> updateUserName(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid EditNameRequest request
    ) {
        userService.updateUserName(user.getId(), request.getName());
        return ResponseEntity.ok(
            UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build());
    }
}