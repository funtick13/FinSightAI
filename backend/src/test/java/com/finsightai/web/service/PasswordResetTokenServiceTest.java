package com.finsightai.web.service;

import com.finsightai.web.model.PasswordResetToken;
import com.finsightai.web.model.User;
import com.finsightai.web.repository.PasswordResetTokenRepository;
import com.finsightai.web.service.token.PasswordResetTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceTest {
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private PasswordResetTokenService passwordResetTokenService;

    @Captor
    private ArgumentCaptor<PasswordResetToken> tokenCaptor;

    @Test
    void createSavesPasswordResetTokenForUser() {
        User user = new User();
        user.setEmail("user@example.com");
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PasswordResetToken token = passwordResetTokenService.create(user);

        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertSame(savedToken, token);
        assertSame(user, savedToken.getUser());
        assertNotNull(savedToken.getToken());
        assertFalse(savedToken.getToken().isBlank());
        assertNotNull(savedToken.getCreatedAt());
        assertNotNull(savedToken.getExpiresAt());
        assertTrue(savedToken.getExpiresAt().isAfter(savedToken.getCreatedAt()));
        assertEquals(24, java.time.Duration.between(savedToken.getCreatedAt(), savedToken.getExpiresAt()).toHours());
        assertNull(savedToken.getUsedAt());
    }
}
