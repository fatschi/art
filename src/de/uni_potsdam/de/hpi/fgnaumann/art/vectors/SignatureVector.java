package de.uni_potsdam.de.hpi.fgnaumann.art.vectors;

import de.uni_potsdam.de.hpi.fgnaumann.art.util.Bit;
import de.uni_potsdam.de.hpi.fgnaumann.art.util.ComparableBitSet;

/**
 * SignatureVectors are {@link Vector}<Bit> which can be permuted, compared to others
 * lexicographically and by Hamming distance. Each belongs to a parent
 * {@link FeatureVector}.
 * 
 * @author fabian
 * 
 */
public interface SignatureVector extends Vector<Bit>,
		Comparable<SignatureVector> {

	Double computeNormalizedHammingDistance(SignatureVector secondVector);

	SignatureVector permute(int[] randomPermutation);

	ComparableBitSet getValuesAsBitSet();

	Long getParentVectorId();

}
