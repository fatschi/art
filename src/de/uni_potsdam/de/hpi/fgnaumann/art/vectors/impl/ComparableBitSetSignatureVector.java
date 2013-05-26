package de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import de.uni_potsdam.de.hpi.fgnaumann.art.util.Bit;
import de.uni_potsdam.de.hpi.fgnaumann.art.util.ComparableBitSet;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.SignatureVector;

public class ComparableBitSetSignatureVector implements SignatureVector,
		Comparable<ComparableBitSetSignatureVector> {

	protected ComparableBitSet values;

	private Integer size;

	private FeatureVector<? extends Number> parentVector;

	public ComparableBitSetSignatureVector(
			FeatureVector<? extends Number> parentVector, Integer dimensionality) {
		this.values = new ComparableBitSet(dimensionality);
		this.size = dimensionality;
		this.parentVector = parentVector;
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
	public FeatureVector<? extends Number> getParentVector() {
		return parentVector;
	}

	@Override
	public int compareTo(ComparableBitSetSignatureVector toComp) {
		return this.values.compareTo(toComp.values);
	}

	@Override
	public String toString() {
		return "SignatureVector [values=" + values + ", size=" + size
				+ ", parentVector="
				+ (parentVector != null ? parentVector.getId() : "no parent")
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
				this.parentVector, this.getDimensionality());
		int i = 0;
		for (int position : randomPermutation) {
			permutation.setValue(i, this.getValue(position));
			i++;
		}
		return permutation;
	}

	@Override
	public BitSet getValuesAsBitSet() {
		return this.values;
	}

}
