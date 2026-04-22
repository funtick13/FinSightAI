package com.finsightai.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResetPasswordRequest {
    private String token;
    @NotBlank(message = "Пароль не может быть пустым")
    private String newPassword;
}
