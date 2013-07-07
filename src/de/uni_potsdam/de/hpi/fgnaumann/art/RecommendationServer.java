package de.uni_potsdam.de.hpi.fgnaumann.art;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
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

	private static int PORT = 8181;

	public static void main(String[] args) throws Exception {
		// create Options object
				Options options = createOptions();
				// create the parser
				CommandLineParser parser = new PosixParser();
				try {
					// parse the command line arguments
					CommandLine line = parser.parse(options, args);
					if (line.hasOption("help")) {
						HelpFormatter formatter = new HelpFormatter();
						formatter.printHelp("RecommendationServer", options);
					}
					evaluateCLIParameters(line);
				} catch (ParseException exp) {
					// oops, something went wrong
					System.err.println("Parsing failed.  Reason: " + exp.getMessage());
				}
		WebServer webServer = new WebServer(PORT);
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
	
	@SuppressWarnings("static-access")
	private static Options createOptions() {
		Options cliOptions = new Options();

		Option port = OptionBuilder.withArgName("port").hasArg()
				.withDescription("the port of the XML-RPC-server")
				.create("port");
		Option help = new Option("help", "print this message");

		return cliOptions.addOption(port).addOption(help);

	}

	private static void evaluateCLIParameters(CommandLine line) {
		if (line.hasOption("port")) {
			PORT = Integer.parseInt(line
					.getOptionValue("port"));
		}
	}
}
