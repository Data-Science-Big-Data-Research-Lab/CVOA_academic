package fitness;

import core.Utilities;
import core.Individual;

public class F2 extends FitnessFunction {

    @Override
    public double fitness(Individual individual) {
        
    	// Optimal reached at x = 0
    	
    	int x = (Utilities.getInstance()).binaryToDecimal(individual);
    	
        return Math.pow(x,2);
    }
}
