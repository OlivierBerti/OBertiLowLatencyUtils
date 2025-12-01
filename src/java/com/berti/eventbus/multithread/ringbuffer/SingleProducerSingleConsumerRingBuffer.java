package com.berti.eventbus.multithread.ringbuffer;

import com.berti.data.DataSetter;

import java.util.function.Supplier;


// This class implements a single producer/single consumer ring buffer
// In a real dev I certainly would use LMAX Disruptor
public class SingleProducerSingleConsumerRingBuffer<T> implements RingBuffer<T> {

    private static final class IndexedElement<T> {

       private final T event;

       // a volatile variable here is enough since
       private volatile int index;

       public IndexedElement(Supplier<T> supplier) {
           this.event = supplier.get();
           this.index = -1;
       }
    }

    protected final BaseRingBuffer<IndexedElement<T>> ringBuffer;

    protected final Supplier<T> supplier;

    protected final DataSetter<T> dataSetter;

    private volatile int lastWritten = -1;

    private volatile int lastRead = -1;


    @SuppressWarnings("unchecked")
    public SingleProducerSingleConsumerRingBuffer(int length, Supplier<T> supplier, DataSetter<T> dataSetter) throws RingBufferException {

        Supplier<IndexedElement<T>> elementSupplier = () -> new IndexedElement<>(supplier);
        this.supplier = supplier;
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
        indexedElement.index = indexToWrite;
        return true;
    }

    private int claimWriteIndex() {
        int lastReadIndex = lastRead;

        // Check we are not writing into a not yet read element IOW if the RingBuffer is not congested
        if (lastWritten - lastReadIndex >= ringBuffer.getLength()-1) {
            return -1;
        }

        lastWritten ++;
        return lastWritten;
    }


    public T poll(T event) {
        if (isEmpty()) {
            // no pending event
            return null;
        }
        return getEvent(event, lastRead + 1);
    }

    public T pollLast(T event) {
        int lastWrittenindex = lastWritten;
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
        T result = indexedElement.event;
        return isElementReadable(result) ? result :null;
    }

    // By redefining this method, we can tmporarily block the reading (see WinowedQueue)
    // Just be sure the condition can't remain false forever
    protected boolean isElementReadable(T result) {
        return true;
    }

    public boolean isEmpty() {
        return lastRead >= lastWritten;
    }

    public int nbActiveElements() {
        return lastWritten - lastRead;
    }
}
