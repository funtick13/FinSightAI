package com.finsightai.web.service;

import com.finsightai.web.model.User;
import com.finsightai.web.model.UserToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class TokenService<T extends UserToken> {
    private static final int TOKEN_EXPIRATION_HOURS = 24;

    private final JpaRepository<T, UUID> repository;

    public T create(User user) {
        T token = createToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS));
        token.setUsedAt(null);

        return repository.save(token);
    }

    protected abstract T createToken();
}
