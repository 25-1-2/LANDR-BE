package com.landr.service.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.landr.domain.user.UserDevice;
import com.landr.repository.userdevice.UserDeviceRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;
    private final UserDeviceRepository userDeviceRepository;

    /**
     * 특정 사용자에게 푸시 알림을 전송합니다. 가장 최신의 FCM 토큰을 사용합니다.
     *
     * @param userId 사용자 ID
     * @param title  알림 제목
     * @param body   알림 내용
     * @return 전송 성공 여부
     */
    public boolean sendNotificationToUser(Long userId, String title, String body) {
        try {
            Optional<UserDevice> latestDevice = userDeviceRepository.findLatestByUserId(userId);

            if (latestDevice.isEmpty()) {
                log.warn("사용자 {}의 FCM 토큰을 찾을 수 없습니다.", userId);
                return false;
            }

            String fcmToken = latestDevice.get().getDeviceIdentifier();
            return sendNotification(fcmToken, title, body);

        } catch (Exception e) {
            log.error("사용자 {}에게 푸시 알림 전송 실패", userId, e);
            return false;
        }
    }

    /**
     * FCM 토큰으로 푸시 알림을 전송합니다.
     *
     * @param fcmToken FCM 토큰
     * @param title    알림 제목
     * @param body     알림 내용
     * @return 전송 성공 여부
     */
    private boolean sendNotification(String fcmToken, String title, String body) {
        try {
            Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

            Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(notification)
                .build();

            String response = firebaseMessaging.send(message);
            log.info("푸시 알림 전송 성공: {}", response);
            return true;

        } catch (Exception e) {
            log.error("FCM 푸시 알림 전송 실패: token={}", fcmToken, e);
            return false;
        }
    }
}