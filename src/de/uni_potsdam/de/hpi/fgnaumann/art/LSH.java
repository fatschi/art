package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import de.uni_potsdam.de.hpi.fgnaumann.art.permutation.FisherYates;
import de.uni_potsdam.de.hpi.fgnaumann.art.permutation.PermutationGenerator;

public class LSH {
	
	private static final int NUMBER_OF_RANDOM_VECTORS_d = 100;
	private static final int NUMBER_OF_PERMUTATIONS_q = 5;
	private static final int WINDOW_SIZE_B = 5;
	private static Random rnd = new Random();
	
	public static List<Pair<Float, FeatureVector>> computeNeighbours(
			FeatureVector searchVector, Set<FeatureVector> inputVectors,
			float maxDistance) {
		
		//step 2
		Set<WeightVector> randomVectors = generateRandomWeightVectors(NUMBER_OF_RANDOM_VECTORS_d, searchVector.getDimensionality());
		
		//step 3
		searchVector.createLSH(randomVectors);
		for(FeatureVector inputVector : inputVectors){
			inputVector.createLSH(randomVectors);
		}
		
		//step 4
		Map<int[], List<SignatureVector>> randomPermutations = new HashMap<int[], List<SignatureVector>>();
		PermutationGenerator permutationGenerator = new FisherYates();
		for(int i = 0; i < NUMBER_OF_PERMUTATIONS_q; i++){
			randomPermutations.put(permutationGenerator.generateRandomPermutation(NUMBER_OF_RANDOM_VECTORS_d), new ArrayList<SignatureVector>());
		}
		
		Map<FeatureVector, Float> candidates = new HashMap<FeatureVector, Float>();
		
		for(int[] randomPermutation : randomPermutations.keySet()){
			List<SignatureVector> sortedPermutationList = randomPermutations.get(randomPermutation);
			SignatureVector searchVectorPermutation = searchVector.permute(randomPermutation);
			sortedPermutationList.add(searchVectorPermutation);
			for(FeatureVector inputVector : inputVectors){
				sortedPermutationList.add(inputVector.permute(randomPermutation));
			}
			Collections.sort(sortedPermutationList);
			Integer searchVectorsSignaturePosition = sortedPermutationList.indexOf(searchVectorPermutation);
			SignatureVector searchVectorsSignature = sortedPermutationList.get(searchVectorsSignaturePosition);
			int i = searchVectorsSignaturePosition-WINDOW_SIZE_B;
			i = i < 0 ? i = 0:i;
			for(;i< searchVectorsSignaturePosition+WINDOW_SIZE_B && i < sortedPermutationList.size();i++){
				SignatureVector candidate = sortedPermutationList.get(i);
				Float candidatesHammingDistances = candidates.get(candidate);
				if(candidatesHammingDistances!=null){
					break;
				}
				candidatesHammingDistances = searchVectorsSignature.computeNormalizedHammingDistance(candidate);
				candidates.put(candidate.getParentVector(), candidatesHammingDistances);
			}
		}
		
		List<Pair<Float, FeatureVector>> resultList = new ArrayList<Pair<Float,FeatureVector>>();
		for(Entry<FeatureVector, Float> hammingDistances : candidates.entrySet()){
				if (hammingDistances.getValue() <= maxDistance) {
					resultList.add(new ImmutablePair<Float, FeatureVector>(hammingDistances.getValue(), hammingDistances.getKey()));
			}
		} 
		
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
		/*FeatureVector searchVector = new FeatureVector(1,2,3,4,5,6,7,8,9,10,11,12,13,14, 15);
		FeatureVector inputVector1 = new FeatureVector(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14);
		FeatureVector inputVector2  = new FeatureVector(-1,-2,-3,-4,-5,-6,-7,-8,-9,-10,-11,-12,-13,-14,-15);
		FeatureVector inputVector3  = new FeatureVector(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
		inputVectors.add(inputVector1);
		inputVectors.add(inputVector2);
		inputVectors.add(inputVector3);*/
		
		final int randomFeatureVectorSize = 1000;
		
		Integer[] zeroFeatureValues = new Integer[randomFeatureVectorSize];
		for(int j = 0; j< randomFeatureVectorSize; j++){
			zeroFeatureValues[j] = 1;
		}
		FeatureVector searchVector = new FeatureVector(zeroFeatureValues);
		
		Integer[] closeValueFeatureValues = new Integer[randomFeatureVectorSize];
		for(int j = 0; j< randomFeatureVectorSize; j++){
			closeValueFeatureValues[j] = j%5+1;
		}
		FeatureVector closeValueFeatureVector = new FeatureVector(closeValueFeatureValues);
		inputVectors.add(closeValueFeatureVector);
		
		for(int i = 0; i<1000;i++){
			Integer[] randomFeatureValues = new Integer[randomFeatureVectorSize];
			for(int j = 0; j< randomFeatureVectorSize; j++){
				randomFeatureValues[j] = rnd.nextInt()%100;
			}
			FeatureVector randomFeatureVector = new FeatureVector(randomFeatureValues);
			inputVectors.add(randomFeatureVector);
		}
		
		for(Pair<Float, FeatureVector> match : LSH.computeNeighbours(searchVector, inputVectors, 0.5f)){
			System.out.println(match);
		}
	}
}
