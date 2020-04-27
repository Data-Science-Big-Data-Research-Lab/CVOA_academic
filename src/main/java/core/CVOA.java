package core;

/**
 *
 * @author Data Science & Big Data Lab, Pablo de Olavide University
 *
 * Parallel Coronavirus Optimization Algorithm
 * 
 * Version 3.0 Academic version for a binary codification
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
    private int SOCIAL_DISTANCING = 9; // Iterations without social distancing
    private double P_ISOLATION = 0.75; 
    private double P_TRAVEL = 0.1;
    private double P_REINFECTION = 0.0014;
    private double SUPERSPREADER_PERC = 0.1;
    private double DEATH_PERC = 0.15;

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
    public CVOA(int size, int max_time, String id, int seed, 
    		int maxSpread, int minSuperSpread, int maxSuperSpread,  int socialDistancing,
    		double pIsolation, double pTravel, double pInfection, 
    		double superSpreaderPerc, double deathPerc) {

        initializeCommon(size, max_time, id, seed);

        this.MAX_SPREAD = maxSpread;
        this.MIN_SUPERSPREAD = minSuperSpread;
        this.MAX_SUPERSPREAD = maxSuperSpread;
        this.P_TRAVEL = pTravel;
        this.P_REINFECTION = pInfection;
        this.SUPERSPREADER_PERC = superSpreaderPerc;
        this.DEATH_PERC = deathPerc;
        this.P_ISOLATION = pIsolation;
        this.SOCIAL_DISTANCING = socialDistancing;
    }

    public CVOA(int size, int max_time, String id, int seed) {
        initializeCommon(size, max_time, id, seed);
    }

    private void initializeCommon(int nBits, int max_time, String id, int seed) {

        this.nBits = nBits;
        this.max_time = max_time;
        this.strainID = id;
        
        this.rnd = new Random(System.currentTimeMillis() + seed);
        
        this.infectedStrain = new HashSet<Individual>();
        this.superSpreaderStrain = new HashSet<Individual>();
        this.deathStrain = new HashSet<Individual>();

    }

    public static void initializePandemic(Individual initialIndividual, FitnessFunction function) {
        bestSolution = initialIndividual;
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
        worstSuperSpreaderIndividualStrain = Utilities.getInstance().getExtremeIndividual(true);

        bestDeadIndividualStrain = Utilities.getInstance().getExtremeIndividual(false);

        System.out.println("Patient Zero (" + strainID + "): " + pz);

        // Step 3. The main loop for the disease propagation
        time = 0;

        // Suggestion: add another stop criterion if bestSolution does not change in X consecutive iterations
        while (epidemic && time < max_time && !bestSolutionFound ) {

            propagateDisease();

            if (infectedStrain.isEmpty()) {
                epidemic = false;
            }

            // Stop if best known fitness is found (or fitness satisfying your requirements)
            if (bestSolutionStrain.getFitness() == 0.0) {
                bestSolutionFound=true;
                bestSolution.setDiscoveringIteration(time);
                System.out.println("Best solution (by fitness) found by "+ strainID);
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
        // Number of new infected individuals by each infected one
        int nInfected = 0;

        // This condition replaces the instruction "Collections.sort(infected)" from 
        // the academic version 2.0 with the aim of reducing the execution time
        updateDeathSuperspreadersStrain ();
        
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
            int travel_distance = 1;
            if (rnd.nextDouble() < P_TRAVEL) {
                travel_distance = rnd.nextInt(nBits + 1);
            }

            // 3. Every individual infects as many times as indicated by ninfected
            for (int j = 0; j < nInfected; j++) {
                Individual newInfectedIndividual; // = infect(x, travel_distance);

                // Propagate with no social distancing measures
                if (time < SOCIAL_DISTANCING) {
                    newInfectedIndividual = infect(x, travel_distance);
                    updateNewInfectedPopulation(newInfectedPopulation,newInfectedIndividual);
                }  
                else {// After SOCIAL_DISTANCING iterations, there is a P_ISOLATION of not being infected
                    newInfectedIndividual = infect(x, 1);
                    if (rnd.nextDouble() > P_ISOLATION) {
                        updateNewInfectedPopulation(newInfectedPopulation,newInfectedIndividual);
                        
                    } else {
                        updateIsolatedPopulation(newInfectedIndividual);
                        
                    }
                }
            }
        }

        // Just one println to ensure it is printed without interfering with other threads
        System.out.print("\n[" + strainID + "] - Iteration #" + (time+1)+"\n\tBest global fitness = " + bestSolution+"\n\tBest strain fitness = " + bestSolutionStrain+"\n\t#NewInfected = " + newInfectedPopulation.size()+
        		"\n\tR0 = " + (Utilities.getInstance()).getDecimalFormat("#.##").format((double) newInfectedPopulation.size() / infectedStrain.size()) + "\n");
        
        // Update infected populations for the next iteration
        infectedStrain.clear();
        infectedStrain.addAll(newInfectedPopulation);

    }

    // This method could be improved if a wiser selection of PZ is done
    // It could be selected orthogonal PZs or PZs with high Hamming distance
    // Another strategy can consist in creating PZs evenly spaced (and their complement to 1 values)
    private Individual infectPZ() {
    
        Individual pz = Utilities.getInstance().randomInfection(nBits, fitnessFunction);
             
        return pz;
    }

    // Infect a new individual by mutating as many bits as indicated by travel_distance
    private Individual infect(Individual individual, int travel_distance) {
        List<Integer> mutatedPositions = new LinkedList<Integer>();
        int[] newData = Arrays.copyOf(individual.getData(), nBits);
        int i = 0, pos;

        while (i < travel_distance) {
            pos = rnd.nextInt(nBits);
            if (!mutatedPositions.contains(pos)) {
                newData[pos] = newData[pos] == 0 ? 1 : 0;
                mutatedPositions.add(pos);
                i++;
            }
        }

        return Utilities.getInstance().buildIndividual(newData, fitnessFunction);
    }

    // Supporting methods

    
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
        int numberOfSuperSpreaders; 
        int numberOfDeaths;
        
       // Individual aux = Utilities.getInstance().buildIndividual(bestSolution.getData(), fitnessFunction);;
       // infectedStrain.add(aux);
        
        numberOfSuperSpreaders = (int) Math.ceil(SUPERSPREADER_PERC * infectedStrain.size());
        numberOfDeaths = (int) Math.ceil(DEATH_PERC * infectedStrain.size());
        
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
