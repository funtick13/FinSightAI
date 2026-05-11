package com.finsightai.web.exception;

public class UnsupportedFileTypeException extends RuntimeException {
    public UnsupportedFileTypeException() {
        super("Можно загружать только PDF-файлы");
    }

    public UnsupportedFileTypeException(String message) {
        super(message);
    }
}
