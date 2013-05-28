package de.uni_potsdam.de.hpi.fgnaumann.art.vectors;

import java.io.Serializable;
import java.util.Set;

public interface FeatureVector<T extends Number> extends Vector<T>, Serializable{

	void createLSH(Set<FeatureVector<? extends Number>> randomVectors);

	Integer getId();

	SignatureVector getLocalitySensitiveHashed();

}
