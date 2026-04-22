package com.finsightai.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "Эл. почта не может быть пустой")
    @Email(message = "Неверный формат почты")
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}
