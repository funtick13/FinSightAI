package com.finsightai.web.service;

import com.finsightai.web.model.EmailConfirmationToken;
import com.finsightai.web.model.User;
import com.finsightai.web.repository.EmailConfirmationTokenRepository;
import com.finsightai.web.repository.UserRepository;
import com.finsightai.web.service.auth.EmailConfirmationTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailConfirmationTokenServiceTest {
    @Mock
    private EmailConfirmationTokenRepository emailConfirmationTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailConfirmationTokenService emailConfirmationTokenService;

    @Captor
    private ArgumentCaptor<EmailConfirmationToken> tokenCaptor;

    @Test
    void createTokenSavesTokenForUser() {
        User user = new User();
        user.setEmail("user@example.com");
        when(emailConfirmationTokenRepository.save(any(EmailConfirmationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmailConfirmationToken token = emailConfirmationTokenService.create(user);

        verify(emailConfirmationTokenRepository).save(tokenCaptor.capture());
        EmailConfirmationToken savedToken = tokenCaptor.getValue();
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

    @Test
    void confirmEmailMarksUserAsConfirmedAndTokenAsUsed() {
        User user = new User();
        user.setEmailConfirmed(false);
        user.setUpdatedAt(LocalDateTime.now().minusDays(1));

        EmailConfirmationToken token = new EmailConfirmationToken();
        token.setToken("confirmation-token");
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now().minusHours(1));
        token.setExpiresAt(LocalDateTime.now().plusHours(1));

        when(emailConfirmationTokenRepository.findByTokenAndUsedAtIsNull("confirmation-token"))
                .thenReturn(Optional.of(token));

        emailConfirmationTokenService.confirmEmail("confirmation-token");

        assertNotNull(token.getUsedAt());
        assertTrue(user.getUpdatedAt().isAfter(token.getCreatedAt()));
        verify(userRepository).save(user);
        verify(emailConfirmationTokenRepository).save(token);
    }

    @Test
    void confirmEmailRejectsUnknownOrUsedToken() {
        when(emailConfirmationTokenRepository.findByTokenAndUsedAtIsNull("bad-token"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> emailConfirmationTokenService.confirmEmail("bad-token"));

        verify(userRepository, never()).save(any(User.class));
        verify(emailConfirmationTokenRepository, never()).save(any(EmailConfirmationToken.class));
    }

    @Test
    void confirmEmailRejectsExpiredToken() {
        User user = new User();

        EmailConfirmationToken token = new EmailConfirmationToken();
        token.setToken("expired-token");
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now().minusDays(2));
        token.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(emailConfirmationTokenRepository.findByTokenAndUsedAtIsNull("expired-token"))
                .thenReturn(Optional.of(token));

        assertThrows(RuntimeException.class, () -> emailConfirmationTokenService.confirmEmail("expired-token"));

        verify(userRepository, never()).save(any(User.class));
        verify(emailConfirmationTokenRepository, never()).save(any(EmailConfirmationToken.class));
    }
}
