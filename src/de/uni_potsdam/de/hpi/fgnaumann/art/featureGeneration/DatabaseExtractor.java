package de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class DatabaseExtractor {

	static Options options = null;
	static String connectionString = null;
	static Connection connection = null;
	static ResultSet resultSet = null;
	static Statement statement = null;

	public static void main(String[] args) {

		options = createOptions();
		CommandLineParser parser = new PosixParser();
		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("LSH", options);
			}
			if (evaluateCLIParameters(line)) {
				connectAndDoSomething();
			}
		} catch (ParseException exp) {
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		}

	}

	private static void connectAndDoSomething() {
		try {
			if (connectionString.contains("sqlite")) {
				Class.forName("org.sqlite.JDBC");
			} else if (connectionString.contains("postgresql")) {
				Class.forName("org.postgresql.Driver");
			} else {
				throw new IllegalArgumentException(
						"There is no known DBMS for your given connection string: "
								+ connectionString);
			}

			connection = DriverManager
					.getConnection(connectionString);
			statement = connection.createStatement();
			resultSet = statement
					.executeQuery("SELECT * FROM rss_news_source;");
			while (resultSet.next()) {
				System.out.println(resultSet.getString("name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				resultSet.close();
				statement.close();
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("static-access")
	private static Options createOptions() {
		Options cliOptions = new Options();

		Option connectionString = OptionBuilder
				.withArgName("connectionString")
				.hasArg()
				.withDescription(
						"the jdbc connection string, eg. 'jdbc:sqlite:/home/fabian/art/corpora/news.sqlite' or 'jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP' - DBMS type will be infered")
				.create("connectionString");

		Option help = new Option("help", "print this message");

		return cliOptions.addOption(connectionString).addOption(help);
	}

	private static boolean evaluateCLIParameters(CommandLine line) {
		if (line.hasOption("connectionString")) {
			connectionString = line.getOptionValue("connectionString");
		} else {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("DatabaseExtractor", options);
			return false;
		}
		return true;
	}
}