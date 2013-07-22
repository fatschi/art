package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;

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
		// FeatureGenerator featureGenerator = (FeatureGenerator) factory
		// .newInstance(FeatureGenerator.class);
		//
		// // BEST
		// featureGenerator
		// .runPreprocessing(0.0f,
		// LIMIT,
		// FeatureType.BEST,
		// 1,
		// "corpora/final/bestCorpusMapVectorWithoutLSH.lsh",
		// // "jdbc:sqlite:/home/fabian/art/corpora/news.sqlite");
		// "jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP");
		// lshRunner.loadData("corpora/final/bestCorpusMapVectorWithoutLSH.lsh");
		// lshRunner.runLSH(32, 1000, 100);
		// lshRunner.storeData("corpora/final/bestCorpusMapVectorWithLSH100.lsh");
		// lshRunner.runLSH(32, 1000, 200);
		// lshRunner.storeData("corpora/final/bestCorpusMapVectorWithLSH200.lsh");
		// lshRunner.runLSH(32, 1000, 300);
		// lshRunner.storeData("corpora/final/bestCorpusMapVectorWithLSH300.lsh");
		//
		// // BEST_WORST_3
		// featureGenerator
		// .runPreprocessing(
		// 0.0f,
		// LIMIT,
		// FeatureType.BEST_WORST_N,
		// 3,
		// "corpora/final/bestWorst3CorpusMapVectorWithoutLSH.lsh",
		// "jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP");
		// lshRunner
		// .loadData("corpora/final/bestWorst3CorpusMapVectorWithoutLSH.lsh");
		// lshRunner.runLSH(32, 1000, 100);
		// lshRunner
		// .storeData("corpora/final/bestWorst3CorpusMapVectorWithLSH100.lsh");
		// lshRunner.runLSH(32, 1000, 200);
		// lshRunner
		// .storeData("corpora/final/bestWorst3CorpusMapVectorWithLSH200.lsh");
		// lshRunner.runLSH(32, 1000, 300);
		// lshRunner
		// .storeData("corpora/final/bestWorst3CorpusMapVectorWithLSH300.lsh");
		//
		// // ALL
		// featureGenerator
		// .runPreprocessing(
		// 0.0f,
		// LIMIT,
		// FeatureType.ALL,
		// 1,
		// "corpora/final/allCorpusMapVectorWithoutLSH.lsh",
		// "jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP");
		// lshRunner.loadData("corpora/final/allCorpusMapVectorWithoutLSH.lsh");
		// lshRunner.runLSH(32, 1000, 100);
		// lshRunner.storeData("corpora/final/allCorpusMapVectorWithLSH100.lsh");
		// lshRunner.runLSH(32, 1000, 200);
		// lshRunner.storeData("corpora/final/allCorpusMapVectorWithLSH200.lsh");
		
		long searchVector = 113025;// 113025
		
//		lshRunner
//		.loadData("corpora/final/bestCorpusMapVectorWithLSH100.lsh");
//		logger.trace(lshRunner.listVectorIds().size());
//		logger.trace("load cache");
//		logger.trace(lshRunner.runSearch(searchVector, 0.4, 100, 1));
//		logger.trace("single core");
//		logger.trace(lshRunner.runSearch(searchVector, 0.4, 100, 1));
//		logger.trace("8 core");
//		logger.trace(lshRunner.runSearch(searchVector, 0.4, 100, 8));
//		logger.trace("16 core");
//		logger.trace(lshRunner.runSearch(searchVector, 0.4, 100, 16));
//		logger.trace("32 core");
//		logger.trace(lshRunner.runSearch(searchVector, 0.4, 100, 32));
//		logger.trace("32 core, 32 permutation");
//		logger.trace(lshRunner.runSearch(searchVector, 0.4, 100, 32, 32, 100));
		
		
		Set<Long> union = new TreeSet<Long>();

		lshRunner.loadData("corpora/final/allCorpusMapVectorWithLSH100.lsh");
		logger.trace(lshRunner.runSearch(searchVector, 0.4, 50, 1));
		union.addAll(toIdList(lshRunner.runSearch(searchVector, 0.4, 50, 1)));
//		lshRunner
//				.loadData("corpora/final/bestWorst10CorpusMapVectorWithLSH100.lsh");
//		union.addAll(toIdList(lshRunner.runSearch(searchVector, 0.4, 100, 1)));
//		lshRunner
//				.loadData("corpora/final/bestWorst3CorpusMapVectorWithLSH300.lsh");
//		union.addAll(toIdList(lshRunner.runSearch(searchVector, 0.4, 100, 1)));
//		lshRunner.loadData("corpora/final/bestCorpusMapVectorWithLSH300.lsh");
//		union.addAll(toIdList(lshRunner.runSearch(searchVector, 0.4, 100, 1)));
		lshRunner.loadData("corpora/final/bestCorpusMapVectorWithLSH100.lsh");
		union.addAll(toIdList(lshRunner.runSearch(searchVector, 0.4, 50, 1)));

		logger.trace(union);
		logger.trace(union.size());

		Set<Long> intersection1 = new TreeSet<Long>(union);

		lshRunner.loadData("corpora/final/allCorpusMapVectorWithLSH100.lsh");
		intersection1.retainAll(toIdList(lshRunner.runSearch(searchVector, 0.4,
				50, 1)));
//		lshRunner
//				.loadData("corpora/final/bestWorst10CorpusMapVectorWithLSH100.lsh");
//		intersection1.retainAll(toIdList(lshRunner.runSearch(searchVector, 0.4,
//				100, 1)));
//		lshRunner
//				.loadData("corpora/final/bestWorst3CorpusMapVectorWithLSH300.lsh");
//		intersection1.retainAll(toIdList(lshRunner.runSearch(searchVector, 0.4,
//				100, 1)));
		lshRunner.loadData("corpora/final/bestCorpusMapVectorWithLSH300.lsh");
		intersection1.retainAll(toIdList(lshRunner.runSearch(searchVector, 0.4,
				50, 1)));
//		lshRunner.loadData("corpora/final/bestCorpusMapVectorWithLSH100.lsh");
//		intersection1.retainAll(toIdList(lshRunner.runSearch(searchVector, 0.4,
//				100, 1)));

		logger.trace(intersection1);
		logger.trace(intersection1.size());

		Set<Long> intersection2 = new TreeSet<Long>(union);

		lshRunner.loadData("corpora/final/allCorpusMapVectorWithLSH100.lsh");
		// intersection2.retainAll(toIdList(lshRunner.runSearch(searchVector,
		// 0.4, 100, 1)));
		lshRunner
				.loadData("corpora/final/bestWorst10CorpusMapVectorWithLSH100.lsh");
		// intersection2.retainAll(toIdList(lshRunner.runSearch(searchVector,
		// 0.4, 100, 1)));
		lshRunner
				.loadData("corpora/final/bestWorst3CorpusMapVectorWithLSH300.lsh");
		//intersection2.retainAll(toIdList(lshRunner.runSearch(searchVector, 0.4,
		//		100, 1)));
		lshRunner.loadData("corpora/final/bestCorpusMapVectorWithLSH300.lsh");
		intersection2.retainAll(toIdList(lshRunner.runSearch(searchVector, 0.4,
				100, 1)));
		lshRunner.loadData("corpora/final/bestCorpusMapVectorWithLSH100.lsh");
		intersection2.retainAll(toIdList(lshRunner.runSearch(searchVector, 0.4,
				100, 1)));

		logger.trace(intersection2);
		logger.trace(intersection2.size());
	}

	private static Collection<Long> toIdList(List<Pair<Double, Long>> resultList) {
		Set<Long> tempSet = new TreeSet<Long>();
		for (Pair<Double, Long> pair : resultList) {
			tempSet.add(pair.getValue());
		}
		return tempSet;

	}
}
