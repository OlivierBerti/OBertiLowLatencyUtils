package com.berti.statistics.data;

import com.berti.data.DataSetter;

public final class SlidingWindowStatisticsEventDataSetter
        implements DataSetter<SlidingWindowStatisticsEvent> {

    @Override
    public void copyData(SlidingWindowStatisticsEvent source, SlidingWindowStatisticsEvent target) {
        target.setValue(source.getValue());
        target.setEventType(source.getEventType());
    }
}
