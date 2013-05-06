package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.util.ArrayList;
import java.util.List;

public class WeightVector implements Vector<Float> {
	@Override
	public String toString() {
		return "WeightVector [values=" + values + "]";
	}

	private List<Float> values;
	
	public WeightVector(Integer dimensionality){
		this.values = new ArrayList<Float>(dimensionality);
	}
	
	@Override
	public List<Float> getValues() {
		return this.values;
	}

	@Override
	public Integer getDimensionality() {
		return this.values.size();
	}

	@Override
	public void setValue(Integer dimension, Float value) {
		this.values.add(dimension, value);
	}

	@Override
	public Float getValue(Integer dimension) {
		return this.values.get(dimension);
	}

}
