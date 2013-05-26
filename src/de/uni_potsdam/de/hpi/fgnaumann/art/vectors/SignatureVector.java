package de.uni_potsdam.de.hpi.fgnaumann.art.vectors;

import java.util.BitSet;

import de.uni_potsdam.de.hpi.fgnaumann.art.util.Bit;

public interface SignatureVector extends Vector<Bit> {

	Double computeNormalizedHammingDistance(SignatureVector secondVector);

	SignatureVector permute(int[] randomPermutation);

	FeatureVector<? extends Number> getParentVector();

	BitSet getValuesAsBitSet();

}
