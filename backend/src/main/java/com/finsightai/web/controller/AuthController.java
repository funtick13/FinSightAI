package com.finsightai.web.controller;

import com.finsightai.web.dto.ForgotPasswordRequest;
import com.finsightai.web.dto.AuthRequest;
import com.finsightai.web.dto.MessageResponse;
import com.finsightai.web.dto.ResetPasswordRequest;
import com.finsightai.web.service.AuthService;
import com.finsightai.web.service.EmailConfirmationTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final EmailConfirmationTokenService emailConfirmationTokenService;

    @PostMapping("/login")
    public MessageResponse login(@Valid @RequestBody AuthRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    public MessageResponse register(@Valid @RequestBody AuthRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @GetMapping("/confirm")
    public MessageResponse confirmEmail(@RequestParam String token) {
        return emailConfirmationTokenService.confirmEmail(token);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        return authService.forgotPassword(forgotPasswordRequest);
    }

    @PutMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        return authService.resetPassword(resetPasswordRequest);
    }
}
