package com.berti.statistics.impl;

import com.berti.eventbus.EventBusSubscriber;
import com.berti.statistics.Statistics;
import com.berti.statistics.StatisticsCalculator;
import com.berti.statistics.StatisticsSubscriber;
import com.berti.statistics.data.MeasurementPack;

public final class MeasurementPackSubscriberBridge implements EventBusSubscriber<MeasurementPack> {

    private final StatisticsSubscriber statisticsSubscriber;

    private final StatisticsCalculator statisticsCalculator;

    public MeasurementPackSubscriberBridge(StatisticsSubscriber statisticsSubscriber, StatisticsCalculator statisticsCalculator) {
        this.statisticsSubscriber = statisticsSubscriber;
        this.statisticsCalculator = statisticsCalculator;
    }

    @Override
    public void onEvent(MeasurementPack measurementPack) {
        Statistics statistics = statisticsCalculator.createStatistics(measurementPack.measurementsStream());
        if (statisticsSubscriber.accept(statistics)) {
            statisticsSubscriber.onStatistics(statistics);
        }
    }
}
