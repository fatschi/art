package de.uni_potsdam.de.hpi.fgnaumann;

import java.util.Random;


/**
 * @author Nils Rethmeier
 * Linear classifier that is used to map a high dimensional feature vector into a lower dimensional (1 Bit dimensional)
 * representation by classifying it as either 0 or 1. The classifiers weight vector w_i is chosen at random from a 
 * pseudo Normal Distribution with mean=0 and variance 1 for each individual weight.
 * The linear decision function is f(xi) = wi * xi; where xi is a high dimensional feature vector.
 * The classifier is sign(f(x)) = { 1 if f(xi) > 0; 0 else
 */
public class RandomProjection {
	
	/** defines the # of random hyperplanes as well as the lenght of the resulting lower dimensional feature vector. */
	private int d = 0;
	private int k = 0;
	private float[][] w = new float[d][k];
	private Random rnd = new Random();
	
	public RandomProjection(int numOfDimsToMapDownTo, int dimOfFeatures) {
		d = numOfDimsToMapDownTo;
		k = dimOfFeatures;
		w = initW(d,k);
	}

	private float[][] initW(int d, int k) {
		float[][] w = new float[d][k];
		// For d random projections.
		for (int di=0; di!= d; ++di) {
			// Randomly generate a weight vector of random normal mean 0 and variance 1 weights.
			for (int ki=0; ki!= k; ++ki) {
				w[di][ki] = drawNormal();
			}
		}
		return w;
	}

	/**
	 * Li, Church aproach.
	 * @param mean
	 * @param variance
	 * @return a float
	 * Very Sparse Random Projections
	 * "Achlioptas proposed sparse random projections by replacing the N (0, 1) entries 2 in R with
	 * entries in {−1, 0, 1} with probabilities { 1/6 , 2/3 , 1/6 },
	 * TODO implements and test sparse projection.
	 */
	private float sparseDrawNormal(double mean, double variance) {
		return (float) (mean + rnd.nextGaussian() * variance);
	}
	
	/**
	 * Draw from a Gaussian using mean 0 and variance 1.
	 * This is implemented in JDK vi the Box Mueller transform.
	 * @return a gaussian random value.
	 */
	private float drawNormal() {
		return (float) rnd.nextGaussian();
	}

	/**
	 * Classify an feature vector as 0 or 1.
	 * @param x
	 * @param wd
	 * @return
	 */
	public byte classify(int[] x, float[] wd) {
		float sum = 0;
		// Scalar Product
		for (int i = 0; i!=k; ++i) {
			sum += wd[i] * (float) x[i]; 
		}
		
		// sign binary function
		if (sum <= 0) {
			return 0; // yes
		}
		else {
			return 1; // no
		}
	}
	
	/**
	 * Create a byte signature using d classification steps. 
	 * @param x - the input feature vector of counts.
	 * @return x's byte signature from d random projection classifications.
	 */
	public byte[] createSignature(int[] x) {
		byte[] signature = new byte[d];
		// Do d number of classifications
		for (int i=0; i!=d; ++i) {
			signature[i] = classify(x, w[i]);
		}
		return signature;
	}
}
