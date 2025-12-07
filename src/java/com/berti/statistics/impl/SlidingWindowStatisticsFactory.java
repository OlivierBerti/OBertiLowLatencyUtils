package com.berti.statistics.impl;

import com.berti.eventbus.EventBus;
import com.berti.eventbus.EventBusException;
import com.berti.eventbus.EventBusFactory;
import com.berti.eventbus.multithread.RingBufferConfiguration;
import com.berti.statistics.SlidingWindowStatistics;
import com.berti.statistics.SlidingWindowStatisticsException;
import com.berti.statistics.data.MeasurementPack;
import com.berti.throttling.Throttler;
import com.berti.throttling.ThrottlingConfiguration;
import com.berti.throttling.impl.ThrottlerFactory;

import java.util.function.Supplier;

public final class SlidingWindowStatisticsFactory {

    private static final SlidingWindowStatisticsFactory instance = new SlidingWindowStatisticsFactory();

    public static SlidingWindowStatisticsFactory getInstance() {
        return instance;
    }

    private static final long DEFAULT_TEMPO_IN_NANOS = 1000;

    private static final int DEFAULT_RING_BUFFER_LENGTH = 1024;

    private static final int MEASUREMENT_PACK_INITIAL_CAPACITY = 100;

    public SlidingWindowStatistics createSlidingWindowStatistics(
            long windowSizeMillisec, ThrottlingConfiguration throttlingConfiguration) throws SlidingWindowStatisticsException {
        try {
            RingBufferConfiguration ringBufferConfiguration =
                    new RingBufferConfiguration(DEFAULT_RING_BUFFER_LENGTH, DEFAULT_TEMPO_IN_NANOS, true);
            EventBus<MeasurementPack> measurementPackEventBus = createMeasurementPackEventBus();
            Supplier<MeasurementPack> supplier =  ()-> new MeasurementPack(MEASUREMENT_PACK_INITIAL_CAPACITY);

            Throttler throttler = ThrottlerFactory.getInstance().createThrottler(throttlingConfiguration);
            SlidingWindowStatisticsMaker impl = new SlidingWindowStatisticsMaker(
                windowSizeMillisec, throttler, ringBufferConfiguration, measurementPackEventBus, supplier);

            impl.start();
            return impl;
        } catch (Exception e) {
            throw new SlidingWindowStatisticsException(
                    "Error initializing statistics maker: " + e.getMessage(), e);
        }
    }

    private EventBus<MeasurementPack> createMeasurementPackEventBus() throws EventBusException {
        Supplier<MeasurementPack> supplier =  ()-> new MeasurementPack(MEASUREMENT_PACK_INITIAL_CAPACITY);

        RingBufferConfiguration ringBufferConfiguration =
                new RingBufferConfiguration(DEFAULT_RING_BUFFER_LENGTH, DEFAULT_TEMPO_IN_NANOS, false);

        return EventBusFactory.getInstance().createConflatingMultithreadedEventBus(
                        MeasurementPack.class, supplier, ringBufferConfiguration);
    }
}
