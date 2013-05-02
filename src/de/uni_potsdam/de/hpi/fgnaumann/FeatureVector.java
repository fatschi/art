package de.uni_potsdam.de.hpi.fgnaumann;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatureVector {
	private Integer id;
	private List<Integer> features;
	private BitSet localitySensitiveHashed;
	private Map<int[], BitSet> permutations;
	
	public void createLSH(Set<FeatureVector> randomVectors){
		
	}
	
	public void permute(int[] randomPermutation) {
		BitSet permutation = new BitSet();
		int i = 0;
		for(int position : randomPermutation){
			permutation.set(i, this.localitySensitiveHashed.get(position));
			i++;
		}
	}
	
	public FeatureVector(int... features){
		for(int feature : features){
			this.features.add(feature);
		}
	}

	public List<Integer> getFeatures() {
		return features;
	}

	public void setFeatures(List<Integer> features) {
		this.features = features;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public BitSet getLocalitySensitiveHashed() {
		return localitySensitiveHashed;
	}

	public void setLocalitySensitiveHashed(BitSet localitySensitiveHashed) {
		this.localitySensitiveHashed = localitySensitiveHashed;
	}


	public Map<int[], BitSet> getPermutations() {
		return permutations;
	}


	public void setPermutations(Map<int[], BitSet> permutations) {
		this.permutations = permutations;
	}
}
