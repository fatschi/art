package de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import qtag.Tagger;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

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
	/*
	 * Controls how many strings are buffer as one large text line before they
	 * are tagged. REASON: Tagger is a lot faster when its used less often
	 * instead of often n small test
	 */
	static final int LINES_TO_BUFFER = 100;
	static Tagger tagger = null; // .mat ending is implicit

	public static void main(String[] args) throws IOException {

		// INIT
		HashMap<String, Long> posMap = new HashMap<String, Long>(50, 0.95f);
		Tagger tagger = new Tagger("lib/qtag-eng");

		// INPUT
		String location = args[0];
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
				posMap.putAll(getNouns(oneLongString.toString()));
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
				posMap.putAll(getNouns(oneLongString.toString()));
				stillBuffered = false;
				oneLongString.delete(0, oneLongString.length());
			}

			// OUTPUT
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(location + "_Nouns"), "ascii"));
			for (String poskey : posMap.keySet()) {
				bw.write(poskey + "\t" + posMap.get(poskey) + "\n");
			}
			
			// STATS
			System.out.println("# lines processed:" + linecount);
			bw.close();
		}
	}

	/**
	 * Extract nouns from a 
	 * @param oneLongString
	 * @return
	 */
	private static HashMap<String, Long> getNouns(String oneLongString) {
		
		// Result
		HashMap<String, Long> nouns = new HashMap<String, Long>();
		
		List<String> tokenList = new ArrayList<String>();
		List<String> whiteList = new ArrayList<String>();
		
		// Tokenize (chunk) the text.
		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(
				oneLongString.toCharArray(), 0,
				oneLongString.length());
		tokenizer.tokenize(tokenList, whiteList);
		String[] tokens = new String[tokenList.size()]; // Words
		String[] tags = tagger.tag(tokens);				// POS tag per word
		
		// Save found nouns to HashMap
		for (int i = 0; i < tags.length; i++) {
			if (tags[i].startsWith("N") ||   // Noun types
				tags[i].startsWith("??")) {  // Unknowns (we assume nouns, names etc.) Details in README.md.
				Long oldcount = nouns.get(tokens[i]);
				if (oldcount != null) {
					nouns.put(tokens[i], oldcount + 1); // found once more
				} else {
					nouns.put(tokens[i], 1L); 			// found once
				}
			}
		}
		return nouns;
	}
}

