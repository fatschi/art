package de.uni_potsdam.de.hpi.fgnaumann.art.lsh;

import java.util.HashSet;
import java.util.Set;

import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;

public class ClassifierWorker implements Runnable {
	private Set<FeatureVector<? extends Number>> workingChunk = new HashSet<FeatureVector<? extends Number>>();
	private Set<FeatureVector<? extends Number>> randomWeightVectors = new HashSet<FeatureVector<? extends Number>>();
	private boolean forceLSHUpdate;

	public ClassifierWorker(Set<FeatureVector<? extends Number>> workingChunk,
			Set<FeatureVector<? extends Number>> randomWeightVectors, boolean forceLSHUpdate) {
		this.workingChunk = workingChunk;
		this.randomWeightVectors = randomWeightVectors;
		this.forceLSHUpdate = forceLSHUpdate;
	}

	@Override
	public void run() {
		for (FeatureVector<? extends Number> inputVector : workingChunk) {
			if(inputVector.getLocalitySensitiveHashed()==null || forceLSHUpdate)
			inputVector.createLSH(randomWeightVectors);
		}
	}
}
