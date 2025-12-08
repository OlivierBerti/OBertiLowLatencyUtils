package com.berti.eventbus.multithread.sample;

import com.berti.data.SampleEvent;
import com.berti.eventbus.EventBusSubscriber;

import com.berti.util.GlobalTimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleEventBusSubscriber implements EventBusSubscriber<SampleEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SampleEventBusSubscriber.class);

    private final String name;

    public SampleEventBusSubscriber(String name) {
        this.name = name;
    }

    @Override
    public void onEvent(SampleEvent event) {
        event.setReceptionTime(GlobalTimeProvider.getGlobalTime().getTimeMs());

        logger.info( "{} received event {}{} value = {} duration = {} ms",
            name, event.getProducerId(), event.getEventNumber(), event.getValue(), event.getLatency());
    }
}
