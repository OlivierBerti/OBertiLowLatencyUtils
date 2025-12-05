package com.berti.ringbuffer;


public interface RingBuffer<T> {

    boolean push(T event);

    T poll(T event);

    T pollLast(T event);
}
