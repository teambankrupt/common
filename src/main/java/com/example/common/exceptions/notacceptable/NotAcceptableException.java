package com.example.common.exceptions.notacceptable;

public class NotAcceptableException extends RuntimeException{
    public NotAcceptableException() {
        super();
    }

    public NotAcceptableException(String message) {
        super(message);
    }
}
