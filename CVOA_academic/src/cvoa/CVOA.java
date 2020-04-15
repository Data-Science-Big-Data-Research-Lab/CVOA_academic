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

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class CVOA {

	protected List<Individual> infected, recovered, deaths;
	protected Individual bestSolution;
	protected int size, pandemic_duration;
	protected long seed;
	protected Random rnd;
	
	public static final DecimalFormat DF = new DecimalFormat("#.##");
	
	public final static int MIN_SPREAD = 0, MAX_SPREAD = 5;
	public final static int MIN_SUPERSPREAD = 6, MAX_SUPERSPREAD = 15;
	public final static double P_TRAVEL = 0.1, P_REINFECTION = 0.14, P_ISOLATION = 0.5;
	public final static double SUPERSPREADER_PERC = 0.1, DEATH_PERC = 0.05;
	
	
	public CVOA (int size, int max_time) {
		infected = new LinkedList<>();
		recovered = new LinkedList<>();
		deaths = new LinkedList<>();
		this.size = size;
		seed = System.currentTimeMillis();
		rnd = new Random(seed);
		this.pandemic_duration = max_time;
	}
	
	public Individual run () {
		Individual pz; // Patient zero, first infected individual
		boolean pandemic = true;  // Stop criterion
		int time = 0;
				
		// Step 1. Infect PZ //
		pz = infectPZ();
                infected.add(pz);
		bestSolution = new Individual(Arrays.copyOf(pz.getData(), size));
		System.out.println("Patient Zero: " + pz + "\n");
		
		// Step 2. The main loop for the disease propagation //
		while (pandemic && time < pandemic_duration) {
			propagateDisease();
			System.out.println("Iteration " + time + "\nBest fitness so far: " + fitness(bestSolution));
			System.out.println("Infected: " + infected.size() + "; Recovered: " + recovered.size() + "; Deaths: " + deaths.size());
			System.out.println("Recovered/Infected: " + DF.format(100 * (((double)recovered.size())/infected.size())) + "%\n");
			if (infected.isEmpty())
				pandemic = false;
			time++;
		}		
		System.out.println("Converged after " + time + " iterations.");
		
		return bestSolution;		
	}
	
	protected void propagateDisease() {
		int i, j, super_spreader, death, ninfected, travel_distance;
		boolean traveler;
		Individual new_infected;
		List<Individual> new_infected_list = new LinkedList<>();
		
		// Step 1. Assess fitness for each individual.
		for (Individual x : infected) {
			x.setFitness(fitness(x));
		}
		
		// Step 2. Sort the infected list by fitness (ascendent).
		Collections.sort(infected);
		
		// Step 3. Update best global solution, if proceed.
		if (infected.get(0).getFitness() < fitness(bestSolution))
			bestSolution = infected.get(0);
		
		// Step 4. Assess indexes to point super-spreaders and deaths parts of the infected list.
		super_spreader = infected.size() == 1 ? 1 : (int)(SUPERSPREADER_PERC * infected.size());
		death = infected.size() == 1 ? Integer.MAX_VALUE : infected.size() - (int)(DEATH_PERC * infected.size());
		
		// Step 5. Disease propagation.
		i = 0;
		for (Individual x : infected) {
			// Step 5.1 If the individual belongs to the death part, then die!
			if (i >= death) {
				deaths.add(x);
			} else {
				// Step 5.2 Determine the number of new infected individuals.
				if (i < super_spreader) { // Super-spreader case
					ninfected = MIN_SUPERSPREAD + rnd.nextInt(MAX_SUPERSPREAD - MIN_SUPERSPREAD + 1);
				} else {
					ninfected = rnd.nextInt(MAX_SPREAD + 1);
				} 
				
				// Step 5.3 Determine whether the individual has traveled
				traveler = rnd.nextDouble() < P_TRAVEL;
				
				// Step 5.4 Determine the travel distance: how far is the new infected individual?
				if (traveler)
					travel_distance = rnd.nextInt(size+1);
				else
					travel_distance = 1;
				
				// Step 5.5 Infect!
				for (j = 0; j < ninfected; j++) {
					new_infected = infect(x, travel_distance);
					if (!deaths.contains(new_infected) && !recovered.contains(new_infected) && 
							!new_infected_list.contains(new_infected) && !infected.contains(new_infected))
						new_infected_list.add(new_infected);
					else if (recovered.contains(new_infected) && !new_infected_list.contains(new_infected)) {
						if (rnd.nextDouble() < P_REINFECTION) {
							new_infected_list.add(new_infected);
							recovered.remove(new_infected);
						}
					}
				}
			}
			i++;
		}
		
		// Step 6. Add the current infected individuals to the recovered list.
		recovered.addAll(infected);

		// Step 7. Update the infected list with the new infected individuals.		
		infected = new_infected_list;		
		
	}
	
	protected Individual infect (Individual individual, int travel_distance) {
		List<Integer> replicate = new LinkedList<>();
		int [] res = Arrays.copyOf(individual.getData(), size);
		int i = 0, pos;
		
		while (i < travel_distance) {
			pos = rnd.nextInt(size);
			if (!replicate.contains(pos)) {
				res[pos] = res[pos] == 0 ? 1: 0;
				replicate.add(pos);
				i++;
			}			
		}
		
		return new Individual(res);		
	}
	
	public static double fitness (Individual individual) {
		return Math.pow(binaryToDecimal(individual) - 15, 2);
	}
	
	public static int binaryToDecimal (Individual binary) {
		
		int i, res = 0;
		int [] data = binary.getData(); 
		
		for (i = 0; i < data.length; i++)
			res += data[i] * Math.pow(2, i);
		
		return res;
		
	}
	
	protected Individual infectPZ () {		
		int [] res = new int[size];
		int i;
		
		for (i = 0; i < size; i++) {
			res[i] = rnd.nextInt(2);
		}
		
		return new Individual(res);
	}
	
	public static void printVector (int [] v) {
		int i;
		
		System.out.print("[" + v[0]);
		for (i = 1; i < v.length; i++)
			System.out.print("," + v[i]);
		System.out.println("]");
	}
}

