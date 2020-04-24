package fitness;

import core.Individual;
import core.Utilities;

public class F1 extends FitnessFunction {

    @Override
    public double fitness(Individual individual) {
        // Optimal reached at x = 15 (In binary: 11110000...)
        return Math.pow((Utilities.getInstance()).binaryToDecimal(individual) - 15, 2);
    }

}
