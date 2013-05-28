package de.uni_potsdam.de.hpi.fgnaumann.art.lsh;

import java.util.HashSet;
import java.util.Set;

import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;

public class ClassifierWorker implements Runnable {
	Set<FeatureVector<? extends Number>> workingChunk = new HashSet<FeatureVector<? extends Number>>();
	Set<FeatureVector<? extends Number>> randomWeightVectors = new HashSet<FeatureVector<? extends Number>>();

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
