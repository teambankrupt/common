package com.example.common.exceptions.exists.not;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NotExistsException extends RuntimeException {
    private Map<String, Set<String>> headers = new HashMap<>();

    public NotExistsException() {
    }

    public NotExistsException(String message, Map<String, Set<String>> headers) {
        super(message);
        this.headers = headers;
    }

    public Map<String, Set<String>> getHeaders() {
        return this.headers;
    }
}
