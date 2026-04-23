package com.finsightai.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.IdGeneratorType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResetPasswordRequest {
    private String token;
    @NotBlank(message = "Пароль не может быть пустым")
    private String newPassword;
}
