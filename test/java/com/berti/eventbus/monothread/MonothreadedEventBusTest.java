package com.berti.eventbus.monothread;

import com.berti.data.SampleEvent;
import com.berti.data.SampleEventDataSetter;
import com.berti.eventbus.EventBus;
import com.berti.eventbus.EventBusException;
import com.berti.eventbus.EventBusFactory;
import com.berti.eventbus.EventBusSubscriber;
import com.berti.ringbuffer.DataSetterRegistry;
import com.berti.util.TimeUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class MonothreadedEventBusTest {

    private static final class SampleEventBusSubscriber implements EventBusSubscriber<SampleEvent> {

        private final List<SampleEvent> events = new ArrayList<>();

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

    private EventBus<SampleEvent> eventBus;

    private SampleEventBusSubscriber fullSubscriber;
    private SampleEventBusSubscriber filteredSubscriber;


    @Before
    public void setUp() throws Exception {
        DataSetterRegistry.register(SampleEvent.class, new SampleEventDataSetter());
        fullSubscriber = new SampleEventBusSubscriber();
        filteredSubscriber = new SampleEventBusSubscriber();

        Function<SampleEvent, Boolean> filter = x-> x.getValue()%3==0;

        eventBus = EventBusFactory.getInstance().createMonothreadedEventBus(
                SampleEvent.class, SampleEvent::new);

        eventBus.addSubscriber(SampleEvent.class, fullSubscriber);
        eventBus.addSubscriberForFilteredEvents(SampleEvent.class, filteredSubscriber, filter);
    }

    @Test
    public void testEventBus() throws EventBusException {
        SampleEvent event1 = createSampleEvent(1, 3);
        SampleEvent event2 = createSampleEvent(2, 1);
        SampleEvent event3 = createSampleEvent(3, 4);
        SampleEvent event4 = createSampleEvent(4, 21);

        eventBus.publishEvent(event1);
        eventBus.publishEvent(event2);
        eventBus.publishEvent(event3);
        eventBus.publishEvent(event4);

        TimeUtils.sleepMillis(1000);

        assertEquals(4, fullSubscriber.nbEvents());
        assertEquals(2, filteredSubscriber.nbEvents());

        assertEquals(3, fullSubscriber.getEvent(0).getValue());
        assertEquals(1, fullSubscriber.getEvent(1).getValue());
        assertEquals(4, fullSubscriber.getEvent(2).getValue());
        assertEquals(21, fullSubscriber.getEvent(3).getValue());

        assertEquals(3, filteredSubscriber.getEvent(0).getValue());
        assertEquals(21, filteredSubscriber.getEvent(1).getValue());
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
