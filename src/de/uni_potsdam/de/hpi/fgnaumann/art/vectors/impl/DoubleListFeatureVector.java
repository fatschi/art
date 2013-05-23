package de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.uni_potsdam.de.hpi.fgnaumann.art.util.Bit;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.SignatureVector;

public class DoubleListFeatureVector implements FeatureVector<Double>{
	private Integer id;
	private List<Double> features;
	private SignatureVector localitySensitiveHashed;
	
	public DoubleListFeatureVector(Double... features){
		this.features = new ArrayList<Double>();
		for(double feature : features){
			this.features.add(feature);
		}
	}

	public DoubleListFeatureVector(Integer id, Double... features){
		this(features);
		this.id = id;
	}
	
	/**
	 * Create a bit signature using d classification steps. 
	 * @param d - the input random weight vector of counts.
	 */
	@Override
	public void createLSH(Set<FeatureVector<? extends Number>> randomVectors){
		this.localitySensitiveHashed = new ComparableBitSetSignatureVector(this, randomVectors.size());
		// Do d number of classifications
		int i=0;
		for(FeatureVector<? extends Number> weightVector : randomVectors){
			this.getLocalitySensitiveHashed().setValue(i,new Bit(classify(weightVector)));
			i++;
		}
	}
	
	/**
	 * Classify an feature vector as 0 or 1.
	 * @param x
	 * @param wd
	 * @return
	 */
	private boolean classify(FeatureVector<? extends Number> wd) {
		float sum = 0;
		// Scalar Product
		for (int i = 0; i!=this.getDimensionality(); ++i) {
			sum += wd.getValue(i).doubleValue() * this.getValue(i); 
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
		return "FeatureVector [id=" + id //+ ", features=" + features
				+ ", localitySensitiveHashed=" + getLocalitySensitiveHashed()
				+ "]";
	}
	
	@Override
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public List<Double> getValues() {
		return features;
	}

	@Override
	public Integer getDimensionality() {
		return this.features.size();
	}
	
	@Override
	public void setValue(Integer dimension, Double value) {
		this.features.add(dimension, value);
	}
	
	@Override
	public Double getValue(Integer dimension) {
		return this.features.get(dimension);
	}
	
	@Override
	public SignatureVector getLocalitySensitiveHashed() {
		return localitySensitiveHashed;
	}
}
