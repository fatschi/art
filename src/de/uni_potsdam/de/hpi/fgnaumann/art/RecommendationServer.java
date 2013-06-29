package de.uni_potsdam.de.hpi.fgnaumann.art;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

public class RecommendationServer {

	public static void main(String[] args) throws Exception {
		WebServer webServer = new WebServer(8080);
	      XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
	      PropertyHandlerMapping phm = new PropertyHandlerMapping();
	      LSHRunner lshRunner = new LSHRunner();
	      phm.setRequestProcessorFactoryFactory(new EchoRequestProcessorFactoryFactory(lshRunner));
	      phm.setVoidMethodEnabled(true);
	      phm.addHandler(LSHRunner.class.getName(), LSHRunner.class);
	      xmlRpcServer.setHandlerMapping(phm);

	      XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
	      serverConfig.setEnabledForExtensions(true);
	      serverConfig.setContentLengthOptional(false);
	      webServer.start();
	}
}
