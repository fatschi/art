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

public class LSH {
	
	private static Logger logger = LogManager.getFormatterLogger(LSH.class.getName());
	
	private static final int CORES = Runtime.getRuntime().availableProcessors();
	private static final int NTHREADS= CORES*4;
	private static final int CHUNK_SIZE_CLASSIFIER_WORKER = 10;
	private static final int NUMBER_OF_RANDOM_VECTORS_d = 100;
	private static final int NUMBER_OF_PERMUTATIONS_q = 10;
	private static final int WINDOW_SIZE_B = 5;

	private static final int NUMBER_OF_SIMULATION_VECTORS = 10000;
	private static final int DIMENSIONS_OF_SIMULATION_VECTORS = 1000;
	
	private static Random rnd = new Random();
	
	public static List<Pair<Float, FeatureVector>> computeNeighbours(
			FeatureVector searchVector, Set<FeatureVector> inputVectors,
			float maxDistance) {

		inputVectors.add(searchVector);
		
		//step 2
		logger.trace("starting generation of random vectors");
		Set<WeightVector> randomVectors = generateRandomWeightVectors(NUMBER_OF_RANDOM_VECTORS_d, searchVector.getDimensionality());
		logger.trace("finished generation of random vectors");
		
		logger.trace("setting up executor for classification");
		ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
		
		logger.trace("assigning workers to executor");
		int chunkSize = 0;
		Set<FeatureVector> tempSet = new HashSet<FeatureVector>();
		int chunkCount = 0;
		for(FeatureVector inputVector : inputVectors){
			if(!(chunkSize != 0 && chunkSize%CHUNK_SIZE_CLASSIFIER_WORKER==0)){
				tempSet.add(inputVector);
				chunkSize++;
			}else{
				Runnable worker = new ClassifierWorker(tempSet, randomVectors);
			    executor.execute(worker);
				tempSet = new HashSet<FeatureVector>();
				chunkSize = 0;
				chunkCount++;
			}
		}
		Runnable worker = new ClassifierWorker(tempSet, randomVectors);
	    executor.execute(worker);
	    chunkCount++;
	    
	    logger.trace("finished assigning of %,d worker chunks to executor", chunkCount);
	    executor.shutdown();
	    while (!executor.isTerminated()) {

	    }
	    logger.trace("all classification workers finished");
		//step 4
	    logger.trace("started creation of random permutations");
		Map<int[], TreeBag> randomPermutations = new HashMap<int[], TreeBag>();
		PermutationGenerator permutationGenerator = new FisherYates();
		for(int i = 0; i < NUMBER_OF_PERMUTATIONS_q; i++){
			randomPermutations.put(permutationGenerator.generateRandomPermutation(NUMBER_OF_RANDOM_VECTORS_d), new TreeBag());
		}
		logger.trace("finished creation of random permutations");
		
		logger.trace("started random permutations and sorting with neighbour lookup");
		Map<FeatureVector, Float> candidates = new HashMap<FeatureVector, Float>();
		
		for(int[] randomPermutation : randomPermutations.keySet()){
			TreeBag sortedPermutationList = randomPermutations.get(randomPermutation);
			SignatureVector searchVectorPermutation = searchVector.permute(randomPermutation);
			sortedPermutationList.add(searchVectorPermutation);
			for(FeatureVector inputVector : inputVectors){
				sortedPermutationList.add(inputVector.permute(randomPermutation));
			}
			SignatureVector[] sortedPermutationArray = new SignatureVector[sortedPermutationList.size()];
			sortedPermutationList.toArray(sortedPermutationArray);
			//try to optimize gc
			sortedPermutationList = null;
			int searchVectorsSignaturePosition = Arrays.binarySearch(sortedPermutationArray, searchVectorPermutation);
			int i = searchVectorsSignaturePosition-WINDOW_SIZE_B;
			i = i < 0 ? i = 0:i;
			for(;i< searchVectorsSignaturePosition+WINDOW_SIZE_B && i < sortedPermutationArray.length;i++){
				SignatureVector candidate = sortedPermutationArray[i];
				Float candidatesHammingDistances = candidates.get(candidate);
				if(candidatesHammingDistances!=null){
					break;
				}
				candidatesHammingDistances = searchVectorPermutation.computeNormalizedHammingDistance(candidate);
				candidates.put(candidate.getParentVector(), candidatesHammingDistances);
			}
		}
		logger.trace("finished random permutations and sorting with neighbour lookup");
		
		logger.trace("started filtering of neighbours by threshold");
		List<Pair<Float, FeatureVector>> resultList = new ArrayList<Pair<Float,FeatureVector>>();
		for(Entry<FeatureVector, Float> hammingDistances : candidates.entrySet()){
				if (hammingDistances.getValue() <= maxDistance) {
					resultList.add(new ImmutablePair<Float, FeatureVector>(hammingDistances.getValue(), hammingDistances.getKey()));
			}
		}
		logger.trace("finished filtering of neighbours by threshold");
		
		return resultList;
	}

	private static Set<WeightVector> generateRandomWeightVectors(Integer numberOfRandomVectors, Integer dimensionality) {
		Set<WeightVector> randomWeightVectors = new HashSet<WeightVector>();
		// For d random projections.
		for (int di=0; di< numberOfRandomVectors; di++) {
			// Randomly generate a weight vector of random normal mean 0 and variance 1 weights.
			WeightVector dI = new WeightVector(dimensionality);
			for (int ki=0; ki< dimensionality; ki++) {
				dI.setValue(ki, drawNormal());
			}
			randomWeightVectors.add(dI);
		}
		return randomWeightVectors;
	}
	
	/**
	 * Draw from a Gaussian using mean 0 and variance 1.
	 * This is implemented in JDK vi the Box Mueller transform.
	 * @return a gaussian random value.
	 */
	private static float drawNormal() {
		return (float) rnd.nextGaussian();
	}
	
	public static void main(String args[]){
		
		Set<FeatureVector> inputVectors = new HashSet<FeatureVector>();
		
		Integer[] zeroFeatureValues = new Integer[DIMENSIONS_OF_SIMULATION_VECTORS];
		for(int j = 0; j< DIMENSIONS_OF_SIMULATION_VECTORS; j++){
			zeroFeatureValues[j] = 1;
		}
		FeatureVector searchVector = new FeatureVector(zeroFeatureValues);
		
		Integer[] closeValueFeatureValues = new Integer[DIMENSIONS_OF_SIMULATION_VECTORS];
		for(int j = 0; j< DIMENSIONS_OF_SIMULATION_VECTORS; j++){
			closeValueFeatureValues[j] = j%5+1;
		}
		FeatureVector closeValueFeatureVector = new FeatureVector(closeValueFeatureValues);
		inputVectors.add(closeValueFeatureVector);
		
		for(int i = 0; i<NUMBER_OF_SIMULATION_VECTORS;i++){
			Integer[] randomFeatureValues = new Integer[DIMENSIONS_OF_SIMULATION_VECTORS];
			for(int j = 0; j< DIMENSIONS_OF_SIMULATION_VECTORS; j++){
				randomFeatureValues[j] = rnd.nextInt()%100;
			}
			FeatureVector randomFeatureVector = new FeatureVector(randomFeatureValues);
			inputVectors.add(randomFeatureVector);
		}
		
		for(Pair<Float, FeatureVector> match : LSH.computeNeighbours(searchVector, inputVectors, 0.2f)){
			logger.info(match);
		}
	}
}
