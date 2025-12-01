package com.berti.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public final class TimeUtils {

    private static final Logger LOG  = LoggerFactory.getLogger(TimeUtils.class);

    public static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted thread", e);
        }
    }

    public static void sleepNanos(long nanos) {
        try {
            Thread.sleep(Duration.ofNanos(nanos));
        } catch (InterruptedException e) {
            LOG.warn("Interrupted thread", e);
        }
    }

    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            LOG.warn( "Interrupted thread", e);
        }
    }

    private TimeUtils() {}
}
