package de.uni_potsdam.de.hpi.fgnaumann.art;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;

public class EchoRequestProcessorFactoryFactory implements
      RequestProcessorFactoryFactory {
    private final RequestProcessorFactory factory =
      new EchoRequestProcessorFactory();
    private final LSHRunner lshRunner;

    public EchoRequestProcessorFactoryFactory(LSHRunner echo) {
      this.lshRunner = echo;
    }

    public RequestProcessorFactory getRequestProcessorFactory(Class aClass)
         throws XmlRpcException {
      return factory;
    }

    private class EchoRequestProcessorFactory implements RequestProcessorFactory {
      public Object getRequestProcessor(XmlRpcRequest xmlRpcRequest)
          throws XmlRpcException {
        return lshRunner;
      }
    }
  }