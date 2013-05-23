package de.uni_potsdam.de.hpi.fgnaumann.art.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.uni_potsdam.de.hpi.fgnaumann.art.util.Bit;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl.ComparableBitSetSignatureVector;

public class SignatureVectorComparableTest {

	@Test
	public void testRightOrderingOfSignatureVectors() {
		ComparableBitSetSignatureVector foo1 = new ComparableBitSetSignatureVector(null, 5);
		ComparableBitSetSignatureVector foo2 = new ComparableBitSetSignatureVector(null, 5);
		ComparableBitSetSignatureVector foo3 = new ComparableBitSetSignatureVector(null, 5);
		ComparableBitSetSignatureVector foo4 = new ComparableBitSetSignatureVector(null, 5);
		ComparableBitSetSignatureVector foo5 = new ComparableBitSetSignatureVector(null, 5);
		ComparableBitSetSignatureVector foo6 = new ComparableBitSetSignatureVector(null, 5);
		ComparableBitSetSignatureVector foo7 = new ComparableBitSetSignatureVector(null, 5);
		foo7.setValue(1, new Bit(true));foo7.setValue(2, new Bit(true));foo7.setValue(3, new Bit(true));foo7.setValue(4, new Bit(true));foo7.setValue(5, new Bit(true));
		
		
		foo1.setValue(1, new Bit(true));
		foo1.setValue(2, new Bit(true));
		foo2.setValue(2, new Bit(true));
		foo2.setValue(3, new Bit(true));
		foo3.setValue(2, new Bit(true));
		foo4.setValue(4, new Bit(true));
		foo5.setValue(5, new Bit(true));
		
		List<ComparableBitSetSignatureVector> sortedList = new ArrayList<ComparableBitSetSignatureVector>();
		sortedList.add(foo4);
		sortedList.add(foo3);
		sortedList.add(foo2);
		sortedList.add(foo5);
		sortedList.add(foo1);
		sortedList.add(foo7);
		sortedList.add(foo6);
		
		Collections.sort(sortedList);
		List<ComparableBitSetSignatureVector> assertedList = new ArrayList<ComparableBitSetSignatureVector>();
		assertedList.add(foo7);
		assertedList.add(foo1);
		assertedList.add(foo2);
		assertedList.add(foo3);
		assertedList.add(foo4);
		assertedList.add(foo5);
		assertedList.add(foo6);
		assertEquals(sortedList, assertedList);
		
		
	}

}
