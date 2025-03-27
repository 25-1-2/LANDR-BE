package com.landr.service.user;

import com.landr.domain.user.User;
import com.landr.domain.user.UserDevice;
import com.landr.repository.user.UserDeviceRepository;
import com.landr.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    @Transactional
    public User findOrCreateUser(String email, String name, String fcmToken) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(name)
                            .provider("GOOGLE") // 제공자 정보 추가
                            .build();
                    return userRepository.save(newUser);
                });

        // FCM 토큰이 제공되었다면 디바이스 정보 저장
        if (fcmToken != null && !fcmToken.isEmpty()) {
            saveOrUpdateUserDevice(user, fcmToken);
        }

        return user;
    }

    @Transactional
    public void saveOrUpdateUserDevice(User user, String deviceIdentifier) {
        // 기존 디바이스 찾기
        Optional<UserDevice> existingDevice = userDeviceRepository
                .findByUserIdAndDeviceIdentifier(user.getId(), deviceIdentifier);

        if (existingDevice.isEmpty()) {
            // 새 디바이스 등록
            UserDevice userDevice = new UserDevice();
            userDevice.setUser(user);
            userDevice.setDeviceIdentifier(deviceIdentifier);
            userDeviceRepository.save(userDevice);
        }
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}