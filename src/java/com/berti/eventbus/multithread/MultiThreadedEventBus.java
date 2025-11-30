package com.berti.eventbus.multithread;

import com.berti.data.DataSetter;
import com.berti.eventbus.*;
import com.berti.eventbus.multithread.ringbuffer.RingBufferException;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiThreadedEventBus<T> extends AbstractRunnableRingBufferedModule<T> implements AdvancedEventBus<T> {

    private static final Logger LOG = Logger.getLogger(MultiThreadedEventBus.class.getName());

    private static final long TEMPO_IN_NANOS = 1000;

    private final int ringBufferLength;

    private final DataSetter<T> dataSetter;

    private volatile Map<EventBusSubscriber<T>, MultiThreadedEventBusListener<T>> listeners = new IdentityHashMap<>();

    public MultiThreadedEventBus(int ringBufferLength, Supplier<T> supplier, DataSetter<T> dataSetter,
                                 boolean multiProducer) throws RingBufferException {
        super(new RingBufferConfiguration(ringBufferLength, TEMPO_IN_NANOS, multiProducer), supplier, dataSetter, false);
        this.dataSetter = dataSetter;
        this.ringBufferLength = ringBufferLength;
    }

    @Override
    public void addSubscriber(Class<T> clazz, EventBusSubscriber<T> subscriber, Supplier<T> supplier) throws EventBusException {
        doAddSubscriber(clazz, subscriber, supplier, null, false);
    }

    @Override
    public void addSubscriberForFilteredEvents(
            Class<T> clazz, EventBusSubscriber<T> subscriber, Supplier<T> supplier, Function<T, Boolean> filter) throws EventBusException {
        doAddSubscriber(clazz, subscriber, supplier, filter, false);
    }

    @Override
    public void addSubscriberWithConflation(Class<T> clazz, EventBusSubscriber<T> subscriber, Supplier<T> supplier) throws EventBusException {
        doAddSubscriber(clazz, subscriber, supplier, null, true);
    }

    @Override
    public void addSubscriberForFilteredEventsWithConflation(Class<T> clazz, EventBusSubscriber<T> subscriber, Supplier<T> supplier, Function<T, Boolean> filter) throws EventBusException {
        doAddSubscriber(clazz, subscriber, supplier, filter, true);
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
            MultiThreadedEventBusListener<T> listener = new MultiThreadedEventBusListener<>(
                    this.ringBufferLength, subscriber, supplier, dataSetter, filter, conflationMode);
            listener.start();
            newListeners.put(subscriber, listener);
            listeners = newListeners;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error adding listener for subscriber " + subscriber, e);
            throw new EventBusException("Error when trying to add subscriber: " + e.getMessage(), e);
        }
    }

    @Override
    protected void processEvent(T eventBuffer) throws Exception {
        Map<EventBusSubscriber<T>, MultiThreadedEventBusListener<T>> currentListeners = this.listeners;
        for (MultiThreadedEventBusListener<T> listener : currentListeners.values()) {
            listener.pushEvent(eventBuffer);
        }
    }

    @Override
    protected void onRingBufferFull(T event) throws EventBusException {
        String msg = "Error while sending event to subscriber : RingBuffer full";
        LOG.log(Level.SEVERE, msg);
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
