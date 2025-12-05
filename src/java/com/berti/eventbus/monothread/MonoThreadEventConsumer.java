package com.berti.eventbus.monothread;

import com.berti.data.DataSetter;
import com.berti.eventbus.*;
import lombok.Getter;

import java.util.function.Function;
import java.util.function.Supplier;

@Getter
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

    //TODO; supplier?
    @Override
    public void consumeEvent(T event) {
        T eventCopy = supplier.get();
        dataSetter.copyData(event, eventCopy);
        subscriber.onEvent(eventCopy);
    }
}
