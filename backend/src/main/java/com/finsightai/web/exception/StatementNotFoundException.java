package com.finsightai.web.exception;

public class StatementNotFoundException extends RuntimeException {
    public StatementNotFoundException() {
        super("Выписка не найдена");
    }

    public StatementNotFoundException(String message) {
        super(message);
    }
}
