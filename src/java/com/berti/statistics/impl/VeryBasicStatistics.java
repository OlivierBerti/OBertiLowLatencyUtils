package com.berti.statistics.impl;

import com.berti.statistics.Statistics;

import java.util.Arrays;
import java.util.List;

public class VeryBasicStatistics implements Statistics {

    private final double mean;

    private final List<Integer>  sortedValues;

    public VeryBasicStatistics(List<Integer> values, double mean) {
        this.mean = mean;
        this.sortedValues = values.stream().sorted().toList();
    }

    @Override
    public double getMean() {
        return mean;
    }

    @Override
    public double getMode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getPctile(int pctile) {
        if (sortedValues.isEmpty()) {
            return 0;
        }

        int index = (int) Math.ceil(sortedValues.size() * pctile /100.0d);
        if (index == sortedValues.size()) {
            index --;
        }
        return sortedValues.get(index);
    }
}
