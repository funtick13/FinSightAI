package com.finsightai.web.repository;

import com.finsightai.web.model.EmailConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, UUID> {
    Optional<EmailConfirmationToken> findByToken(String token);

    Optional<EmailConfirmationToken> findByTokenAndUsedAtIsNull(String token);

    Optional<EmailConfirmationToken> findFirstByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(UUID userId);
}
