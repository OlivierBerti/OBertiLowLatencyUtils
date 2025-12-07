package com.berti.statistics.client;

import com.berti.statistics.SlidingWindowStatistics;
import com.berti.statistics.Statistics;
import com.berti.statistics.StatisticsSubscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LatencyAlerter extends StatisticsSubscriberClientTest implements StatisticsSubscriber {

    private final int threshold;

    public LatencyAlerter(int threshold) {
        this.threshold = threshold;
    }
    @Override
    public boolean accept(Statistics statistics) {
        return statistics.getMean() >= threshold;
    }
}
