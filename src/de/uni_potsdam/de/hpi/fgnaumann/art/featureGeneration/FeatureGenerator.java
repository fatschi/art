package de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration;

import java.io.IOException;
import java.sql.SQLException;

import de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration.AllFeaturesDatabaseExtractor.FeatureType;

public interface FeatureGenerator {
	/**
	 * Method takes an article collection, extracts its features (using 1 of
	 * three methods {@see FeatureType}) and computes TF IDF values while also
	 * tracking collection statistics for TF and IDF.
	 * 
	 * @param addLimit
	 *            Limits how many features may be added via
	 *            {@link #addArticleToFeatures(String)} after preprocessing.
	 *            Each added article updated collection statistics which
	 *            invalidated old TFIDF values if too many adds were made. A
	 *            good value is < 0.05 = 5%.
	 * @param limit
	 *            Number of articles to process
	 * @param ftype
	 *            The method of feature extraction
	 * @param featureN
	 *            Only valid for the method BEST_WORST_N -- see
	 *            {@link FeatureType} for details.
	 * @param articleFeatureVecs
	 *            The TFIDFs of the collection.
	 * @param termInNumDocsCounts
	 *            IDF noun collection counts
	 * @param globalFeaturePositionMap
	 *            Noun feature position ordering (so all feature vector have the
	 *            same ordering)
	 * @param docCount
	 *            Num of docs in the collection (IDF)
	 * @param filePath
	 * @param connectionString
	 * @throws IOException
	 */
	public void runPreprocessing(float addLimit, int limit, FeatureType ftype,
			int featureN, String filePath, String connectionString)
			throws IOException, ClassNotFoundException, SQLException;

	/**
	 * Method to add a new article to the feature collection. <b>Note:<b> Corpus
	 * noun counts and document statistics are updated when using this method.
	 * However, the TFIDFs vectors would have to be recomputed each time a new
	 * article is added due to collection statistics changes. This is
	 * infeasable. Instead, preexistent feature TFIDFs are left unchange and the
	 * method issues an ERROR if too many features are added for the old values
	 * to still be correct (otherwise results will become unexplainable as
	 * features are added).
	 * 
	 * @param uncleanedArticle
	 * @throws Exception
	 */
	public void addArticleToFeatures(String uncleanedArticle, long ID)
			throws Exception;
}
