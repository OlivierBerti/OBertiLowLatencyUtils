package com.berti.eventbus.multithread.ringbuffer;

import com.berti.data.DataSetter;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

// This class implements a multiple producer/single consumer ring buffer
// In a real dev I certainly would use LMAX Disruptor
public class SingleConsumerThrottledRingBuffer<T> implements RingBuffer<T> {

    private static final class IndexedTimestampedElement<T> {

       private final T event;

       // a volatile variable here is enough since
       private volatile int index;

       private volatile long timestamp;

       public IndexedTimestampedElement(Supplier<T> supplier) {
           this.event = supplier.get();
           this.index = -1;
           this.timestamp = 0;
       }
    }

    private final BaseRingBuffer<IndexedTimestampedElement<T>> ringBuffer;

    private final DataSetter<T> dataSetter;

    private final AtomicInteger lastWritten = new AtomicInteger(0);

    //Unlike lastWritten, lastRead is  updated by a single thread
    // => a volatile int is good enough
    private volatile int lastRead = -1;

    private long timeWindow;

    private int nbMaxEventsInTimeWindow;

    private final IndexedTimestampedElement<T> pendingElement;

    @SuppressWarnings("unchecked")
    public SingleConsumerThrottledRingBuffer(
            int length, Supplier<T> supplier, DataSetter<T> dataSetter,
            long timeWindow, int nbMaxEventsInTimeWindow) throws RingBufferException {
        this.timeWindow = timeWindow;
        this.nbMaxEventsInTimeWindow = nbMaxEventsInTimeWindow;

        Supplier<IndexedTimestampedElement<T>> elementSupplier = () -> new IndexedTimestampedElement<>(supplier);
        this.dataSetter = dataSetter;
        IndexedTimestampedElement<T> element = new IndexedTimestampedElement<>(supplier);
        this.ringBuffer = new BaseRingBuffer<>(length, (Class<IndexedTimestampedElement<T>>) element.getClass(), elementSupplier);

        this.pendingElement = new IndexedTimestampedElement<>(supplier);
    }

    public boolean push(T event) {
        int indexToWrite = this.claimWriteIndex();


        if (indexToWrite == -1) {
            dataSetter.copyData(event, pendingElement.event);
            pendingElement.timestamp = Instant.now().toEpochMilli();
            return false;
        }

        IndexedTimestampedElement<T> indexedTimestampedElement = ringBuffer.get(indexToWrite);
        dataSetter.copyData(event, indexedTimestampedElement.event);
        indexedTimestampedElement.timestamp = Instant.now().toEpochMilli();
        indexedTimestampedElement.index = indexToWrite;
        return true;
    }

    private int claimWriteIndex() {
        int lastReadIndex = lastRead;
        int lastWritttenIndex = lastWritten.get();

        // Check we are not writing into a not yet read element IOW if the RingBuffer is not congested
        if (lastWritttenIndex - lastReadIndex >= ringBuffer.getLength()-1) {
            return -1;
        }

        // Increment lastWritten only if it has not changed while we were checking the congestion
        // otherwise restart the claimWriteIndex process
        if (!lastWritten.compareAndSet(lastWritttenIndex, lastWritttenIndex + 1)) {
            return claimWriteIndex();
        }
        return lastWritttenIndex + 1;
    }

    public T poll(T event) {
        if (lastRead >= lastWritten.get()) {
            // no pending event
            return null;
        }
        return getEvent(event, lastRead + 1);
    }

    public T pollLast(T event) {
        int lastWrittenindex = lastWritten.get();
        if (lastRead >= lastWrittenindex) {
            // no pending event
            return null;
        }
        return getEvent(event, lastWrittenindex);
    }

    private T getEvent(T event, int indexToRead) {
        IndexedTimestampedElement<T> indexedTimestampedElement = ringBuffer.get(indexToRead);
        T ringBufferEvent = readWhenAvailable(indexedTimestampedElement, indexToRead);

        if (ringBufferEvent != null) {
            dataSetter.copyData(ringBufferEvent, event);
            lastRead = indexToRead;
            return event;
        }
        else {
            return null;
        }
    }

    private T readWhenAvailable(IndexedTimestampedElement<T> indexedTimestampedElement, int indexToRead) {
        // this test ensures we are not trying to read the element before its producer finished to write it
        while (indexedTimestampedElement.index != indexToRead) {
            Thread.yield();
        }
        return indexedTimestampedElement.event;
    }
}
