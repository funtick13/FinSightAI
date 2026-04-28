package com.finsightai.web.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginResponse {
    private boolean success;
    private String message;
    private String accessToken;
}
