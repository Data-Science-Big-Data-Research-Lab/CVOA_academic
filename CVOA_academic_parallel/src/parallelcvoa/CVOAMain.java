package parallelcvoa;

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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CVOAMain {

    public static final int MAX_THREADS = 16;
    public static final int SEED1 = 200;
    public static final int SEED2 = 5000;
    public static final int SEED3 = 10000;

    public static void main(String[] args) throws InterruptedException {

        long time;
        double bestFitness = Double.MAX_VALUE;
        Individual bestSolution = null;
        
        CVOA.bestSolution = new Individual();

        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);

        // Sample execution with 3 threads, or CVOA running in parallel
        // Strain 1 and 2 are COVID-19 parametrization, strain 3 may be used for simulating any other pandemic
        Collection<CVOA> concurrentCVOAs = new LinkedList<>();
        concurrentCVOAs.add(new CVOA(30, 10, "Strain #1", SEED1));
        concurrentCVOAs.add(new CVOA(30, 10, "Strain #2", SEED2));
        concurrentCVOAs.add(new CVOA(30, 10, "Strain #3", SEED3, 0, 3, 4, 20, 0.2, 0.5, 0.03, 0.10, 0.8));
        //concurrentCVOAs.add(new CVOA(20, 15, "Strain #4", SEED2));
        //concurrentCVOAs.add(new CVOA(20, 15, "Strain #5", SEED1));
        //concurrentCVOAs.add(new CVOA(20, 15, "Strain #6", SEED2));
        //concurrentCVOAs.add(new CVOA(20, 15, "Strain #7", SEED1));
        //concurrentCVOAs.add(new CVOA(20, 15, "Strain #8", SEED2));
        
        
        List<Future<Individual>> results = new LinkedList<Future<Individual>>();

        time = System.currentTimeMillis();
        
        try {
            results = pool.invokeAll(concurrentCVOAs);
            int i = 1;

            System.out.println("\n************** BEST RESULTS BY STRAIN **************");
            for (Future<Individual> r : results) {
                Individual auxIndividual = r.get();
                //double auxFitness = CVOA.fitness(auxIndividual);
                double auxFitness = auxIndividual.getFitness();
                
                System.out.println("Solution strain #" + i + ":" + auxIndividual);
                System.out.println("Best fitness for strain #" + i + ": " + auxFitness + "\n");
                i++;

                // Search for the best solution
                if (auxFitness < bestFitness) {
                    bestFitness = auxFitness;
                    bestSolution = auxIndividual;
                }
                
                
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        pool.shutdown();

        time = System.currentTimeMillis() - time;

        System.out.println("\n************** BEST RESULT **************");
        System.out.println("Best soltution: " + CVOA.bestSolution);
        System.out.println("Best fitness: " + CVOA.bestSolution.getFitness());
        System.out.println("\nExecution time: " + CVOA.DF.format(((double) time) / 60000) + " mins");
    }

}
