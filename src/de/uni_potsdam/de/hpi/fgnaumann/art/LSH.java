package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.bag.TreeBag;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.de.hpi.fgnaumann.art.permutation.FisherYates;
import de.uni_potsdam.de.hpi.fgnaumann.art.permutation.PermutationGenerator;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.SignatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl.ComparableBitSetSignatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl.NumberListFeatureVector;

public class LSH {

	private static Logger logger = LogManager.getFormatterLogger(LSH.class
			.getName());

	private static final int CORES = Runtime.getRuntime().availableProcessors();
	private static final int NTHREADS = CORES * 2;
	private static final int CHUNK_SIZE_CLASSIFIER_WORKER = 10;

	private static final int NUMBER_OF_RANDOM_VECTORS_d = 100;
	private static final int NUMBER_OF_PERMUTATIONS_q = 10000;
	private static final int WINDOW_SIZE_B = 50;

	private static final int NUMBER_OF_SIMULATION_VECTORS = 998;
	private static final int DIMENSIONS_OF_SIMULATION_VECTORS = 10000;

	private static Random rnd = new Random();

	public static List<Pair<Float, FeatureVector<? extends Number>>> computeNeighbours(
			FeatureVector<?> searchVector, Set<FeatureVector<?>> inputVectors,
			float maxDistance) {
		inputVectors.add(searchVector);
		// step 2
		logger.trace("starting generation of random vectors");
		Set<FeatureVector<? extends Number>> randomVectors = generateRandomWeightVectors(
				NUMBER_OF_RANDOM_VECTORS_d, searchVector.getDimensionality());
		logger.trace("finished generation of random vectors");

		logger.trace("setting up executor for classification");
		ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);

		logger.trace("starting assignment of workers to executor");
		int chunkSize = 0;
		Set<FeatureVector<? extends Number>> tempSet = new HashSet<FeatureVector<? extends Number>>();
		int chunkCount = 0;
		for (FeatureVector<? extends Number> inputVector : inputVectors) {
			if (!(chunkSize != 0 && chunkSize % CHUNK_SIZE_CLASSIFIER_WORKER == 0)) {
				tempSet.add(inputVector);
				chunkSize++;
			} else {
				Runnable worker = new ClassifierWorker(tempSet, randomVectors);
				executor.execute(worker);
				tempSet = new HashSet<FeatureVector<? extends Number>>();
				chunkSize = 0;
				chunkCount++;
				tempSet.add(inputVector);
				chunkSize++;
			}
		}

		if (tempSet.size() > 0) {
			Runnable worker = new ClassifierWorker(tempSet, randomVectors);
			executor.execute(worker);
			chunkCount++;
		}

		logger.trace("finished assignment of %d %s chunks to executor",
				chunkCount, (chunkCount > 1 ? "workers" : "worker"));
		executor.shutdown();
		while (!executor.isTerminated()) {

		}
		logger.trace("all classification workers finished");

		// step 4
		logger.trace("started creation of random permutations");
		Map<int[], TreeBag> randomPermutations = new HashMap<int[], TreeBag>();
		PermutationGenerator permutationGenerator = new FisherYates();
		for (int i = 0; i < NUMBER_OF_PERMUTATIONS_q; i++) {
			randomPermutations.put(permutationGenerator
					.generateRandomPermutation(NUMBER_OF_RANDOM_VECTORS_d),
					new TreeBag());
		}
		logger.trace("finished creation of random permutations");

		logger.trace("started random permutations and sorting with neighbour lookup");
		Map<FeatureVector<? extends Number>, Float> candidates = new HashMap<FeatureVector<? extends Number>, Float>();

		for (int[] randomPermutation : randomPermutations.keySet()) {
			TreeBag sortedPermutationList = randomPermutations
					.get(randomPermutation);

			SignatureVector searchVectorPermutation = searchVector
					.getLocalitySensitiveHashed().permute(randomPermutation);
			sortedPermutationList.add(searchVectorPermutation);

			for (FeatureVector<? extends Number> inputVector : inputVectors) {
				sortedPermutationList.add(inputVector
						.getLocalitySensitiveHashed()
						.permute(randomPermutation));
			}
			SignatureVector[] sortedPermutationArray = new ComparableBitSetSignatureVector[sortedPermutationList
					.size()];
			sortedPermutationList.toArray(sortedPermutationArray);

			// try to optimize gc
			sortedPermutationList = null;

			int searchVectorsSignaturePosition = Arrays.binarySearch(
					sortedPermutationArray, searchVectorPermutation);
			int i = searchVectorsSignaturePosition - WINDOW_SIZE_B / 2;
			i = i < 0 ? i = 0 : i;
			for (; i < searchVectorsSignaturePosition + WINDOW_SIZE_B / 2
					&& i < sortedPermutationArray.length; i++) {
				SignatureVector candidate = sortedPermutationArray[i];
				Float candidatesHammingDistances = candidates.get(candidate);
				if (candidatesHammingDistances != null) {
					break;
				}
				candidatesHammingDistances = searchVectorPermutation
						.computeNormalizedHammingDistance(candidate);
				candidates.put(candidate.getParentVector(),
						candidatesHammingDistances);
			}
		}
		logger.trace("finished random permutations and sorting with neighbour lookup");

		logger.trace("started filtering of neighbours by threshold");
		List<Pair<Float, FeatureVector<? extends Number>>> resultList = new ArrayList<Pair<Float, FeatureVector<? extends Number>>>();
		for (Entry<FeatureVector<? extends Number>, Float> hammingDistances : candidates
				.entrySet()) {
			if (hammingDistances.getValue() <= maxDistance) {
				resultList
						.add(new ImmutablePair<Float, FeatureVector<? extends Number>>(
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

		Set<FeatureVector<? extends Number>> inputVectors = new HashSet<FeatureVector<?>>();

		Integer[] zeroFeatureValues = new Integer[DIMENSIONS_OF_SIMULATION_VECTORS];
		for (int j = 0; j < DIMENSIONS_OF_SIMULATION_VECTORS; j++) {
			zeroFeatureValues[j] = 1;
		}
		FeatureVector<? extends Number> searchVector = new NumberListFeatureVector<Integer>(
				-1, zeroFeatureValues);
		Integer[] closeValueFeatureValues = new Integer[DIMENSIONS_OF_SIMULATION_VECTORS];
		for (int j = 0; j < DIMENSIONS_OF_SIMULATION_VECTORS; j++) {
			closeValueFeatureValues[j] = j % 5 + 1;
		}
		FeatureVector<? extends Number> closeValueFeatureVector = new NumberListFeatureVector<Integer>(
				inputVectors.size(), closeValueFeatureValues);
		inputVectors.add(closeValueFeatureVector);

		for (int i = 0; i < NUMBER_OF_SIMULATION_VECTORS; i++) {
			Integer[] randomFeatureValues = new Integer[DIMENSIONS_OF_SIMULATION_VECTORS];
			for (int j = 0; j < DIMENSIONS_OF_SIMULATION_VECTORS; j++) {
				randomFeatureValues[j] = rnd.nextInt() % 100;
			}
			FeatureVector<? extends Number> randomFeatureVector = new NumberListFeatureVector<Integer>(
					inputVectors.size(), randomFeatureValues);
			inputVectors.add(randomFeatureVector);
		}

		for (Pair<Float, FeatureVector<? extends Number>> match : LSH
				.computeNeighbours(searchVector, inputVectors, 0.2f)) {
			logger.info(match);
		}
	}
}
