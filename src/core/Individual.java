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
	private int nSuperSpreaderInfected;
	private int nInfected;
	private int discoveringIteration;

	public Individual() {
		super();
		this.data = null;
		nSuperSpreaderInfected = 0;
		nInfected = 0;		
		discoveringIteration = -1;
	}

	public int[] getData() {
		return data;
	}

	public void setData(int[] data) {
		this.data = data;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
	public int getnSuperSpreaderInfected() {
		return nSuperSpreaderInfected;
	}

	public void setnSuperSpreaderInfected(int nSuperSpreaderInfected) {
		this.nSuperSpreaderInfected = nSuperSpreaderInfected;
	}

	public int getnInfected() {
		return nInfected;
	}

	public void setnInfected(int nInfected) {
		this.nInfected = nInfected;
	}
	
	

	public int getDiscoveringIteration() {
		return discoveringIteration;
	}

	public void setDiscoveringIteration(int discoveringIteration) {
		this.discoveringIteration = discoveringIteration;
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

	@Override
	public boolean equals(Object obj) {
		
		Individual indiv = (Individual) obj;
		
		int i = 0, diff = 0;

		while (diff == 0 && i < data.length) {
			diff = data[i] - indiv.data[i];
			i++;
		}

		return diff == 0;
	}

	@Override
	public String toString() {
		
		String res = "";
		
//		if (data != null) {
//			
//			res = "[" + data[0];
//			
//			for (int i = 1; i < data.length; i++) 
//				res += "," + data[i];
//			
//			res += "]";
//		}
		
		
		if (data != null) 
			res += "{"+CvoaUtilities.getInstance().binaryToDecimal(this.data);
		else
			res+= "VOID";
		
		res += "--" + (CvoaUtilities.getInstance()).getDecimalFormat().format(this.fitness)+"}";
		
//		res += ", " + (CvoaUtilities.getInstance()).getDecimalFormat().format(this.fitness)+" , NI = "+this.nInfected+" NS = "+this.nSuperSpreaderInfected;
		
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
