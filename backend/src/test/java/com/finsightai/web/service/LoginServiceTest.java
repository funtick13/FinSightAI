package com.finsightai.web.service;

import com.finsightai.web.dto.AuthRequest;
import com.finsightai.web.dto.LoginResponse;
import com.finsightai.web.exception.EmailNotConfirmedException;
import com.finsightai.web.exception.InvalidCredentialsException;
import com.finsightai.web.exception.UserNotFoundException;
import com.finsightai.web.model.User;
import com.finsightai.web.repository.PasswordResetTokenRepository;
import com.finsightai.web.repository.UserRepository;
import com.finsightai.web.service.auth.AuthService;
import com.finsightai.web.service.auth.EmailConfirmationTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailConfirmationTokenService emailConfirmationTokenService;

    @Mock
    private PasswordResetTokenService passwordResetTokenService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginReturnsSuccessWhenEmailIsConfirmedAndPasswordMatches() {
        AuthRequest request = new AuthRequest("user@example.com", "plain-password");
        User user = confirmedUser();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertTrue(response.isSuccess());
        assertNotNull(response.getMessage());
        assertNotNull(response.getAccessToken());
        verify(passwordEncoder).matches("plain-password", "encoded-password");
        verify(jwtService).generateToken(user);
        verifyNoInteractions(emailConfirmationTokenService, passwordResetTokenService, passwordResetTokenRepository);
    }

    @Test
    void loginRejectsUserWhenEmailIsNotConfirmed() {
        AuthRequest request = new AuthRequest("user@example.com", "plain-password");
        User user = confirmedUser();
        user.setEmailConfirmed(false);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThrows(EmailNotConfirmedException.class, () -> authService.login(request));

        verify(passwordEncoder, never()).matches("plain-password", "encoded-password");
    }

    @Test
    void loginRejectsUserWhenPasswordDoesNotMatch() {
        AuthRequest request = new AuthRequest("user@example.com", "wrong-password");
        User user = confirmedUser();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        verify(passwordEncoder).matches("wrong-password", "encoded-password");
    }

    @Test
    void loginRejectsUnknownEmail() {
        AuthRequest request = new AuthRequest("missing@example.com", "plain-password");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(request));

        verifyNoInteractions(passwordEncoder, emailConfirmationTokenService, passwordResetTokenService, passwordResetTokenRepository);
    }

    private User confirmedUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash("encoded-password");
        user.setEmailConfirmed(true);
        return user;
    }
}
