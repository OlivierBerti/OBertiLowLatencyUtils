package com.berti.statistics.impl;

import com.berti.eventbus.EventBus;
import com.berti.eventbus.EventBusFactory;
import com.berti.eventbus.multithread.AbstractRunnableRingBufferedModule;
import com.berti.eventbus.multithread.RingBufferConfiguration;
import com.berti.ringbuffer.DataSetterRegistry;
import com.berti.statistics.Statistics;
import com.berti.statistics.StatisticsCalculator;
import com.berti.statistics.client.LatencyAlerter;
import com.berti.statistics.client.LatencyLogger;
import com.berti.statistics.data.MeasurementPack;
import com.berti.statistics.data.MeasurementPackDataSetter;
import com.berti.statistics.data.SlidingWindowStatisticsEvent;
import com.berti.statistics.data.SlidingWindowStatisticsEventDataSetter;
import com.berti.testutils.ThrottlerMock;
import com.berti.testutils.TimeMsMock;
import com.berti.util.GlobalTimeProvider;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class SlidingWindowsStatisticsMakerTest {

    private static final int WINDOW_SIZE_MS = 1000;

    private static final int TEST_TEMPO = 100;

    private static final int ALERTER_THRESHOLD = 220;

    private static TimeMsMock globalTmeMock = new TimeMsMock();


    private SlidingWindowStatisticsMaker statisticsMaker;

    private EventBus<MeasurementPack> measurementPackBus;

    private ThrottlerMock throttler;

    private StatisticsCalculator statisticsCalculator;

    private LatencyLogger latencyLogger;

    private LatencyAlerter latencyAlerter;

    @BeforeClass
    public static void init() {
        GlobalTimeProvider.setGlobalTimeSpecialImpl(globalTmeMock);
        DataSetterRegistry.register(MeasurementPack.class, new MeasurementPackDataSetter());
        DataSetterRegistry.register(SlidingWindowStatisticsEvent.class, new SlidingWindowStatisticsEventDataSetter());
    }

    @Before
    public void setUp() throws Exception {
        //timeMs = new TimeMsMock();
        globalTmeMock.reset();
        throttler = new ThrottlerMock();
        statisticsCalculator = new StatisticsCalculatorImpl();
        RingBufferConfiguration config = new RingBufferConfiguration(128, 1000, false);

        measurementPackBus = EventBusFactory.getInstance().createConflatingMultithreadedEventBus(
                MeasurementPack.class, MeasurementPack::new, config);

        statisticsMaker = new SlidingWindowStatisticsMaker(
                WINDOW_SIZE_MS, throttler, statisticsCalculator, config, measurementPackBus, MeasurementPack::new);
        statisticsMaker.start();

        latencyLogger = new LatencyLogger();
        latencyAlerter = new LatencyAlerter(ALERTER_THRESHOLD);

        statisticsMaker.subscribeForStatistics(latencyLogger);
        statisticsMaker.subscribeForStatistics(latencyAlerter);
    }

    @After
    public void tearDown() {
        statisticsMaker.stop();
        ((AbstractRunnableRingBufferedModule<MeasurementPack>) measurementPackBus).stop();
        statisticsMaker = null;
    }

    @Test
    public void testPushOnly() throws Exception {

        sendThrottlerNotification();
        addMeasurement(50);
        globalTmeMock.sleep(300);
        addMeasurement(40);
        globalTmeMock.sleep(400);
        addMeasurement(300);
        throttler.sendNotification();
        addMeasurement(400);
        addMeasurement(200);
        addMeasurement(100);
        globalTmeMock.sleep(500); // the first value is out of the window
        sendThrottlerNotification();
        globalTmeMock.sleep(200); // the second value is out of the window
        sendThrottlerNotification();
        sendThrottlerNotification(); // same measurements as the previous one => won't be taken in account

        Statistics expectedStatistics0 = createStatistics(50, 40, 300);
        Statistics expectedStatistics1 = createStatistics(40, 300, 400, 200, 100);
        Statistics expectedStatistics2 = createStatistics(300, 400, 200, 100);

        assertEquals(3, latencyLogger.getNbReceivedStatistics());
        assertEquals(expectedStatistics0, latencyLogger.getStatistics(0));
        assertEquals(expectedStatistics1, latencyLogger.getStatistics(1));
        assertEquals(expectedStatistics2, latencyLogger.getStatistics(2));

        assertEquals(1, latencyAlerter.getNbReceivedStatistics());
        assertEquals(expectedStatistics2, latencyAlerter.getStatistics(0));
    }

    @Test
    public void testPushAndPoll() throws Exception {

        sendThrottlerNotification();
        addMeasurement(50);
        globalTmeMock.sleep(300);
        addMeasurement(40);
        Statistics currentStatistics01 = pollStatistics(true);
        globalTmeMock.sleep(400);
        addMeasurement(300);
        throttler.sendNotification();
        addMeasurement(400);
        Statistics currentStatistics02 = pollStatistics(false);
        addMeasurement(200);
        Statistics currentStatistics03 = pollStatistics(true);
        addMeasurement(100);
        globalTmeMock.sleep(500); // the first value is out of the window
        sendThrottlerNotification();
        Statistics currentStatistics04 = pollStatistics(true);
        globalTmeMock.sleep(200); // the second value is out of the window
        sendThrottlerNotification();
        sendThrottlerNotification(); // same measurements as the previous one => won't be taken in account


        Statistics expectedStatistics0 = createStatistics(50, 40, 300);
        Statistics expectedStatistics1 = createStatistics(40, 300, 400, 200, 100);
        Statistics expectedStatistics2 = createStatistics(300, 400, 200, 100);


        Statistics expectedCurrentStatistics01 = createStatistics(50, 40);
        Statistics expectedCurrentStatistics03 = createStatistics(50, 40, 300, 400, 200);
        Statistics expectedCurrentStatistics04 = createStatistics(40, 300, 400, 200, 100);


        assertEquals(3, latencyLogger.getNbReceivedStatistics());
        assertEquals(expectedStatistics0, latencyLogger.getStatistics(0));
        assertEquals(expectedStatistics1, latencyLogger.getStatistics(1));
        assertEquals(expectedStatistics2, latencyLogger.getStatistics(2));

        assertEquals(1, latencyAlerter.getNbReceivedStatistics());
        assertEquals(expectedStatistics2, latencyAlerter.getStatistics(0));

        assertEquals(expectedCurrentStatistics01, currentStatistics01);
        assertNull(currentStatistics02);
        assertEquals(expectedCurrentStatistics03, currentStatistics03);
        assertEquals(expectedCurrentStatistics04, currentStatistics04);
    }


    private Statistics createStatistics(int... values) {
        List<Integer> valuesList = new ArrayList<>();
        for (int value : values) {
            valuesList.add(value);
        }
        return statisticsCalculator.createStatistics(valuesList);
    }

    private void addMeasurement(int measurement) throws Exception {
        statisticsMaker.add(measurement);
        Thread.sleep(TEST_TEMPO);
    }

    private void sendThrottlerNotification() throws Exception {
        throttler.sendNotification();
        Thread.sleep(TEST_TEMPO);
    }

    private Statistics pollStatistics(boolean shouldProceed) {
        throttler.setShouldProceed(shouldProceed);
        Statistics result = statisticsMaker.getLatestStatistics();
        return result;
    }
}
