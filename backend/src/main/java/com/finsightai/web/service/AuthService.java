package com.finsightai.web.service;

import com.finsightai.web.dto.ForgotPasswordRequest;
import com.finsightai.web.dto.RegisterRequest;
import com.finsightai.web.dto.MessageResponse;
import com.finsightai.web.dto.ResetPasswordRequest;
import com.finsightai.web.model.PasswordResetToken;
import com.finsightai.web.model.User;
import com.finsightai.web.repository.PasswordResetTokenRepository;
import com.finsightai.web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailConfirmationTokenService emailConfirmationTokenService;
    private final PasswordResetTokenService passwordResetTokenService;

    public boolean isEmailUnique(RegisterRequest registerRequest) {
        return !userRepository.existsByEmail(registerRequest.getEmail());
    }

    public MessageResponse register(RegisterRequest request) {
        if (!isEmailUnique(request)) {
            return new MessageResponse(false, "Пользователь с таким email уже существует");
        }

        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmailConfirmed(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);
        emailConfirmationTokenService.create(savedUser);

        return new MessageResponse(true, "Подтвердите email");
    }

    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            return new MessageResponse(
                    true,
                    "Ссылка для сброса пароля отправлена на почту"
            );
        }

        PasswordResetToken token = passwordResetTokenService.create(user);

        String resetLink = "http://localhost:8080/api/auth/reset-password?token=" + token.getToken();
        System.out.println("Reset password link: " + resetLink);

        return new MessageResponse(
                true,
                "Ссылка для сброса пароля отправлена на почту"
        );
    }

    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Токен восстановления пароля не найден"));

        if (token.getUsedAt() != null) {
            throw new RuntimeException("Токен восстановления пароля уже использован");
        }

        LocalDateTime now = LocalDateTime.now();
        if (token.getExpiresAt().isBefore(now)) {
            throw new RuntimeException("Срок действия токена восстановления пароля истёк");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(now);

        token.setUsedAt(now);

        userRepository.save(user);
        passwordResetTokenRepository.save(token);

        return new MessageResponse(
                true,
                "Пароль успешно изменён"
        );
    }
}
