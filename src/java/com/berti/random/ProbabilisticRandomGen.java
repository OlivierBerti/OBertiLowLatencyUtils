package com.berti.random;

import lombok.Getter;

public interface ProbabilisticRandomGen {

    int nextFromSample();

    @Getter
    class NumAndProbability {

        private final int number;

        private final float probabilityOfSample;

        public NumAndProbability(int number, float probabilityOfSample) {
            this.number = number;
            this.probabilityOfSample = probabilityOfSample;
        }
    }
}
