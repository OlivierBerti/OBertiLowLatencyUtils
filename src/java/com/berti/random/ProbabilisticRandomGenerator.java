package com.berti.random;

import java.util.*;

public class ProbabilisticRandomGenerator implements ProbabilisticRandomGen {

    private final List<NumAndProbability> possibleNumbers;
    private final SortedSet<Double> probabilitiesLadder =  new TreeSet<>();

    private final Random random = new Random();


    public ProbabilisticRandomGenerator(List<NumAndProbability> inputNumbers) {
        // first removing negatives or null probabilities
        possibleNumbers = inputNumbers.stream()
                .filter(x->x.getProbabilityOfSample() > 0)
                .toList();

        int nbValues = possibleNumbers.size();

        if (nbValues == 0) {
            throw new IllegalArgumentException("We should have at least 1 input number with a strictly positive probability");
        }

        // then getting the sum of probabilities. The real probability of a number is its probability divided by the sum.
        double total = possibleNumbers.stream()
                .mapToDouble(NumAndProbability::getProbabilityOfSample)
                .sum();

        // Now we build a ladder of cumulated probabilities
        double cumulativeProb = 0.0d;
        for (int i=0; i<nbValues; i++) {
            cumulativeProb += possibleNumbers.get(i).getProbabilityOfSample()/total;
            if (i<nbValues-1) {
                probabilitiesLadder.add(cumulativeProb);
            }
            // to avoid a max value of 0.999999998d provoking an IndexOutOfBoundsException in the nextFromSample() method
            else {
                probabilitiesLadder.add(1.0d);
            }
        }
    }

    @Override
    public int nextFromSample() {
        // All we have to do now is to generate a random double in [0, 1[ and find its index in the list
        // The index is the number of smaller elements in the probabilities ladder
        double randomValue = random.nextDouble();
        int index = probabilitiesLadder.headSet(randomValue).size();
        return possibleNumbers.get(index).getNumber();
    }
}
