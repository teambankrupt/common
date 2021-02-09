package com.example.common.exceptions.nullpointer;

public class NullException extends RuntimeException{
    public NullException() {
        super();
    }

    public NullException(String message) {
        super(message);
    }
}
