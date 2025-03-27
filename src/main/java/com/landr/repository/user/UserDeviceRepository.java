package com.landr.repository.user;

import com.landr.domain.user.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByUserIdAndDeviceIdentifier(Long userId, String deviceIdentifier);
}