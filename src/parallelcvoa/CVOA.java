package parallelcvoa;

/**
 *
 * @author Data Science & Big Data Lab, Pablo de Olavide University
 *
 * Parallel Coronavirus Optimization Algorithm
 * Version 2.5 
 * Academic version for a binary codification
 *
 * March 2020
 *
 */

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

public class CVOA implements Callable<Individual> {

	// Lists shared by all concurrent strains
	protected static volatile List<Individual> recovered, deaths;
	// Best solution shared by all concurrent strains
	public static volatile Individual bestSolution;

	protected Individual bestSolutionStrain;
	protected List<Individual> infected;
	protected int size, max_time; // size stands for number of bits, max_time stands for iterations
	protected int time;
	protected long seed;
	protected Random rnd;
	protected String strainID;
	public static final DecimalFormat DF = new DecimalFormat("#.##");

	// Modify these values to simulate other pandemics
	public int MIN_SPREAD = 0;
	public int MAX_SPREAD = 5;
	public int MIN_SUPERSPREAD = 6;
	public int MAX_SUPERSPREAD = 15;
	public int SOCIAL_DISTANCING = 7; // Iterations without social distancing
	public double P_ISOLATION = 0.5;
	public double P_TRAVEL = 0.1;
	public double P_REINFECTION = 0.001;
	public double SUPERSPREADER_PERC = 0.1;
	public double DEATH_PERC = 0.15;

	public CVOA(int size, int max_time, String id, int seed, int minSpread, int maxSpread, int minSuperSpread,
			int maxSuperSpread, double pTravel, double pInfection, double superSpreaderPerc, double deathPerc,
			double pIsolation, int socialDistancing) {

		initializeCommon(size, max_time, id, seed);

		this.MIN_SPREAD = minSpread;
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

	private void initializeCommon(int size, int max_time, String id, int seed) {
		infected = new LinkedList<Individual>();
		this.size = size;
		this.seed = System.currentTimeMillis() + seed;
		rnd = new Random(seed);
		this.max_time = max_time;
		this.strainID = id;
	}

	public static void initializePandemic(Individual best) {

		bestSolution = best;

		deaths = Collections.synchronizedList(new LinkedList<Individual>());
		// deaths = new LinkedList<Individual>();

		recovered = Collections.synchronizedList(new LinkedList<Individual>());
		// recovered = new LinkedList<Individual>();

	}

	@Override
	public Individual call() throws Exception {
		Individual res = this.run();
		return res;
	}

	public Individual run() {
		Individual pz;
		boolean epidemic = true;

		// Step 1. Infect patient zero (PZ) //
		pz = infectPZ();
		infected.add(pz);
		bestSolutionStrain = new Individual(Arrays.copyOf(pz.getData(), size));
		bestSolutionStrain.setFitness(fitness(bestSolutionStrain));
		System.out.println("Patient Zero (" + strainID + "): " + pz + "\n");

		// Step 2. The main loop for the disease propagation //
		time = 0;
		while (epidemic && time < max_time) {
			propagateDisease();
			// (Un)comment this line to hide/show intermediate information
			System.out.println(strainID + " - Iteration " + time + "\nBest fitness so far: "
					+ fitness(bestSolutionStrain) + "\nInfected: " + infected.size() + "; Recovered: "
					+ recovered.size() + "; Deaths: " + deaths.size() + "\n");
			if (infected.isEmpty()) {
				epidemic = false;
			}
			time++;
		}
		System.out.println(strainID + " converged after " + time + " iterations.");

		return bestSolutionStrain;
	}

	protected void propagateDisease() {
		int i, j, idx_super_spreader, idx_deaths, ninfected, travel_distance;
		boolean traveler;
		Individual new_infected;
		List<Individual> new_infected_list = new LinkedList<>();

		// Step 1. Assess fitness for each individual
		// Step 2. Update best global and local (strain) solutions, if proceed.
		for (Individual x : infected) {
			x.setFitness(fitness(x));
			if (x.getFitness() < bestSolution.getFitness()) {
				bestSolution = x;
				// Uncomment if you do not want to miss bestSolution when cancelling an
				// execution
				// System.out.println("Best solution so far: " + bestSolution);
			}

			if (x.getFitness() < bestSolutionStrain.getFitness()) {
				bestSolutionStrain = x;
			}

		}
		// Step 3. Sort the infected list by fitness (ascendent).
		Collections.sort(infected);

		// Step 4. Assess indexes to point super-spreaders and deaths parts of the
		// infected list.
		idx_super_spreader = infected.size() == 1 ? 1 : (int) Math.ceil(SUPERSPREADER_PERC * infected.size());
		idx_deaths = infected.size() == 1 ? Integer.MAX_VALUE : infected.size() - (int)  Math.ceil(DEATH_PERC * infected.size());

		// Step 5. Disease propagation.
		i = 0;
		for (Individual x : infected) {
			// Step 5.1 If the individual belongs to the death part, then die
			if (i >= idx_deaths) {
				deaths.add(x);
			} else {
				// Step 5.2 Determine the number of new infected individuals.
				if (i < idx_super_spreader) { // This is the super-spreader!
					ninfected = MIN_SUPERSPREAD + rnd.nextInt(MAX_SUPERSPREAD - MIN_SUPERSPREAD + 1);
				} else {
					ninfected = rnd.nextInt(MAX_SPREAD + 1);
				}

				// Step 5.3 Determine whether the individual has traveled
				traveler = rnd.nextDouble() < P_TRAVEL;

				// Step 5.4 Determine the travel distance, which is how far is the new infected
				// individual.
				if (traveler) {
					travel_distance = rnd.nextInt(size + 1);
				} else {
					travel_distance = 1;
				}

				// Step 5.5 Infect
				for (j = 0; j < ninfected; j++) {
					new_infected = infect(x, travel_distance);

					// Propagate with no social distancing measures
					if (time < SOCIAL_DISTANCING) {
						if (!deaths.contains(new_infected) && !recovered.contains(new_infected)
								&& !new_infected_list.contains(new_infected) && !infected.contains(new_infected)) {
							new_infected_list.add(new_infected);
						} else if (recovered.contains(new_infected) && !new_infected_list.contains(new_infected)) {
							if (rnd.nextDouble() < P_REINFECTION) {
								new_infected_list.add(new_infected);
								recovered.remove(new_infected);
							}
						}

					}
					// After SOCIAL_DISTANCING iterations, there is a P_ISOLATION of not being
					// infected
					else {
						if (rnd.nextDouble() > P_ISOLATION) {
							if (!deaths.contains(new_infected) && !recovered.contains(new_infected)
									&& !new_infected_list.contains(new_infected) && !infected.contains(new_infected)) {
								new_infected_list.add(new_infected);
							} else if (recovered.contains(new_infected) && !new_infected_list.contains(new_infected)) {
								if (rnd.nextDouble() < P_REINFECTION) {
									new_infected_list.add(new_infected);
									recovered.remove(new_infected);
								}
							}
						} else { // Those saved by social distancing are sent to the recovered list
							if (!deaths.contains(new_infected) && !recovered.contains(new_infected)
									&& !new_infected_list.contains(new_infected) && !infected.contains(new_infected)) {
								recovered.add(new_infected);
							}
						}
					}
				}
				if (!deaths.contains(x) && !recovered.contains(x))
					recovered.add(x);
			}
			i++;
		}

		// Step 6. Update the infected list with the new infected individuals (shared by
		// all threads).
		infected = new_infected_list;

	}

	// Infect a new individual by mutating as many bits as indicated by
	// travel_distance
	protected Individual infect(Individual individual, int travel_distance) {
		List<Integer> mutated = new LinkedList<Integer>();
		int[] res = Arrays.copyOf(individual.getData(), size);
		int i = 0, pos;

		while (i < travel_distance) {
			pos = rnd.nextInt(size);
			if (!mutated.contains(pos)) {
				res[pos] = res[pos] == 0 ? 1 : 0;
				mutated.add(pos);
				i++;
			}
		}

		return new Individual(res);
	}

	// Optimal reached at x = 15 (In binary: 11110000...)
	public static double fitness(Individual individual) {
		return Math.pow(binaryToDecimal(individual) - 15, 2);
	}

	public static int binaryToDecimal(Individual binary) {

		int i, res = 0;
		int[] data = binary.getData();

		if (data != null) {
			for (i = 0; i < data.length; i++) {
				res += data[i] * Math.pow(2, i);
			}
		}
		return res;
	}

	// This method could be improved if a wiser selection of PZ is done
	// It could be selected orthogonal PZs or PZs with high Hamming distance
	protected Individual infectPZ() {
		Individual PZ = new Individual();
		int[] res = new int[size];
		int i;
		int aux;

		for (i = 0; i < size; i++) {
			aux = rnd.nextInt(2);
			res[i] = aux;
		}
		PZ.setData(res);
		PZ.setFitness(fitness(PZ));
		return PZ;
	}
}
