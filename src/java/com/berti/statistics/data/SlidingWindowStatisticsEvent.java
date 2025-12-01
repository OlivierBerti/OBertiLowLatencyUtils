package com.berti.statistics.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class SlidingWindowStatisticsEvent {

    private EventType eventType;

    private int value;

    public SlidingWindowStatisticsEvent() {
    }

    public SlidingWindowStatisticsEvent(EventType eventType, int value) {
        this.eventType = eventType;
        this.value = value;
    }
}
