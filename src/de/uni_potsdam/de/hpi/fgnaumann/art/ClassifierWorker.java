package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.util.HashSet;
import java.util.Set;

public class ClassifierWorker implements Runnable {
	Set<FeatureVector> workingChunk = new HashSet<FeatureVector>();
	Set<WeightVector> randomVectors = new HashSet<WeightVector>();

	public ClassifierWorker(Set<FeatureVector> workingChunk,
			Set<WeightVector> randomVectors) {
		this.workingChunk = workingChunk;
		this.randomVectors = randomVectors;
	}

	@Override
	public void run() {
		for (FeatureVector inputVector : workingChunk) {
			inputVector.createLSH(randomVectors);
		}
	}
}
