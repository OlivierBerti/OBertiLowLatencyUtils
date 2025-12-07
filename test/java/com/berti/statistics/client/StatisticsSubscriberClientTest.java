package com.berti.statistics.client;

import com.berti.statistics.Statistics;
import com.berti.statistics.StatisticsSubscriber;

import java.util.ArrayList;
import java.util.List;

public abstract class StatisticsSubscriberClientTest implements StatisticsSubscriber {

    private final List<Statistics> statisticsList = new ArrayList<>();

    @Override
    public void onStatistics(Statistics statistics) {
        statisticsList.add(statistics);
    }

    public Statistics getStatistics(int index) {
        return statisticsList.get(index);
    }

    public int getNbReceivedStatistics() {
        return statisticsList.size();
    }
}
