package com.finsightai.web.exception;

public class TokenExpiredException extends AuthException {
    public TokenExpiredException(String tokenPurpose) {
        super("Срок действия токена " + tokenPurpose + " истёк");
    }
}
