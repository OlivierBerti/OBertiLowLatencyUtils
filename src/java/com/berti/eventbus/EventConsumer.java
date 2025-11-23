package com.berti.eventbus;


//TODO get rid of this
public interface EventConsumer<T> {

    // returns true if the event is not filtered by the consumer
    boolean acceptEvent(T event);

    // consumes the event
    void consumeEvent(T event);
}
