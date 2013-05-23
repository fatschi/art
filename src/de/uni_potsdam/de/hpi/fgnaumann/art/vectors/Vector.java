package de.uni_potsdam.de.hpi.fgnaumann.art.vectors;

import java.util.List;

public interface Vector<T extends Number> {
	List<T> getValues();
	Integer getDimensionality();
	void setValue(Integer dimension, T value);
	T getValue(Integer dimension);
}
