package com.berti.statistics;

import java.util.List;
import java.util.stream.Stream;

public interface StatisticsCalculator {

    Statistics createStatistics(List<Integer> stream);

    Statistics createStatistics(Stream<Integer> stream);
}
