package com.berti.eventbus.monothread;

import com.berti.data.DataSetter;
import com.berti.eventbus.*;
import lombok.Getter;

import java.util.function.Function;
import java.util.function.Supplier;

// On monothread architecture, the event bus is just an Observer pattern with two extra features:
// - filtering the events
// - making copies of events for each subscriber (unnecessary if the event is immutable)
public class MonoThreadEventConsumer<T> implements EventConsumer<T> {

    private final EventBusSubscriber<T> subscriber;

    private final Function<T, Boolean> filter;

    private final Supplier<T> supplier;

    private final DataSetter<T> dataSetter;

    public MonoThreadEventConsumer(Class<T> clazz, EventBusSubscriber<T> subscriber,
                                   Supplier<T> supplier, DataSetter<T> dataSetter, Function<T, Boolean> filter) throws EventBusException {
        this.subscriber = subscriber;
        this.filter = filter;
        this.supplier = supplier;
        this.dataSetter = dataSetter;
        if (this.dataSetter == null) {
            throw new EventBusException("No DataSetter registered for " + clazz);
        }
    }

    @Override
    public boolean acceptEvent(T event) {
        return filter == null || filter.apply(event);
    }

    @Override
    public void consumeEvent(T event) {
        T outputEvent = supplier.get();
        dataSetter.copyData(event, outputEvent);
        subscriber.onEvent(outputEvent);
    }
}
