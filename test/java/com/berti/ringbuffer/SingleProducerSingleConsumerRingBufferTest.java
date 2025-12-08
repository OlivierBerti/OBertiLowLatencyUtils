package com.berti.ringbuffer;

import com.berti.data.SampleEvent;
import com.berti.data.SampleEventDataSetter;

import com.berti.ringbuffer.RingBufferException;
import com.berti.ringbuffer.SingleProducerSingleConsumerRingBuffer;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class SingleProducerSingleConsumerRingBufferTest {

    private SingleProducerSingleConsumerRingBuffer<SampleEvent> ringBuffer;

    private SampleEvent buffer;

    @Before
    public void init() throws RingBufferException {
        buffer = new SampleEvent();
        ringBuffer = new SingleProducerSingleConsumerRingBuffer<>(
                128, SampleEvent::new, new SampleEventDataSetter());
    }

    @Test
    public void testPushPoll() {
        SampleEvent event1 = createSampleEvent(1, 3);
        SampleEvent event2 = createSampleEvent(2, 1);
        SampleEvent event3 = createSampleEvent(3, 4);

        ringBuffer.push(event1);
        ringBuffer.push(event2);

        buffer = ringBuffer.poll(buffer);
        assertEquals(event1, buffer);

        ringBuffer.push(event3);
        buffer = ringBuffer.poll(buffer);
        assertEquals(event2, buffer);
        buffer = ringBuffer.poll(buffer);
        assertEquals(event3, buffer);

        buffer = ringBuffer.poll(buffer);
        assertNull(buffer);
    }

    @Test
    public void testPollLast() {
        SampleEvent event1 = createSampleEvent(1, 3);
        SampleEvent event2 = createSampleEvent(2, 1);
        SampleEvent event3 = createSampleEvent(3, 4);

        ringBuffer.push(event1);
        ringBuffer.push(event2);
        ringBuffer.push(event3);
        buffer = ringBuffer.pollLast(buffer);
        assertEquals(event3, buffer);
    }

    private SampleEvent createSampleEvent(int numEvent, int value) {
        SampleEvent sampleEvent = new SampleEvent();
        sampleEvent.setCreationTime(Instant.now().toEpochMilli());
        sampleEvent.setEventNumber(numEvent);
        sampleEvent.setProducerId("XXX");
        sampleEvent.setValue(value);
        return sampleEvent;
    }
}

