package com.berti.throttling.impl;

import com.berti.throttling.Throttler;
import com.berti.throttling.ThrottlerClient;
import com.berti.throttling.ThrottlingConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestThrottlerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(TestThrottlerImpl.class);

    private interface IntegerConsumer {
        void add(int a);
    }

    private static final class IntegerConsumerImpl implements IntegerConsumer {

        private final List<Integer> list = new ArrayList<>();

        @Override
        public void add(int value) {
            list.add(value);
        }

        public List<Integer> getList() {
            return Collections.unmodifiableList(list);
        }
    }

    private static final class IntegerConsumerThrottler1 implements IntegerConsumer {

        private final Throttler throttler;

        private final IntegerConsumer integerConsumer;

        private IntegerConsumerThrottler1(Throttler throttler, IntegerConsumer integerConsumer) {
            this.throttler = throttler;
            this.integerConsumer = integerConsumer;
        }

        @Override
        public void add(int a) {
            Throttler.ThrottleResult result = throttler.shouldProceed();
            if (result == Throttler.ThrottleResult.PROCEED) {
                integerConsumer.add(a);
            }
        }
    }

    private static final class IntegerConsumerThrottler2 implements IntegerConsumer, ThrottlerClient {

        private final IntegerConsumer integerConsumer;

        // Works because we now the values are scent in crescent order starting from 0;
        private volatile int lastReadValue = -1;
        private volatile int lastWriteValue = -2;

        private IntegerConsumerThrottler2(Throttler throttler, IntegerConsumer integerConsumer) {
            this.integerConsumer = integerConsumer;
            throttler.notifyWhenCanProceed(this);
        }

        @Override
        public void add(int a) {
            lastReadValue = a;
        }

        @Override
        public void proceedThrottledEvent() {
            int eventValue = lastReadValue;
            int lastSentValue = lastWriteValue;
            if (eventValue != lastSentValue) {
                LOG.info("proceed with event {}", eventValue);
                lastWriteValue = eventValue;
                integerConsumer.add(eventValue);
            }
        }
    }

    private ThrottlerImpl throttler;

    private IntegerConsumerImpl integerConsumer;

    private IntegerConsumerThrottler1 integerConsumerThrottler1;

    private IntegerConsumerThrottler2 integerConsumerThrottler2;

    @Before
    public void setUp() throws Exception {

        ThrottlingConfiguration config = new ThrottlingConfiguration(2000, 4);
        throttler = new ThrottlerImpl(config);
        integerConsumer = new IntegerConsumerImpl();
        throttler.start();
        Thread.sleep(1000);
    }

    @After
    public void tearDown() {
        throttler.stop();
    }

    @Test
    public void testPoll() throws InterruptedException {
        integerConsumerThrottler1 = new IntegerConsumerThrottler1(throttler, integerConsumer);
        for (int i = 0; i < 20; i++) {
            integerConsumerThrottler1.add(i);
            Thread.sleep(300);
        }
        Thread.sleep(2000);
        List<Integer> list = integerConsumer.getList();
        LOG.info("consumed values: {}", list);
        assertTrue(list.size() >= 11);
        assertTrue(list.size() <= 13);
    }

    @Test
    public void testPush() throws InterruptedException {
        integerConsumerThrottler2 = new IntegerConsumerThrottler2(throttler, integerConsumer);
        for (int i = 0; i < 20; i++) {
            integerConsumerThrottler2.add(i);
            Thread.sleep(300);
        }
        Thread.sleep(2000);
        List<Integer> list = integerConsumer.getList();
        LOG.info("consumed values: {}", list);
        assertTrue(list.size() >= 11);
        assertTrue(list.size() <= 13);
    }
}
