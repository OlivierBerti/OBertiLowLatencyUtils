package com.berti.eventbus.multithread;

import com.berti.eventbus.*;
import com.berti.eventbus.multithread.ringbuffer.RingBufferException;
import com.berti.eventbus.multithread.ringbuffer.SingleConsumerRingBuffer;
import com.berti.util.TimeUtils;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiThreadedEventBus<T> implements EventBus<T>, Runnable {

    private static final Logger log = Logger.getLogger(MultiThreadedEventBus.class.getName());

    private final SingleConsumerRingBuffer<T> internalRingBuffer;

    private final DataSetter<T> dataSetter;

    private volatile Map<EventBusSubscriber<T>, MultiThreadedEventBusListener<T>> listeners = new IdentityHashMap<>();

    private final Executor executor;

    private volatile boolean end = false;

    private final T eventBuffer;

    public MultiThreadedEventBus(int ringBufferLength, Class<T> clazz,
                                 Supplier<T> supplier, DataSetter<T> dataSetter) throws RingBufferException {
        this.eventBuffer = supplier.get();
        this.internalRingBuffer = new SingleConsumerRingBuffer<>(ringBufferLength, clazz, supplier, dataSetter);
        this.dataSetter = dataSetter;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void publishEvent(T event) {

        try {
            this.internalRingBuffer.push(event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addSubscriber(Class<T> clazz, EventBusSubscriber<T> subscriber, Supplier<T> supplier) throws EventBusException {
        addSubscriberForFilteredEvents(clazz, subscriber, supplier, null);
    }

    @Override
    public void addSubscriberForFilteredEvents(
            Class<T> clazz, EventBusSubscriber<T> subscriber, Supplier<T> supplier, Function<T, Boolean> filter) throws EventBusException {

        if (listeners.containsKey(subscriber)) {
            return;
        }

        if (end) {
            throw new EventBusException("Trying to add subscribers after the bus is stopped");
        }

        Map<EventBusSubscriber<T>, MultiThreadedEventBusListener<T>> newListeners = new IdentityHashMap<>();
        newListeners.putAll(this.listeners);

        try {
            MultiThreadedEventBusListener<T> listener =
                    new MultiThreadedEventBusListener<>(50, clazz, subscriber, supplier, dataSetter, filter);
            listener.start();
            newListeners.put(subscriber, listener);
            listeners = newListeners;
        } catch (Exception e) {
            log.log(Level.WARNING, "Error adding listener for subscriber " + subscriber, e);
            throw new EventBusException("Error when trying to add subscriber: " + e.getMessage(), e);
        }
    }


    public void start() {
        executor.execute(this);
    }

    public void stop() {
        end = true;
        for (MultiThreadedEventBusListener<T> listener : listeners.values()) {
            listener.stop();
        }
    }

    @Override
    public void run() {
        while (!end) {
            try {
                if (internalRingBuffer.poll(eventBuffer) != null) {
                    Map<EventBusSubscriber<T>, MultiThreadedEventBusListener<T>> currentListeners = this.listeners;
                    for (MultiThreadedEventBusListener<T> listener : currentListeners.values()) {
                        listener.onEvent(eventBuffer);
                    }
                }
                else {
                    TimeUtils.sleepNanos(1000);
                }
            } catch (RingBufferException e) {
                log.log(Level.SEVERE, "Event bus ring buffer error: " + e.getMessage(), e);
            }
        }
    }

}
