package com.berti.eventbus.multithread.ringbuffer;

import com.berti.eventbus.DataSetter;
import com.berti.util.TimeUtils;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


// This class implements a multiple producer/single consumer ring buffer
// In a real dev I certainly would use LMAX Disruptor
public class SingleConsumerRingBuffer<T> {

    private static final class IndexedElement<T> {

       private final T event;

       // a volatile variable here is enough since
       private volatile int index;

       public IndexedElement(Supplier<T> supplier) {
           this.event = supplier.get();
           this.index = -1;
       }
    }

    private final RingBuffer<IndexedElement<T>> ringBuffer;

    private final DataSetter<T> dataSetter;

    private final AtomicInteger lastWritten = new AtomicInteger(0);

    //Unlike lastWritten, lastRead is  updated by a single thread
    // => a volatile int is good enough
    private volatile int lastRead = -1;


    @SuppressWarnings("unchecked")
    public SingleConsumerRingBuffer(int length, Supplier<T> supplier, DataSetter<T> dataSetter) throws RingBufferException {

        Supplier<IndexedElement<T>> elementSupplier = () -> new IndexedElement<>(supplier);
        this.dataSetter = dataSetter;
        IndexedElement<T> element = new IndexedElement<>(supplier);
        this.ringBuffer = new RingBuffer<>(length, (Class<IndexedElement<T>>) element.getClass(), elementSupplier);

    }

    public boolean push(T event) {
        int indexToWrite = lastWritten.getAndIncrement();
        int lastActiveIndex = lastRead;

        // Check we are not writing into a not yet read element IOW if the RingBuffer is not congested
        if (indexToWrite - lastActiveIndex >= ringBuffer.getLength()) {
            return false;
        }

        IndexedElement<T> indexedElement = ringBuffer.get(indexToWrite);
        dataSetter.copyData(event, indexedElement.event);
        indexedElement.index = indexToWrite;
        return true;
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
        IndexedElement<T> indexedElement = ringBuffer.get(indexToRead);
        T ringBufferEvent = readWhenAvailable(indexedElement, indexToRead);

        if (ringBufferEvent != null) {
            dataSetter.copyData(ringBufferEvent, event);
            lastRead = indexToRead;
            return event;
        }
        else {
            return null;
        }
    }

    private T readWhenAvailable(IndexedElement<T> indexedElement, int indexToRead) {
        // this test ensures we are not trying to read the element before its producer finished to write it
        while (indexedElement.index != indexToRead) {
            Thread.yield();
        }
        return indexedElement.event;
    }
}
