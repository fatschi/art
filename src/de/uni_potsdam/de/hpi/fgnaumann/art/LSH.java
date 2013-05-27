package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.de.hpi.fgnaumann.art.permutation.FisherYates;
import de.uni_potsdam.de.hpi.fgnaumann.art.permutation.PermutationGenerator;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl.NumberListFeatureVector;

public class LSH {

	private static Logger logger = LogManager.getFormatterLogger(LSH.class
			.getName());

	private static double SIMILARITY_THRESHOLD = 0.05d;
	private static int CORES = Runtime.getRuntime().availableProcessors();
	private static int NTHREADS = CORES;
	private static int CHUNK_SIZE_CLASSIFIER_WORKER = 100;

	private static int NUMBER_OF_RANDOM_VECTORS_d = 100;
	private static int NUMBER_OF_PERMUTATIONS_q = 100000;
	private static int WINDOW_SIZE_B = 50;

	private static int NUMBER_OF_SIMULATION_VECTORS = 1000;
	private static int NUMBER_OF_SIMULATION_VECTORS_CLOSE = 20;
	private static int DIMENSIONS_OF_SIMULATION_VECTORS = 10000;
	private static int SIMULATION_VECTOR_VALUE_SPACE = 1000;

	private static Random rnd = new Random();

	public static List<Pair<Double, FeatureVector<? extends Number>>> computeNeighbours(
			FeatureVector<?> searchVector, Set<FeatureVector<?>> inputVectors,
			double maxDistance) {
		inputVectors.add(searchVector);
		// step 2
		logger.trace("starting generation of random vectors");
		Set<FeatureVector<? extends Number>> randomVectors = generateRandomWeightVectors(
				NUMBER_OF_RANDOM_VECTORS_d, searchVector.getDimensionality());
		logger.trace("finished generation of random vectors");

		// step 3
		ExecutorService classifierExecutor = Executors
				.newFixedThreadPool(NTHREADS);

		logger.trace("starting assignment of classifier workers to executor");
		int chunkSize = 0;
		Set<FeatureVector<? extends Number>> tempSet = new HashSet<FeatureVector<? extends Number>>();
		int chunkCount = 0;
		for (FeatureVector<? extends Number> inputVector : inputVectors) {
			if (!(chunkSize != 0 && chunkSize % CHUNK_SIZE_CLASSIFIER_WORKER == 0)) {
				tempSet.add(inputVector);
				chunkSize++;
			} else {
				Runnable worker = new ClassifierWorker(tempSet, randomVectors);
				classifierExecutor.execute(worker);
				tempSet = new HashSet<FeatureVector<? extends Number>>();
				chunkSize = 0;
				chunkCount++;
				tempSet.add(inputVector);
				chunkSize++;
			}
		}

		if (tempSet.size() > 0) {
			Runnable worker = new ClassifierWorker(tempSet, randomVectors);
			classifierExecutor.execute(worker);
			chunkCount++;
		}

		logger.trace("finished assignment of %d %s chunks to executor",
				chunkCount, (chunkCount > 1 ? "workers" : "worker"));
		classifierExecutor.shutdown();
		while (!classifierExecutor.isTerminated()) {

		}
		logger.trace("all classification workers finished");

		// step 4
		logger.trace("started creation of random permutations");
		Set<int[]> randomPermutations = new HashSet<int[]>();
		PermutationGenerator permutationGenerator = new FisherYates();
		for (int i = 0; i < NUMBER_OF_PERMUTATIONS_q; i++) {
			randomPermutations.add(permutationGenerator
					.generateRandomPermutation(NUMBER_OF_RANDOM_VECTORS_d));
		}
		logger.trace("finished creation of random permutations");

		logger.trace("starting assignment of permutation workers to executor");
		Map<FeatureVector<? extends Number>, Double> candidates = new HashMap<FeatureVector<? extends Number>, Double>();

		ExecutorService pemutationExecutor = Executors
				.newFixedThreadPool(NTHREADS);
		List<Future<Map<FeatureVector<? extends Number>, Double>>> list = new ArrayList<Future<Map<FeatureVector<? extends Number>, Double>>>();

		for (int[] randomPermutation : randomPermutations) {
			Callable<Map<FeatureVector<? extends Number>, Double>> worker = new PermutationWorker(
					randomPermutation, searchVector, inputVectors,
					WINDOW_SIZE_B);
			Future<Map<FeatureVector<? extends Number>, Double>> submit = pemutationExecutor
					.submit(worker);
			list.add(submit);
		}
		logger.trace(
				"finished assignment of %d %s permutation workers to executor",
				randomPermutations.size(),
				(randomPermutations.size() > 1 ? "workers" : "worker"));

		for (Future<Map<FeatureVector<? extends Number>, Double>> future : list) {
			try {
				candidates.putAll(future.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		pemutationExecutor.shutdown();

		logger.trace("started filtering of neighbours by threshold");
		List<Pair<Double, FeatureVector<? extends Number>>> resultList = new ArrayList<Pair<Double, FeatureVector<? extends Number>>>();
		for (Entry<FeatureVector<? extends Number>, Double> hammingDistances : candidates
				.entrySet()) {
			if (hammingDistances.getValue() <= maxDistance) {
				resultList
						.add(new ImmutablePair<Double, FeatureVector<? extends Number>>(
								hammingDistances.getValue(), hammingDistances
										.getKey()));
			}
		}
		logger.trace("finished filtering of neighbours by threshold");

		return resultList;
	}

	private static Set<FeatureVector<? extends Number>> generateRandomWeightVectors(
			Integer numberOfRandomVectors, Integer dimensionality) {
		Set<FeatureVector<? extends Number>> randomWeightVectors = new HashSet<FeatureVector<? extends Number>>();
		// For d random projections.
		for (int di = 0; di < numberOfRandomVectors; di++) {
			// Randomly generate a weight vector of random normal mean 0 and
			// variance 1 weights.
			FeatureVector<Double> dI = new NumberListFeatureVector<Double>();
			for (int ki = 0; ki < dimensionality; ki++) {
				dI.setValue(ki, rnd.nextGaussian());
			}
			randomWeightVectors.add(dI);
		}
		return randomWeightVectors;
	}

	public static void main(String args[]) {

		// create Options object
		Options options = createOptions();
		// create the parser
		CommandLineParser parser = new PosixParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("LSH", options);
			}
			evaluateCLIParameters(line);
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		}

		Integer[] searchVectorValues = new Integer[DIMENSIONS_OF_SIMULATION_VECTORS];
		for (int j = 0; j < DIMENSIONS_OF_SIMULATION_VECTORS; j++) {
			searchVectorValues[j] = rnd.nextInt(SIMULATION_VECTOR_VALUE_SPACE);
		}
		FeatureVector<? extends Number> searchVector = new NumberListFeatureVector<Integer>(
				-1, searchVectorValues);

		Set<FeatureVector<? extends Number>> inputVectors = generateSimulationVectors(searchVectorValues);
		;

		for (Pair<Double, FeatureVector<? extends Number>> match : LSH
				.computeNeighbours(searchVector, inputVectors,
						SIMILARITY_THRESHOLD)) {
			logger.info(match);
		}
	}

	private static void evaluateCLIParameters(CommandLine line) {
		if (line.hasOption("threshold")) {
			// initialise the member variable
			SIMILARITY_THRESHOLD = Double.parseDouble(line
					.getOptionValue("threshold"));
		}
		// TODO

	}

	@SuppressWarnings("static-access")
	private static Options createOptions() {
		Options cliOptions = new Options();

		Option threshold = OptionBuilder.withArgName("threshold").hasArg()
				.withDescription("the maximum hamming distance")
				.create("threshold");

		Option threads = OptionBuilder.withArgName("threads").hasArg()
				.withDescription("the number of threads to spawn")
				.create("NTHREADS");

		Option chunkSize = OptionBuilder
				.withArgName("chunk")
				.hasArg()
				.withDescription(
						"chunk size of the vectors classified in one thread")
				.create("chunkSize");

		Option d = OptionBuilder.withArgName("d").hasArg()
				.withDescription("number of random vectors").create("d");

		Option q = OptionBuilder.withArgName("q").hasArg()
				.withDescription("number of random permutations").create("q");

		Option B = OptionBuilder.withArgName("B").hasArg()
				.withDescription("window size").create("B");

		Option numberOfSimulationVectors = OptionBuilder
				.withArgName("numberOfSimulationVectors")
				.hasArg()
				.withDescription(
						"number of vectors to be generated for test simulation")
				.create("numberOfSimulationVectors");

		Option numberOfSimulationVectorsClose = OptionBuilder
				.withArgName("numberOfSimulationVectorsClose")
				.hasArg()
				.withDescription(
						"number of vectors to be generated for test simulation that are close to the search vector")
				.create("numberOfSimulationVectorsClose");

		Option dimensionalityOfSimulationVectors = OptionBuilder
				.withArgName("dimensionalityOfSimulationVectors")
				.hasArg()
				.withDescription(
						"the dimensionality of the search/input vector generated in the simulation")
				.create("dimensionalityOfSimulationVectors");
		
		Option help = new Option( "help", "print this message" );

		return cliOptions.addOption(dimensionalityOfSimulationVectors)
				.addOption(numberOfSimulationVectorsClose)
				.addOption(numberOfSimulationVectors).addOption(B).addOption(q)
				.addOption(d).addOption(chunkSize).addOption(threads)
				.addOption(threshold).addOption(help);

	}

	private static Set<FeatureVector<? extends Number>> generateSimulationVectors(
			Integer[] searchVectorValues) {
		Set<FeatureVector<? extends Number>> inputVectors = new HashSet<FeatureVector<? extends Number>>();
		for (int i = 0; i < NUMBER_OF_SIMULATION_VECTORS_CLOSE; i++) {
			Integer[] closeValueFeatureValues = new Integer[DIMENSIONS_OF_SIMULATION_VECTORS];
			for (int j = 0; j < DIMENSIONS_OF_SIMULATION_VECTORS; j++) {
				int variance = (int) (SIMULATION_VECTOR_VALUE_SPACE / 10);
				closeValueFeatureValues[j] = searchVectorValues[j]
						- rnd.nextInt(variance) + rnd.nextInt(variance);

			}
			FeatureVector<? extends Number> closeValueFeatureVector = new NumberListFeatureVector<Integer>(
					inputVectors.size(), closeValueFeatureValues);
			inputVectors.add(closeValueFeatureVector);
		}

		for (int i = 0; i < NUMBER_OF_SIMULATION_VECTORS; i++) {
			Integer[] randomFeatureValues = new Integer[DIMENSIONS_OF_SIMULATION_VECTORS];
			for (int j = 0; j < DIMENSIONS_OF_SIMULATION_VECTORS; j++) {
				randomFeatureValues[j] = rnd
						.nextInt(SIMULATION_VECTOR_VALUE_SPACE);
			}
			FeatureVector<? extends Number> randomFeatureVector = new NumberListFeatureVector<Integer>(
					inputVectors.size(), randomFeatureValues);
			inputVectors.add(randomFeatureVector);
		}
		return inputVectors;
	}
}
