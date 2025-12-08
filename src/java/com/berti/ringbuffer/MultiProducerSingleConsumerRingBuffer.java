package com.berti.ringbuffer;

import com.berti.data.DataSetter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


// This class implements a multiple producer/single consumer ring buffer
// In a real dev I certainly would use LMAX Disruptor
public class MultiProducerSingleConsumerRingBuffer<T> implements RingBuffer<T> {

    private static final class IndexedElement<T> {

       private final T event;

       // even in multi producer a volatile variable here is enough
       // because the claimWriteIndex method will prevent two threads from updating it at the same time
       private volatile int index;

       public IndexedElement(Supplier<T> supplier) {
           this.event = supplier.get();
           this.index = -1;
       }
    }

    private final BaseRingBuffer<IndexedElement<T>> ringBuffer;

    private final DataSetter<T> dataSetter;

    // Two publisher may want to update lastWritten at the same time
    // => a volatile int is not good enough
    private final AtomicInteger lastWritten = new AtomicInteger(-1);

    private volatile int lastRead = -1;


    @SuppressWarnings("unchecked")
    public MultiProducerSingleConsumerRingBuffer(int length, Supplier<T> supplier, DataSetter<T> dataSetter) throws RingBufferException {

        Supplier<IndexedElement<T>> elementSupplier = () -> new IndexedElement<>(supplier);
        this.dataSetter = dataSetter;
        IndexedElement<T> element = new IndexedElement<>(supplier);
        this.ringBuffer = new BaseRingBuffer<>(length, (Class<IndexedElement<T>>) element.getClass(), elementSupplier);
    }

    public boolean push(T event) {
        int indexToWrite = this.claimWriteIndex();

        if (indexToWrite == -1) {
            return false;
        }

        IndexedElement<T> indexedElement = ringBuffer.get(indexToWrite);
        dataSetter.copyData(event, indexedElement.event);

        // The index must be updated last ecause the consumer will use it to know the event is ready
        indexedElement.index = indexToWrite;
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
