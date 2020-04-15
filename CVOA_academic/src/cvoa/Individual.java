package cvoa;

/**
 *
 * @author 
 * Data Science & Big Data Lab
 * Pablo de Olavide University
 * 
 * Academic version for a binary codification
 * 
 */

public class Individual implements Comparable {
	
	protected int [] data;      // Solution
	protected double fitness;   // and its fitness        

	public Individual(int[] data) {
		super();
		this.data = data;
	}

	public int[] getData() {
		return data;
	}

	public void setData(int[] data) {
		this.data = data;
	}

	@Override
	public int compareTo(Object o) {
		if (fitness > ((Individual)o).fitness)
			return 1;
		else if (fitness == ((Individual)o).fitness)
			return 0;
		else
			return -1;
	}

	@Override
	public boolean equals(Object obj) {
		Individual indiv = (Individual)obj;
		int i = 0, diff = 0;
		
		while (diff == 0 && i < data.length) {
			diff = data[i] - indiv.data[i];
			i++;
		}
	
		return diff == 0;
	}

	@Override
	public String toString() {
		String res; //
		int i;
		
		res = "[" + data[0];
		for (i = 1; i < data.length; i++)
			res += "," + data[i];
		
		res += "]";
		
		return res;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
}
