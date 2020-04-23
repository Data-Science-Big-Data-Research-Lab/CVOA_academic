package fitness;

import core.Individual;
import core.CVOAUtilities;

public class Xminus15 extends FitnessFunction {

    @Override
    public double fitness(Individual individual) {
        // Optimal reached at x = 15 (In binary: 11110000...)
        return Math.pow((CVOAUtilities.getInstance()).binaryToDecimal(individual) - 15, 2);
    }

}
