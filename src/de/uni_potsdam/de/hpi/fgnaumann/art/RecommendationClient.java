package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;

import de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration.AllFeaturesDatabaseExtractor.FeatureType;
import de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration.FeatureGenerator;

/**
 * An example implementation of an Apache-XML-RPC-client communicating with a
 * {@link RecommendationServer}.
 */

public class RecommendationClient {
	private static Logger logger = LogManager
			.getFormatterLogger(RecommendationClient.class.getName());

	private static int LIMIT = -1;

	public static void main(String[] args) throws Exception {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL("http://isfet:8282/xmlrpc"));
		config.setEnabledForExtensions(true);
		config.setConnectionTimeout(0);
		config.setReplyTimeout(0);
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);
		ClientFactory factory = new ClientFactory(client);
		LSHRunner lshRunner = (LSHRunner) factory.newInstance(LSHRunner.class);
		FeatureGenerator featureGenerator = (FeatureGenerator) factory
				.newInstance(FeatureGenerator.class);
		
		//PREPROCESSING
		// BEST
		featureGenerator
				.runPreprocessing(0.0f,
						LIMIT,
						FeatureType.BEST,
						1,
						"corpora/final/bestCorpusMapVectorWithoutLSH.lsh",
						"jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP");
		
		lshRunner.runLSH(32, 1000, 300);
		lshRunner.storeData("corpora/bestCorpusMapVectorWithLSH300.lsh");

		// BEST_WORST_3
		featureGenerator
				.runPreprocessing(
						0.0f,
						LIMIT,
						FeatureType.BEST_WORST_N,
						3,
						"corpora/bestWorst3CorpusMapVectorWithoutLSH.lsh",
						"jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP");
		
		lshRunner.runLSH(32, 1000, 300);
		lshRunner
				.storeData("corpora/bestWorst3CorpusMapVectorWithLSH300.lsh");

		// ALL
		featureGenerator
				.runPreprocessing(
						0.0f,
						LIMIT,
						FeatureType.ALL,
						1,
						"corpora/allCorpusMapVectorWithoutLSH.lsh",
						"jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP");
		lshRunner.loadData("corpora/allCorpusMapVectorWithoutLSH.lsh");
		lshRunner.runLSH(32, 1000, 300);
		
		lshRunner.storeData("corpora/allCorpusMapVectorWithLSH300.lsh");

		//QUERYING
		long searchVectorId = 113025;

		lshRunner.loadData("corpora/allCorpusMapVectorWithLSH300.lsh");
		logger.trace(lshRunner.runSearch(searchVectorId, 0.5, 10, 32));

	}

}
