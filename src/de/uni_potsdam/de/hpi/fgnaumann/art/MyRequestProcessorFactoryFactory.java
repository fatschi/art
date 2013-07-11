package de.uni_potsdam.de.hpi.fgnaumann.art;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;

import de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration.FeatureGenerator;

/**
 * The {@link LSHRunner} factory which only creates a
 * {@link LSHRunnerImplementation} once and reuses it for further requests.
 * 
 * @author fabian
 * 
 */
public class MyRequestProcessorFactoryFactory implements
		RequestProcessorFactoryFactory {
	private final RequestProcessorFactory lshRunnerFactory = new LSHRunnerRequestProcessorFactory();
	private final RequestProcessorFactory featureGeneratorFactory = new FeatureGeneratorRequestProcessorFactory();
	private final LSHRunner lshRunner;
	private final FeatureGenerator featureGenerator;

	public MyRequestProcessorFactoryFactory(LSHRunner lshRunner,
			FeatureGenerator featureGenerator) {
		this.lshRunner = lshRunner;
		this.featureGenerator = featureGenerator;
	}

	public RequestProcessorFactory getRequestProcessorFactory(
			@SuppressWarnings("rawtypes") Class aClass) throws XmlRpcException {
		if (LSHRunner.class.isAssignableFrom(aClass)) {
			return lshRunnerFactory;
		} else if (FeatureGenerator.class.isAssignableFrom(aClass)) {
			return featureGeneratorFactory;
		} else
			return null;
	}

	private class LSHRunnerRequestProcessorFactory implements
			RequestProcessorFactory {
		public Object getRequestProcessor(XmlRpcRequest xmlRpcRequest)
				throws XmlRpcException {
			return lshRunner;
		}
	}

	private class FeatureGeneratorRequestProcessorFactory implements
			RequestProcessorFactory {
		public Object getRequestProcessor(XmlRpcRequest xmlRpcRequest)
				throws XmlRpcException {
			return featureGenerator;
		}
	}
}