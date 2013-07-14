package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.net.URL;

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
		
		//BEST
		featureGenerator
				.runPreprocessing(0.0f,
						-1,
						FeatureType.BEST,
						1,
						"corpora/final/bestCorpusMapVectorWithoutLSH.lsh",
						// "jdbc:sqlite:/home/fabian/art/corpora/news.sqlite");
						"jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP");
		lshRunner.loadData("corpora/final/bestCorpusMapVectorWithoutLSH.lsh");
		lshRunner.runLSH(32, 1000, 100);
		lshRunner.storeData("corpora/final/bestCorpusMapVectorWithLSH100.lsh");
		lshRunner.runLSH(32, 1000, 200);
		lshRunner.storeData("corpora/final/bestCorpusMapVectorWithLSH200.lsh");
		lshRunner.runLSH(32, 1000, 300);
		lshRunner.storeData("corpora/final/bestCorpusMapVectorWithLSH300.lsh");
		
		//BEST_WORST_3
		featureGenerator
				.runPreprocessing(
						0.0f,
						-1,
						FeatureType.BEST_WORST_N,
						3,
						"corpora/final/bestWorst3CorpusMapVectorWithoutLSH.lsh",
						"jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP");
		lshRunner
				.loadData("corpora/final/bestWorst3CorpusMapVectorWithoutLSH.lsh");
		lshRunner.runLSH(32, 1000, 100);
		lshRunner
				.storeData("corpora/final/bestWorst3CorpusMapVectorWithLSH100.lsh");
		lshRunner.runLSH(32, 1000, 200);
		lshRunner
				.storeData("corpora/final/bestWorst3CorpusMapVectorWithLSH200.lsh");
		lshRunner.runLSH(32, 1000, 300);
		lshRunner
				.storeData("corpora/final/bestWorst3CorpusMapVectorWithLSH300.lsh");
		
		//ALL
		featureGenerator
				.runPreprocessing(
						0.0f,
						-1,
						FeatureType.ALL,
						1,
						"corpora/final/allCorpusMapVectorWithoutLSH.lsh",
						"jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP");
		lshRunner
				.loadData("corpora/final/allCorpusMapVectorWithoutLSH.lsh");
		lshRunner.runLSH(32, 1000, 100);
		lshRunner
				.storeData("corpora/final/allCorpusMapVectorWithLSH100.lsh");
		lshRunner.runLSH(32, 1000, 200);
		lshRunner
				.storeData("corpora/final/allCorpusMapVectorWithLSH200.lsh");

		// long searchVector = 112236l;//113025
		// lshRunner.loadData("corpora/bestCorpusMapVectorWithLSH333.lsh");
		// System.out.println(lshRunner.runSearch(searchVector, 0.4, 100, 1));
		// lshRunner.loadData("corpora/bestCorpusMapVectorWithLSH100.lsh");
		// System.out.println(lshRunner.showVector(searchVector).getDimensionality());
		// System.out.println(lshRunner.runSearch(searchVector, 0.4, 100, 1));
		// lshRunner.loadData("corpora/bestWorst10CorpusMapVectorWithLSH100.lsh");
		// System.out.println(lshRunner.showVector(searchVector).getDimensionality());
		// System.out.println(lshRunner.runSearch(searchVector, 0.4, 100, 1));
	}
}
