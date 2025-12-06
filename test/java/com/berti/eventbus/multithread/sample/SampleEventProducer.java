package com.berti.eventbus.multithread.sample;

import com.berti.data.SampleEvent;
import com.berti.eventbus.EventBus;
import com.berti.util.TimeUtils;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class SampleEventProducer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SampleEventProducer.class);

    private final String name;

    private final EventBus<SampleEvent> eventBus;

    private final int maxNumberOfElements;

    private final int startingValue;

    private final long tempoInNanos;

    private volatile boolean end = false;

    public SampleEventProducer(String name, EventBus<SampleEvent> eventBus,
                               int maxNumberOfElements, int startingValue, long tempoInNanos) {
        this.name = name;
        this.eventBus = eventBus;
        this.maxNumberOfElements = maxNumberOfElements;
        this.startingValue = startingValue;
        this.tempoInNanos = tempoInNanos;
    }

    @Override
    public void run() {
        int i =0;
        while (i < maxNumberOfElements && !end) {
            int value = startingValue + i;
            i ++;
            try {
                eventBus.publishEvent(createSampleEvent(i, value));
            }
            catch (Exception e) {
                LOG.warn("Error publishing event for producer " + name
                        + ": " + e.getMessage(), e);
            }
            TimeUtils.sleepNanos(tempoInNanos);
        }
    }

    public void start() {
        newSingleThreadExecutor().execute(this);
    }

    public void stop() {
        end = true;
    }

    private SampleEvent createSampleEvent(int numEvent, int value) {
        SampleEvent sampleEvent = new SampleEvent();
        sampleEvent.setCreationTime(Instant.now().toEpochMilli());
        sampleEvent.setEventNumber(numEvent);
        sampleEvent.setProducerId(this.name);
        sampleEvent.setValue(value);
        return sampleEvent;
    }
}
