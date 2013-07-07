package de.uni_potsdam.de.hpi.fgnaumann.art.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.uni_potsdam.de.hpi.fgnaumann.art.util.Bit;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.SignatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl.ComparableBitSetSignatureVector;
/**
 * {@link Test} class for {@link SignatureVector}.
 * @author fabian
 *
 */
public class SignatureVectorTest {

	@Test
	public void testRightPermutationOfVector() {
		SignatureVector testVector = new ComparableBitSetSignatureVector(null, 5);
		testVector.setValue(0, new Bit(true));
		testVector.setValue(1, new Bit(false));
		testVector.setValue(2, new Bit(true));
		testVector.setValue(3, new Bit(false));
		testVector.setValue(4, new Bit(true));
		SignatureVector testVectorPermuted = testVector.permute(new int[]{1,0,3,2,4});
		SignatureVector testVectorPermutedExpected = new ComparableBitSetSignatureVector(null, 5);
		testVectorPermutedExpected.setValue(0, new Bit(false));
		testVectorPermutedExpected.setValue(1, new Bit(true));
		testVectorPermutedExpected.setValue(2, new Bit(false));
		testVectorPermutedExpected.setValue(3, new Bit(true));
		testVectorPermutedExpected.setValue(4, new Bit(true));
		assertEquals(testVectorPermutedExpected, testVectorPermuted);
	}
	
	@Test
	public void testRightOrderingOfSignatureVectors() {
		SignatureVector foo1 = new ComparableBitSetSignatureVector(
				null, 5);
		SignatureVector foo2 = new ComparableBitSetSignatureVector(
				null, 5);
		SignatureVector foo3 = new ComparableBitSetSignatureVector(
				null, 5);
		SignatureVector foo4 = new ComparableBitSetSignatureVector(
				null, 5);
		SignatureVector foo5 = new ComparableBitSetSignatureVector(
				null, 5);
		SignatureVector foo6 = new ComparableBitSetSignatureVector(
				null, 5);
		SignatureVector foo7 = new ComparableBitSetSignatureVector(
				null, 5);
		foo7.setValue(1, new Bit(true));
		foo7.setValue(2, new Bit(true));
		foo7.setValue(3, new Bit(true));
		foo7.setValue(4, new Bit(true));
		foo7.setValue(5, new Bit(true));

		foo1.setValue(1, new Bit(true));
		foo1.setValue(2, new Bit(true));
		foo2.setValue(2, new Bit(true));
		foo2.setValue(3, new Bit(true));
		foo3.setValue(2, new Bit(true));
		foo4.setValue(4, new Bit(true));
		foo5.setValue(5, new Bit(true));

		List<SignatureVector> sortedList = new ArrayList<SignatureVector>();
		sortedList.add(foo4);
		sortedList.add(foo3);
		sortedList.add(foo2);
		sortedList.add(foo5);
		sortedList.add(foo1);
		sortedList.add(foo7);
		sortedList.add(foo6);

		Collections.sort(sortedList);
		List<SignatureVector> assertedList = new ArrayList<SignatureVector>();
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
