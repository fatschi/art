package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.net.URL;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;

import de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration.FeatureGenerator;
import de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration.AllFeaturesDatabaseExtractor.FeatureType;

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
		featureGenerator
				.runPreprocessing(
						0.0f,
						-1,
						FeatureType.ALL,
						3,
						"corpora/augmentedTFIDF.lsh",
						"jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP");
		lshRunner.runLSH(32, 10, 333);
		System.out.println(lshRunner.runSearch("27793", 0.8, 5, 32, 30, 1000));
		System.out.println(lshRunner.runSimulationBenchmark());
	}
}
