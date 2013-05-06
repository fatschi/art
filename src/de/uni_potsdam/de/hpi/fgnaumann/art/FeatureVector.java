package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FeatureVector implements Vector<Integer>{
	private Integer id;
	private List<Integer> features;
	private SignatureVector localitySensitiveHashed;
	
	public FeatureVector(Integer... features){
		this.features = new ArrayList<Integer>();
		for(int feature : features){
			this.features.add(feature);
		}
	}
	
	/**
	 * Create a bit signature using d classification steps. 
	 * @param d - the input random weight vector of counts.
	 */
	public void createLSH(Set<WeightVector> randomVectors){
		this.localitySensitiveHashed = new SignatureVector(this, randomVectors.size());
		// Do d number of classifications
		int i=0;
		for(WeightVector weightVector : randomVectors){
			this.localitySensitiveHashed.setValue(i,new Bit(classify(weightVector)));
			i++;
		}
	}
	
	/**
	 * Classify an feature vector as 0 or 1.
	 * @param x
	 * @param wd
	 * @return
	 */
	private boolean classify(WeightVector wd) {
		float sum = 0;
		// Scalar Product
		for (int i = 0; i!=this.getDimensionality(); ++i) {
			sum += wd.getValue(i) * this.getValue(i); 
		}
		
		// sign binary function
		if (sum <= 0) {
			return false; // yes, war vorher 0!?
		}
		else {
			return true; // no, war vorher 1?!
		}
	}
	
	@Override
	public String toString() {
		return "FeatureVector [id=" + id + ", features=" + features
				+ ", localitySensitiveHashed=" + localitySensitiveHashed
				+ "]";
	}

	public SignatureVector permute(int[] randomPermutation) {
		SignatureVector permutation = new SignatureVector(this,
				this.localitySensitiveHashed.getDimensionality());
		int i = 0;
		for (int position : randomPermutation) {
			permutation.setValue(i,
					this.localitySensitiveHashed.getValue(position));
			i++;
		}
		return permutation;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public List<Integer> getValues() {
		return features;
	}

	@Override
	public Integer getDimensionality() {
		return this.features.size();
	}
	
	@Override
	public void setValue(Integer dimension, Integer value) {
		this.features.add(dimension, value);
	}
	
	@Override
	public Integer getValue(Integer dimension) {
		return this.features.get(dimension);
	}
}
