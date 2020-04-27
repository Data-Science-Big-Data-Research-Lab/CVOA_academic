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

import core.Utilities;
import core.Individual;

// F2 = f(x) = (x)^2
public class F2 extends FitnessFunction {

    @Override
    public long fitness(Individual individual) {
        
    	// Optimal reached at x = 0. In binary x = 0000000...
    	
        return (long) Math.pow((Utilities.getInstance()).binaryToDecimal(individual),1);
    }
}
