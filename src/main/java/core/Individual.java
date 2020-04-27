package core;

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

public class Individual implements Comparable<Individual> {

    private int[] data;                 // Array codifying a binary solution
    private long fitness;             // Fitness of the solution
    private long value;                  // Decimal value for data
    private int discoveringIteration;   // Iteration of best solution found

    public Individual() {
        super();
        this.data = null;
        this.value = -1;
        this.discoveringIteration = -1;
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
        this.data = data;
        this.value = (long)(Utilities.getInstance()).binaryToDecimal(this.data);
    }

    public long getFitness() {
        return fitness;
    }

    public void setFitness(long fitness) {
        this.fitness = fitness;
    }

    public int getDiscoveringIteration() {
        return discoveringIteration;
    }

    public void setDiscoveringIteration(int discoveringIteration) {
        this.discoveringIteration = discoveringIteration;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value=value;
    }

    @Override
    public int compareTo(Individual o) {

        double cmpFitness = o.getFitness();
        int res = 0;

        if (this.fitness > cmpFitness) {
            res = 1;
        } else if (this.fitness < cmpFitness) {
            res = -1;
        }

        return res;

    }
    
    @Override
    public boolean equals(Object obj) {

        boolean res = false;
        long diff = 0;

        if (obj != null) {
            if (obj instanceof Individual) {
                if (obj == this) {
                    res = true;
                } else {
                    Individual ind = (Individual) obj;
                    diff = this.value - ind.getValue();
                    res = diff == 0;
                }
            }
        }

        return res;
    }
    
    
    @Override
    public int hashCode() {
        return (int) this.value;
    }

    @Override
    public String toString() {

        String res = "";

        //res += ", " + (Utilities.getInstance()).getDecimalFormat().format(this.fitness) + ", " + this.discoveringIteration + "}";
        res += (Utilities.getInstance()).getDecimalFormat().format(this.fitness);

        return res;
    }

    
}
