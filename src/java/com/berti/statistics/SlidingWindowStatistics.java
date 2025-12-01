package com.berti.statistics;

public interface SlidingWindowStatistics {

    void add(int measurement) throws SlidingWindowStatisticsException;

    // subscriber will have a callback that'll deliver a Statistics instance (push)
    void subscribeForStatistics(StatisticsSubscriber subscriber);

    // get latest statistics (poll)
    Statistics getLatestStatistics();
}
