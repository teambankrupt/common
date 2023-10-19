package com.example.common.exceptions.exists;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AlreadyExistsException extends RuntimeException {
    private Map<String, Set<String>> headers = new HashMap<>();

    public AlreadyExistsException() {
    }

    public AlreadyExistsException(String message, Map<String, Set<String>> headers) {
        super(message);
        this.headers = headers;
    }

    public AlreadyExistsException(String message) {
        super(message);
    }

    public AlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyExistsException(Throwable cause) {
        super(cause);
    }

    public AlreadyExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public Map<String, Set<String>> getHeaders() {
        return this.headers;
    }
}
