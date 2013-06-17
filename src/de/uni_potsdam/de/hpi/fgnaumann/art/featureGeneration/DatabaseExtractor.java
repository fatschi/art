package de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;
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
	static boolean debug = true; // Print debug info
			
    

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
//				FIXME HashSet<String> descriptiveNouns = getDescriptiveNouns(); // Uncomment in case of new corpus
				
				// A dump of the most descriptive rss article corpus nouns .
				HashSet<String> descriptiveNouns = loadFakeCollectionSet();
				// Verbose
				if (debug) {
					for (String globalNoun : descriptiveNouns) {
						System.out.println(globalNoun);
					}
					System.out.println("NOUN#=" + descriptiveNouns.size());
				}
				
				int LIMIT = -1;
				LinkedList<HashMap<Integer, Float>> articleFeatureVecs = new LinkedList<HashMap<Integer, Float>>();
				HashMap<Integer, Long> termInNumDocsCounts = new HashMap<Integer, Long>(descriptiveNouns.size());		
				long docCount = genFeatureVecs(descriptiveNouns, LIMIT, articleFeatureVecs, termInNumDocsCounts);
				
				//TFIDF
				augment2TFIDF(new ArrayList<HashMap<Integer, Float>>(articleFeatureVecs), termInNumDocsCounts, docCount);
				
				writeFeatures(articleFeatureVecs, "corpora/augmentedTFIDF.ser");
				 
				LinkedList<HashMap<Integer, Float>> tfidfFeatures = readfeatures("corpora/augmentedTFIDF.ser");
				 
			}
		} catch (ParseException exp) {
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		}

	}

	private static void writeFeatures(LinkedList<HashMap<Integer, Float>> articleFeatureVecs, String path) {
		 try{
		      //use buffering
		      OutputStream file = new FileOutputStream(path);
		      OutputStream buffer = new BufferedOutputStream( file );
		      ObjectOutput output = new ObjectOutputStream( buffer );
		      try{
		        output.writeObject(articleFeatureVecs);
		      }
		      finally{
		        output.close();
		      }
		    }  
		    catch(IOException ex){
		      System.err.println("Cannot perform output." + ex);
		    }		
	}

	public static LinkedList<HashMap<Integer, Float>> readfeatures(String path) {
		LinkedList<HashMap<Integer, Float>> recoveredList  = null; 
		try{
		      //use buffering
		      InputStream file = new FileInputStream(path);
		      InputStream buffer = new BufferedInputStream( file );
		      ObjectInput input = new ObjectInputStream ( buffer );
		      try{
		        //deserialize the List
		    	recoveredList = (LinkedList<HashMap<Integer, Float>>) input.readObject();
		        //display its data
		        printFeatureVec(recoveredList);
		      }
		      finally{
		        input.close();
		      }
		    }
		    catch(ClassNotFoundException ex){
		      System.err.println("Cannot perform input. Class not found." + ex);
		    }
		    catch(IOException ex){
		    	System.err.println("Cannot perform input." + ex);
		    }
		return recoveredList;
	}

	private static void printFeatureVec(
			LinkedList<HashMap<Integer, Float>> recoveredList) {
		short i = 0;
		long entries = 0l;
		for (HashMap<Integer, Float> hashMap : recoveredList) {
			System.out.print(i++ + "\t");
			entries += hashMap.size(); // Count num features > 0
			for (Integer pos : hashMap.keySet()) {
				System.out.print(pos + "=" + hashMap.get(pos) + ", ");
			}
			System.out.println();
		}
		System.out.println("Features:" + entries);
	}

	/**
	 * Turn TF into TF IDF vector.
	 * @param articleFeatureVecs
	 * @param termInNumDocsCounts
	 * @param docCount
	 * @return 
	 */
	private static ArrayList<HashMap<Integer, Float>> augment2TFIDF(
				   ArrayList<HashMap<Integer, Float>> articleFeatureVecs,
					   HashMap<Integer, Long>   termInNumDocsCounts,
					   					long    docCount) {
		for (int i = 0; i!=articleFeatureVecs.size(); ++i) {
			HashMap<Integer, Float> featureVec = articleFeatureVecs.get(i);
			for (Integer pos : featureVec.keySet()) {
				float TF  = featureVec.get(pos);
				double IDF = Math.log((float) docCount/
						    (float) termInNumDocsCounts.get(pos));
				
				// Update the TF value to the TF IDF value
				featureVec.put(pos, (float) (TF*IDF));
			}
			articleFeatureVecs.set(i, featureVec);
		}
		return articleFeatureVecs;
	}

	/**
	 * Method that counts for every documents its feature counts
	 */
	private static long genFeatureVecs(HashSet<String> commonNouns, long limit, LinkedList<HashMap<Integer, Float>> articleFeatureVecs, HashMap<Integer, Long> termInNumDocsCounts) {
		/** IDF parts. */
		long doccount = 0;
		
		// Sort the global features so they appear in the same order in ever article feature vector.
		HashMap<String, Integer> globalFeaturePositionMap = toSortedGlobalFeatureMap(commonNouns);
		System.out.println(globalFeaturePositionMap);
		
		String LIMIT = "";
		if (limit != -1) {
			LIMIT = " LIMIT " + limit;
		}
		
		
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
					.executeQuery("SELECT id, cleaned_text FROM rss_article" + LIMIT + ";");
			
			
			// Create nounextraction object
			NounExtractor nE = new NounExtractor();
			int lines = 0;
			while (resultSet.next()) {
				// Feedback
				if (lines++%1000 ==0 && debug) {
					System.out.println("Processed Lines:" + (lines-1));
				}
				
				// Pattern
				// Ends with. previous page
				String fulltext = resultSet.getString("cleaned_text");
				
				// Skip articles that have no content.
				if(fulltext==null || fulltext.length() < 50) {
					continue;
				}
				
				++doccount;
				
				fulltext = cleanText(fulltext);
				
				// Generate augmentedTF feature vector. Also get IDF counts.
				articleFeatureVecs.add(nE.generateFeature(globalFeaturePositionMap, termInNumDocsCounts, fulltext));
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
		return doccount;
	}

	/**
	 * Sort the global features so they appear in the same order in every articles' feature vector.
	 * @param commonNouns
	 * @return
	 */
	private static HashMap<String, Integer> toSortedGlobalFeatureMap(
			HashSet<String> commonNouns) {
		HashMap<String, Integer> globalFeaturePositionMap = new HashMap<String, Integer>(commonNouns.size());
		ArrayList<String> collectionFeaturePosition = new ArrayList<String>(new TreeSet<String>(commonNouns)); // Sort them. e.g. lexicographically
		int i = 0;
		for (String gloabalFeature : collectionFeaturePosition) {
			globalFeaturePositionMap.put(gloabalFeature, i++);
		}
		return globalFeaturePositionMap;
	}

	/**
	 * Textual clean up.
	 * @param fulltext
	 * @return
	 */
	private static String cleanText(String fulltext) {
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
		return mspace.replaceAll(" ").trim();
	}

	private static void connectAndDoSomething() {
		HashMap<String, Long> collectionMap = new HashMap<String, Long>(1000, 0.95f);
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
					.executeQuery("SELECT id, cleaned_text FROM rss_article;");
			
			
			// Create nounextraction object
			NounExtractor nE = new NounExtractor();
			int lines = 0;
			while (resultSet.next()) {
//				System.out.println(resultSet.getString("id")+";"+resultSet.getString("cleaned_text"));
				
				// Pattern
				// Ends with. previous page
				String fulltext = resultSet.getString("cleaned_text");
				// Skip articles that have no content.
				if(fulltext==null || fulltext.length() < 50) {
					continue;
				}
				
				// Feedback
				if (lines++%1000 ==0 && debug) {
					System.out.println("Processed Lines:" + (lines-1));
				}
				
				fulltext = cleanText(fulltext);
				
				addNeededNouns(collectionMap, fulltext, nE); // Modifies the input map
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
		for (String globalNoun : collectionMap.keySet()) {
			System.out.println(globalNoun + ":" + collectionMap.get(globalNoun));
		} 
		System.out.println("NOUN#=" + collectionMap.size());
	}

	/**
	 * Extends the global map by a list of at most one new noun needed to describe ONE article.
	 * At most, because the map gets no adds if it already contains at least ONE noun that is 
	 * also present in the article.
	 * @param collectionMap
	 * @param fulltext
	 * @param nE
	 */
	private static void addNeededNouns(HashMap<String, Long> collectionMap, String fulltext, NounExtractor nE) {
		// Extract nouns
		long max = -1l;
		boolean alreadyGlobal = false;
		HashMap<String, Long> localNouns = nE.getLowercaseNouns(fulltext);
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
				if (localNouns.get(noun) > max) {
					max = localNouns.get(noun);
					mostFrequentLocal = noun;
				}
			} 
		}
		if (!alreadyGlobal && (mostFrequentLocal!=null)) {
			collectionMap.put(mostFrequentLocal, max);
		}
		
	}

	private static HashSet<String> getDescriptiveNouns() {
		HashSet<String> collectionMap = new HashSet<String>(1000, 0.95f);
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
					.executeQuery("SELECT id, cleaned_text FROM rss_article;");
			
			
			// Create nounextraction object
			NounExtractor nE = new NounExtractor();
			int lines = 0;
			while (resultSet.next()) {
//				System.out.println(resultSet.getString("id")+";"+resultSet.getString("cleaned_text"));
				
				// Pattern
				// Ends with. previous page
				String fulltext = resultSet.getString("cleaned_text");
				// Skip articles that have no content.
				if(fulltext==null || fulltext.length() < 50) {
					continue;
				}
				
				// Feedback
				if (lines++%1000 ==0 && debug) {
					System.out.println("Processed Lines:" + (lines-1));
				}
	
				fulltext = cleanText(fulltext);
				
				getNeededNounSet(collectionMap, fulltext, nE);
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
		
		return collectionMap;
	}
	
	/**
	 * Extends the global map by a list of at most one new noun needed to describe ONE article.
	 * At most, because the map gets no adds if it already contains at least ONE noun that is 
	 * also present in the article.
	 * <p>
	 * NOTE: This is a more minimal version of {@link #addNeededNouns(HashMap, String, NounExtractor)}
	 * @param collectionMap
	 * @param fulltext
	 * @param nE
	 * @return NONE. SET IS MODIFIED DIRECTLY.
	 */
	private static void getNeededNounSet(HashSet<String> collectionMap,
			String fulltext, NounExtractor nE) {
		// Extract nouns
		long max = -1l;
		boolean alreadyGlobal = false;
		HashMap<String, Long> localNouns = nE.getLowercaseNouns(fulltext);
		String mostFrequentLocal = null;
		for (String noun: localNouns.keySet()) {
			noun = noun.toLowerCase();
			if (collectionMap.contains(noun)) {
				alreadyGlobal = true;	
				// Update global noun counts
				collectionMap.add(noun);
			} else {
				// determine most frequent local noun
				if (localNouns.get(noun) > max) {
					max = localNouns.get(noun);
					mostFrequentLocal = noun;
				}
			} 
		}
		if (!alreadyGlobal && (mostFrequentLocal!=null)) {
			collectionMap.add(mostFrequentLocal);
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
		} /*else {
			System.err.println("ERROR: NO OUTPATH SPECIFIED");
			return false;
		}*/
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
	
	static HashSet<String> loadFakeCollectionSet() {
		HashSet<String> fake = new HashSet<String>();
		String globalNouns[] = "auction alliance snowfall need leeds president jeopardy fish lord party carluccio parade time thrones malley video somalia image companies smoke hits air salvador hospital instruments austin mclaren john boccanegra oil southampton trouble berry nicosia porsche cookies man barnet look tags beat eu web hour living fire titanic wiggins comments banks amendment hong bob firms surgery expenses doctor march planes challengers bang vibe victory dozens milan magicians savills odi timescast sale warwickshire sign australia mushroom update sex set ipad pulse education business argentina twin lights knicks england style earnings sites music mail winners thatcher precision speed houston source rome nba wedding peace lions pistons ireland manager elin harlequins street shuttle mcauliffe court french roth council operation barrier bangui futures villa lira nations thoughts ot screen museum health depression funds pc month inflation series creation home scott fashion inmates arbor click space publisher chicken joe volcano squash water enthusiasm bombs mr green links plane whale afp second yacht carpet stockton twitter lewis mayer newshour thompson howard hagel erizku credit inaoka peter bed printing league flood mattress trafalgar panel scotland chairman rafael pernisco moon loss cheese security stage network war army steve japan mungiu oprah agenda it knox list griffiths clegg election prank football hostel abortion spinach things energy howse havens fight elliott countries cornwall teachers temperature practices olympics snow movements year naughton signs books francis priebus ebert miami williams simpson sunday steubenville liew press taxi advertising use date boing benedict spring villiers strike life redknapp crisis baby hair confinement park celaya carnival news colville vegetables cyprus tv dog world percent com dreams arsenal kabul toronto album earthquake sports faber igor science bangkok carl takes goodies mud gummer cheltenham cars swansea control index prix cocaine billion shares james binge syrup cast islands sofia car raid android children singer re sun singapore hurricane hand child neruda tom top cbs approval feet blair alarm norma masters journalism birth surface fox test guardian norovirus tim people course hostages airport retron sunderland heather rearranges information wind dame problems triffids goat josh celebrities plagiarism jerusalem tax alcohol david position might budget medals line title words opera india garrido campaign richardson migration barcroft bali murray post coes mayor payphones gun point leg transplant russia cloud stones photographers phone bolt bus contrast minerva police woman power gym shoe alvarez work patrick hartley mark picture market mars food company images filter west bolden jane messina fans paper movies greenfield state north apartments hatches religions witches york couple office film aerospace chaff boycott pyro minutes inch justin hanson gates women george maps yn turkey".split(" ");
		for (String string : globalNouns) {
			fake.add(string);
		}
		return fake;
	}
}