package de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.uni_potsdam.de.hpi.fgnaumann.art.util.Bit;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.SignatureVector;

public class NumberListFeatureVector<T extends Number> implements
		FeatureVector<T>{
	private static final long serialVersionUID = -583575794565479250L;
	
	private Integer id;
	private List<T> features;
	private SignatureVector localitySensitiveHashed;

	@SafeVarargs
	public NumberListFeatureVector(T... features) {
		this.features = new ArrayList<T>();
		for (T feature : features) {
			this.features.add(feature);
		}
	}

	@SafeVarargs
	public NumberListFeatureVector(Integer id, T... features) {
		this(features);
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((features == null) ? 0 : features.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		NumberListFeatureVector<? extends Number> other = (NumberListFeatureVector<? extends Number>) obj;
		if (features == null) {
			if (other.features != null)
				return false;
		} else if (!features.equals(other.features))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (localitySensitiveHashed == null) {
			if (other.localitySensitiveHashed != null)
				return false;
		} else if (!localitySensitiveHashed
				.equals(other.localitySensitiveHashed))
			return false;
		return true;
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
					* this.getValue(i).doubleValue();
		}

		// sign binary function
		if (sum <= 0) {
			return false; // yes, war vorher 0!?
		} else {
			return true; // no, war vorher 1?!
		}
	}

	@Override
	public String toString() {
		return ("FeatureVector [id=" + id + ", localitySensitiveHashed="
				+ getLocalitySensitiveHashed() + ", features=" + features + "]");
	}

	@Override
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public List<T> getValues() {
		return features;
	}

	@Override
	public Integer getDimensionality() {
		return this.features.size();
	}

	@Override
	public void setValue(Integer dimension, T value) {
		this.features.add(dimension, value);
	}

	@Override
	public T getValue(Integer dimension) {
		return this.features.get(dimension);
	}

	@Override
	public SignatureVector getLocalitySensitiveHashed() {
		return localitySensitiveHashed;
	}
}
