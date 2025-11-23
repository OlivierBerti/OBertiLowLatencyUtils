package com.berti.eventbus.multithread;

import com.berti.eventbus.DataSetter;
import com.berti.eventbus.EventBusSubscriber;
import com.berti.eventbus.multithread.ringbuffer.RingBufferException;
import com.berti.eventbus.multithread.ringbuffer.SingleConsumerRingBuffer;
import com.berti.util.TimeUtils;
import lombok.Setter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class MultiThreadedEventBusListener<T> implements Runnable {

    private static final Logger LOG = Logger.getLogger(MultiThreadedEventBusListener.class.getName());


    private final SingleConsumerRingBuffer<T> ringBuffer;

    private final EventBusSubscriber<T> subscriber;

    private final Function<T, Boolean> filter;

    private final T buffer;

    private final Executor executor;

    @Setter
    private volatile boolean end = false;

    public MultiThreadedEventBusListener(int bufferMaxSize, Class<T> eventClass, EventBusSubscriber<T> eventBusSubscriber,
                                         Supplier<T> supplier, DataSetter<T> dataSetter, Function<T, Boolean> filter) throws RingBufferException {
       // this.eventClass = eventClass;
        this.subscriber = eventBusSubscriber;
      //  this.supplier = supplier;
        this.filter = filter;
        this.buffer = supplier.get();
        this.ringBuffer = new SingleConsumerRingBuffer<>(bufferMaxSize, eventClass, supplier, dataSetter);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void onEvent(T event) {

        if (!acceptEvent(event)) {
            return;
        }

        try {
            ringBuffer.push(event);
        } catch (RingBufferException e) {
            LOG.log(Level.SEVERE, "Error while sending new Event to subscriber: " + e.getMessage(), e);
        }
    }

    public void start() {
        newSingleThreadExecutor().execute(this);
    }

    public void stop() {
        end = true;
    }


    @Override
    public void run() {
        while (!end) {
            try {
                if (ringBuffer.poll(buffer) != null) {
                    subscriber.onEvent(buffer);
                }
                else {
                    TimeUtils.sleepNanos(1000);
                }
            } catch (RingBufferException e) {
                LOG.log(Level.SEVERE, "Error while getting new Event from event bus: " + e.getMessage(), e);
            }
        }
    }


    private boolean acceptEvent(T event) {
        return filter == null || filter.apply(event);
    }

}
