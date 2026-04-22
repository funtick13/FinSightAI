package com.finsightai.web.repository;

import com.finsightai.web.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByTokenAndUsedAtIsNull(String token);

    Optional<PasswordResetToken> findFirstByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(UUID userId);
}
