package com.berti.eventbus.multithread;

import com.berti.eventbus.DataSetter;
import com.berti.eventbus.multithread.ringbuffer.RingBufferException;
import com.berti.eventbus.multithread.ringbuffer.SingleConsumerRingBuffer;
import com.berti.util.TimeUtils;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// An abstract module runs in his own thread
// It has its own RingBuffer to receives its inputs
// => The RingBuffer is single-consummer

// Note: We assume all its inputs are on the same type and on the same purpose
// but it is possible to create a eventClass T with multiple internal buffers of different types
// plus a variable to tell the module which is the real type and how it must be processed
public abstract class AbstractRunnableModule<T> {

    private final SingleConsumerRingBuffer<T> internalRingBuffer;

    private final Executor executor;

    private volatile boolean end = false;

    private final T eventBuffer;

    private final Duration tempo;

    protected Function<T, Boolean> filter = null;

    protected AbstractRunnableModule(
            int ringBufferLength, Supplier<T> supplier, DataSetter<T> dataSetter,
            long tempoInNanos) throws RingBufferException {
        this.eventBuffer = supplier.get();
        this.internalRingBuffer = new SingleConsumerRingBuffer<>(ringBufferLength, supplier, dataSetter);
        this.executor = Executors.newSingleThreadExecutor();
        this.tempo =  Duration.ofNanos(tempoInNanos);
    }


    public void pushEvent(T event) throws Exception {
        if (event == null) {
            // should never happen
            return;
        }
        if (filter != null && !filter.apply(event)) {
            // event is filtered
            return;
        }
        boolean ringFull = !internalRingBuffer.push(event);
        if (ringFull)  {
            onRingBufferFull(event);
        }
    }

    public void publishEvent(T event) {
        try {
            this.pushEvent(event);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error while pushing into internal ringBuffer " + e.getMessage(), e);
        }
    }

    protected final boolean isStopped() {
        return end;
    }


    public void start() {
        executor.execute(this::run);
    }

    public void stop() {
        end = true;
        onStop();
    }

    protected void onStop() {
        // to be overridden in subclasses
    }

    public void run() {
        while (!end) {
            try {
                if (internalRingBuffer.poll(eventBuffer) != null) {
                    processEvent(eventBuffer);
                }
                else {
                    TimeUtils.sleep(tempo);
                }
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Event bus ring buffer error: " + e.getMessage(), e);
            }
        }
    }


    protected abstract void onRingBufferFull(T event) throws Exception;

    protected abstract void processEvent(T event) throws Exception;

    protected abstract Logger getLogger();
}
