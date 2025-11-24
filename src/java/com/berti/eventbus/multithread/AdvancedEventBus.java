package com.berti.eventbus.multithread;

import com.berti.eventbus.EventBus;
import com.berti.eventbus.EventBusException;
import com.berti.eventbus.EventBusSubscriber;

import java.util.function.Function;
import java.util.function.Supplier;

public interface AdvancedEventBus<T> extends EventBus<T> {

    void addSubscriberWithConflation(
            Class<T> clazz, EventBusSubscriber<T> subscriber, Supplier<T> supplier) throws EventBusException;

    void addSubscriberForFilteredEventsWithConflation(
            Class<T> clazz, EventBusSubscriber<T> subscriber, Supplier<T> supplier, Function<T,Boolean> filter) throws EventBusException;

}
