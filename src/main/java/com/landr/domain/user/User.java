package com.landr.domain.user;

import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {

    @Id
    private Long id;

    private String email;

    private String name;

    private String subject;

    private String provider;

    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private boolean is_deleted;

    // 디바이스 정보
    private List<String> devices;

}

