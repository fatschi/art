package de.uni_potsdam.de.hpi.fgnaumann.art.lsh;

import java.util.HashSet;
import java.util.Set;

import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.SignatureVector;

/**
 * A {@link Runnable} that computes the {@link SignatureVector} for a every
 * {@link FeatureVector} in a given {@link Set}.
 * 
 * @author fabian
 * 
 */
public class ClassifierWorker implements Runnable {
	private Set<FeatureVector<? extends Number>> workingChunk = new HashSet<FeatureVector<? extends Number>>();
	private Set<FeatureVector<? extends Number>> randomWeightVectors = new HashSet<FeatureVector<? extends Number>>();

	public ClassifierWorker(Set<FeatureVector<? extends Number>> workingChunk,
			Set<FeatureVector<? extends Number>> randomWeightVectors) {
		this.workingChunk = workingChunk;
		this.randomWeightVectors = randomWeightVectors;
	}

	@Override
	public void run() {
		for (FeatureVector<? extends Number> inputVector : workingChunk) {
			inputVector.createLSH(randomWeightVectors);
		}
	}
}
