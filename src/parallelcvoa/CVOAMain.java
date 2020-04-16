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
    public static final int BITS = 20;
    public static final int ITERATIONS = 12;
    public static void main(String[] args) throws InterruptedException {

        long time;

        CVOA.bestSolution = new Individual();

        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);

        // Sample execution with 3 threads, or CVOA running in parallel
        // Use only 1 thread if you don't want to have more than one strain
        // Strain 1 and 2 are COVID-19 parametrization, strain 3 may be used for simulating any other pandemic
        Collection<CVOA> concurrentCVOAs = new LinkedList<>();
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #1", SEED1));
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #2", SEED2));
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #3", SEED3, 0, 3, 4, 20, 0.2, 0.5, 0.03, 0.10, 0.8, 6));
        //concurrentCVOAs.add(new CVOA(40, 16, "Strain #4", SEED4));
        //concurrentCVOAs.add(new CVOA(20, 15, "Strain #5", SEED5));
        //concurrentCVOAs.add(new CVOA(20, 15, "Strain #6", SEED6));
        //concurrentCVOAs.add(new CVOA(20, 15, "Strain #7", SEED7));
        //concurrentCVOAs.add(new CVOA(20, 15, "Strain #8", SEED8));

        List<Future<Individual>> results = new LinkedList<Future<Individual>>();

        time = System.currentTimeMillis();

        try {
            results = pool.invokeAll(concurrentCVOAs);
            int i = 1;

            System.out.println("\n************** BEST RESULTS BY STRAIN **************");
            for (Future<Individual> r : results) {
                System.out.println("Best solution for strain #" + i + ":" + r.get());
                System.out.println("Best fitness for strain #" + i + ": " + r.get().getFitness() + "\n");
                i++;
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
        System.out.println("\nTotal space explored: " + (CVOA.deaths.size()+CVOA.recovered.size())/Math.pow(2,BITS));
    }

}
