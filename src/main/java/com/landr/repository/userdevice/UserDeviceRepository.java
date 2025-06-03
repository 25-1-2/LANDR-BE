package com.landr.repository.userdevice;

import com.landr.domain.user.UserDevice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    Optional<UserDevice> findByUserId(Long userId);

    Optional<UserDevice> findByUserIdAndDeviceIdentifier(Long userId, String deviceIdentifier);

    @Query("SELECT ud FROM UserDevice ud WHERE ud.user.id = :userId ORDER BY ud.createdAt DESC")
    List<UserDevice> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT ud FROM UserDevice ud WHERE ud.user.id = :userId ORDER BY ud.createdAt DESC LIMIT 1")
    Optional<UserDevice> findLatestByUserId(Long userId);
}
