package com.finsightai.web.repository;

import com.finsightai.web.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByTokenAndUsedAtIsNull(String token);

    Optional<PasswordResetToken> findFirstByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(UUID userId);
}
