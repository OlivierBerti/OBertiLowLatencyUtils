package com.berti.statistics.client;

import com.berti.statistics.Statistics;
import com.berti.statistics.StatisticsSubscriber;

// LatencyALerter: example of StatisticsSubscriber with filter
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
