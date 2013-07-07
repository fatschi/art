package de.uni_potsdam.de.hpi.fgnaumann.art.util;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 *         This class implements an {@link Iterator} over {@link ComparableBitSet}.
 *         
 * @author Patrick Schulze (patrick.schulze@student.hpi.uni-potsdam.de)
 * @author Fabian Tschirschnitz
 *         (fabian.tschirschnitz@student.hpi.uni-potsdam.de)
 */
public class ComparableBitSetIterator implements Iterator<Integer> {
 
  private int position = 0;
  private ComparableBitSet cbs = null;
 
  /**
   * standard constructor.
   * @param cbs a ComparableBitSet
   */
  public ComparableBitSetIterator(ComparableBitSet cbs){
	  this.cbs = cbs;
	  this.position = this.cbs.nextSetBit(this.position);
  }
  
  /**
   * Returns true if the iteration has more elements.
   * 
   * @return Returns true if there are further bits set in the ComparableBitSet.
   * 
   */
  public boolean hasNext() {
    return (0 <= this.position && this.position <= this.cbs.size() && this.cbs.nextSetBit(this.position)!=-1);
  }
  
  /**
   * Returns the next element in the iteration.
   * 
   * @return Returns the index of the next found set bit. 
   * 
   */
  public Integer next() throws NoSuchElementException {
 
    if (!hasNext()) {
      throw new NoSuchElementException("No more elements");
    }
    int oldPosition = this.position;
    this.position = this.cbs.nextSetBit(position+1);
    return new Integer(oldPosition);
  }
  
  /**
   * Removes from the underlying collection the last element returned by the iterator (optional operation).
   */
  public void remove() throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Operation is not supported");
  }
}
