package de.uni_potsdam.de.hpi.fgnaumann.art.vectors;

import java.io.Serializable;
import java.util.Set;

/**
 * This interface represents feature vectors for which you can compute an LSH.
 * 
 * @author fabian
 * 
 * @param <T>
 *            The type of values contained in the Vector.
 */
public interface FeatureVector<T extends Number> extends Vector<T>,
		Serializable, Comparable<FeatureVector<T>>{
	Long getId();

	void createLSH(Set<FeatureVector<? extends Number>> randomVectors);

	SignatureVector getLocalitySensitiveHashed();

}
