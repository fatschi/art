package de.uni_potsdam.de.hpi.fgnaumann.art.lsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.collections.bag.TreeBag;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.de.hpi.fgnaumann.art.LSHRunnerImpl;
import de.uni_potsdam.de.hpi.fgnaumann.art.permutation.FisherYates;
import de.uni_potsdam.de.hpi.fgnaumann.art.permutation.PermutationGenerator;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.SignatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl.NumberArrayFeatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl.PrimitiveMapFeatureVector;

/**
 * The main algorithm implementing "Randomized Algorithms and NLP: Using
 * Locality Sensitive Hash Functions for High Speed Noun Clustering" by Deepak
 * Ravichandran, Patrick Pantel, and Eduard Hovy.
 * 
 * @author fabian
 * 
 */
public class LSH {

	private static Logger logger = LogManager.getFormatterLogger(LSH.class
			.getName());

	private static Random rnd = new Random();

	public static NavigableSet<Pair<Double, Long>> searchNeighbours(
			FeatureVector<? extends Number> searchVector,
			NavigableSet<FeatureVector<? extends Number>> inputVectors,
			double maxDistance, int topK, int NTHREADS) {
		Map<Long, Double> candidates = new HashMap<Long, Double>();

		if (NTHREADS > 1) {
			ExecutorService pemutationExecutor = Executors
					.newFixedThreadPool(NTHREADS);
			List<Future<Map<Long, Double>>> list = new ArrayList<Future<Map<Long, Double>>>();

			int chunkSize = 0;
			Set<FeatureVector<? extends Number>> tempSet = new HashSet<FeatureVector<? extends Number>>();

			for (FeatureVector<? extends Number> inputVector : inputVectors) {
				if (chunkSize < inputVectors.size() / NTHREADS) {
					tempSet.add(inputVector);
					chunkSize++;
				} else {
					Callable<Map<Long, Double>> worker = new LookupWorker(
							searchVector, inputVectors);
					Future<Map<Long, Double>> submit = pemutationExecutor
							.submit(worker);
					list.add(submit);
					tempSet = new HashSet<FeatureVector<? extends Number>>();
					chunkSize = 0;
					tempSet.add(inputVector);
					chunkSize++;
				}
			}

			if (tempSet.size() > 0) {
				Callable<Map<Long, Double>> worker = new LookupWorker(
						searchVector, inputVectors);
				Future<Map<Long, Double>> submit = pemutationExecutor
						.submit(worker);
				list.add(submit);
			}

			for (Future<Map<Long, Double>> future : list) {
				try {
					candidates.putAll(future.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			pemutationExecutor.shutdown();
		} else {
			TreeBag signatureVectorList = new TreeBag();

			SignatureVector searchVectorSignature = searchVector
					.getLocalitySensitiveHashed();
			signatureVectorList.add(searchVectorSignature);

			for (FeatureVector<? extends Number> inputVector : inputVectors) {
				signatureVectorList.add(inputVector
						.getLocalitySensitiveHashed());
			}

			for (Object candidateO : signatureVectorList) {
				SignatureVector candidate = (SignatureVector) candidateO;
				if (candidate.getParentVectorId() != searchVector.getId()) {
					candidates
							.put(candidate.getParentVectorId(),
									searchVectorSignature
											.computeNormalizedHammingDistance(candidate));
				}
			}
		}

		logger.trace("started filtering of neighbours by threshold");
		NavigableSet<Pair<Double, Long>> resultList = new TreeSet<Pair<Double, Long>>();
		for (Entry<Long, Double> hammingDistances : candidates.entrySet()) {
			if (hammingDistances.getValue() <= maxDistance) {
				resultList.add(new ImmutablePair<Double, Long>(hammingDistances
						.getValue(), hammingDistances.getKey()));
			}
		}
		logger.trace("finished filtering of neighbours by threshold");

		// do topK return
		if (resultList.size() <= topK) {
			return resultList;
		} else {
			NavigableSet<Pair<Double, Long>> resultListTopK = new TreeSet<Pair<Double, Long>>();
			for (int i = 0; i <= topK; i++) {
				resultListTopK.add(resultList.pollFirst());
			}
			return resultListTopK;
		}
	}

	public static NavigableSet<Pair<Double, Long>> searchNeighbours(
			FeatureVector<?> searchVector, Set<FeatureVector<?>> inputVectors,
			double maxDistance, int topK, int NTHREADS,
			int NUMBER_OF_PERMUTATIONS_q, int WINDOW_SIZE_B) {
		FeatureVector<? extends Number> exampleVector;
		Iterator<FeatureVector<? extends Number>> inputVectorsIterator = inputVectors
				.iterator();
		if (inputVectorsIterator.hasNext()) {
			exampleVector = inputVectorsIterator.next();
		} else {
			throw new IllegalStateException(
					"Your input vectors seem to be empty.");
		}
		// step 4 of paper: random permutation of bit vectors and window lookup
		// for hamming distance calculation
		logger.trace("started creation of random permutations");
		Set<int[]> randomPermutations = new HashSet<int[]>();
		PermutationGenerator permutationGenerator = new FisherYates();
		for (int i = 0; i < NUMBER_OF_PERMUTATIONS_q; i++) {
			randomPermutations.add(permutationGenerator
					.generateRandomPermutation(exampleVector
							.getDimensionality()));
		}
		logger.trace("finished creation of random permutations");

		logger.trace("starting assignment of permutation workers to executor");
		Map<Long, Double> candidates = new HashMap<Long, Double>();

		ExecutorService pemutationExecutor = Executors
				.newFixedThreadPool(NTHREADS);
		List<Future<Map<Long, Double>>> list = new ArrayList<Future<Map<Long, Double>>>();

		for (int[] randomPermutation : randomPermutations) {
			Callable<Map<Long, Double>> worker = new PermutationWorker(
					randomPermutation, searchVector, inputVectors,
					WINDOW_SIZE_B);
			Future<Map<Long, Double>> submit = pemutationExecutor
					.submit(worker);
			list.add(submit);
		}
		logger.trace(
				"finished assignment of %d %s permutation workers to executor",
				randomPermutations.size(),
				(randomPermutations.size() > 1 ? "workers" : "worker"));

		for (Future<Map<Long, Double>> future : list) {
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
		NavigableSet<Pair<Double, Long>> resultList = new TreeSet<Pair<Double, Long>>();
		for (Entry<Long, Double> hammingDistances : candidates.entrySet()) {
			if (hammingDistances.getValue() <= maxDistance) {
				resultList.add(new ImmutablePair<Double, Long>(hammingDistances
						.getValue(), hammingDistances.getKey()));
			}
		}
		logger.trace("finished filtering of neighbours by threshold");

		// do topK return
		if (resultList.size() <= topK) {
			return resultList;
		} else {
			NavigableSet<Pair<Double, Long>> resultListTopK = new TreeSet<Pair<Double, Long>>();
			for (int i = 0; i <= topK; i++) {
				resultListTopK.add(resultList.pollFirst());
			}
			return resultListTopK;
		}
	}

	public static NavigableSet<FeatureVector<? extends Number>> computeLSH(
			NavigableSet<FeatureVector<? extends Number>> inputVectors,
			int NTHREADS, int CHUNK_SIZE_CLASSIFIER_WORKER,
			int NUMBER_OF_RANDOM_VECTORS_d) {

		// step 2 of paper: generation of random hyperplanes
		logger.trace("starting generation of random vectors");
		if (inputVectors.first() == null) {
			throw new IllegalArgumentException(
					"Your input vectors seem to be empty.");
		}
		Set<FeatureVector<? extends Number>> randomVectors = generateRandomWeightVectors(
				NUMBER_OF_RANDOM_VECTORS_d, inputVectors.first()
						.getDimensionality());
		logger.trace("finished generation of random vectors");

		// step 3 of paper: classification
		ExecutorService classifierExecutor = Executors
				.newFixedThreadPool(NTHREADS);

		logger.trace(
				"starting assignment of classifier workers to executor with %s threads",
				NTHREADS);
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
		return inputVectors;
	}

	private static Set<FeatureVector<? extends Number>> generateRandomWeightVectors(
			Integer numberOfRandomVectors, Integer dimensionality) {
		Set<FeatureVector<? extends Number>> randomWeightVectors = new HashSet<FeatureVector<? extends Number>>();
		// For d random projections.
		for (long di = 0; di < numberOfRandomVectors; di++) {
			// Randomly generate a weight vector of random normal mean 0 and
			// variance 1 weights.

			FeatureVector<Double> dI = null;
			if (LSHRunnerImpl.vectorImplementationClass
					.equals(PrimitiveMapFeatureVector.class)) {
				dI = new PrimitiveMapFeatureVector<Double>(di, dimensionality);
			} else if (LSHRunnerImpl.vectorImplementationClass
					.equals(NumberArrayFeatureVector.class)) {
				dI = new NumberArrayFeatureVector<Double>((long) di,
						dimensionality);
			}

			for (int ki = 0; ki < dimensionality; ki++) {
				dI.setValue(ki, rnd.nextGaussian());
			}
			randomWeightVectors.add(dI);
		}
		return randomWeightVectors;
	}
}
