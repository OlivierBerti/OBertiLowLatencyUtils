package com.berti.statistics.impl;

import com.berti.eventbus.EventBus;
import com.berti.eventbus.EventBusException;
import com.berti.eventbus.multithread.AbstractRunnableRingBufferedModule;
import com.berti.eventbus.multithread.RingBufferConfiguration;
import com.berti.statistics.*;
import com.berti.statistics.data.*;
import com.berti.throttling.Throttler;
import com.berti.throttling.ThrottlerClient;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.berti.util.GlobalTimeProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlidingWindowStatisticsMaker
        extends AbstractRunnableRingBufferedModule<SlidingWindowStatisticsEvent>
        implements SlidingWindowStatistics, ThrottlerClient {

    private static final Logger LOG = LoggerFactory.getLogger(SlidingWindowStatisticsMaker.class);

    private final long windowSizeMillisec;

    private final Throttler throttler;

    private final List<Measurement>  measurements = new LinkedList<>();

    private final MeasurementPack currentMeasurementPack;

    private final AtomicInteger currentMeasurementPackCounter = new AtomicInteger(0);

    private final EventBus<MeasurementPack> measurementPackEventBus;

    private final StatisticsCalculator statisticsCalculator;

    //To avoid pushing the same measurement pack twice
    private boolean measurementPackChangedSinceLastPush = false;

    public SlidingWindowStatisticsMaker(
            long windowSizeMillisec,
            Throttler throttler,
            StatisticsCalculator statisticsCalculator,
            RingBufferConfiguration ringBufferConfiguration,
            EventBus<MeasurementPack> measurementPackEventBus,
            Supplier<MeasurementPack> measurementPackSupplier) throws Exception {
        super(SlidingWindowStatisticsEvent.class, ringBufferConfiguration,
                SlidingWindowStatisticsEvent::new,false);

        this.windowSizeMillisec = windowSizeMillisec;
        this.throttler = throttler;
        this.statisticsCalculator = statisticsCalculator;
        this.throttler.notifyWhenCanProceed(this);
        this.measurementPackEventBus = measurementPackEventBus;
        this.currentMeasurementPack =  measurementPackSupplier.get();
    }

    @Override
    public void add(int measurement) throws SlidingWindowStatisticsException {
        try {
            this.pushEvent(new SlidingWindowStatisticsEvent(EventType.ADD_MEASUREMENT, measurement));
        } catch (SlidingWindowStatisticsException e) {
            throw e;
        } catch (Exception e) {
            throw new SlidingWindowStatisticsException("impossible to add measures: " + e.getMessage(), e);
        }
    }

    @Override
    public void subscribeForStatistics(StatisticsSubscriber statisticsSubscriber) {
        try {
            this.measurementPackEventBus.addSubscriber(
                    MeasurementPack.class,
                    new MeasurementPackSubscriberBridge(statisticsSubscriber, statisticsCalculator));
        } catch (Exception e) {
            LOG.error("Impossible to add statistics subscriber: "+e.getMessage(), e);
        }
    }

    @Override
    public void proceedThrottledEvent() {
        try {
            this.pushEvent(new SlidingWindowStatisticsEvent(EventType.PUSH_STATISTICS, 0));
        } catch (Exception e) {
            LOG.warn("impossible to push statistics: {}", e.getMessage());
        }
    }

    @Override
    public Statistics getLatestStatistics() {
        int currentStatisticsCounter = this.currentMeasurementPackCounter.get();
        if (throttler.shouldProceed() == Throttler.ThrottleResult.PROCEED) {
            try {
                this.pushEvent(new SlidingWindowStatisticsEvent(EventType.PULL_STATISTICS, 0));
                while (currentStatisticsCounter == this.currentMeasurementPackCounter.get()
                        && !isRingBufferFull()) {
                    Thread.yield();
                }
                return statisticsCalculator.createStatistics(
                        this.currentMeasurementPack.measurementsStream());
            } catch (Exception e) {
                LOG.warn("impossible to get last statistics: {}", e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void processEvent(SlidingWindowStatisticsEvent eventBuffer) {
        switch (eventBuffer.getEventType()) {
            case ADD_MEASUREMENT:
                doAddMeasurement(eventBuffer.getValue());
                break;
            case PUSH_STATISTICS:
                this.doCalculateCurrentStatistics();
                this.doPushStatistics();
                break;
            case PULL_STATISTICS:
                this.doCalculateCurrentStatistics();
                break;
        }
    }

    // Note: All the methods with a name starting with "do" are private
    // and called only in the event processing trade
    // => There will be no concurency issue in the measurements list updates

    private void doAddMeasurement(int measurement) {
        measurements.add(new Measurement(measurement, GlobalTimeProvider.getGlobalTime().getTimeMs()));
        measurementPackChangedSinceLastPush = true;
    }

    private void doCalculateCurrentStatistics() {
        currentMeasurementPack.clear();
        doCleanOldMeasurements();
        if (measurements.isEmpty()) {
            return;
        }

        for (Measurement measurement: measurements) {
            currentMeasurementPack.addMeasurement(measurement);
        }
        this.currentMeasurementPackCounter.incrementAndGet();
    }

    private void doCleanOldMeasurements() {
        long now = GlobalTimeProvider.getGlobalTime().getTimeMs();
        while (!measurements.isEmpty()) {
            Measurement measurement = measurements.getFirst();
            if (now - measurement.getTimestamsp() > this.windowSizeMillisec) {
                measurements.removeFirst();
                measurementPackChangedSinceLastPush = true;
            } else  {
                break;
            }
        }
    }

    // The event bus will make sure each subscriber will process the statistics in its own thread.
    // => a subscriber won't try to process 2 statistics in the same time.
    // => a too slow or bugged subscriber won't block the other ones.
    private void doPushStatistics() {
        try {
            if (!currentMeasurementPack.isEmpty() && measurementPackChangedSinceLastPush) {
                measurementPackEventBus.publishEvent(currentMeasurementPack);
                measurementPackChangedSinceLastPush = false;
            }
        } catch (EventBusException e) {
            LOG.error("impossible to publish statistics: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onRingBufferFull(SlidingWindowStatisticsEvent event) throws Exception {
        throw new SlidingWindowStatisticsException("Sliding Window Statistics maker is is blocked: ringBuffer congested");
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
