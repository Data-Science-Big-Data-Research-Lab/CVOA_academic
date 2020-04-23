package fitness;

import core.CVOAUtilities;
import core.Individual;

public class Xsquared extends FitnessFunction {

    @Override
    public double fitness(Individual individual) {
        
    	// Optimal reached at x = 0
    	
    	int x = (CVOAUtilities.getInstance()).binaryToDecimal(individual);
    	
        return Math.pow(x,2);
    }
}
