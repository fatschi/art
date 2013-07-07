package de.uni_potsdam.de.hpi.fgnaumann.art.vectors;

import java.util.List;
/**
 * Generic Vector interface
 * @author fabian
 *
 * @param <T> The type of the values the vector contains.
 */
public interface Vector<T extends Number>{
	List<T> getValues();
	Integer getDimensionality();
	void setValue(Integer dimension, T value);
	T getValue(Integer dimension);
}
