package com.finsightai.web.service;

import com.finsightai.web.dto.AuthRequest;
import com.finsightai.web.dto.MessageResponse;
import com.finsightai.web.exception.EmailAlreadyExistsException;
import com.finsightai.web.model.EmailConfirmationToken;
import com.finsightai.web.model.User;
import com.finsightai.web.repository.UserRepository;
import com.finsightai.web.service.auth.AuthService;
import com.finsightai.web.service.auth.EmailConfirmationTokenService;
import com.finsightai.web.service.token.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailConfirmationTokenService emailConfirmationTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService registerService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void isEmailUniqueReturnsTrueWhenEmailDoesNotExist() {
        AuthRequest request = new AuthRequest("user@example.com", "password");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);

        boolean result = registerService.isEmailUnique(request);

        assertTrue(result);
    }

    @Test
    void isEmailUniqueReturnsFalseWhenEmailAlreadyExists() {
        AuthRequest request = new AuthRequest("user@example.com", "password");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        boolean result = registerService.isEmailUnique(request);

        assertFalse(result);
    }

    @Test
    void registerCreatesUnconfirmedUserAndRequestsEmailConfirmationTokenWhenEmailIsUnique() {
        AuthRequest request = new AuthRequest("user@example.com", "plain-password");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        EmailConfirmationToken token = new EmailConfirmationToken();
        token.setToken("confirm-token");
        when(emailConfirmationTokenService.create(any(User.class))).thenReturn(token);

        MessageResponse response = registerService.register(request);

        assertTrue(response.isSuccess());
        assertNotNull(response.getMessage());


        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("user@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPasswordHash());
        assertFalse(savedUser.isEmailConfirmed());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());

        verify(emailConfirmationTokenService).create(savedUser);
    }

    @Test
    void registerRejectsRequestWhenEmailAlreadyExists() {
        AuthRequest request = new AuthRequest("user@example.com", "plain-password");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> registerService.register(request));

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, emailConfirmationTokenService);
    }
}
