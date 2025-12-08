package com.berti.eventbus;

import java.util.function.Function;

// We need to know how to instantiate or copy an event
// So a template interface is better suited than using Object as event class
public interface EventBus<T> {

    void publishEvent(T event) throws EventBusException;

    void addSubscriber(
            Class<T> clazz, EventBusSubscriber<T> subscriber) throws EventBusException;

    void addSubscriberForFilteredEvents(
            Class<T> clazz, EventBusSubscriber<T> subscriber, Function<T,Boolean> filter) throws EventBusException;
}
