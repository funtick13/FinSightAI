package com.finsightai.web.model;

import java.time.LocalDateTime;

public interface UserToken {
    String getToken();

    void setToken(String token);

    User getUser();

    void setUser(User user);

    LocalDateTime getCreatedAt();

    void setCreatedAt(LocalDateTime createdAt);

    LocalDateTime getExpiresAt();

    void setExpiresAt(LocalDateTime expiresAt);

    LocalDateTime getUsedAt();

    void setUsedAt(LocalDateTime usedAt);
}
