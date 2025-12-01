package com.berti.statistics;

public interface StatisticsSubscriber {

    boolean accept(Statistics statistics);

    void onStatistics(Statistics statistics);
}
