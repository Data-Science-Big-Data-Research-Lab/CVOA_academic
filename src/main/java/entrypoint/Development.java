package entrypoint;

/**
 *
 * @author Data Science & Big Data Lab, Pablo de Olavide University
 *
 * Parallel Coronavirus Optimization Algorithm Version 2.0 Academic version for
 * a binary codification
 *
 * March 2020
 *
 */
import java.util.Collection;
//import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
//import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import core.CVOA;
import core.Individual;
import core.CVOAUtilities;
import fitness.FitnessFunction;
import java.text.DecimalFormat;
import java.util.Arrays;

public class Development {

    public static final String FITNESS_FUNCTION = "fitness.Xminus15";
    public static final int MAX_THREADS = 5;
    public static final int SEED1 = 200;
    public static final int SEED2 = 5000;
    public static final int SEED3 = 10000;
    public static final int BITS = 10;
    public static final int ITERATIONS = 20;
    public static final DecimalFormat DF = new DecimalFormat("#.##");
    
    public static void main(String[] args) throws InterruptedException {
        
        try {
            CVOA.initializePandemic(Individual.getExtremeIndividual(false), (FitnessFunction) Class.forName(FITNESS_FUNCTION).newInstance());
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
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #1", SEED1));
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #2", SEED2));
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #3", SEED3, 3, 4, 20, 6, 0.8, 0.2, 0.5, 0.03, 0.10));
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #4", SEED1));
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #5", SEED2));
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #6", SEED3, 3, 4, 20, 6, 0.8, 0.2, 0.5, 0.03, 0.10));
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #7", SEED1));
        concurrentCVOAs.add(new CVOA(BITS, ITERATIONS, "Strain #8", SEED2));
  
        List<Future<Individual>> results = new LinkedList<Future<Individual>>();

        long time = System.currentTimeMillis();

        try {
            results = pool.invokeAll(concurrentCVOAs);
            int i = 1;

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
        System.out.println("Execution time: " + (CVOAUtilities.getInstance()).getDecimalFormat("#.##").format(((double) time) / 60000) + " mins");
        System.out.println("\nTotal space explored = " + DF.format((double)100*(CVOA.deaths.size() + CVOA.recovered.size()) / Math.pow(2, BITS))+"%");
        System.out.println("\tRecovered: " + CVOA.recovered.size());
        System.out.println("\tDeaths: " + CVOA.deaths.size());
        System.out.println("\tIsolated: " + CVOA.isolated.size());
        System.out.println("\tSearch space : " + (int)Math.pow(2, BITS));
    }

}
