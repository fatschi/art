package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;

/**
 * Interface describing the functionality of an {@link LSHRunner}.
 * 
 * @author fabian
 * 
 */
public interface LSHRunner {

	/**
	 * Initially loads the {@link FeatureVector}s.
	 * 
	 * @param filePath
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	void loadData(String filePath) throws IOException, ClassNotFoundException;

	/**
	 * Stores the {@link FeatureVector}s.
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	void storeData(String filePath) throws IOException;

	/**
	 * Computes the LSH hashes on the given set of {@link FeatureVector}s. Overwrites existing ones.
	 * @param inputFilePath
	 * @param NTHREADS
	 * @param CHUNK_SIZE_CLASSIFIER_WORKER
	 * @param NUMBER_OF_RANDOM_VECTORS_d
	 */
	void runLSH(int NTHREADS, int CHUNK_SIZE_CLASSIFIER_WORKER,
			int NUMBER_OF_RANDOM_VECTORS_d);
	
	/**
	 * List all vector ids in the {@link Set} of {@link FeatureVector}s.
	 * @return
	 */
	List<Long> listVectorIds();
	
	/**
	 * Shows all the information about a particular {@link FeatureVector}.
	 * @return
	 */
	FeatureVector<?>  showVector(Long vectorId);

	/**
	 * 
	 * @param inputFilePath
	 * @param searchVectorId
	 * @param SIMILARITY_THRESHOLD
	 * @param TOP_K
	 * @param NTHREADS
	 * @param NUMBER_OF_PERMUTATIONS_q
	 * @param WINDOW_SIZE_B
	 * @return
	 */
	@Deprecated
	List<Pair<Double, Long>> runSearch(Long searchVectorId,
			double SIMILARITY_THRESHOLD, int TOP_K, int NTHREADS,
			int NUMBER_OF_PERMUTATIONS_q, int WINDOW_SIZE_B);
	
	/**
	 * Performs a KNN search for a given vector id.
	 * @param searchVectorId
	 * @param SIMILARITY_THRESHOLD
	 * @param TOP_K
	 * @param NTHREADS
	 * @return
	 */
	List<Pair<Double, Long>> runSearch(Long searchVectorId,
			double SIMILARITY_THRESHOLD, int TOP_K, int NTHREADS);

	/**
	 * Runs the simulation benchmark which performs all steps of the algorithm and which you can parameterize with the variables in {@link LSHRunnerImpl}.
	 * @return
	 */
	List<Pair<Double, Long>> runSimulationBenchmark();
}
