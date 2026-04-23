package com.finsightai.web.exception;

public class TokenNotFoundException extends AuthException {
    public TokenNotFoundException(String tokenPurpose) {
        super("Токен " + tokenPurpose + " не найден");
    }
}
