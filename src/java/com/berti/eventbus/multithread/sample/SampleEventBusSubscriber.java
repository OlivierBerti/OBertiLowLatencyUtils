package com.berti.eventbus.multithread.sample;

import com.berti.eventbus.EventBusSubscriber;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SampleEventBusSubscriber implements EventBusSubscriber<SampleEvent> {

    private static final Logger logger = Logger.getLogger(SampleEventBusSubscriber.class.getName());

    private final String name;

    private Object[] toLog= new Object[5];

    public SampleEventBusSubscriber(String name) {
        this.name = name;
        toLog[0]=name;
    }

    @Override
    public void onEvent(SampleEvent event) {
        event.setReceptionTime(Instant.now().toEpochMilli());
        toLog[1]=event.getEventNumber();
        toLog[2]=event.getValue();
        toLog[3]=event.getProducerId();
        toLog[4]=event.getLatency();

        logger.log(Level.INFO, "{0} received event {1} | {2} | {3} | {4} ", toLog);
    }

}
