package com.berti.statistics.client;

import com.berti.statistics.SlidingWindowStatistics;
import com.berti.statistics.Statistics;
import com.berti.statistics.StatisticsSubscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LatencyAlerter implements StatisticsSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(LatencyAlerter.class);

    private final int threshold;

    public LatencyAlerter(SlidingWindowStatistics statisticsMaker, int threshold) {
        this.threshold = threshold;
        statisticsMaker.subscribeForStatistics(this);
    }
    @Override
    public boolean accept(Statistics statistics) {
        return statistics.getMean() >= threshold;
    }

    @Override
    public void onStatistics(Statistics statistics) {
        LOG.warn("Latency statistics " + statistics);
    }
}
