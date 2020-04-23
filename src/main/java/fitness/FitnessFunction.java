package fitness;

import core.Individual;

public abstract class FitnessFunction {

    public static double getWorstValue() {
        return Double.MAX_VALUE;
    }

    public static double getBestValue() {
        return 0.0;
    }

    public abstract double fitness(Individual individual);

}
