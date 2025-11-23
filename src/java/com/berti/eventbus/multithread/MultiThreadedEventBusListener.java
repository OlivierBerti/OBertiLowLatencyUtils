package com.berti.eventbus.multithread;

import com.berti.eventbus.DataSetter;
import com.berti.eventbus.EventBusSubscriber;
import com.berti.eventbus.multithread.ringbuffer.RingBufferException;
import lombok.Setter;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class MultiThreadedEventBusListener<T> extends AbstractRunnableModule<T> {

    private static final Logger LOG = Logger.getLogger(MultiThreadedEventBusListener.class.getName());

    private static final long TEMPO_IN_NANOS = 1000;

    private final EventBusSubscriber<T> subscriber;

    @Setter
    private volatile boolean end = false;

    public MultiThreadedEventBusListener(int bufferMaxSize, Class<T> eventClass, EventBusSubscriber<T> eventBusSubscriber,
                                         Supplier<T> supplier, DataSetter<T> dataSetter, Function<T, Boolean> filter) throws RingBufferException {
        super(bufferMaxSize, supplier, dataSetter, TEMPO_IN_NANOS);
        this.subscriber = eventBusSubscriber;
        this.filter = filter;
    }

    @Override
    protected void processEvent(T event) {
        subscriber.onEvent(event);
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
