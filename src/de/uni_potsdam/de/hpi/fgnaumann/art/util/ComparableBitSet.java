package de.uni_potsdam.de.hpi.fgnaumann.art.util;

import java.util.BitSet;
import java.util.Iterator;

/**
 * This class extends java.util.BitSet and implements the interfaces Comparable
 * and Iterable.
 * 
 * @author Patrick Schulze (patrick.schulze@student.hpi.uni-potsdam.de)
 * @author Fabian Tschirschnitz
 *         (fabian.tschirschnitz@student.hpi.uni-potsdam.de)
 */

public class ComparableBitSet extends BitSet implements Comparable<BitSet>,
		Iterable<Integer> {

	private static final long serialVersionUID = 1L;

	/**
	 * standard constructor
	 */
	public ComparableBitSet() {
		super();
	}

	/**
	 * Creates a ComparableBitSet with the given size.
	 * 
	 * @param size
	 *            Size of the ComparableBitSet.
	 */
	public ComparableBitSet(int size) {
		super(size);
	}

	/**
	 * Logical exclusive or with no side-effect to this object.
	 * 
	 * @param o
	 *            second operand
	 * @return Returns an exclusive or between this object and o.
	 */
	public ComparableBitSet xorResult(BitSet o) {
		ComparableBitSet copy = (ComparableBitSet) this.clone();
		copy.xor(o);
		return copy;
	}

	/**
	 * Logical or with no side-effect to this object.
	 * 
	 * @param o
	 *            second operand
	 * @return Returns an or between this object and o.
	 */
	public ComparableBitSet orResult(BitSet o) {
		ComparableBitSet copy = (ComparableBitSet) this.clone();
		copy.or(o);
		return copy;
	}

	/**
	 * Logical and with no side-effect to this object.
	 * 
	 * @param o
	 *            second operand
	 * @return Returns an and between this object and o.
	 */
	public ComparableBitSet andResult(BitSet o) {
		ComparableBitSet copy = (ComparableBitSet) this.clone();
		copy.and(o);
		return copy;
	}

	/**
	 * Set-semantical minus with side-effect to this object.
	 * 
	 * @param o
	 *            subtrahend
	 */
	public void minus(BitSet o) {
		ComparableBitSet copyOfO = (ComparableBitSet) o.clone();
		if (copyOfO.size() > 0)
			copyOfO.flip(0, copyOfO.size() - 1);
		this.and(copyOfO);
	}

	/**
	 * Set-semantical minus with no side-effect to this object.
	 * 
	 * @param o
	 *            subtrahend
	 * @return Returns an minus between this object and o.
	 */
	public ComparableBitSet minusResult(BitSet o) {
		ComparableBitSet copy = (ComparableBitSet) this.clone();
		copy.minus(o);
		return copy;
	}

	/**
	 * Flips a certain bits of this object with no side-effect to this object.
	 * 
	 * @param index
	 *            The index of the bit to flip
	 * @return Returns a copy of this object with the specified bit flipped.
	 */
	public ComparableBitSet flipResult(int index) {
		ComparableBitSet copy = (ComparableBitSet) this.clone();
		copy.flip(index);
		return copy;
	}

	/**
	 * Flips all bits of this object with no side-effect to this object.
	 * 
	 * @return Returns a copy of this object with all used bits flipped.
	 */
	public ComparableBitSet flipAllResult() {
		ComparableBitSet copy = (ComparableBitSet) this.clone();
		copy.flip(0, copy.size());
		return copy;
	}

	/**
	 * Flips all bits of this object in a certain range with no side-effect to
	 * this object.
	 * 
	 * @param start
	 *            The index from where to start
	 * @param end
	 *            The index of the last bit to flip-1
	 * @return Returns a copy of this object with all selected bits flipped.
	 */
	public ComparableBitSet flipResult(int start, int end) {
		ComparableBitSet copy = (ComparableBitSet) this.clone();
		copy.flip(start, end);
		return copy;
	}

	/**
	 * Clears the i-th bit on a copy of this object.
	 * 
	 * @param i
	 *            Index of the bit to clear.
	 * @return Copy of this object with i-th bit cleared.
	 */
	public ComparableBitSet clearResult(int i) {
		ComparableBitSet copy = (ComparableBitSet) this.clone();
		copy.clear(i);
		return copy;
	}

	/**
	 * Sets the i-th bit on a copy of this object.
	 * 
	 * @param i
	 *            Index of the bit to set.
	 * @return Copy of this object with i-th bit set.
	 */
	public ComparableBitSet setResult(int i) {
		ComparableBitSet copy = (ComparableBitSet) this.clone();
		copy.set(i);
		return copy;
	}

	/**
	 * Tests if a ComparableBitSet is subset of this object
	 * 
	 * @param o
	 *            The potential subset of this object
	 * @return true if o is a subset, false if not
	 */
	public boolean isSupersetOf(ComparableBitSet o) {
		return this.equals(this.orResult(o));
	}

	/**
	 * Tests if a ComparableBitSet is proper subset of this object
	 * 
	 * @param o
	 *            The potential proper subset of this object
	 * @return true if o is a proper subset, false if not
	 */
	public boolean isProperSupersetOf(ComparableBitSet o) {
		if (this.cardinality() == o.cardinality()) {
			return false;
		}
		return this.isSupersetOf(o);
	}

	/**
	 * Compares two ComparableBitSets lexicographically
	 * 
	 * @param o
	 *            ComparableBitSet to compare with.
	 * @return The value 0 if this ComparableBitSet is equal to the argument
	 *         ComparableBitSet; The value -1 if this ComparableBitSet is
	 *         lexicographically is less than the argument ComparableBitSet; And
	 *         1 if this ComparableBitSet is lexicographically greater than the
	 *         argument ComparableBitSet.
	 */
	public int compareTo(BitSet o) {
		ComparableBitSet copy = (ComparableBitSet) this.clone();
		copy.xor(o);
		int lowestBit = copy.nextSetBit(0);
		if (lowestBit == -1) {
			return 0;
		} else if (this.get(lowestBit)) {
			return -1;
		} else {
			return 1;
		}
	}

	/**
	 * Returns a ComparableBitSet iterator of set bits in this ComparableBitSet
	 * (in proper sequence).
	 */
	public Iterator<Integer> iterator() {
		return new ComparableBitSetIterator(this);
	}

	/**
	 * Returns and int-array with the set bit's-indexes.
	 */
	public int[] toIntArray() {
		int[] arr = new int[this.cardinality()];

		int pos = 0;
		for (Integer index : this) {
			arr[pos++] = index;
		}
		return arr;
	}
}
