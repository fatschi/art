package de.uni_potsdam.de.hpi.fgnaumann.art.lsh;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.collections.bag.TreeBag;

import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.SignatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl.ComparableBitSetSignatureVector;

public class PermutationWorker implements
		Callable<Map<Long, Double>> {

	private int[] randomPermutation;
	private FeatureVector<? extends Number> searchVector;
	private Set<FeatureVector<? extends Number>> inputVectors;
	private int WINDOW_SIZE_B;

	public PermutationWorker(int[] randomPermutation,
			FeatureVector<? extends Number> searchVector,
			Set<FeatureVector<? extends Number>> inputVectors, int WINDOW_SIZE_B) {
		this.randomPermutation = randomPermutation;
		this.searchVector = searchVector;
		this.inputVectors = inputVectors;
		this.WINDOW_SIZE_B = WINDOW_SIZE_B;
	}

	@Override
	public Map<Long, Double> call() throws Exception {
		Map<Long, Double> outputMap = new HashMap<Long, Double>();

		TreeBag sortedPermutationList = new TreeBag();

		SignatureVector searchVectorPermutation = searchVector
				.getLocalitySensitiveHashed().permute(randomPermutation);
		sortedPermutationList.add(searchVectorPermutation);

		for (FeatureVector<? extends Number> inputVector : inputVectors) {
			sortedPermutationList.add(inputVector.getLocalitySensitiveHashed()
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
			if (candidate.getParentVectorId() != searchVector.getId()) {
				outputMap.put(candidate.getParentVectorId(),
						searchVectorPermutation
								.computeNormalizedHammingDistance(candidate));
			}
		}
		return outputMap;
	}

}
