package com.finsightai.web.exception;

public class EmailNotConfirmedException extends AuthException {
    public EmailNotConfirmedException() {
        super("Подтвердите email перед входом в систему");
    }
}
