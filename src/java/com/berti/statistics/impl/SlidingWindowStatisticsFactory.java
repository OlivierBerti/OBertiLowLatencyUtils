package com.berti.statistics.impl;

import com.berti.data.DataSetter;
import com.berti.eventbus.EventBus;
import com.berti.eventbus.multithread.MultiThreadedEventBus;
import com.berti.eventbus.multithread.RingBufferConfiguration;
import com.berti.eventbus.multithread.ringbuffer.RingBufferException;
import com.berti.statistics.SlidingWindowStatistics;
import com.berti.statistics.SlidingWindowStatisticsException;
import com.berti.statistics.data.MeasurementPack;
import com.berti.statistics.data.MeasurementPackDataSetter;
import com.berti.throttling.ThrottlingConfiguration;

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

            SlidingWindowStatisticsMaker impl = new SlidingWindowStatisticsMaker(
                windowSizeMillisec, throttlingConfiguration, ringBufferConfiguration, measurementPackEventBus, supplier);

            impl.start();
            return impl;
        } catch (Exception e) {
            throw new SlidingWindowStatisticsException(
                    "Error initializing statistics maker: " + e.getMessage(), e);
        }
    }

    private EventBus<MeasurementPack> createMeasurementPackEventBus() throws RingBufferException {
        Supplier<MeasurementPack> supplier =  ()-> new MeasurementPack(MEASUREMENT_PACK_INITIAL_CAPACITY);
        DataSetter<MeasurementPack> dataSetter = new MeasurementPackDataSetter();

        MultiThreadedEventBus<MeasurementPack> impl = new MultiThreadedEventBus<>(DEFAULT_RING_BUFFER_LENGTH, supplier, dataSetter, false);
        impl.start();
        return impl;
    }
}
