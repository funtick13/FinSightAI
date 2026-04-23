package com.finsightai.web.exception;

public class JwtKeyException extends AuthException {
    public JwtKeyException(Throwable cause) {
        super("Не удалось подготовить ключ подписи JWT", cause);
    }
}
