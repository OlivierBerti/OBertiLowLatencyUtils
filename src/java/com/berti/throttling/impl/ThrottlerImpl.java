package com.berti.throttling.impl;

import com.berti.data.DataSetter;
import com.berti.eventbus.multithread.ringbuffer.RingBufferException;
import com.berti.eventbus.multithread.ringbuffer.SingleProducerSingleConsumerRingBuffer;
import com.berti.throttling.Throttler;
import com.berti.throttling.ThrottlerClient;
import com.berti.throttling.ThrottlingConfiguration;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO define token
public final class ThrottlerImpl implements Throttler {

    private static final Logger LOG = LoggerFactory.getLogger(ThrottlerImpl.class);

    public static final class ThrottlerToken {
        long timestamp;
    }

    public static final class ThrottlerTokenDataSetter implements DataSetter<ThrottlerToken> {

        @Override
        public void copyData(ThrottlerToken source, ThrottlerToken target) {
            target.timestamp = source.timestamp;
        }
    }

    private final int nbMaxEvents;

    private final long windowSizeInMs;

    private final long tokenGenPeriodInMs;

    private final Set<ThrottlerClient> listeners = new CopyOnWriteArraySet<>();

    private final SingleProducerSingleConsumerRingBuffer<ThrottlerToken> tokenBuffer;

    private final ThrottlerToken inToken = new ThrottlerToken();
    private ThrottlerToken outBufferToken;

    public ThrottlerImpl(ThrottlingConfiguration config) throws RingBufferException {
        this.nbMaxEvents = config.getNbEventMax();
        this.windowSizeInMs = config.getTimeWindowMs();
        this.tokenGenPeriodInMs = windowSizeInMs / nbMaxEvents;
        this.tokenBuffer = new SingleProducerSingleConsumerRingBuffer<>(
                nbMaxEvents, ThrottlerToken::new, new ThrottlerTokenDataSetter());
    }

    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::addToken, tokenGenPeriodInMs, tokenGenPeriodInMs, TimeUnit.MILLISECONDS);
    }

    private void addToken() {
        if (!this.tokenBuffer.push(inToken)) {
            LOG.info("notifyWhenCanProceed clients they can proceed");
            for(ThrottlerClient listener : listeners) {
                listener.proceedThrottledEvent();
            }
        }
    }

    @Override
    public ThrottleResult shouldProceed() {
        if (this.tokenBuffer.isEmpty()) {
            return ThrottleResult.DO_NOT_PROCEED;
        }
        ThrottlerToken throttlerToken = tokenBuffer.poll(outBufferToken);
        return throttlerToken != null ? ThrottleResult.PROCEED : ThrottleResult.DO_NOT_PROCEED;
    }

    @Override
    public void notifyWhenCanProceed(ThrottlerClient throttlerClient) {
        this.listeners.add(throttlerClient);
    }
}
