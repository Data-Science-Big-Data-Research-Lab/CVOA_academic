package core;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import fitness.FitnessFunction;

public class CVOA implements Callable<Individual> {

    /**
     * Common resources for multi-threading execution.
     */
    // Lists shared by all concurrent strains
    public static volatile Set<Individual> recovered, deaths, isolated;
    // Best solution shared by all concurrent strains
    public static volatile Individual bestSolution;
    public static volatile boolean bestSolutionFound;

    /**
     * Common experiment properties.
     */
    private static FitnessFunction fitnessFunction;
    // Make sure your fitness values fit in the Individual attributes (64 bits for long types)
    private int nBits;
    private int nStrains;

    /**
     * Specific properties for each strain.
     */
    // Inputs
    private int max_time; // max_time stands for iterations
    private String strainID;

    // Modify these values to simulate other pandemics
    private int MAX_SPREAD = 5;
    private int MIN_SUPERSPREAD = 6;
    private int MAX_SUPERSPREAD = 15;
    private int SOCIAL_DISTANCING = 10; // Iterations without social distancing
    private double P_ISOLATION = 0.7;
    private double P_TRAVEL = 0.1;
    private double P_REINFECTION = 0.0014;
    private double SUPERSPREADER_PERC = 0.1;
    private double DEATH_PERC = 0.05;

    // Auxiliary properties
    private Random rnd;
    private int time;
    private Set<Individual> infectedStrain;
    private Set<Individual> deathStrain;
    private Set<Individual> superSpreaderStrain;
    private Individual bestSolutionStrain;
    private Individual bestDeadIndividualStrain;
    private Individual worstSuperSpreaderIndividualStrain;

    /**
     * Constructors.
     */
    public CVOA(int size, int max_time, String strainID, int seed, int nStrains,
            int maxSpread, int minSuperSpread, int maxSuperSpread, int socialDistancing,
            double pIsolation, double pTravel, double pInfection,
            double superSpreaderPerc, double deathPerc) {

        initializeCommon(size, max_time, strainID, seed, nStrains);

        this.MAX_SPREAD = maxSpread;
        this.MIN_SUPERSPREAD = minSuperSpread;
        this.MAX_SUPERSPREAD = maxSuperSpread;
        this.P_TRAVEL = pTravel;
        this.P_REINFECTION = pInfection;
        this.SUPERSPREADER_PERC = superSpreaderPerc;
        this.DEATH_PERC = deathPerc;
        this.P_ISOLATION = pIsolation;
        this.SOCIAL_DISTANCING = socialDistancing;
        this.nStrains = nStrains;
    }

    public CVOA(int size, int max_time, String strainID, int seed, int nStrains) {
        initializeCommon(size, max_time, strainID, seed, nStrains);
    }

    private void initializeCommon(int nBits, int max_time, String strainID, int seed, int nStrains) {

        this.nBits = nBits;
        this.max_time = max_time;
        this.strainID = strainID;

        this.rnd = new Random(System.currentTimeMillis() + seed);

        this.infectedStrain = new HashSet<Individual>();
        this.superSpreaderStrain = new HashSet<Individual>();
        this.deathStrain = new HashSet<Individual>();
        this.nStrains = nStrains;

    }

    public static void initializePandemic(Individual best, FitnessFunction function) {
        bestSolution = best;
        fitnessFunction = function;
        deaths = Collections.synchronizedSet(new HashSet<Individual>());
        recovered = Collections.synchronizedSet(new HashSet<Individual>());
        isolated = Collections.synchronizedSet(new HashSet<Individual>());
        bestSolutionFound = false;
    }

    /**
     * Multithreading method.
     */
    @Override
    public Individual call() throws Exception {
        Individual res = this.run();
        return res;
    }

    /**
     * Algorithm operators.
     */
    /**
     * Run CVOA.
     */
    public Individual run() {

        Individual pz;
        boolean epidemic = true;

        // Step 1. Infect patient zero (PZ)
        pz = infectPZ();

        // Step 2. Initialize strain: infected and best solution.
        infectedStrain.add(pz);
        bestSolutionStrain = pz;
        superSpreaderStrain.add(pz);
        System.out.println("\nPatient Zero (" + strainID + "): \n" + Arrays.toString(pz.getData()));
        worstSuperSpreaderIndividualStrain = Individual.getExtremeIndividual(true);
        bestDeadIndividualStrain = Individual.getExtremeIndividual(false);

        // Step 3. The main loop for the disease propagation
        time = 0;

        // Suggestion: add another stop criterion if bestSolution does not change after X consecutive iterations
        while (epidemic && time < max_time && !bestSolutionFound) {

            propagateDisease();

            // Stopping criteria            
            if (infectedStrain.isEmpty()) { // Stop if no new infected individuals
                epidemic = false;
                System.out.println("No new infected individuals in " + strainID);
            } else if (bestSolutionStrain.getFitness() == 0.0) { // Stop if best known fitness is found (or fitness satisfying your requirements)
                bestSolutionFound = true;
                bestSolution.setDiscoveringIteration(time);
                System.out.println("Best solution (by fitness) found by " + strainID);
            }

            time++;
        }

        System.out.println("\n\n" + strainID + " converged after " + time + " iterations.");
        System.out.println("Best individual = " + bestSolutionStrain);

        return bestSolutionStrain;
    }

    private void propagateDisease() {

        // New infected people will be stored here (from infectedStrain)
        Set<Individual> newInfectedPopulation = new HashSet<Individual>();

        // Number of new infected individuals for each current infected one
        int nInfected = 0;
        Individual newInfectedIndividual;
        int travel_distance = 1;

        // This condition replaces the instruction "Collections.sort(infected)" from the previous version with the aim of reducing the execution time
        updateDeathSuperspreadersStrain();

        // Ensure the best solutions by strains are kept in the next iteration
        newInfectedPopulation.add(bestSolutionStrain);

        // Each indvidual infects new ones and add them to newInfectedPopulation 
        for (Individual x : infectedStrain) {
            //Calculation of number of new infected and whether they travel or not
            //1. Determine the number of new individuals depending of SuperSpreader or Common
            if (superSpreaderStrain.contains(x)) {
                nInfected = MIN_SUPERSPREAD + rnd.nextInt(MAX_SUPERSPREAD - MIN_SUPERSPREAD + 1);
            } else {
                nInfected = rnd.nextInt(MAX_SPREAD + 1);
            }

            //2. Determine the travel distance, which is how far is the new infected individual (number of bits mutating)
            if (rnd.nextDouble() < P_TRAVEL) {
                travel_distance = rnd.nextInt(nBits + 1);
                //travel_distance = 1+ rnd.nextInt((int) (nBits*P_TRAVEL));
            }

            // 3. Every individual infects as many times as indicated by nInfected
            for (int j = 0; j < nInfected; j++) {
                // Propagate with no social distancing measures
                if (time < SOCIAL_DISTANCING) {
                    newInfectedIndividual = infect(x, travel_distance);
                    updateNewInfectedPopulation(newInfectedPopulation, newInfectedIndividual);
                } 
                // After SOCIAL_DISTANCING iterations, there is a P_ISOLATION of not being infected
                // travel_distance is set to 1, simulating an individual cannot travel anymore
                else {
                    newInfectedIndividual = infect(x, 1);  
                    if (rnd.nextDouble() > P_ISOLATION) {
                        updateNewInfectedPopulation(newInfectedPopulation, newInfectedIndividual);
                    }
                    // Uncomment if you want to send isolated individuals to its set
                    // This effect is similar to sending them to the deaths set
                    //else {
                    //    updateIsolatedPopulation(newInfectedIndividual);
                    //}
                }
            }
        }

        // Just one println to ensure it is printed without interfering with other threads
        System.out.print("\n[" + strainID + "] - Iteration #" + (time + 1) + "\n\tBest global fitness = " + bestSolution + "\n\tBest strain fitness = " + bestSolutionStrain + "\n\t#NewInfected = " + newInfectedPopulation.size()
                + "\n\tR0 = " + (Utilities.getInstance()).getDecimalFormat("#.##").format((double) newInfectedPopulation.size() / infectedStrain.size()) + "\n");

        // Update infected populations for the next iteration
        infectedStrain.clear();
        infectedStrain.addAll(newInfectedPopulation);

    }

    //Generating PZs evenly spaced
    private Individual infectPZ() {

        Individual pz = null;
        int[] data = new int[nBits];

        long problemSize = (long) Math.pow(2, nBits);
        long threadID = Thread.currentThread().getId() - 10;  // -10 to ignore the pool ID  

        long offset = (long) Math.floor(Math.pow(2, nBits) / nStrains);
        long decimal = 0;

        if (nStrains == 1) {
            decimal = (long) Math.floor(problemSize / 2);
        } else {
            for (int i = 0; i < threadID; i++) {
                decimal += offset;
            }
        }

        decimal += (long) Math.floor(offset / 2);
        // Convert decimal value into binary and add to data[]
        String binary = Long.toBinaryString(decimal);

        for (int i = 0; i < binary.length(); i++) {
            data[i] = Character.getNumericValue(binary.charAt((binary.length() - 1) - i));
        }

        // Zero padding
        if (nBits > binary.length()) {
            for (int i = 0; i < (nBits - binary.length()); i++) {
                data[binary.length() + i] = 0;
            }
        }

        //System.out.println("\nActive threads: "+nStrains);
        //System.out.println("\nOffset: "+Math.floor(Math.pow(2, nBits)/nStrains));
        //System.out.println("Offeset/2: "+(long)Math.floor(Math.pow(2, nBits)/nStrains/2));
        //System.out.println("decimal "+threadID+": "+decimal); 
        pz = buildIndividual(data);

        return pz;

    }

    // Infect a new individual by mutating as many bits as indicated by travel_distance
    private Individual infect(Individual individual, int travel_distance) {
        List<Integer> mutatedPositions = new LinkedList<Integer>();
        int[] newData = Arrays.copyOf(individual.getData(), nBits);
        int i = 0, pos;

        while (i < travel_distance) {
            pos = rnd.nextInt(nBits - 1);
            if (!mutatedPositions.contains(pos)) {
                newData[pos] = newData[pos] == 0 ? 1 : 0;
                mutatedPositions.add(pos);
            }
            i++;
        }

        return buildIndividual(newData);
    }

    // Supporting methods
    // Build an individual given its attributes
    private Individual buildIndividual(int[] data) {

        Individual res = new Individual();
        res.setData(data);
        res.setFitness(fitnessFunction.fitness(res));
        return res;

    }

    // Update auxiliary death set and shared recovered set
    private boolean updateRecoveredDeathStrain(Set<Individual> bag, Individual toInsert, int remaining) {

        boolean dead = false;

        dead = insertIntoSetStrain(bag, toInsert, remaining, 'd');

        if (!dead) {
            if (!deaths.contains(toInsert)) {
                recovered.add(toInsert);
            }
        }

        return dead;

    }

    // Insert the individual in the strain sets (death or super spreader)
    private boolean insertIntoSetStrain(Set<Individual> bag, Individual toInsert, int remaining, char type) {

        boolean r = false;
        Object[] aux = getCompareToValueAndElement(type);
        int compareToValue = ((Integer) aux[0]).intValue();
        Individual border = ((Individual) aux[1]);

        if (remaining > 0) {
            r = bag.add(toInsert);

            if (toInsert.compareTo(border) == compareToValue) {
                updateBorder(toInsert, type);
            }
        } else {

            if (toInsert.compareTo(border) == compareToValue) {
                bag.remove(border);
                r = bag.add(toInsert);
                updateBorder(toInsert, type);
            }
        }

        return r;
    }

    // Return worst super spreader (case 's') or best dead (case 'd') individual
    private Object[] getCompareToValueAndElement(char type) {

        Object[] r = new Object[2];
        if (type == 's') {// Super spreaders bag
            r[0] = new Integer(1); // Used in CompareTo
            r[1] = worstSuperSpreaderIndividualStrain;
        }
        if (type == 'd') {// Deaths strain bag

            r[0] = new Integer(-1); // Used in CompareTo
            r[1] = bestDeadIndividualStrain;
        }

        return r;
    }

    // Determine if we are updating deaths ('d') or super spreaders ('s')
    private void updateBorder(Individual toUpdate, char type) {

        if (type == 's') // Super-spreader strain bag
        {
            worstSuperSpreaderIndividualStrain = toUpdate;
        } else if (type == 'd') // Deaths strain bag
        {
            bestDeadIndividualStrain = toUpdate;
        }
    }

    // Update new infected population and recovered (in case of reinfection)
    private void updateNewInfectedPopulation(Set<Individual> newInfectedPopulation, Individual newInfectedIndividual) {
        if (!deaths.contains(newInfectedIndividual) && !recovered.contains(newInfectedIndividual)) {
            newInfectedPopulation.add(newInfectedIndividual); // Add new infected individual
        } else if (recovered.contains(newInfectedIndividual)) {
            if (rnd.nextDouble() < P_REINFECTION) { // Chance of reinfection
                newInfectedPopulation.add(newInfectedIndividual);
                recovered.remove(newInfectedIndividual);
            }
        }
    }

    // Update isolated population
    private void updateIsolatedPopulation(Individual z) {
        if (!deaths.contains(z) && !recovered.contains(z) && !isolated.contains(z)) {
            isolated.add(z);// People not infected thanks to social isolation measures
        }
    }

    private void updateDeathSuperspreadersStrain() {
        // Super spreader and deaths strain sets for each iteration 
        int numberOfSuperSpreaders = (int) Math.ceil(SUPERSPREADER_PERC * infectedStrain.size());
        int numberOfDeaths = (int) Math.ceil(DEATH_PERC * infectedStrain.size());

        if (infectedStrain.size() != 1) {

            for (Individual individual : infectedStrain) {

                if (insertIntoSetStrain(superSpreaderStrain, individual, numberOfSuperSpreaders, 's')) {
                    numberOfSuperSpreaders--;
                }
                if (updateRecoveredDeathStrain(deathStrain, individual, numberOfDeaths)) {
                    numberOfDeaths--;
                }
                if (bestSolution.compareTo(individual) == 1) {
                    bestSolution = individual;
                    System.out.println("\nNew best solution found by " + strainID + "!\n");
                }
                if (bestSolutionStrain.compareTo(individual) == 1) {
                    bestSolutionStrain = individual;
                }
            }
            deaths.addAll(deathStrain);
            bestSolutionStrain.setDiscoveringIteration(time + 1);
        }
        recovered.removeAll(deaths);
    }

}
