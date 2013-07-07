package de.uni_potsdam.de.hpi.fgnaumann.art.permutation;

import java.util.Arrays;
import java.util.Random;
/**
 * A {@link PermutationGenerator} implementation based on the FisherYates algorithm.
 * @author fabian
 *
 */
public class FisherYates implements PermutationGenerator{

	@Override
	public int[] generateRandomPermutation(int size) {
		int[] initialArray = initializeArray(size);
		
		Random r = new Random();
	    for (int i = size - 1; i > 0; i--) {
	        int index = r.nextInt(i);
	        //swap
	        int tmp = initialArray[index];
	        initialArray[index] = initialArray[i];
	        initialArray[i] = tmp;
	    }
	    return initialArray;
	} 
	
	private int[] initializeArray(int size){
		int[] initialArray = new int[size];
		for(int i = 0; i < size; i++){
			initialArray[i] = i;
		}
		return initialArray;
	}
	
	public static void main(String[] args){
		PermutationGenerator fisherYates = new FisherYates();
		System.out.println(Arrays.toString(fisherYates.generateRandomPermutation(10)));
	}
}
