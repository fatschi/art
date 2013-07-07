package de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl;

import it.unimi.dsi.fastutil.ints.Int2DoubleAVLTreeMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_potsdam.de.hpi.fgnaumann.art.util.Bit;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.SignatureVector;

/**
 * A more sophisticated implementation of {@link FeatureVector} backed up by
 * fastutil's {@link Int2DoubleAVLTreeMap} to better support sparse vectors.
 * 
 * @author fabian
 * 
 * @param <T>
 *            The type of the values the vector contains.
 */
public class PrimitiveMapFeatureVector<T extends Number> implements
		FeatureVector<T> {

	private static final long serialVersionUID = -514366940793968554L;
	private Int2DoubleAVLTreeMap featuresMap;

	private Long id;
	private Integer dimensions;
	private SignatureVector localitySensitiveHashed;

	@SafeVarargs
	public PrimitiveMapFeatureVector(Long id, Integer dimensionality) {
		this.dimensions = dimensionality;
		featuresMap = new Int2DoubleAVLTreeMap();
		featuresMap.defaultReturnValue(0);
		this.id = id;
	}

	@SafeVarargs
	public PrimitiveMapFeatureVector(Long id, T... features) {
		this.dimensions = features.length;
		featuresMap = new Int2DoubleAVLTreeMap();
		featuresMap.defaultReturnValue(0);
		for (int i = 0; i < features.length; i++) {
			if (features[i] != null)
				this.featuresMap.put(i, features[i].intValue());
		}
		this.id = id;
	}

	@SafeVarargs
	public PrimitiveMapFeatureVector(Long id, Integer dimensionality,
			Map<Integer, Double> features) {
		this.dimensions = dimensionality;
		featuresMap = new Int2DoubleAVLTreeMap(features);
		featuresMap.defaultReturnValue(0);
		this.id = id;
	}

	// FIXME
	@SuppressWarnings("unchecked")
	@Override
	@Deprecated
	public List<T> getValues() {
		return Arrays.asList(((T[]) this.featuresMap.values().toArray()));
	}

	@Override
	public Integer getDimensionality() {
		return this.dimensions;
	}

	@Override
	public void setValue(Integer dimension, T value) {
		this.featuresMap.put(dimension.intValue(), value.floatValue());
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getValue(Integer dimension) {
		return (T) this.featuresMap.get(dimension);
	}

	/**
	 * Create a bit signature using d classification steps.
	 * 
	 * @param d
	 *            - the input random weight vector of counts.
	 */
	@Override
	public void createLSH(Set<FeatureVector<? extends Number>> randomVectors) {
		this.localitySensitiveHashed = new ComparableBitSetSignatureVector(
				this.getId(), randomVectors.size());
		// Do d number of classifications
		int i = 0;
		for (FeatureVector<? extends Number> weightVector : randomVectors) {
			this.getLocalitySensitiveHashed().setValue(i,
					new Bit(classify(weightVector)));
			i++;
		}
	}

	/**
	 * Classify an feature vector as 0 or 1.
	 * 
	 * @param x
	 * @param wd
	 * @return
	 */
	private boolean classify(FeatureVector<? extends Number> wd) {
		float sum = 0;
		// Scalar Product
		for (int i = 0; i != this.getDimensionality(); ++i) {
			// TODO check the type of the vector
			sum += wd.getValue(i).doubleValue() * this.featuresMap.get(i);
		}

		// sign binary function
		if (sum <= 0) {
			return false; // yes, war vorher 0!?
		} else {
			return true; // no, war vorher 1?!
		}
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public SignatureVector getLocalitySensitiveHashed() {
		return this.localitySensitiveHashed;
	}

	@Override
	public String toString() {
		return ("FeatureVector [id=" + id + ", features=" + this.featuresMap
				+ ", localitySensitiveHashed=" + getLocalitySensitiveHashed()
				+ ", features=" + featuresMap + "]");
	}

}
