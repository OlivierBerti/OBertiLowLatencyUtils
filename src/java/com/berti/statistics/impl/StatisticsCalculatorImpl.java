package com.berti.statistics.impl;

import com.berti.statistics.Statistics;
import com.berti.statistics.StatisticsCalculator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticsCalculatorImpl implements StatisticsCalculator {

    @Getter
    @Setter
    private static final class CountedValues {
        private int value;
        private int count;

        public CountedValues(int value, int count) {
            this.value = value;
            this.count = count;
        }
    }

    @EqualsAndHashCode
    private static final class SimpleStatistics implements Statistics {

        @Getter
        private final double mean;

        private final List<Integer> mode;

        private final List<Integer> sortedValues;


        private SimpleStatistics(double mean, List<Integer> mode, List<Integer> sortedValues) {
            this.mean = mean;
            this.mode = mode;
            this.sortedValues = sortedValues;
        }

        @Override
        public List<Integer> getMode() {
            return Collections.synchronizedList(mode);
        }

        @Override
        public int getPercentile(int pctile) {
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

    @Override
    public Statistics createStatistics(List<Integer> numbers) {
        if (numbers == null) {
            numbers = new ArrayList<>();
        }
        return doCreateStatistics(numbers.stream());
    }

    @Override
    public Statistics createStatistics(Stream<Integer> stream) {
        return doCreateStatistics(stream);
    }

    private Statistics doCreateStatistics(Stream<Integer> stream) {
        List<Integer> sortedNumbers = stream.sorted().toList();
        double mean = 0.0;

        if (!sortedNumbers.isEmpty()) {
            mean = sortedNumbers.stream().mapToDouble(x -> x).sum() / sortedNumbers.size();
        }

        Map<Integer, Integer> countedValues  = sortedNumbers.stream()
                .map(i->new CountedValues(i, 1))
                .collect(Collectors.groupingBy(CountedValues::getValue,
                        Collectors.summingInt(CountedValues::getCount)))
                ;

        int modeNbOccurences = 0;
        List<Integer> mode = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : countedValues.entrySet()) {
            if (entry.getValue() == modeNbOccurences) {
                mode.add(entry.getKey());
            } else if (entry.getValue() > modeNbOccurences) {
                mode.clear();
                mode.add(entry.getKey());
                modeNbOccurences = entry.getValue();
            }
        }
        mode = mode.stream().sorted().toList();

        return new SimpleStatistics(mean, mode, sortedNumbers);
    }
}
