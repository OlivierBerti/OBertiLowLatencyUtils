package com.berti.eventbus.multithread;

import com.berti.eventbus.EventBusSubscriber;
import com.berti.ringbuffer.RingBufferException;

import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// With this class, each subscriber gets its own ringbuffer
//  By doing this, I make sure a too slow consumer will block the other ones
//  I also avoid the complexities of multi-consumer ringbuffers
class MultiThreadedEventBusListener<T> extends AbstractRunnableRingBufferedModule<T> {

    private static final Logger LOG = LoggerFactory.getLogger(MultiThreadedEventBusListener.class);

    private final EventBusSubscriber<T> subscriber;

    private final Function<T, Boolean> filter;

    public MultiThreadedEventBusListener(
            Class<T> clazz, EventBusSubscriber<T> eventBusSubscriber, Function<T, Boolean> filter,
            int ringBufferSize, long tempoInNanos, Supplier<T> supplier,  boolean conflationMode) throws RingBufferException {
        super(clazz, new RingBufferConfiguration(ringBufferSize, tempoInNanos, false), supplier, conflationMode);
        this.subscriber = eventBusSubscriber;
        this.filter = filter;
    }

    public boolean accept(T event) {
        return filter == null || filter.apply(event);
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
        LOG.error( msg);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
