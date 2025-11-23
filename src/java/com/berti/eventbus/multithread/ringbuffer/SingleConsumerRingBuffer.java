package com.berti.eventbus.multithread.ringbuffer;

import com.berti.eventbus.DataSetter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


// This class implements a multiple producer/single consumer ring buffer
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

    private final AtomicInteger lastRead = new AtomicInteger(-1);


    @SuppressWarnings("unchecked")
    public SingleConsumerRingBuffer(int length, Class<T> clazz, Supplier<T> supplier, DataSetter<T> dataSetter) throws RingBufferException {

        Supplier<IndexedElement<T>> elementSupplier = () -> new IndexedElement<>(supplier);
        this.dataSetter = dataSetter;
        IndexedElement<T> element = new IndexedElement<>(supplier);
        this.ringBuffer = new RingBuffer<>(length, (Class<IndexedElement<T>>) element.getClass(), elementSupplier);

    }

    public void push(T event) throws RingBufferException {
        int indexToWrite = lastWritten.getAndIncrement();
        int lastActiveIndex = lastRead.get();

        // Check we are not writing into a not yet read element
        if (indexToWrite - lastActiveIndex >= ringBuffer.getLength()) {
            throw new RingBufferException("RingBuffer congested!! event = "+ event +", index to write=" + indexToWrite + ", lastActiveIndex=" + lastActiveIndex) ;
        }

        IndexedElement<T> indexedElement = ringBuffer.get(indexToWrite);
        dataSetter.copyData(event, indexedElement.event);
        indexedElement.index = indexToWrite;
    }


    public T poll(T event) throws RingBufferException {
        if (lastRead.get() >= lastWritten.get()) {
            // no pending event
            return null;
        }

        int indexToRead = lastRead.get() + 1;
        IndexedElement<T> indexedElement = ringBuffer.get(indexToRead);

        // this test ensures we are not trying to read the element before its producer finished to write it
        if (indexedElement.index != indexToRead) {
            return null;
        }
        dataSetter.copyData(indexedElement.event, event);
        lastRead.incrementAndGet();
        return event;
    }
}
