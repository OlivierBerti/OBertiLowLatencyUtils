package com.berti.eventbus.monothread;

import com.berti.eventbus.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleEventBus<T> implements EventBus<T> {

    private final DataSetter<T> dataSetter;

    private final List<EventConsumer<T>> consumers = new ArrayList<>();

    public SimpleEventBus(DataSetter<T> dataSetter) {
        this.dataSetter = dataSetter;
    }

    @Override
    public void publishEvent(T event) {
        for (EventConsumer<T> publisher : consumers) {
            if (publisher.acceptEvent(event)) {
                publisher.consumeEvent(event);
            }
        }
    }

    @Override
    public void addSubscriber(
            Class<T> clazz, EventBusSubscriber<T> subscriber, Supplier<T> supplier) throws EventBusException {
        consumers.add(new MonoThreadEventConsumer<>(clazz, subscriber, supplier, dataSetter,null));
    }

    @Override
    public void addSubscriberForFilteredEvents(
            Class<T> clazz, EventBusSubscriber<T> subscriber,
            Supplier<T> supplier, Function<T, Boolean> filter) throws EventBusException {
        consumers.add(new MonoThreadEventConsumer<>(clazz, subscriber, supplier, dataSetter, filter));
    }
}
