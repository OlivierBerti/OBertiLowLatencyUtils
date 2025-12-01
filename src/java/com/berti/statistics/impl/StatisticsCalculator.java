package com.berti.statistics.impl;

import com.berti.statistics.Statistics;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StatisticsCalculator {

    public static Statistics createStatistics(List<Integer> numbers) {
        if (numbers == null) {
            numbers = new ArrayList<>();
        }
        return createStatistics(numbers.stream());
    }

    public static Statistics createStatistics(Stream<Integer> stream) {
        List<Integer> sortedNumbers = stream.sorted().toList();

        double mean = 0.0;
        if (!sortedNumbers.isEmpty()) {
            mean = sortedNumbers.stream().mapToDouble(x -> x).sum() / sortedNumbers.size();
        }
        return new VeryBasicStatistics(sortedNumbers, mean);
    }
}
