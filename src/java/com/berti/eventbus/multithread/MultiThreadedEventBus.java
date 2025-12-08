package com.berti.eventbus.multithread;

import com.berti.eventbus.*;
import com.berti.ringbuffer.RingBufferException;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiThreadedEventBus<T> extends AbstractRunnableRingBufferedModule<T> implements EventBus<T> {

    private static final Logger LOG = LoggerFactory.getLogger(MultiThreadedEventBus.class);

    private final int ringBufferLength;

    private final boolean conflatingEventBus;


    private volatile Map<EventBusSubscriber<T>, MultiThreadedEventBusListener<T>> listeners = new IdentityHashMap<>();

    public MultiThreadedEventBus(Class<T> clazz, Supplier<T> supplier,
                                 RingBufferConfiguration ringBufferConfiguration, boolean conflation) throws RingBufferException {
        // The conflation should be set only in listeners ringbuffers
        // to prevent suscribers with filters from missing "their" events
        super(clazz, ringBufferConfiguration, supplier, false);
        this.ringBufferLength = ringBufferConfiguration.getRingBufferSize();
        this.conflatingEventBus = conflation;
    }

    @Override
    public void publishEvent(T event) throws EventBusException {
        try {
            this.pushEvent(event);
        } catch (EventBusException e) {
            getLogger().error("Error while pushing into internal ringBuffer " + e.getMessage(), e);
            throw e;
        }  catch (Exception e) {
            String message = "Error while pushing into internal ringBuffer " + e.getMessage();
            getLogger().error(message, e);
            throw new EventBusException(message, e);
        }
    }

    @Override
    public void addSubscriber(Class<T> clazz, EventBusSubscriber<T> subscriber) throws EventBusException {
        doAddSubscriber(clazz, subscriber, supplier, null, conflatingEventBus);
    }

    @Override
    public void addSubscriberForFilteredEvents(
            Class<T> clazz, EventBusSubscriber<T> subscriber, Function<T, Boolean> filter) throws EventBusException {
        doAddSubscriber(clazz, subscriber, supplier, filter, conflatingEventBus);
    }

    private void doAddSubscriber(
            Class<T> clazz, EventBusSubscriber<T> subscriber, Supplier<T> supplier, Function<T, Boolean> filter, boolean conflationMode) throws EventBusException {
        if (listeners.containsKey(subscriber)) {
            return;
        }

        if (isStopped()) {
            throw new EventBusException("Trying to add subscribers after the bus is stopped");
        }

        Map<EventBusSubscriber<T>, MultiThreadedEventBusListener<T>> newListeners =
                new IdentityHashMap<>(this.listeners);

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
            try {
                if (listener.accept(eventBuffer)) {
                    listener.pushEvent(eventBuffer);
                }
             } catch (Exception e) {
                getLogger().error("Error while pushing event to listener " + e.getMessage(), e);
            }
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
