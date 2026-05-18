package com.finsightai.web.service.auth;

import com.finsightai.web.dto.auth.AuthRequest;
import com.finsightai.web.dto.auth.ForgotPasswordRequest;
import com.finsightai.web.dto.auth.LoginResponse;
import com.finsightai.web.dto.auth.ResetPasswordRequest;
import com.finsightai.web.dto.exception.MessageResponse;
import com.finsightai.web.exception.EmailAlreadyExistsException;
import com.finsightai.web.exception.EmailNotConfirmedException;
import com.finsightai.web.exception.InvalidCredentialsException;
import com.finsightai.web.exception.TokenAlreadyUsedException;
import com.finsightai.web.exception.TokenExpiredException;
import com.finsightai.web.exception.TokenNotFoundException;
import com.finsightai.web.exception.UserNotFoundException;
import com.finsightai.web.model.EmailConfirmationToken;
import com.finsightai.web.model.PasswordResetToken;
import com.finsightai.web.model.User;
import com.finsightai.web.repository.PasswordResetTokenRepository;
import com.finsightai.web.repository.UserRepository;
import com.finsightai.web.service.token.JwtService;
import com.finsightai.web.service.token.PasswordResetTokenService;
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
    private final JwtService jwtService;

    public boolean isEmailUnique(AuthRequest authRequest) {
        return !userRepository.existsByEmail(authRequest.getEmail());
    }

    public LoginResponse login(AuthRequest authRequest) {
        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(UserNotFoundException::new);

        if (!user.isEmailConfirmed()) {
            throw new EmailNotConfirmedException();
        }

        boolean passwordMatches = passwordEncoder.matches(
                authRequest.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateToken(user);

        return new LoginResponse(
                true,
                "Авторизация выполнена успешно",
                accessToken
                );
    }

    public MessageResponse register(AuthRequest request) {
        if (!isEmailUnique(request)) {
            throw new EmailAlreadyExistsException();
        }

        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmailConfirmed(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);
        EmailConfirmationToken token = emailConfirmationTokenService.create(savedUser);

        String confirmEmailLink = "http://localhost:8080/api/auth/confirm?token=" + token.getToken();
        System.out.println("Ссылка для подтверждения пароля: " + confirmEmailLink);

        return new MessageResponse(
                true,
                "Аккаунт создан. Подтвердите email для активации учётной записи");
    }

    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            return new MessageResponse(
                    true,
                    "Если пользователь существует, ссылка для сброса пароля будет отправлена на почту"
            );
        }

        PasswordResetToken token = passwordResetTokenService.create(user);

        String resetLink = "http://localhost:8080/api/auth/reset-password?token=" + token.getToken();
        System.out.println("Ссылка для сброса пароля: " + resetLink);

        return new MessageResponse(
                true,
                "Ссылка для сброса пароля отправлена на почту"
        );
    }

    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new TokenNotFoundException("восстановления пароля"));

        if (token.getUsedAt() != null) {
            throw new TokenAlreadyUsedException("восстановления пароля");
        }

        LocalDateTime now = LocalDateTime.now();
        if (token.getExpiresAt().isBefore(now)) {
            throw new TokenExpiredException("восстановления пароля");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(now);

        token.setUsedAt(now);

        userRepository.save(user);
        passwordResetTokenRepository.save(token);

        return new MessageResponse(true, "Пароль успешно изменён");
    }
}
