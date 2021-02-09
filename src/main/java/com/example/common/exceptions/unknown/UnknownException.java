package com.example.common.exceptions.unknown;

public class UnknownException extends RuntimeException{
    public UnknownException() {
        super();
    }

    public UnknownException(String message) {
        super(message);
    }
}
