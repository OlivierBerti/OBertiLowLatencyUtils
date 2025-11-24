package com.berti.eventbus.multithread.sample;

import com.berti.eventbus.multithread.MultiThreadedEventBus;
import com.berti.util.TimeUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    private static final LogManager logManager = LogManager.getLogManager();
    private static final Logger LOGGER = Logger.getLogger( Main.class.getPackage().getName() );

    static{
        try {
            InputStream ins = Main.class.getClassLoader().getResourceAsStream("loggings.properties");
            logManager.readConfiguration(new BufferedInputStream(ins));
        } catch ( IOException exception ) {
            LOGGER.log( Level.SEVERE, "Cannot read configuration file", exception );
        }
    }

    public static void main(String[] args) {
        MultiThreadedEventBus<SampleEvent> eventBus = null;
        SampleEventProducer producer1 = null;
        SampleEventProducer producer2 = null;
        SampleEventProducer producer3 = null;
        try {

            eventBus = new MultiThreadedEventBus<>(
                    500, SampleEvent.class, SampleEvent::new, new SampleEventDataSetter());
            eventBus.start();

            SampleEventBusSubscriber subscriber1 = new SampleEventBusSubscriber("X");
            SampleEventBusSubscriber subscriber2 = new SampleEventBusSubscriber("Y");
            SampleEventBusSubscriber subscriber3 = new SampleEventBusSubscriber("Z");

            eventBus.addSubscriber(SampleEvent.class, subscriber1, SampleEvent::new);
            eventBus.addSubscriberForFilteredEvents(SampleEvent.class, subscriber2, SampleEvent::new, event->event.getValue()%7==0);
            eventBus.addSubscriberForFilteredEvents(SampleEvent.class, subscriber3, SampleEvent::new, event->event.getValue()%3==0);

            producer1 = new SampleEventProducer("A", eventBus, 100, 1001, 20000);
            producer2 = new SampleEventProducer("B", eventBus, 100, 2001,20000);
            producer3 = new SampleEventProducer("C", eventBus, 100, 3001,10000);

            TimeUtils.sleepMillis(1000);

            producer1.start();
            producer2.start();
            producer3.start();

            TimeUtils.sleepMillis(20000);

        } catch(Exception ex) {
            LOGGER.log(Level.SEVERE, "Error during demo " + ex.getMessage(), ex);
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
