package com.berti.throttling.impl;

import com.berti.data.DataSetter;
import com.berti.ringbuffer.RingBufferException;
import com.berti.ringbuffer.SingleProducerSingleConsumerRingBuffer;
import com.berti.throttling.Throttler;
import com.berti.throttling.ThrottlerClient;
import com.berti.throttling.ThrottlingConfiguration;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// Implementation of Throttler interface
// Please note the following restrictions:
//   - a client may use the push mode or the poll mode but not both in the same time
//   - the throttler can support as many clients as we want in push mode but only one client in poll mode
public final class ThrottlerImpl implements Throttler {

    private static final Logger LOG = LoggerFactory.getLogger(ThrottlerImpl.class);

    public static final class ThrottlerToken {
        // unnecessary field, but may be useful for debugging
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

    private final SingleProducerSingleConsumerRingBuffer<ThrottlerToken> tokenRingBuffer;

    private final ScheduledExecutorService scheduler;

    private final ThrottlerToken inTokenBuffer = new ThrottlerToken();
    private final ThrottlerToken outTokenBuffer = new ThrottlerToken();

    public ThrottlerImpl(ThrottlingConfiguration config) throws RingBufferException {
        this.nbMaxEvents = config.getNbEventMax();
        this.windowSizeInMs = config.getTimeWindowMs();
        this.tokenGenPeriodInMs = windowSizeInMs / nbMaxEvents;
        this.tokenRingBuffer = new SingleProducerSingleConsumerRingBuffer<>(
                nbMaxEvents, ThrottlerToken::new, new ThrottlerTokenDataSetter());
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::addToken, tokenGenPeriodInMs, tokenGenPeriodInMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void addToken() {
        inTokenBuffer.timestamp = Instant.now().toEpochMilli();
        this.tokenRingBuffer.push(inTokenBuffer);
        LOG.info("notifyWhenCanProceed clients they can proceed");
        for (ThrottlerClient listener : listeners) {
            listener.proceedThrottledEvent();
        }
    }

    @Override
    public ThrottleResult shouldProceed() {
        LOG.info("shouldProceed() ? nb active elems = {}", tokenRingBuffer.nbActiveElements());
        if (this.tokenRingBuffer.isEmpty()) {
            return ThrottleResult.DO_NOT_PROCEED;
        }
        ThrottlerToken throttlerToken = tokenRingBuffer.poll(outTokenBuffer);
        return throttlerToken != null ? ThrottleResult.PROCEED : ThrottleResult.DO_NOT_PROCEED;
    }

    @Override
    public void notifyWhenCanProceed(ThrottlerClient throttlerClient) {
        this.listeners.add(throttlerClient);
    }
}
