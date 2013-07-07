package de.uni_potsdam.de.hpi.fgnaumann.art.permutation;
/**
 * An interface for generators of permutations. A permutation of size n is represented as an int[n].
 * @author fabian
 *
 */
public interface PermutationGenerator {
	int[] generateRandomPermutation(int size);
}
