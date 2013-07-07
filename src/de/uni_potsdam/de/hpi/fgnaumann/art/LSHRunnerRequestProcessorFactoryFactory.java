package de.uni_potsdam.de.hpi.fgnaumann.art;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;
/**
 * The {@link LSHRunner} factory which only creates a {@link LSHRunnerImplementation} once and reuses it for further requests.
 * @author fabian
 *
 */
public class LSHRunnerRequestProcessorFactoryFactory implements
      RequestProcessorFactoryFactory {
    private final RequestProcessorFactory factory =
      new LSHRunnerRequestProcessorFactory();
    private final LSHRunner lshRunner;

    public LSHRunnerRequestProcessorFactoryFactory(LSHRunner lshRunner) {
      this.lshRunner = lshRunner;
    }

    public RequestProcessorFactory getRequestProcessorFactory(@SuppressWarnings("rawtypes") Class aClass)
         throws XmlRpcException {
      return factory;
    }

    private class LSHRunnerRequestProcessorFactory implements RequestProcessorFactory {
      public Object getRequestProcessor(XmlRpcRequest xmlRpcRequest)
          throws XmlRpcException {
        return lshRunner;
      }
    }
  }