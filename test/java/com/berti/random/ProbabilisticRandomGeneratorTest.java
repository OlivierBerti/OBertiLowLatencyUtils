package com.berti.random;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProbabilisticRandomGeneratorTest {

    @Mock
    private Random random;

    private ProbabilisticRandomGenerator randomGenerator;

    @Test
    public void testGenerator() {
        List<ProbabilisticRandomGen.NumAndProbability> possibleNumbers = new ArrayList<>();

        possibleNumbers.add(new ProbabilisticRandomGen.NumAndProbability(4, 3));
        possibleNumbers.add(new ProbabilisticRandomGen.NumAndProbability(7, -1));
        possibleNumbers.add(new ProbabilisticRandomGen.NumAndProbability(6, 9));
        possibleNumbers.add(new ProbabilisticRandomGen.NumAndProbability(20, 15));
        possibleNumbers.add(new ProbabilisticRandomGen.NumAndProbability(10, 0));
        possibleNumbers.add(new ProbabilisticRandomGen.NumAndProbability(17, 3));

        randomGenerator = new ProbabilisticRandomGenerator(possibleNumbers, random);

        List<ProbabilisticRandomGen.NumAndProbability> numbers = randomGenerator.getPossibleNumbers();
        SortedSet<Double> ladder = randomGenerator.getProbabilitiesLadder();

        assertEquals(4, ladder.size());
        assertEquals(4, numbers.size());

        Integer[] expectedNumbers = new Integer[] {4, 6, 20, 17};
        Double[] expectedProbabilities = new Double[] {0.1, 0.4, 0.9, 1.0};

        assertArrayEquals(expectedNumbers, numbers.stream().map(ProbabilisticRandomGen.NumAndProbability::getNumber).toArray());
        assertArrayEquals(expectedProbabilities, ladder.toArray());

        when(random.nextDouble()).thenReturn(0.01d);
        assertEquals(4, randomGenerator.nextFromSample());
        when(random.nextDouble()).thenReturn(0.95d);
        assertEquals(17, randomGenerator.nextFromSample());
        when(random.nextDouble()).thenReturn(0.25d);
        assertEquals(6, randomGenerator.nextFromSample());
        when(random.nextDouble()).thenReturn(0.60d);
        assertEquals(20, randomGenerator.nextFromSample());
    }
}
