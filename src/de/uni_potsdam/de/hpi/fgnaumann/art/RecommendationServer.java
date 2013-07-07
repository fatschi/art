package de.uni_potsdam.de.hpi.fgnaumann.art;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;
/**
 * An standalone Apache XML-RPC-server allowing to communicate with a running instance of a {@link LSHRunner}.
 * @author fabian
 *
 */
public class RecommendationServer {

	public static void main(String[] args) throws Exception {
		WebServer webServer = new WebServer(8080);
	      XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
	      PropertyHandlerMapping phm = new PropertyHandlerMapping();
	      LSHRunner lshRunner = new LSHRunnerImplementation();
	      phm.setRequestProcessorFactoryFactory(new LSHRunnerRequestProcessorFactoryFactory(lshRunner));
	      phm.setVoidMethodEnabled(true);
	      phm.addHandler(LSHRunner.class.getName(), LSHRunner.class);
	      xmlRpcServer.setHandlerMapping(phm);

	      XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
	      serverConfig.setEnabledForExtensions(true);
	      serverConfig.setContentLengthOptional(false);
	      webServer.start();
	}
}
