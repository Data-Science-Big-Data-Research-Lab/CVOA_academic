package fitness;

/**
 *
 * @author Data Science & Big Data Lab, Pablo de Olavide University
 *
 * Parallel Coronavirus Optimization Algorithm
 * 
 * Version 3.0 Academic version for a binary codification
 *
 * April 2020
 *
 */

import core.Individual;

// Extend this class if you want to add new fitness functions 
// Add as many classes as you want, one per fitness function
public abstract class FitnessFunction {

    public static double getWorstValue() {
        return Double.MAX_VALUE;
    }

    public static double getBestValue() {
        return 0.0;
    }

    public abstract double fitness(Individual individual);

}
