package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
/**
 * Interface describing the functionality of an {@link LSHRunner}.
 * @author fabian
 *
 */
public interface LSHRunner {
	/**
	 * 
	 * @param inputFilePath
	 * @param NTHREADS
	 * @param CHUNK_SIZE_CLASSIFIER_WORKER
	 * @param NUMBER_OF_RANDOM_VECTORS_d
	 */
	void runLSH(String inputFilePath, int NTHREADS,
			int CHUNK_SIZE_CLASSIFIER_WORKER, int NUMBER_OF_RANDOM_VECTORS_d);
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
	List<Pair<Double, Long>> runSearch(String inputFilePath,
			String searchVectorId, double SIMILARITY_THRESHOLD, int TOP_K,
			int NTHREADS, int NUMBER_OF_PERMUTATIONS_q, int WINDOW_SIZE_B);
	/**
	 * 
	 * @return
	 */
	List<Pair<Double, Long>> runSimulationBenchmark();
}
