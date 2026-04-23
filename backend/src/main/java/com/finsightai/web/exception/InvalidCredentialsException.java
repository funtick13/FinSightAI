package com.finsightai.web.exception;

public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super("Неверный email или пароль");
    }
}
