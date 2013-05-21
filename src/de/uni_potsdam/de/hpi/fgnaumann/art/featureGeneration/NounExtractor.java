package de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.xml.sax.InputSource;

import qtag.Tagger;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.sax.HTMLFetcher;

/**
 * Class to extract only the nouns from a text. NLP parsing task. 1. Divide text
 * into sentences. 2. POS tag sentences. 3. Only keep nouns. 4. Generate a
 * vector given a list of the most common nouns in all documents.
 * 
 * @author Nils Rethmeier
 */
public class NounExtractor {

	static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
	static final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();
	static final ArticleExtractor FULLTEXT_EXTRACTOR = new ArticleExtractor();
	static BufferedReader BR = null;
	/*
	 * Controls how many strings are buffered as one large text line before they
	 * are tagged. REASON: Tagger is a lot faster when its used less often
	 * instead of often n small test
	 */
	static final int LINES_TO_BUFFER = 100;
    private Tagger tagger = null; // .mat ending is implicit
	
	public NounExtractor() throws IOException {
		tagger = new Tagger("lib/english");
	}
	
	/**
	 * Initialize a LineReader so that the class can extraxt HTLM 
	 * pages from a corpus.
	 * @param bufr
	 * @throws IOException
	 */
	public NounExtractor(BufferedReader bufr) throws IOException {
		Tagger tagger = new Tagger("lib/english");
		BR = bufr;
	}

	public static BufferedReader makeReader(String path) throws FileNotFoundException, UnsupportedEncodingException {
		FileInputStream fis = new FileInputStream(path);
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(in);
		return br;
	}

	/**
	 * Extracts a plain text, one article per sentence, version from
	 * the SQL dump corpus. Cleans up uneeded clutter.
	 * @param args
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void extractFullTextCorpus(String[] args)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {
		args[0] = "/media/zwerg/art/rss_article.sql.earaseEndingToUse";
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(args[0] + "__Nouns.txt"), "ascii"));
		NounExtractor nE = new NounExtractor(makeReader(args[0]));
		String page = null;
		long count = 0;
		try { // Note: The first line in the results might be database clutter
			while ((page = nE.nextWebPage()) != null) {
				page = nE.htmlToFulltext(page).replaceAll("', '", "").replaceAll(" +", " ").trim(); // Removes unnecessary spaces and left over ', ' clutter.
				bw.write(page.replace("Custom Content\nour partners\n", "") // removes boiler plate left overs
							 .replaceAll("Follow \\@\\w+ ", "")				// removes twitter tags
							 .replaceAll("\n", " ")                         // turn article into one lines
							 + "\n");          								// Write article into one line
				if (++count % 100 ==0) {
					System.out.println(count + " pages processed");
				}
				
			}
		} catch (Exception e) {
			System.err.println(page);
			e.printStackTrace();
		}
		finally {
			nE.close(); // Once all pages have been parsed
			bw.close();
		}
	}

	/**
	 * Close the class's {@link BufferedReader}.
	 */
	private void close() {
		try {
			BR.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String nextWebPage() throws IOException {
		String line = null;
		StringBuffer paragraph = new StringBuffer();
		try {
			while ((line = BR.readLine()) != null) {
				if (line.contains("</head>")) {
					return paragraph.toString().replaceAll("''", "'"); // End of this document. Continue Later on 
				}
				paragraph.append(line);
				paragraph.append('\n');
			}
		} catch (Exception e) {
		} 
		return null;
	}

	/**
	 * Test function to test the fultext extractor
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private static void fulltextTest() throws FileNotFoundException,
			UnsupportedEncodingException, IOException {
		// Fulltext Test
		BufferedReader br = makeReader("TestDocs/fulltextTest2.html");
		String line = null;
		StringBuffer paragraph = new StringBuffer();
		try {
			while ((line = br.readLine()) != null) {
				paragraph.append(line);
				paragraph.append('\n');
			}
			String para = paragraph.toString().replaceAll("''", "'");
			System.out.println(FULLTEXT_EXTRACTOR.getText(para));
		} catch (Exception e) {
		} finally {
			br.close();
		}
	}

	/**
	 * Method to create a noun (1-gram) corpus.
	 * Uses line buffering to speed up the required POS tagger.
	 * @param location path to the corpus.
	 * @throws IOException
	 */
	public void extractNounsAndStoreToFile(String location) throws IOException {
		// INIT
		HashMap<String, Long> posMap = new HashMap<String, Long>(10000, 0.95f);

		// INPUT
		FileInputStream fis = new FileInputStream(location);
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(in);

		// to POS
		long linecount = 0L;
		String line = null;
		boolean stillBuffered = false;
		StringBuffer oneLongString = new StringBuffer(100 * LINES_TO_BUFFER); // Avoid
																				// resizing.

		// Process the input text
		while ((line = br.readLine()) != null) {
			stillBuffered = true;

			oneLongString.append(line);
			oneLongString.append(" ");

			++linecount;

			// buffering for processing speed of the tagger
			if (linecount % LINES_TO_BUFFER == 0) {
				addAll(posMap, getNouns(oneLongString.toString()));
				stillBuffered = false;								// Reset buffer
				oneLongString.delete(0, oneLongString.length());    // ... 
			}
			
			// feedback as percent
			if (linecount % 100000 == 0) {
				// break;
				System.out.println(linecount);
			}

			// To catch non buffered left overs. Do the last few lines.
			if (stillBuffered) {
				addAll(posMap, getNouns(oneLongString.toString()));
				stillBuffered = false;
				oneLongString.delete(0, oneLongString.length());
			}
		}
		
		// OUTPUT
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(location + "_CommonNouns.txt"), "ascii"));
		for (String poskey : posMap.keySet()) {
			bw.write(posMap.get(poskey) + "\t" + poskey + "\n");
		}
		
		// STATS
		System.out.println("# lines processed:" + linecount);
		bw.close();
	}

	/**
	 * Add new noun counts to previous noun counts.
	 * @param posMap
	 * @param nouns
	 */
	private void addAll(HashMap<String, Long> posMap,
			HashMap<String, Long> nouns) {
		Long count = 0l;
		for (String noun : nouns.keySet()) {
			count = posMap.get(noun);
			if (count!=null) {
				posMap.put(noun, nouns.get(noun) + count); // Add previous
			} else {
				posMap.put(noun, nouns.get(noun));		   // Store latest
			}
		}
	}

	/**
	 * Extract nouns from a space concatenated list of sentences.
	 * Multiple sentences work best.
	 * @param oneLongString
	 * @return
	 */
	public HashMap<String, Long> getNouns(String oneLongString) {
		
		// Result
		HashMap<String, Long> nouns = new HashMap<String, Long>();
		
		List<String> tokenList = new ArrayList<String>();
		List<String> whiteList = new ArrayList<String>();
		
		// Tokenize (chunk) the text.
		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(
				oneLongString.toCharArray(), 0,
				oneLongString.length());
		tokenizer.tokenize(tokenList, whiteList);
		String[] tokens = tokenList.toArray(new String[tokenList.size()]); // Words
		String[] tags = this.tagger.tag(tokens);								   // POS tag per word
		
		// Save found nouns to HashMap
		for (int i = 0; i < tags.length; i++) {
			if (tags[i] == null          ||   // words like, capita and fait appelli produce null POS tags. Are infact nouns.
				tags[i].startsWith("FW") ||   // foreign words are often names of objects, concepts or people
				tags[i].startsWith("N")   ){   // Noun types
//				tags[i].startsWith("??")) {   // Unknowns (we assume nouns, names etc.) Details in README.md.
//				if (tokens[i].matches("\\A\\w{2,}\\z")) { // Only words not signs or the like. min 2 letters
					if (tokens[i].matches("^[a-zA-Z]{2,}+$")) { // Only words not signs or the like. min 2 letters
				Long oldcount = nouns.get(tokens[i]);
				if (oldcount != null) {
					nouns.put(tokens[i], oldcount + 1); // found once more
				} else {
					nouns.put(tokens[i], 1L); 			// found once
				}
				}
			}
			}
		return nouns;
	}
	
	/**
	 * Extracts Fulltext from an HTML Document.
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
	
	public static void main(String[] args) throws IOException{
//		extractFullTextCorpus(args);
//		String path = "TestDocs/POSparseTest.txt";
		String path = "/media/zwerg/art/rss_article.sql_Nouns.txt";
		NounExtractor nE = new NounExtractor();
		nE.extractNounsAndStoreToFile(path);
	}
}

