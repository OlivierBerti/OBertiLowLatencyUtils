package com.berti.util;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeUtils {

    private static final Logger LOG = Logger.getLogger(TimeUtils.class.getName());


    public static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOG.log(Level.WARNING, "Interrupted thread", e);
        }
    }

    public static void sleepNanos(long nanos) {
        try {
            Thread.sleep(Duration.ofNanos(nanos));
        } catch (InterruptedException e) {
            LOG.log(Level.WARNING, "Interrupted thread", e);
        }
    }

    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            LOG.log(Level.WARNING, "Interrupted thread", e);
        }
    }

    private TimeUtils() {}
}
