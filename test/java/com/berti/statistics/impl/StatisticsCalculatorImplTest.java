package com.berti.statistics.impl;

import com.berti.statistics.Statistics;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StatisticsCalculatorImplTest {

    private static final double EPSILON = 1e-6d;

    private StatisticsCalculatorImpl calculator;

    @Before
    public void setUp() {
        calculator = new StatisticsCalculatorImpl();
    }

    @Test
    public void testSimpleCase() {
        Statistics statistics = createStatistics(10, 20, 7, 14, 3, 7, 9);
        assertEquals(10.0, statistics.getMean(), EPSILON);
        assertEquals(1, statistics.getMode().size());
        assertEquals(7, statistics.getMode().get(0).intValue());
        assertEquals(3, statistics.getPercentile(0));
        assertEquals(7, statistics.getPercentile(25));
        assertEquals(14, statistics.getPercentile(70));
        assertEquals(20, statistics.getPercentile(99));
    }

    @Test
    public void testEmptySet() {
        Statistics statistics = createStatistics();
        assertEquals(0.0, statistics.getMean(), EPSILON);
        assertEquals(0, statistics.getMode().size());
        assertEquals(0, statistics.getPercentile(0));
        assertEquals(0, statistics.getPercentile(25));
        assertEquals(0, statistics.getPercentile(70));
        assertEquals(0, statistics.getPercentile(99));
    }

    @Test
    public void testModeMultipleValues() {
        Statistics statistics = createStatistics(20, 20, 20, 7, 7, 14, 3, 7, 9, 9);
        assertEquals(2, statistics.getMode().size());
        assertEquals(7, statistics.getMode().get(0).intValue());
        assertEquals(20, statistics.getMode().get(1).intValue());
    }

    private Statistics createStatistics(int... values) {
        List<Integer> valuesList = new ArrayList<>();
        for (int value : values) {
            valuesList.add(value);
        }
        return calculator.createStatistics(valuesList);
    }
}
