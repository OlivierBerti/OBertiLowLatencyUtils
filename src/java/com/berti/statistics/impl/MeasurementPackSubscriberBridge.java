package com.berti.statistics.impl;

import com.berti.eventbus.EventBusSubscriber;
import com.berti.statistics.Statistics;
import com.berti.statistics.StatisticsSubscriber;
import com.berti.statistics.data.MeasurementPack;

public final class MeasurementPackSubscriberBridge implements EventBusSubscriber<MeasurementPack> {

    private final StatisticsSubscriber statisticsSubscriber;

    public MeasurementPackSubscriberBridge(StatisticsSubscriber statisticsSubscriber) {
        this.statisticsSubscriber = statisticsSubscriber;
    }

    @Override
    public void onEvent(MeasurementPack measurementPack) {
        Statistics statistics = StatisticsCalculator.createStatistics(measurementPack.measurementsStream());
        if (statisticsSubscriber.accept(statistics)) {
            statisticsSubscriber.onStatistics(statistics);
        }
    }
}
