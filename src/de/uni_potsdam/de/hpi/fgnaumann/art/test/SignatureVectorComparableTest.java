package de.uni_potsdam.de.hpi.fgnaumann.art.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.uni_potsdam.de.hpi.fgnaumann.art.Bit;
import de.uni_potsdam.de.hpi.fgnaumann.art.SignatureVector;

public class SignatureVectorComparableTest {

	@Test
	public void testRightOrderingOfSignatureVectors() {
		SignatureVector foo1 = new SignatureVector(null, 5);
		SignatureVector foo2 = new SignatureVector(null, 5);
		SignatureVector foo3 = new SignatureVector(null, 5);
		SignatureVector foo4 = new SignatureVector(null, 5);
		SignatureVector foo5 = new SignatureVector(null, 5);
		SignatureVector foo6 = new SignatureVector(null, 5);
		SignatureVector foo7 = new SignatureVector(null, 5);
		foo7.setValue(1, new Bit(true));foo7.setValue(2, new Bit(true));foo7.setValue(3, new Bit(true));foo7.setValue(4, new Bit(true));foo7.setValue(5, new Bit(true));
		
		
		foo1.setValue(1, new Bit(true));
		foo2.setValue(2, new Bit(true));
		foo3.setValue(2, new Bit(true));
		foo3.setValue(3, new Bit(true));
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
		
	}

}
