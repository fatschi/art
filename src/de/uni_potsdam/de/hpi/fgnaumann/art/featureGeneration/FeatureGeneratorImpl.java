package de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.TreeSet;

import de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration.AllFeaturesDatabaseExtractor.FeatureType;
import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;

public class FeatureGeneratorImpl implements FeatureGenerator {

	public NavigableSet<FeatureVector<Double>> articleFeatureVecs = new TreeSet<FeatureVector<Double>>();
	public HashMap<Integer, Long> termInNumDocsCounts = null;
	public HashMap<String, Integer> globalFeaturePositionMap = null;
	public NounExtractor nE = null; // Actual extractor is exchangeable
	public long docCount = 0;
	private long originalCollectionSize = 0;
	@SuppressWarnings("unused")
	private float addlimit = 0.0f;

	@Override
	public void runPreprocessingRead(String filePath, String connectionString)
			throws IOException, ClassNotFoundException, SQLException {	

		docCount = AllFeaturesDatabaseExtractor.readDocCount("CollectionDoccount.lsh");
		HashMap<Integer, Long> termInNumDocsCounts = AllFeaturesDatabaseExtractor.readIDFcounts("IDFtermCounts.lsh");
		HashMap<String, Integer> globalFeaturePositionMap = AllFeaturesDatabaseExtractor.readGlobalPositionMAP("globalFeaturePosition.lsh");
	}

	@Override
	public void runPreprocessing(float addLimit, int limit, FeatureType ftype,
			int featureN, String filePath, String connectionString)
			throws IOException, ClassNotFoundException, SQLException {
		HashSet<String> descriptiveNouns = null; // reset
		descriptiveNouns = AllFeaturesDatabaseExtractor.getAllNouns(ftype,
				featureN, limit, connectionString); // Get all nouns from the
													// corpus
		articleFeatureVecs = new TreeSet<FeatureVector<Double>>();
		termInNumDocsCounts = new HashMap<Integer, Long>(
				descriptiveNouns.size());
		globalFeaturePositionMap = new HashMap<String, Integer>(
				descriptiveNouns.size(), 1.0f);
		nE = new NounExtractor(); // Actual extractor is exchangable
		long docCount = AllFeaturesDatabaseExtractor.genFeatureVecs(
				descriptiveNouns, limit, articleFeatureVecs,
				termInNumDocsCounts, globalFeaturePositionMap, nE,
				connectionString);
		originalCollectionSize = docCount; // Save this size once
		// TFIDF
		AllFeaturesDatabaseExtractor.augment2TFIDF(articleFeatureVecs,
				termInNumDocsCounts, docCount);

		AllFeaturesDatabaseExtractor
				.writeFeatures(articleFeatureVecs, filePath);
		AllFeaturesDatabaseExtractor.writeGolbalPositionMAP(globalFeaturePositionMap, "globalFeaturePosition.lsh");
		AllFeaturesDatabaseExtractor.writeIDFcounts(termInNumDocsCounts, "IDFtermCounts.lsh");
		AllFeaturesDatabaseExtractor.writeDocCount(docCount, "CollectionDoccount.lsh");
	}

	@Override
	public void addArticleToFeatures(String uncleanedArticle, long ID)
			throws Exception {
		try {
			AllFeaturesDatabaseExtractor.addFeature(articleFeatureVecs,
					termInNumDocsCounts, globalFeaturePositionMap,
					uncleanedArticle, nE, docCount, ID);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (docCount > (1.07f * originalCollectionSize)) {
			throw new Exception(
					"To many features added. Collection statistics invalid. Rerun FeatureGenerator.runPreprocession() for the new and "
							+ "larger collection.");
		}
	}
}
