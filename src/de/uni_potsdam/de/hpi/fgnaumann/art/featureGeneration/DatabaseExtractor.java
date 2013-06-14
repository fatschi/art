package de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration;

import java.io.ObjectInputStream.GetField;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

/**
 * 
 * @author Nils R. (TF-IDF Extraction), Fabian T. (SQLITE USAGE)
 *
 */
public class DatabaseExtractor {

	static Options options = null;
	static String connectionString = null;
	static String outPath = null;
	static Connection connection = null;
	static ResultSet resultSet = null;
	static Statement statement = null;
	/**Tweet remover*/
	static public Pattern tweets 		= Pattern.compile("Follow \\@\\w+");
	/**Links*/
	static public Pattern links 		= Pattern.compile("http\\S*\\s");
	static public Pattern htmltags 		= Pattern.compile("\\<.*?>");
	/**Too many whitespace remover*/
	static public Pattern multispaces  = Pattern.compile(" +");
	/**Multi to one line*/
	static public Pattern multiline    = Pattern.compile("\n");
	static public int PPLEN = new String("Previous Page").length();
	static final ArticleExtractor FULLTEXT_EXTRACTOR = new ArticleExtractor();
			
    

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
					.executeQuery("SELECT id, cleaned_text FROM rss_article LIMIT 100;");
			
			
			// Create nounextraction object
			NounExtractor nE = new NounExtractor();
			HashMap<String, Long> collectionMap = new HashMap<String, Long>(10000, 0.95f);
			while (resultSet.next()) {
//				System.out.println(resultSet.getString("id")+";"+resultSet.getString("cleaned_text"));
				
				// Pattern
				// Ends with. previous page
				String fulltext = resultSet.getString("cleaned_text");
				if (fulltext.endsWith("Previous Page")) {
					// rem the link text
					fulltext = fulltext.substring(0,fulltext.length()-PPLEN-1);
				}
				
				
				// Rem tweet tags
				Matcher lnks = links.matcher(fulltext);
				fulltext = lnks.replaceAll(" ");
				
				// Erase HTML tags
				Matcher tags = htmltags.matcher(fulltext);
				fulltext = tags.replaceAll(" ");
				
				
				// Rem tweet tags
				Matcher twt = tweets.matcher(fulltext);
				fulltext = twt.replaceAll("");
				// Rem multiline
				Matcher mline = multiline.matcher(fulltext);
				fulltext = mline.replaceAll(" ");
				// Rem multispace
				Matcher mspace = multispaces.matcher(fulltext);
				fulltext = mspace.replaceAll(" ").trim();
				if (fulltext.contains("<")){
					// some non-sense left overs
					System.err.println(fulltext);
				}
				
				// Extract nouns
				long count = -1l;
				boolean alreadyGlobal = false;
				HashMap<String, Long> localNouns = nE.getNouns(fulltext);
				String mostFrequentLocal = null;
				for (String noun: localNouns.keySet()) {
					noun = noun.toLowerCase();
					if (collectionMap.containsKey(noun)) {
						alreadyGlobal = true;	
						Long oldCount = collectionMap.get(noun);
						Long newOccur = localNouns.get(noun);
						// Update global noun counts
						collectionMap.put(noun, oldCount + newOccur);
					} else {
						// determine most frequent local noun
						
					} 
				}
				
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
						"the jdbc connection string, eg. 'jdbc:sqlite:/media/zwerg/art/ARTposgresdb/news.sqlite' or 'jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP' - DBMS type will be infered")
				.create("connectionString");

		Option help = new Option("help", "print this message");
		Option out = OptionBuilder
				.withArgName("out")
				.hasArg()
				.withDescription(
						"the jdbc connection string, eg. 'jdbc:sqlite:/media/zwerg/art/ARTposgresdb/news.sqlite' or 'jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP' - DBMS type will be infered")
				.create("out");

		return cliOptions.addOption(connectionString).addOption(help).addOption(out);
	}

	private static boolean evaluateCLIParameters(CommandLine line) {
		if (line.hasOption("connectionString")) {
			connectionString = line.getOptionValue("connectionString");
		} else {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("DatabaseExtractor", options);
			return false;
		}
		if (line.hasOption("out")) {
			outPath = line.getOptionValue("out");
		} else {
			System.err.println("ERROR: NO OUTPATH SPECIFIED");
			return false;
		}
		return true;
	}
	
	/**
	 * Extracts Fulltext from an HTML Document.
	 * 
	 * @param html
	 * @return
	 */
	public String htmlToFulltext(String html) {
		String fulltext = null;
		try {
			fulltext = FULLTEXT_EXTRACTOR.getText(html);
		} catch (BoilerpipeProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fulltext;
	}
}