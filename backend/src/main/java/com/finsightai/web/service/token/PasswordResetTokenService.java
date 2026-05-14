package com.finsightai.web.service.token;

import com.finsightai.web.model.PasswordResetToken;
import com.finsightai.web.repository.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetTokenService extends TokenService<PasswordResetToken> {

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository) {
        super(passwordResetTokenRepository);
    }

    @Override
    protected PasswordResetToken createToken() {
        return new PasswordResetToken();
    }

}
