package de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl;

import it.unimi.dsi.fastutil.ints.Int2FloatAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.de.hpi.fgnaumann.art.util.Bit;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.SignatureVector;

public class FloatMapFeatureVector<T extends Float> implements
		FeatureVector<T> {
	private static Logger logger = LogManager
			.getFormatterLogger(FloatMapFeatureVector.class.getName());

	private static final long serialVersionUID = -514366940793968554L;
	private Int2FloatMap features;

	private Integer id;
	private Integer dimensions;
	private SignatureVector localitySensitiveHashed;

	@SafeVarargs
	public FloatMapFeatureVector(T... features) {
		this.dimensions = features.length;
		this.features = new Int2FloatAVLTreeMap();
		this.features.defaultReturnValue(0);
		for (int i = 0; i < features.length; i++) {
			if (features[i] != null)
				this.features.put(i, (float) features[i]);
		}
	}

	@SafeVarargs
	public FloatMapFeatureVector(Integer id, T... features) {
		this(features);
		this.id = id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getValues() {
		return Arrays.asList(((T[]) this.features.values().toArray()));
	}

	@Override
	public Integer getDimensionality() {
		return this.dimensions;
	}

	@Override
	public void setValue(Integer dimension, T value) {
		this.features.put(dimension, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getValue(Integer dimension) {
		return (T) this.features.get(dimension);
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
				this, randomVectors.size());
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
			sum += wd.getValue(i).doubleValue()
					* this.features.get(i);
		}

		// sign binary function
		if (sum <= 0) {
			return false; // yes, war vorher 0!?
		} else {
			return true; // no, war vorher 1?!
		}
	}

	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public SignatureVector getLocalitySensitiveHashed() {
		return this.localitySensitiveHashed;
	}
	
	@Override
	public String toString() {
		return ("FeatureVector [id=" + id + ", features="+this.features+ ", localitySensitiveHashed="
				+ getLocalitySensitiveHashed() + ", features=" + features + "]");
	}

}
