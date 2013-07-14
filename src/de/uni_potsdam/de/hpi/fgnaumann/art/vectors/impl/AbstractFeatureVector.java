package de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl;

import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.SignatureVector;

public abstract class AbstractFeatureVector<T extends Number> implements
		FeatureVector<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5759924699221398348L;
	protected Long id;
	protected Integer dimensionality;
	protected SignatureVector localitySensitiveHashed;

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
		return ("FeatureVector [id=" + id + ", localitySensitiveHashed="
				+ getLocalitySensitiveHashed() + "]");
	}

	@Override
	public int compareTo(FeatureVector<T> o) {
		return this.id.compareTo(o.getId());
	}

	@Override
	public Integer getDimensionality() {
		return this.dimensionality;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		AbstractFeatureVector<?> other = (AbstractFeatureVector<?>) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
