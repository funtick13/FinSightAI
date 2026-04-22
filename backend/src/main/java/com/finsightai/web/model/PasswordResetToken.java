package com.finsightai.web.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetToken implements UserToken {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false, unique = true)
    private String token;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
}
