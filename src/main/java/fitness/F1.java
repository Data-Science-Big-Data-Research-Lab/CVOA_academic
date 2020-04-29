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
import core.Utilities;

// F1 = f(x) = (x-15)^2
public class F1 extends FitnessFunction {
    
    
    @Override
    public long fitness(Individual individual) {
        // Optimal reached at x = 15. In binary: 11110000...
        return (long) Math.pow((Utilities.getInstance()).binaryToDecimal(individual) - 15, 2);
    }

}
