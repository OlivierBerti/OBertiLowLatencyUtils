package com.berti.statistics;

import java.util.List;

public interface Statistics {
    double getMean();

    List<Integer> getMode();

    int getPercentile(int pctile);
}
