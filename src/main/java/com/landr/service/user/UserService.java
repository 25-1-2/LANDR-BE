package com.landr.service.user;

import com.landr.controller.user.dto.LoginRequest;
import com.landr.domain.user.User;
import com.landr.domain.user.UserDevice;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.user.UserRepository;
import com.landr.repository.userdevice.UserDeviceRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserDeviceRepository userDeviceRepository;

    @Transactional
    public User findOrCreateUser(LoginRequest request) {
        User user= userRepository.findByEmail(request.getEmail())
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(request.getEmail());
                newUser.setName(request.getName());
                return userRepository.save(newUser);
            });

        UserDevice userDevice = UserDevice.builder()
            .user(user)
            .deviceIdentifier(request.getFcmToken())
            .build();

        userDeviceRepository.save(userDevice);
        log.info("User device saved: {}", userDevice);

        return user;
    }

    @Transactional
    public void updateUserName(Long userId, String newName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(ExceptionType.USER_NOT_FOUND));
        user.updateName(newName);
    }
}
