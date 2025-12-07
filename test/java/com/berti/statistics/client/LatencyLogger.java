package com.berti.statistics.client;


import com.berti.statistics.Statistics;
import com.berti.statistics.StatisticsSubscriber;


public final class LatencyLogger extends StatisticsSubscriberClientTest implements StatisticsSubscriber {

    @Override
    public boolean accept(Statistics statistics) {
        return true;
    }
}
