package com.berti.eventbus.multithread;

import com.berti.data.SampleEvent;
import com.berti.data.SampleEventDataSetter;
import com.berti.eventbus.EventBusSubscriber;
import com.berti.util.TimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class MultithreadedEventBusTest {

    private static final long TEMPO_IN_NANOS = 1000;

    private static final int RING_BUFFER_LENGTH = 1024;


    private static final class SampleEventBusSubscriber implements EventBusSubscriber<SampleEvent> {

        private final List<SampleEvent> events = new ArrayList<SampleEvent>();

        @Override
        public void onEvent(SampleEvent event) {
            events.add(event);
        }

        public SampleEvent getEvent(int index) {
            return events.get(index);
        }

        public int nbEvents() {
            return events.size();
        }
    }

    private MultiThreadedEventBus<SampleEvent> eventBus;

    private SampleEventBusSubscriber subscriber1;
    private SampleEventBusSubscriber subscriber2;

    private Function<SampleEvent, Boolean> filter;


    @Before
    public void setUp() throws Exception {
        subscriber1 = new SampleEventBusSubscriber();
        subscriber2 = new SampleEventBusSubscriber();

        filter = x-> x.getValue()%3==0;

        eventBus = new MultiThreadedEventBus<>(
                RING_BUFFER_LENGTH, SampleEvent::new, new SampleEventDataSetter(), true);

        eventBus.addSubscriber(SampleEvent.class, subscriber1, SampleEvent::new);
        eventBus.addSubscriberForFilteredEvents(SampleEvent.class, subscriber2, SampleEvent::new, filter);
        eventBus.start();
    }

    @After
    public void tearDown() {
        eventBus.stop();
    }

    @Test
    public void testEventBus() {
        SampleEvent event1 = createSampleEvent(1, 3);
        SampleEvent event2 = createSampleEvent(2, 1);
        SampleEvent event3 = createSampleEvent(3, 4);
        SampleEvent event4 = createSampleEvent(4, 21);

        eventBus.publishEvent(event1);
        eventBus.publishEvent(event2);
        eventBus.publishEvent(event3);
        eventBus.publishEvent(event4);

        TimeUtils.sleepMillis(1000);

        assertEquals(4, subscriber1.nbEvents());
        assertEquals(2, subscriber2.nbEvents());

        assertEquals(3, subscriber1.getEvent(0).getValue());
        assertEquals(1, subscriber1.getEvent(1).getValue());
        assertEquals(4, subscriber1.getEvent(2).getValue());
        assertEquals(21, subscriber1.getEvent(3).getValue());

        assertEquals(3, subscriber2.getEvent(0).getValue());
        assertEquals(21, subscriber2.getEvent(1).getValue());
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
