package com.finsightai.web.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResetPasswordRequest {
    private String token;
    @NotBlank(message = "Пароль не может быть пустым")
    private String newPassword;
}
