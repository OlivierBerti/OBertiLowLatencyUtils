package com.berti.eventbus.multithread.sample;

import com.berti.data.SampleEvent;
import com.berti.data.SampleEventDataSetter;
import com.berti.eventbus.multithread.MultiThreadedEventBus;
import com.berti.eventbus.multithread.ringbuffer.SingleProducerSingleConsumerRingBuffer;
import com.berti.util.TimeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleProducerSingleConsumerRingBuffer.class);

    public static void main(String[] args) {
        MultiThreadedEventBus<SampleEvent> eventBus = null;
        SampleEventProducer producer1 = null;
        SampleEventProducer producer2 = null;
        SampleEventProducer producer3 = null;
        try {

            eventBus = new MultiThreadedEventBus<>(
                    500,  SampleEvent::new, new SampleEventDataSetter(), true);
            eventBus.start();

            SampleEventBusSubscriber subscriber1 = new SampleEventBusSubscriber("X");
            SampleEventBusSubscriber subscriber2 = new SampleEventBusSubscriber("Y");
            SampleEventBusSubscriber subscriber3 = new SampleEventBusSubscriber("Z");

            eventBus.addSubscriber(SampleEvent.class, subscriber1, SampleEvent::new);
            eventBus.addSubscriberForFilteredEvents(SampleEvent.class, subscriber2, SampleEvent::new, event->event.getValue()%7==0);
            eventBus.addSubscriberForFilteredEvents(SampleEvent.class, subscriber3, SampleEvent::new, event->event.getValue()%3==0);

            producer1 = new SampleEventProducer("A", eventBus, 200, 1001, 20000);
            producer2 = new SampleEventProducer("B", eventBus, 200, 2001,20000);
            producer3 = new SampleEventProducer("C", eventBus, 200, 3001,10000);

            TimeUtils.sleepMillis(1000);

            producer1.start();
            producer2.start();
            producer3.start();

            TimeUtils.sleepMillis(20000);

        } catch(Exception ex) {
            LOGGER.error("Error during demo " + ex.getMessage(), ex);
        } finally {
            if (producer1 != null) {
                producer1.stop();
            }
            if (producer2 != null) {
                producer2.stop();
            }
            if (producer3 != null) {
                producer3.stop();
            }
            if ( eventBus != null ) {
                eventBus.stop();
            }
        }
        System.exit(0);
    }
}
