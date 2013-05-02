package de.uni_potsdam.de.hpi.fgnaumann;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import de.uni_potsdam.de.hpi.fgnaumann.permutation.FisherYates;
import de.uni_potsdam.de.hpi.fgnaumann.permutation.PermutationGenerator;

public class LSH {
	
	private static final int NUMBER_OF_RANDOM_VECTORS = 10;
	private static final int DIMENSIONALITY_OF_RANDOM_VECTORS = 100;
	private static final int NUMBER_OF_PERMUTATIONS = 5;
	
	public static List<Pair<Integer, FeatureVector>> computeNeighbours(
			FeatureVector searchVector, Set<FeatureVector> inputVectors,
			double maxDistance) {
		
		//step 2
		Set<FeatureVector> randomVectors = generateRandomVectors(NUMBER_OF_RANDOM_VECTORS, DIMENSIONALITY_OF_RANDOM_VECTORS);
		
		//step 3
		searchVector.createLSH(randomVectors);
		for(FeatureVector inputVector : inputVectors){
			inputVector.createLSH(randomVectors);
		}
		
		//step 4
		List<int[]> randomPermutations = new ArrayList<>();
		PermutationGenerator permutationGenerator = new FisherYates();
		for(int i = 0; i < NUMBER_OF_PERMUTATIONS; i++){
			randomPermutations.add(permutationGenerator.generateRandomPermutation(DIMENSIONALITY_OF_RANDOM_VECTORS));
		}
		
		for(int[] randomPermutation : randomPermutations){
			searchVector.permute(randomPermutation);
			for(FeatureVector inputVector : inputVectors){
				inputVector.permute(randomPermutation);
			}
		}
		
		//TODO sort permutation, add sliding window
		
		return null;
	}

	private static Set<FeatureVector> generateRandomVectors(int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}
}
