package de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl;

import java.util.Comparator;

import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.SignatureVector;

public class ComparableBitSetSignatureVectorComparator implements Comparator<SignatureVector>{

	@Override
	public int compare(SignatureVector arg0,
			SignatureVector arg1) {
		return arg0.getValuesAsBitSet().compareTo(arg1.getValuesAsBitSet());
	}


}
