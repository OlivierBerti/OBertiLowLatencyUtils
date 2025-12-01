package com.berti.eventbus.multithread.sample;

import com.berti.data.SampleEvent;
import com.berti.eventbus.EventBusSubscriber;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleEventBusSubscriber implements EventBusSubscriber<SampleEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SampleEventBusSubscriber.class);

    private static final String FORMATTED_MSG = "{0} received event {3}{1} value = {2} duration = {4} ms";

    private final String name;

    public SampleEventBusSubscriber(String name) {
        this.name = name;
    }

    @Override
    public void onEvent(SampleEvent event) {
        event.setReceptionTime(Instant.now().toEpochMilli());

        logger.info( "{} received event {}{} value = {} duration = {} ms",
            name, event.getProducerId(), event.getEventNumber(), event.getValue(), event.getLatency());
    }
}
