package de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.uni_potsdam.de.hpi.fgnaumann.art.util.Bit;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;

/**
 * A primitive {@link FeatureVector} implementation backed up by an
 * {@link ArrayList}.
 * 
 * @author fabian
 * 
 * @param <T>
 *            The type of the values the vector contains.
 */
public class NumberArrayFeatureVector<T extends Number> extends
		AbstractFeatureVector<T> implements FeatureVector<T> {
	private static final long serialVersionUID = -583575794565479250L;

	private T[] features;
	private Number zero = 0.0;

	@SuppressWarnings("unchecked")
	public NumberArrayFeatureVector(Long id, Integer dimensionality) {
		this.dimensionality = dimensionality;
		this.features = (T[]) new Number[dimensionality];
		this.id = id;
	}

	@SafeVarargs
	public NumberArrayFeatureVector(Long id, T... features) {
		this.dimensionality = features.length;
		this.features = features;
		this.id = id;
	}

	@SuppressWarnings("unchecked")
	public NumberArrayFeatureVector(long id, int dimensionality,
			HashMap<Integer, T> features) {
		this.features = (T[]) new Number[dimensionality];
		this.dimensionality = dimensionality;

		for (Entry<Integer, T> feature : features.entrySet()) {
			this.features[feature.getKey()] = feature.getValue();
		}
		this.id = id;
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
		for (int i = 0; i < this.getDimensionality(); ++i) {
			// TODO check the type of the vector
			sum += wd.getValue(i).doubleValue()
					* this.getValue(i).doubleValue();
		}

		// sign binary function
		if (sum <= 0) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public List<T> getValues() {
		return Arrays.asList(this.features);
	}

	@Override
	public void setValue(Integer dimension, T value) {
		if (dimension >= dimensionality) {
			throw new IllegalArgumentException(
					"The given dimension exceeds the dimensionality of this vector: "
							+ dimension);
		}
		this.features[dimension] = value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getValue(Integer dimension) {
		T value = this.features[dimension];
		if (value != null) {
			return value;
		} else {
			return (T) zero;
		}
	}
}
