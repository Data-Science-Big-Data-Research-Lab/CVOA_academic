package fitness;

/**
 *
 * @author Data Science & Big Data Lab, Pablo de Olavide University
 *
 * Parallel Coronavirus Optimization Algorithm
 * 
 * Version 4.0 Academic version for a binary codification
 *
 * April 2020
 *
 */

import core.Individual;

// Extend this class if you want to add new fitness functions 
// Add as many classes as you want, one per fitness function
public abstract class FitnessFunction {

    public static long getWorstValue() {
        return Long.MAX_VALUE;
    }

    public static long getBestValue() {
        return 0;
    }

    public abstract long fitness(Individual individual);

}
