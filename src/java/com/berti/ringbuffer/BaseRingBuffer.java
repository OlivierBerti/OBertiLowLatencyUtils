package com.berti.ringbuffer;

import lombok.Getter;

import java.lang.reflect.Array;
import java.util.function.Supplier;


// Here is the base structure. Should not be called outside the package
final class BaseRingBuffer<T> {
    @Getter
    private final int length;

    private final T[] buffer;

    @SuppressWarnings("unchecked")
    public BaseRingBuffer(int length, Class<T> clazz, Supplier<T> supplier) throws RingBufferException {
        if (length  < 2) {
            String errorMsg = "Ring buffer length should be at very least 2. Got: " + length + " instead.";
            throw new RingBufferException(errorMsg);
        }
        this.length = length;
        this.buffer = (T[]) Array.newInstance(clazz, length);
        for (int i=0; i<length; i++) {
            buffer[i] = supplier.get();
        }
    }

    public T get(int index) {
        return buffer[index % length];
    }
}
