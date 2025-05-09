package com.landr.domain.user;

import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(name = "provider")
    private String provider;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 사용자 이름을 업데이트합니다.
     *
     * @param newName 새로운 사용자 이름
     * @throws ApiException 사용자 이름이 null이거나 비어있거나 길이가 3자 미만 또는 9자 초과인 경우
     */
    public void updateName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new ApiException(ExceptionType.INVALID_USER_NAME);
        }

        if (newName.length() < 3 || newName.length() > 9) {
            throw new ApiException(ExceptionType.INVALID_USER_NAME, "이름은 3자 이상 9자 이하로 입력해야 합니다.");
        }

        this.name = newName;
    }
}