package com.berti.throttling;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ThrottlingConfiguration {

    private final long timeWindowMs;

    private final int nbEventMax;

    public ThrottlingConfiguration(long timeWindowMs, int nbEventMax) {
        this.timeWindowMs = timeWindowMs;
        this.nbEventMax = nbEventMax;
    }
}
