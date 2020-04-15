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

public class CVOAMain {

	public static void main(String[] args) {
		
		CVOA cvoa = new CVOA(10, 10); // First parameter: number of bits. Second parameter: number of iterations
		Individual solution;
		long time;
		
		time = System.currentTimeMillis();
		solution = cvoa.run();
		time = System.currentTimeMillis() - time;
		
		System.out.println("Best solution: " + solution);
		System.out.println("Best fitness: " + CVOA.fitness(solution));
		System.out.println("Execution time: " + CVOA.DF.format(((double)time)/60000) + " mins");

	}

}
