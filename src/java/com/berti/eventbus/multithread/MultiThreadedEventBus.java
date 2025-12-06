package com.berti.eventbus.multithread;

import com.berti.data.DataSetter;
import com.berti.eventbus.*;
import com.berti.ringbuffer.RingBufferException;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiThreadedEventBus<T> extends AbstractRunnableRingBufferedModule<T> implements EventBus<T> {

    private static final Logger LOG = LoggerFactory.getLogger(MultiThreadedEventBus.class);

    private static final long TEMPO_IN_NANOS = 1000;

    private final int ringBufferLength;


    private volatile Map<EventBusSubscriber<T>, MultiThreadedEventBusListener<T>> listeners = new IdentityHashMap<>();

    public MultiThreadedEventBus(Class<T> clazz, Supplier<T> supplier,
                                 RingBufferConfiguration ringBufferConfiguration, boolean conflation) throws RingBufferException {
        super(clazz, ringBufferConfiguration, supplier, conflation);
        this.ringBufferLength = ringBufferConfiguration.getRingBufferSize();
    }

    @Override
    public void addSubscriber(Class<T> clazz, EventBusSubscriber<T> subscriber) throws EventBusException {
        doAddSubscriber(clazz, subscriber, supplier, null, conflationMode);
    }

    @Override
    public void addSubscriberForFilteredEvents(
            Class<T> clazz, EventBusSubscriber<T> subscriber, Function<T, Boolean> filter) throws EventBusException {
        doAddSubscriber(clazz, subscriber, supplier, filter, conflationMode);
    }

    private void doAddSubscriber(
            Class<T> clazz, EventBusSubscriber<T> subscriber, Supplier<T> supplier, Function<T, Boolean> filter, boolean conflationMode) throws EventBusException {
        if (listeners.containsKey(subscriber)) {
            return;
        }

        if (isStopped()) {
            throw new EventBusException("Trying to add subscribers after the bus is stopped");
        }

        Map<EventBusSubscriber<T>, MultiThreadedEventBusListener<T>> newListeners = new IdentityHashMap<>();
        newListeners.putAll(this.listeners);

        try {
            MultiThreadedEventBusListener<T> listener = new MultiThreadedEventBusListener<> (
                    clazz, subscriber, filter, ringBufferLength, tempo.toNanos(), supplier, conflationMode);
            listener.start();
            newListeners.put(subscriber, listener);
            listeners = newListeners;
        } catch (Exception e) {
            LOG.error("Error adding listener for subscriber " + subscriber, e);
            throw new EventBusException("Error when trying to add subscriber: " + e.getMessage(), e);
        }
    }

    @Override
    protected void processEvent(T eventBuffer) {
        Map<EventBusSubscriber<T>, MultiThreadedEventBusListener<T>> currentListeners = this.listeners;
        for (MultiThreadedEventBusListener<T> listener : currentListeners.values()) {
            listener.publishEvent(eventBuffer);
        }
    }

    @Override
    protected void onRingBufferFull(T event) throws EventBusException {
        String msg = "Error while sending event to subscriber : RingBuffer full";
        LOG.error(msg);
        throw new EventBusException(msg);
    }

    @Override
    protected void onStop() {
        for (MultiThreadedEventBusListener<T> listener : listeners.values()) {
            listener.stop();
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
