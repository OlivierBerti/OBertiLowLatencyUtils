package com.berti.throttling;

public class ThrottlingException extends Exception {
    public ThrottlingException(String message) {
        super(message);
    }

    public ThrottlingException(String message, Throwable cause) {
        super(message, cause);
    }
}
