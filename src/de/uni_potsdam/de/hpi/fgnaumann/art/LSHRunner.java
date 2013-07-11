package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

import org.apache.commons.lang3.tuple.Pair;

import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
/**
 * Interface describing the functionality of an {@link LSHRunner}.
 * @author fabian
 *
 */
public interface LSHRunner {
	
	/**
	 * Initially loads the {@link FeatureVector}s.
	 * @param filePath
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	void loadData(String filePath) throws IOException, ClassNotFoundException;
	
	/**
	 * Stores the {@link FeatureVector}s.
	 * @param filePath
	 * @throws IOException
	 */
	void storeData(String filePath) throws IOException;
	
	/**
	 * 
	 * @param inputFilePath
	 * @param NTHREADS
	 * @param CHUNK_SIZE_CLASSIFIER_WORKER
	 * @param NUMBER_OF_RANDOM_VECTORS_d
	 */
	void runLSH(int NTHREADS,
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
	SortedSet<Pair<Double, Long>> runSearch(
			String searchVectorId, double SIMILARITY_THRESHOLD, int TOP_K,
			int NTHREADS, int NUMBER_OF_PERMUTATIONS_q, int WINDOW_SIZE_B);
	/**
	 * 
	 * @return
	 */
	List<Pair<Double, Long>> runSimulationBenchmark();
}
