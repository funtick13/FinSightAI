package com.finsightai.web.exception;

public class EmailAlreadyExistsException extends AuthException {
    public EmailAlreadyExistsException() {
        super("Пользователь с таким email уже существует");
    }
}
