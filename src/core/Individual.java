package core;

/**
 *
 * @author Data Science & Big Data Lab, Pablo de Olavide University
 *
 *         Parallel Coronavirus Optimization Algorithm Version 2.0 Academic
 *         version for a binary codification
 *
 *         March 2020
 *
 */

public class Individual implements Comparable<Individual> {

	private int[] data;
	private double fitness;
	private int value;
	private int discoveringIteration;

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
		this.value = (CvoaUtilities.getInstance()).binaryToDecimal(this.data);
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
	public int getDiscoveringIteration() {
		return discoveringIteration;
	}

	public void setDiscoveringIteration(int discoveringIteration) {
		this.discoveringIteration = discoveringIteration;
	}

	public int getValue() {
		return value;
	}


	@Override
	public int compareTo(Individual o) {
		
		double cmpFitness = o.getFitness();
		int res = 0;

		if (this.fitness > cmpFitness)
			res = 1;
		else if (this.fitness < cmpFitness)
			res = -1;

		return res;

	}


	public boolean equals(Object obj) {
		
		boolean res = false;
		
		if(obj!=null)
			if (obj instanceof Individual)
				if (obj == this)
					res = true;
				else {
					
					Individual ind = (Individual) obj;					
					int i = 0, diff = 0;

					while (diff == 0 && i < this.data.length) {
						diff = this.data[i] - ind.data[i];
						i++;
					}
					res =  diff == 0;
				}
					
			
		return res;
	}
	
	public int hashCode(){
		return this.value;	
	}
	
	@Override
	public String toString() {
		
		String res = "";
		
		if (data != null) 
			res += "{"+this.value;
		else
			res+= "VOID";
		
		res += "," + (CvoaUtilities.getInstance()).getDecimalFormat().format(this.fitness)+","+this.discoveringIteration+"}";
				
		return res;
	}
	
	public static Individual gerExtremeIndividual (boolean best) {
		
		Individual res = new Individual();
		
		res.setFitness(Double.MAX_VALUE);
		
		if (best)
			res.setFitness(0.0);
		
		return res;
	}

}
