package de.uni_potsdam.de.hpi.fgnaumann.art;

import java.net.URL;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;
/**
 * An example implementation of an Apache-XML-RPC-client communicating with a {@link RecommendationServer}. 
 */

public class RecommendationClient {
    public static void main(String[] args) throws Exception {
      XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
      config.setServerURL(new URL("http://127.0.0.1:8080/xmlrpc"));
      config.setEnabledForExtensions(true);
      config.setConnectionTimeout(60 * 1000);
      config.setReplyTimeout(60 * 1000);
      XmlRpcClient client = new XmlRpcClient();
      client.setConfig(config);
      ClientFactory factory = new ClientFactory(client);
      LSHRunner lshRunner = (LSHRunner) factory.newInstance(LSHRunner.class);
      //lshRunner.runLSH("corpora/augmentedTFIDF.lsh");
      //System.out.println(lshRunner.runSearch("corpora/augmentedTFIDF.lsh", "23604"));
      System.out.println(lshRunner.runSimulationBenchmark());
    }
  }
