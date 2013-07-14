package de.uni_potsdam.de.hpi.fgnaumann.art.lsh;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.collections.bag.TreeBag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.SignatureVector;
/**
 * A {@link Callable} that permutes a {@link Set} of {@link FeatureVector}'s {@link SignatureVector}'s and outputs the found neigbours.
 * @author fabian
 *
 */
public class LookupWorker implements
		Callable<Map<Long, Double>> {
	private static Logger logger = LogManager.getFormatterLogger(LookupWorker.class
			.getName());

	private FeatureVector<? extends Number> searchVector;
	private Set<FeatureVector<? extends Number>> inputVectorsChunk;

	public LookupWorker(FeatureVector<? extends Number> searchVector,
			Set<FeatureVector<? extends Number>> inputVectors) {
		this.searchVector = searchVector;
		this.inputVectorsChunk = inputVectors;
	}

	@Override
	public Map<Long, Double> call() throws Exception {
		Map<Long, Double> outputMap = new HashMap<Long, Double>();

		TreeBag signatureVectorList = new TreeBag();

		SignatureVector searchVectorSignature = searchVector
				.getLocalitySensitiveHashed();
		signatureVectorList.add(searchVectorSignature);

		for (FeatureVector<? extends Number> inputVector : inputVectorsChunk) {
			signatureVectorList.add(inputVector.getLocalitySensitiveHashed());
		}

		for (Object candidateO : signatureVectorList) {
			SignatureVector candidate = (SignatureVector) candidateO;
			if (candidate.getParentVectorId() != searchVector.getId()) {
				outputMap.put(candidate.getParentVectorId(),
						searchVectorSignature
								.computeNormalizedHammingDistance(candidate));
			}
		}
		return outputMap;
	}

}
