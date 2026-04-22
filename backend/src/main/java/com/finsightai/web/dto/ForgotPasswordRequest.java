package com.finsightai.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;
}
