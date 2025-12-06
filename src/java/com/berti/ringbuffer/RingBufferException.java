package com.berti.ringbuffer;

public class RingBufferException extends Exception {
    public RingBufferException(String message) {
        super(message);
    }

    public RingBufferException(String message, Throwable cause) {
        super(message, cause);
    }
}
