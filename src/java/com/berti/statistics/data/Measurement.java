package com.berti.statistics.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public final class Measurement {

    private int value;

    private long timestamsp;

    public Measurement() {
        this.value = 0;
        this.timestamsp = 0;
    }

    public Measurement(int value, long timestamsp) {
        this.value = value;
        this.timestamsp = timestamsp;
    }
}
