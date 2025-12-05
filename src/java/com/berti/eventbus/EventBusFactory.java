package com.berti.eventbus;

import com.berti.data.DataSetter;
import com.berti.eventbus.monothread.SimpleEventBus;
import com.berti.eventbus.multithread.MultiThreadedEventBus;
import com.berti.eventbus.multithread.RingBufferConfiguration;
import com.berti.ringbuffer.DataSetterRegistry;

import java.util.function.Supplier;

public final class EventBusFactory {

    private static final EventBusFactory instance = new EventBusFactory() ;

    public static EventBusFactory getInstance() {
        return instance;
    }

    public <T> EventBus<T> createMonothreadedEventBus(Class<T> eventClass, Supplier<T> supplier) {
        DataSetter<T> dataSetter = DataSetterRegistry.getDataSetter(eventClass);
        return new SimpleEventBus<>(dataSetter, supplier);
    }

    public   <T> EventBus<T> createMultithreadedEventBus(Class<T> eventClass, Supplier<T> supplier, RingBufferConfiguration config) throws EventBusException {
        return createRingBufferedBus(eventClass, supplier, config, false);
    }

    public <T> EventBus<T> createConflatingMultithreadedEventBus(Class<T> eventClass, Supplier<T> supplier, RingBufferConfiguration config) throws EventBusException {
        return createRingBufferedBus(eventClass, supplier, config, true);
    }

    private <T> EventBus<T> createRingBufferedBus(Class<T> clazz, Supplier<T> supplier, RingBufferConfiguration config, boolean conflating) throws EventBusException {
        try {
            // TODO config == null?
            DataSetter<T> dataSetter = DataSetterRegistry.getDataSetter(clazz);
            MultiThreadedEventBus<T> eventBus = new MultiThreadedEventBus<>(supplier, dataSetter, config, conflating);
            eventBus.start();
            return eventBus;
        } catch (Exception e) {
            throw new EventBusException("Impossible to instantiate event bus: " + e.getMessage(), e);
        }
    }

    private EventBusFactory() {}

}
