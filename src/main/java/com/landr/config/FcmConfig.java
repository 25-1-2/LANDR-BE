package com.landr.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.landr.config.fcm.FcmProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FcmConfig {

    private final FcmProperties fcmProperties;

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                ByteArrayInputStream secretKey = getKey();
                GoogleCredentials credentials = GoogleCredentials.fromStream(secretKey);

                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase application initialized successfully");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    private ByteArrayInputStream getKey() {
        String encodedSecretKey = fcmProperties.getSecretKey();
        byte[] decodedKey = Base64.getDecoder().decode(encodedSecretKey);
        return new ByteArrayInputStream(decodedKey);
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance();
    }
}