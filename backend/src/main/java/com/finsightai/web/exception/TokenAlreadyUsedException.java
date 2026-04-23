package com.finsightai.web.exception;

public class TokenAlreadyUsedException extends AuthException {
    public TokenAlreadyUsedException(String tokenPurpose) {
        super("Токен " + tokenPurpose + " уже использован");
    }
}
