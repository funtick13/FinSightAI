package com.finsightai.web.exception;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException() {
        super("Операция не найдена");
    }

    public TransactionNotFoundException(String message) {
        super(message);
    }
}