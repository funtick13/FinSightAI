package com.finsightai.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "")
    private String email;
    private String password;
}
