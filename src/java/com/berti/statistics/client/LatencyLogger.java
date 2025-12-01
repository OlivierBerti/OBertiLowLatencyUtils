package com.berti.statistics.client;

import com.berti.statistics.SlidingWindowStatistics;
import com.berti.statistics.Statistics;
import com.berti.statistics.StatisticsSubscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LatencyLogger implements StatisticsSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(LatencyLogger.class);

    public LatencyLogger(SlidingWindowStatistics statisticsMaker) {
        statisticsMaker.subscribeForStatistics(this);
    }

    @Override
    public boolean accept(Statistics statistics) {
        return true;
    }

    @Override
    public void onStatistics(Statistics statistics) {
        LOG.info("Latency statistics " + statistics);
    }
}
