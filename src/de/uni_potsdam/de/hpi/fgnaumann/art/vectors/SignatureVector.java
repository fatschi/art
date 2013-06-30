package de.uni_potsdam.de.hpi.fgnaumann.art.vectors;

import de.uni_potsdam.de.hpi.fgnaumann.art.util.Bit;
import de.uni_potsdam.de.hpi.fgnaumann.art.util.ComparableBitSet;

public interface SignatureVector extends Vector<Bit>, Comparable<SignatureVector>{

	Double computeNormalizedHammingDistance(SignatureVector secondVector);

	SignatureVector permute(int[] randomPermutation);

	ComparableBitSet getValuesAsBitSet();

	Long getParentVectorId();

}
