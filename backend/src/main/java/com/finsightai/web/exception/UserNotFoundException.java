package com.finsightai.web.exception;

public class UserNotFoundException extends AuthException {
    public UserNotFoundException() {
        super("Пользователь с таким email не найден");
    }
}
