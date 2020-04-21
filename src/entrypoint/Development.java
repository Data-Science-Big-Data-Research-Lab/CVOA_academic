package entrypoint;


/**
 *
 * @author Data Science & Big Data Lab, Pablo de Olavide University
 *
 * Parallel Coronavirus Optimization Algorithm
 * Version 2.0 
 * Academic version for a binary codification
 *
 * March 2020
 *
 */


import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import core.CVOA;
import core.Individual;
import core.CvoaUtilities;
import fitness.FitnessFunction;

public class Development {

	
	public static final String FITNESS_FUNCTION = "fitness.Xminus15";
    public static final int MAX_THREADS = 3;
    public static final int SEED1 = 200;
    public static final int SEED2 = 5000;
    public static final int SEED3 = 10000;
    public static final int BITS = 20;
    public static final int ITERATIONS = 50;
 
    
    public static void main(String[] args) throws InterruptedException {

                      
        try {
			CVOA.initializePandemic(Individual.gerExtremeIndividual(false), (FitnessFunction) Class.forName(FITNESS_FUNCTION).newInstance());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e1) {
			e1.printStackTrace();
		}
        
//        (new CVOA(BITS, ITERATIONS, "Strain #1", SEED1)).run();        
//        System.out.println("\n\nBest solution: " + CVOA.bestSolution);       

        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);
        
        

        // Sample execution with 3 threads, or CVOA running in parallel
        // Use only 1 thread if you don't want to have more than one strain
        // Strain 1 and 2 are COVID-19 parametrization, strain 3 may be used for simulating any other pandemic
        Collection<CVOA> concurrentCVOAs = new LinkedList<>();
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "STR#1", SEED1));
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "STR#2", SEED2));
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "STR#3", SEED3, 0, 3, 4, 20, 0.2, 0.5, 0.03, 0.10, 0.8, 6));


        List<Future<Individual>> results = new LinkedList<Future<Individual>>();

        long time = System.currentTimeMillis();

        try {
            results = pool.invokeAll(concurrentCVOAs);
            int i = 1;

            pool.shutdown();
            
            System.out.println("\n************** BEST RESULTS BY STRAIN **************");
            for (Future<Individual> r : results) {
                System.out.println("[Strain #"+i+ "] Best solution = " + r.get());
                i++;
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        

        time = System.currentTimeMillis() - time;

        System.out.println("\n************** BEST RESULT **************");
        System.out.println("Best soltution: " + CVOA.bestSolution);
        System.out.println("\nExecution time: " +(CvoaUtilities.getInstance()).getDecimalFormat("#.#####").format(((double) time) / 60000) + " mins");
        System.out.println("\nTotal space explored: " + (CVOA.deaths.size()+CVOA.recovered.size())/Math.pow(2,BITS));
              
        Set<Individual> d = new HashSet<Individual>(CVOA.deaths);
        Set<Individual> r = new HashSet<Individual>(CVOA.recovered);
        d.retainAll(r);        
        System.out.println("\n#Common deadth and recoveries = " + d.size());
        
//        System.out.println("\n************** SETS **************");
//        
//        System.out.println("Deaths:\n" +CVOA.deaths);
//                
//        System.out.println("\nRecovered:\n" +CVOA.recovered);
        
        
        
    }

}
