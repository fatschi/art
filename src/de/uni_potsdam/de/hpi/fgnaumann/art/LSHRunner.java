package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public interface LSHRunner {
	void runLSH(String inputFilePath, int NTHREADS,
			int CHUNK_SIZE_CLASSIFIER_WORKER, int NUMBER_OF_RANDOM_VECTORS_d);

	List<Pair<Double, Long>> runSearch(String inputFilePath,
			String searchVectorId, double SIMILARITY_THRESHOLD, int TOP_K,
			int NTHREADS, int NUMBER_OF_PERMUTATIONS_q, int WINDOW_SIZE_B);

	List<Pair<Double, Long>> runSimulationBenchmark();
}
