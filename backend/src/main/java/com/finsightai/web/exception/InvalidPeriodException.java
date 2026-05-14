package com.finsightai.web.exception;

public class InvalidPeriodException extends RuntimeException {

    public InvalidPeriodException() {
        super("Некорректный период");
    }

    public InvalidPeriodException(String message) {
        super(message);
    }
}