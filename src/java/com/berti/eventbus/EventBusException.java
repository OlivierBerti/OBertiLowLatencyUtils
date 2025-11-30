package com.berti.eventbus;

public class EventBusException extends Exception {

    public EventBusException(String message) {
        super(message);
    }

    public EventBusException(String message, Throwable cause) {
        super(message, cause);
    }
}
