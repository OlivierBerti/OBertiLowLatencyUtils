package com.berti.eventbus.monothread;

import com.berti.data.DataSetter;
import com.berti.eventbus.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleEventBus<T> implements EventBus<T> {

    private final DataSetter<T> dataSetter;

    private final Supplier<T> supplier;

    private final List<EventConsumer<T>> consumers = new ArrayList<>();

    public SimpleEventBus(DataSetter<T> dataSetter, Supplier<T> supplier) {
        this.dataSetter = dataSetter;
        this.supplier = supplier;
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
            Class<T> clazz, EventBusSubscriber<T> subscriber) throws EventBusException {
        doAddSubscriber(clazz, subscriber,null);
    }

    @Override
    public void addSubscriberForFilteredEvents(
            Class<T> clazz, EventBusSubscriber<T> subscriber, Function<T, Boolean> filter) throws EventBusException {
        doAddSubscriber(clazz, subscriber, filter);
    }

    private void doAddSubscriber(Class<T> clazz, EventBusSubscriber<T> subscriber,
                                 Function<T, Boolean> filter) throws EventBusException {
        consumers.add(new MonoThreadEventConsumer<>(clazz, subscriber, supplier, dataSetter, filter));

    }
}
