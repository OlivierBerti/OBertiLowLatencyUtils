package com.berti.eventbus.multithread;

import com.berti.data.DataSetter;
import com.berti.eventbus.EventBusSubscriber;
import com.berti.eventbus.multithread.ringbuffer.RingBufferException;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiThreadedEventBusListener<T> extends AbstractRunnableRingBufferedModule<T> {

    private static final Logger LOG = Logger.getLogger(MultiThreadedEventBusListener.class.getName());

    private static final long TEMPO_IN_NANOS = 1000;

    private final EventBusSubscriber<T> subscriber;

    public MultiThreadedEventBusListener(
            int bufferMaxSize, EventBusSubscriber<T> eventBusSubscriber,
            Supplier<T> supplier, DataSetter<T> dataSetter, Function<T, Boolean> filter, boolean conflationMode) throws RingBufferException {
        super(new RingBufferConfiguration(bufferMaxSize, TEMPO_IN_NANOS, false),
                supplier, dataSetter, conflationMode);
        this.subscriber = eventBusSubscriber;
        this.filter = filter;
    }

    @Override
    protected void processEvent(T eventBuffer) {
        T newEvent = supplier.get();
        dataSetter.copyData(eventBuffer, newEvent);
        subscriber.onEvent(newEvent);
    }

    @Override
    protected void onRingBufferFull(T event) {
        String msg = "Error while sending event to subscriber : RingBuffer full";
        LOG.log(Level.SEVERE, msg);;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
