package com.finsightai.web.exception;

public class InvalidStatementException extends RuntimeException {
    public InvalidStatementException() {
        super("Некорректные данные выписки");
    }

    public InvalidStatementException(String message) {
        super(message);
    }
}
