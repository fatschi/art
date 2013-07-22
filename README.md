README:

1. import the project into eclipse
2. see the RecommendationClient.java for an example client program
2. adapt the ant build.xml accordingly to your ssh settings
3. run ant
4. start the server process with ./run.sh --server on the desired machine

NOTE:

you can find precomputed files with feature vectors, wheter with or without computed LSH hashes in our team folder on isfet:
+home/art03/corpora/final

[all|bestWorstX|best]CorpusMapVector[WithLSHXXX|WithoutLSH].lsh

name schema:
[all|bestWorstX|best] - chosen features, "all" takes all nouns from the corpus as dimensions, "bestWorstX" takes the most and least occuring X nouns from each document, "best" takes only the most occuring word from each document
[WithLSHXXX|WithoutLSH] - indicates if the LSH hash is written to the serialized FeatureVector objects. XXX is the number of bits used for the hash