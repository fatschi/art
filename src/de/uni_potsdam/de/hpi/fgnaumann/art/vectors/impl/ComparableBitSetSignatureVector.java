package de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import de.uni_potsdam.de.hpi.fgnaumann.art.util.Bit;
import de.uni_potsdam.de.hpi.fgnaumann.art.util.ComparableBitSet;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.SignatureVector;

public class ComparableBitSetSignatureVector implements SignatureVector, Serializable {

	private static final long serialVersionUID = -482400029141498267L;

	protected ComparableBitSet values;

	private Integer size;

	private Long parentVectorId;

	public ComparableBitSetSignatureVector(
			Long parentVectorId, Integer dimensionality) {
		this.values = new ComparableBitSet(dimensionality);
		this.size = dimensionality;
		this.parentVectorId = parentVectorId;
	}

	@Override
	public List<Bit> getValues() {
		List<Bit> tempList = new ArrayList<Bit>();
		for (int i = 0; i < this.getDimensionality(); i++) {
			tempList.add(new Bit(this.values.get(i)));
		}
		return tempList;
	}

	@Override
	public Integer getDimensionality() {
		return this.size;
	}

	@Override
	public void setValue(Integer dimension, Bit value) {
		this.values.set(dimension, value.getState());
	}

	@Override
	public Bit getValue(Integer dimension) {
		return new Bit(this.values.get(dimension));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((parentVectorId == null) ? 0 : parentVectorId.hashCode());
		result = prime * result + ((size == null) ? 0 : size.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		ComparableBitSetSignatureVector other = (ComparableBitSetSignatureVector) obj;
		if (parentVectorId == null) {
			if (other.parentVectorId != null)
				return false;
		} else if (!parentVectorId.equals(other.parentVectorId))
			return false;
		if (size == null) {
			if (other.size != null)
				return false;
		} else if (!size.equals(other.size))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SignatureVector [values=" + values + ", size=" + size
				+ ", parentVector="
				+ (parentVectorId != null ? parentVectorId : "no parent")
				+ "]";
	}

	@Override
	public Double computeNormalizedHammingDistance(SignatureVector secondVector) {
		BitSet foo1 = (BitSet) this.values.clone();
		foo1.xor(secondVector.getValuesAsBitSet());
		return (foo1.cardinality() / (double) this.getDimensionality());
	}

	@Override
	public SignatureVector permute(int[] randomPermutation) {
		SignatureVector permutation = new ComparableBitSetSignatureVector(
				this.parentVectorId, this.getDimensionality());
		int i = 0;
		for (int position : randomPermutation) {
			permutation.setValue(i, this.getValue(position));
			i++;
		}
		return permutation;
	}

	@Override
	public ComparableBitSet getValuesAsBitSet() {
		return this.values;
	}

	@Override
	public int compareTo(SignatureVector o) {

		if (this.values.compareTo(o.getValuesAsBitSet()) == 0) {
			return this.getParentVectorId().compareTo(
					o.getParentVectorId());
		} else
			return this.values.compareTo(o.getValuesAsBitSet());
	}
	
	@Override
	public Long getParentVectorId() {
		return parentVectorId;
	}

}
