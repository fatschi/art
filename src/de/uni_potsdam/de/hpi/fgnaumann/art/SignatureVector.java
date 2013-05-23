package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class SignatureVector implements Vector<Bit>, Comparable<SignatureVector>{
	
	protected ComparableBitSet values;
	
	private Integer size;
	
	private FeatureVector parentVector;
	
	public SignatureVector(FeatureVector parentVector, Integer dimensionality){
		this.values = new ComparableBitSet(dimensionality);
		this.size = dimensionality;
		this.parentVector= parentVector;
	}

	@Override
	public List<Bit> getValues() {
		List<Bit> tempList = new ArrayList<Bit>();
		for(int i = 0;  i < this.getDimensionality(); i++){
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

	public FeatureVector getParentVector() {
		return parentVector;
	}

	@Override
	public int compareTo(SignatureVector toComp) {
		return this.values.compareTo(toComp.values);
	}

	@Override
	public String toString() {
		return "SignatureVector [values=" + values + ", size=" + size
				+ ", parentVector=" + (parentVector != null ? parentVector.getId() : "no parent") + "]";
	}

	public Float computeNormalizedHammingDistance(SignatureVector secondVector){
		BitSet foo1 = (BitSet)this.values.clone();
		foo1.xor(secondVector.values);
		return (foo1.cardinality()/(float)this.getDimensionality());
	}
	
}
