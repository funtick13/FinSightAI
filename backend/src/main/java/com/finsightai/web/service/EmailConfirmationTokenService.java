package com.finsightai.web.service;

import com.finsightai.web.dto.MessageResponse;
import com.finsightai.web.model.EmailConfirmationToken;
import com.finsightai.web.model.User;
import com.finsightai.web.repository.EmailConfirmationTokenRepository;
import com.finsightai.web.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailConfirmationTokenService extends TokenService<EmailConfirmationToken> {
    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;
    private final UserRepository userRepository;

    public EmailConfirmationTokenService(
            EmailConfirmationTokenRepository emailConfirmationTokenRepository,
            UserRepository userRepository
    ) {
        super(emailConfirmationTokenRepository);
        this.emailConfirmationTokenRepository = emailConfirmationTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected EmailConfirmationToken createToken() {
        return new EmailConfirmationToken();
    }

    public MessageResponse confirmEmail(String tokenValue) {
        EmailConfirmationToken token = emailConfirmationTokenRepository.findByTokenAndUsedAtIsNull(tokenValue)
                .orElseThrow(() -> new RuntimeException("Токен подтверждения email не найден"));

        LocalDateTime now = LocalDateTime.now();
        if (token.getExpiresAt().isBefore(now)) {
            throw new RuntimeException("Срок действия токена подтверждения email истёк");
        }

        User user = token.getUser();
        user.setEmailConfirmed(true);
        user.setUpdatedAt(now);
        token.setUsedAt(now);

        userRepository.save(user);
        emailConfirmationTokenRepository.save(token);

        return new MessageResponse(
                true,
                "Email успешно подтверждён"
        );
    }
}
