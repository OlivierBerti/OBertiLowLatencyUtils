package com.berti.eventbus.monothread;


public interface EventConsumer<T> {

    // returns true if the event is not filtered by the consumer
    boolean acceptEvent(T event);

    // consumes the event
    void consumeEvent(T event);
}
