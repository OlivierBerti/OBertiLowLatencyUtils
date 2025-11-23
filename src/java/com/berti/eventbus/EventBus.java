package com.berti.eventbus;

import java.util.function.Function;
import java.util.function.Supplier;

// We need to know how to instantiate or copy an event
// So a template interface is better suited than using Object as event class
public interface EventBus<T> {

    void publishEvent(T event) throws EventBusException;

    void addSubscriber(
            Class<T> clazz, EventBusSubscriber<T> subscriber, Supplier<T> supplier) throws EventBusException;

    void addSubscriberForFilteredEvents(
            Class<T> clazz, EventBusSubscriber<T> subscriber, Supplier<T> supplier, Function<T,Boolean> filter) throws EventBusException;
}
