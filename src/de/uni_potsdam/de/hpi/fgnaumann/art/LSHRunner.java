package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.de.hpi.fgnaumann.art.lsh.LSH;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl.NumberListFeatureVector;

public class LSHRunner {

	private static String INPUT_VECTORS_OUT_FILE = null;

	private static Logger logger = LogManager
			.getFormatterLogger(LSHRunner.class.getName());

	private static double SIMILARITY_THRESHOLD = 0.1d;
	private static int CORES = Runtime.getRuntime().availableProcessors();
	private static int NTHREADS = CORES;
	private static int CHUNK_SIZE_CLASSIFIER_WORKER = 100;

	private static int NUMBER_OF_SIMULATION_VECTORS = 10000;
	private static int NUMBER_OF_SIMULATION_VECTORS_CLOSE = 5;
	private static int DIMENSIONS_OF_SIMULATION_VECTORS = 10000;
	private static int SPARSITY = 10;
	private static int SIMULATION_VECTOR_VALUE_SPACE = 1000;
	private static double VARIANCE_OF_SIMULATION_VECTORS_CLOSE = 0.15;

	private static int NUMBER_OF_RANDOM_VECTORS_d = 50;
	private static int NUMBER_OF_PERMUTATIONS_q = 1;
	private static int WINDOW_SIZE_B = NUMBER_OF_SIMULATION_VECTORS;

	private static Random rnd = new Random();

	public static void main(String args[]) {
		// create Options object
		Options options = createOptions();
		// create the parser
		CommandLineParser parser = new PosixParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("LSH", options);
			}
			evaluateCLIParameters(line);
			if (line.hasOption("simulate")) {
				runSimulationBenchmark(line
						.hasOption("loadSimulationInputFile"));

			}
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		}
	}

	@SuppressWarnings("static-access")
	private static Options createOptions() {
		Options cliOptions = new Options();

		Option threshold = OptionBuilder.withArgName("threshold").hasArg()
				.withDescription("the maximum hamming distance")
				.create("threshold");

		Option threads = OptionBuilder.withArgName("threads").hasArg()
				.withDescription("the number of threads to spawn")
				.create("threads");

		Option chunkSize = OptionBuilder
				.withArgName("chunk")
				.hasArg()
				.withDescription(
						"chunk size of the vectors classified in one thread")
				.create("chunkSize");

		Option d = OptionBuilder.withArgName("d").hasArg()
				.withDescription("number of random vectors").create("d");

		Option q = OptionBuilder.withArgName("q").hasArg()
				.withDescription("number of random permutations").create("q");

		Option B = OptionBuilder.withArgName("B").hasArg()
				.withDescription("window size").create("B");

		Option numberOfSimulationVectors = OptionBuilder
				.withArgName("numberOfSimulationVectors")
				.hasArg()
				.withDescription(
						"number of vectors to be generated for test simulation")
				.create("numberOfSimulationVectors");

		Option numberOfSimulationVectorsClose = OptionBuilder
				.withArgName("numberOfSimulationVectorsClose")
				.hasArg()
				.withDescription(
						"number of vectors to be generated for test simulation that are close to the search vector")
				.create("numberOfSimulationVectorsClose");

		Option dimensionalityOfSimulationVectors = OptionBuilder
				.withArgName("dimensionalityOfSimulationVectors")
				.hasArg()
				.withDescription(
						"the dimensionality of the search/input vector generated in the simulation")
				.create("dimensionalityOfSimulationVectors");

		Option varianceOfSimulationVectorsClose = OptionBuilder
				.withArgName("varianceOfSimulationVectorsClose")
				.hasArg()
				.withDescription(
						"the value variance of the close vectors as a fraction of the overall value range")
				.create("varianceOfSimulationVectorsClose");

		Option sparsityOfSimulationVectors = OptionBuilder
				.withArgName("sparsityOfSimulationVectors")
				.hasArg()
				.withDescription("the value sparsity of the simulation vectors")
				.create("sparsityOfSimulationVectors");

		Option loadSimulationInputFile = OptionBuilder
				.withArgName("loadSimulationInputFile")
				.hasArg()
				.withDescription(
						"load a pre-computed simulation input file form disk, including inputVectors and searchVector")
				.create("loadSimulationInputFile");

		Option help = new Option("help", "print this message");

		Option simulate = new Option("simulate",
				"make a benchmark with randomly generated feature vectors");

		return cliOptions.addOption(loadSimulationInputFile)
				.addOption(sparsityOfSimulationVectors)
				.addOption(varianceOfSimulationVectorsClose)
				.addOption(dimensionalityOfSimulationVectors)
				.addOption(numberOfSimulationVectorsClose)
				.addOption(numberOfSimulationVectors).addOption(B).addOption(q)
				.addOption(d).addOption(chunkSize).addOption(threads)
				.addOption(threshold).addOption(help).addOption(simulate);

	}

	private static void evaluateCLIParameters(CommandLine line) {
		if (line.hasOption("threshold")) {
			SIMILARITY_THRESHOLD = Double.parseDouble(line
					.getOptionValue("threshold"));
		}
		if (line.hasOption("threads")) {
			NTHREADS = Integer.parseInt(line.getOptionValue("threads"));
		}
		if (line.hasOption("chunk")) {
			CHUNK_SIZE_CLASSIFIER_WORKER = Integer.parseInt(line
					.getOptionValue("chunk"));
		}
		if (line.hasOption("d")) {
			NUMBER_OF_RANDOM_VECTORS_d = Integer.parseInt(line
					.getOptionValue("d"));
		}
		if (line.hasOption("q")) {
			NUMBER_OF_PERMUTATIONS_q = Integer.parseInt(line
					.getOptionValue("q"));
		}
		if (line.hasOption("B")) {
			WINDOW_SIZE_B = Integer.parseInt(line.getOptionValue("B"));
		}
		if (line.hasOption("numberOfSimulationVectors")) {
			NUMBER_OF_SIMULATION_VECTORS = Integer.parseInt(line
					.getOptionValue("numberOfSimulationVectors"));
		}
		if (line.hasOption("numberOfSimulationVectorsClose")) {
			NUMBER_OF_SIMULATION_VECTORS_CLOSE = Integer.parseInt(line
					.getOptionValue("numberOfSimulationVectorsClose"));
		}
		if (line.hasOption("dimensionalityOfSimulationVectors")) {
			DIMENSIONS_OF_SIMULATION_VECTORS = Integer.parseInt(line
					.getOptionValue("dimensionalityOfSimulationVectors"));
		}
		if (line.hasOption("varianceOfSimulationVectorsClose")) {
			VARIANCE_OF_SIMULATION_VECTORS_CLOSE = Double.parseDouble(line
					.getOptionValue("varianceOfSimulationVectorsClose"));
		}
		if (line.hasOption("sparsityOfSimulationVectors")) {
			SPARSITY = Integer.parseInt(line
					.getOptionValue("sparsityOfSimulationVectors"));
		}
		if (line.hasOption("loadSimulationInputFile")) {
			INPUT_VECTORS_OUT_FILE = line
					.getOptionValue("loadSimulationInputFile");
		}
	}

	@SuppressWarnings("unchecked")
	private static void runSimulationBenchmark(boolean loadSimulationInputFile) {

		FeatureVector<? extends Number> searchVector = null;
		Set<FeatureVector<? extends Number>> inputVectors = null;
		if (loadSimulationInputFile) {
			try {
				logger.trace(
						"simulation started - started loading of random feature vectors from file %s",
						INPUT_VECTORS_OUT_FILE);
				FileInputStream fis = new FileInputStream(
						INPUT_VECTORS_OUT_FILE);
				ObjectInputStream o = new ObjectInputStream(fis);
				searchVector = (FeatureVector<? extends Number>) o.readObject();
				inputVectors = (Set<FeatureVector<? extends Number>>) o
						.readObject();
				o.close();
				DIMENSIONS_OF_SIMULATION_VECTORS = searchVector
						.getDimensionality();
				NUMBER_OF_SIMULATION_VECTORS = inputVectors.size();
				logger.trace("simulation started - finished loading of random feature vectors");

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			logger.trace("simulation started - started generation of random feature vectors");
			Integer[] searchVectorValues = new Integer[DIMENSIONS_OF_SIMULATION_VECTORS];
			for (int j = 0; j < DIMENSIONS_OF_SIMULATION_VECTORS; j++) {
				if (rnd.nextInt(SPARSITY + 1) % SPARSITY == 0) {
					searchVectorValues[j] = rnd
							.nextInt(SIMULATION_VECTOR_VALUE_SPACE);
				}
			}
			searchVector = new NumberListFeatureVector<Integer>(-1,
					searchVectorValues);

			inputVectors = generateSimulationVectors(searchVectorValues);
			if (NUMBER_OF_SIMULATION_VECTORS * inputVectors.size() <= 1000 * 20000) {
				try {
					FileOutputStream fos;
					fos = new FileOutputStream((new Date()).toString() + "_dim"
							+ DIMENSIONS_OF_SIMULATION_VECTORS + "_#"
							+ NUMBER_OF_SIMULATION_VECTORS + "_#c"
							+ NUMBER_OF_SIMULATION_VECTORS_CLOSE);
					ObjectOutputStream o = new ObjectOutputStream(fos);
					o.writeObject(searchVector);
					o.writeObject(inputVectors);
					o.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			logger.trace("simulation started - finished generation of random feature vectors");
		}

		List<Pair<Double, FeatureVector<? extends Number>>> neighbours = LSH
				.computeNeighbours(searchVector, inputVectors,
						SIMILARITY_THRESHOLD, NTHREADS,
						CHUNK_SIZE_CLASSIFIER_WORKER,
						NUMBER_OF_RANDOM_VECTORS_d, NUMBER_OF_PERMUTATIONS_q,
						WINDOW_SIZE_B);

		for (Pair<Double, FeatureVector<? extends Number>> match : neighbours) {
			logger.info(match.getValue().getId() + " : " + match.getKey());
		}

		Set<Integer> remaining = new HashSet<Integer>();
		for (Integer i = 0; i < NUMBER_OF_SIMULATION_VECTORS_CLOSE; i++) {
			remaining.add(i);
		}
		Set<Integer> falsePositives = new HashSet<Integer>();

		for (Pair<Double, FeatureVector<? extends Number>> match : neighbours) {
			if (!remaining.remove(match.getValue().getId())) {
				falsePositives.add(match.getValue().getId());
			}
		}

		logger.trace("%s of %s close vectors have been found",
				NUMBER_OF_SIMULATION_VECTORS_CLOSE - remaining.size(),
				NUMBER_OF_SIMULATION_VECTORS_CLOSE);
		logger.trace(remaining);

		logger.trace("%s false positives have been found",
				falsePositives.size());
		logger.trace(falsePositives);
	}

	private static Set<FeatureVector<? extends Number>> generateSimulationVectors(
			Integer[] searchVectorValues) {
		Set<FeatureVector<? extends Number>> inputVectors = new HashSet<FeatureVector<? extends Number>>();
		for (int i = 0; i < NUMBER_OF_SIMULATION_VECTORS_CLOSE; i++) {
			Integer[] closeValueFeatureValues = new Integer[DIMENSIONS_OF_SIMULATION_VECTORS];
			for (int j = 0; j < DIMENSIONS_OF_SIMULATION_VECTORS; j++) {
				int variance = (int) (SIMULATION_VECTOR_VALUE_SPACE * VARIANCE_OF_SIMULATION_VECTORS_CLOSE);
				if (searchVectorValues[j] != null) {
					closeValueFeatureValues[j] = searchVectorValues[j]
							- rnd.nextInt(variance) + rnd.nextInt(variance);
				} else {
					if (rnd.nextInt(SPARSITY * SPARSITY + 1) % SPARSITY
							* SPARSITY == 0) {
						closeValueFeatureValues[j] = -rnd.nextInt(variance)
								+ rnd.nextInt(variance);
					}
				}

			}
			FeatureVector<? extends Number> closeValueFeatureVector = new NumberListFeatureVector<Integer>(
					inputVectors.size(), closeValueFeatureValues);
			inputVectors.add(closeValueFeatureVector);
		}

		for (int i = 0; i < NUMBER_OF_SIMULATION_VECTORS; i++) {
			Integer[] randomFeatureValues = new Integer[DIMENSIONS_OF_SIMULATION_VECTORS];
			for (int j = 0; j < DIMENSIONS_OF_SIMULATION_VECTORS; j++) {
				if (rnd.nextInt(SPARSITY + 1) % SPARSITY == 0) {
					randomFeatureValues[j] = rnd
							.nextInt(SIMULATION_VECTOR_VALUE_SPACE);
				}
			}
			FeatureVector<? extends Number> randomFeatureVector = new NumberListFeatureVector<Integer>(
					inputVectors.size(), randomFeatureValues);
			inputVectors.add(randomFeatureVector);
		}
		return inputVectors;
	}
}
