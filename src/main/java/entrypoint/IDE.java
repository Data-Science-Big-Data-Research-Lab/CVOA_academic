package entrypoint;

/**
 *
 * @author Data Science & Big Data Lab, Pablo de Olavide University
 *
 * Parallel Coronavirus Optimization Algorithm
 * 
 * Version 4.0 Academic version for a binary codification
 *
 * April 2020
 *
 */

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import core.CVOA;
import core.Individual;
import core.Utilities;
import fitness.FitnessFunction;
import java.util.Arrays;

public class IDE {

    public static final String FITNESS_FUNCTION = "fitness.F1";
    public static final int NSTRAINS = 4; // Don't forget to update this value when varying the number of strains
    public static final int [] SEEDS = {200, 5000, 10000, 1, 33000, 40, 7000000, 232323232};
    public static final int BITS = 30;
    public static final int ITERATIONS = 20;
   
    public static void main(String[] args) throws InterruptedException {
        
        int i = 1;
        
        try {
            CVOA.initializePandemic(Individual.getExtremeIndividual(false), (FitnessFunction) Class.forName(FITNESS_FUNCTION).newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e1) {
            e1.printStackTrace();
        }

        ExecutorService pool = Executors.newFixedThreadPool(NSTRAINS);

        // Sample execution with 4 threads(CVOA running in parallel with 4 strains)
        // Use only 1 thread if you don't want to have more than one strain
        // Strain 1 and 2 are COVID-19 parametrization, strain 3 may be used for simulating any other pandemic
        Collection<CVOA> concurrentCVOAs = new LinkedList<>();
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #1", SEEDS[0], NSTRAINS));
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #2", SEEDS[1], NSTRAINS));
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #3", SEEDS[2], NSTRAINS, 3, 4, 20, 8, 0.8, 0.2, 0.5, 0.05, 0.15));
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #4", SEEDS[3], NSTRAINS));
        //concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #5", SEEDS[4], NSTRAINS));
        //concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #6", SEEDS[5], NSTRAINS, 3, 4, 20, 8, 0.8, 0.2, 0.5, 0.03, 0.10));
        //concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #7", SEEDS[6], NSTRAINS));
        //concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #8", SEEDS[7], NSTRAINS));

  
        List<Future<Individual>> results = new LinkedList<Future<Individual>>();

        long time = System.currentTimeMillis();

        try {
            results = pool.invokeAll(concurrentCVOAs);
            
            pool.shutdown();

            System.out.println("\n************** BEST RESULTS BY STRAIN **************");
            for (Future<Individual> r : results) {
                System.out.println("[Strain #" + i + "] Best solution = " + r.get());
                i++;
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        time = System.currentTimeMillis() - time;

        System.out.println("\n************** BEST RESULT **************");
        System.out.println("Best individual: " + Arrays.toString(CVOA.bestSolution.getData()));
        System.out.println("Best fitness: " + CVOA.bestSolution.getFitness());
        
        System.out.println("\n************** PERFORMANCE **************");
        System.out.println("Execution time: " + (Utilities.getInstance()).getDecimalFormat("#.##").format(((double) time) / 60000) + " mins");
        System.out.println("\nTotal space explored = " + (Utilities.getInstance()).getDecimalFormat("#.####").format((double)100*(CVOA.deaths.size() + CVOA.recovered.size()) / Math.pow(2, BITS))+"%");
        System.out.println("\tRecovered: " + CVOA.recovered.size());
        System.out.println("\tDeaths: " + CVOA.deaths.size());
        //System.out.println("\tIsolated: " + CVOA.isolated.size());
        System.out.println("\tSearch space : " + (long)Math.pow(2, BITS));
    }

}
