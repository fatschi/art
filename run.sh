#!/bin/bash

RETVAL=0

case "$1" in
   "") 
      echo "Usage: $0 [--simulate] [--server]"
      RETVAL=1
      ;;
   --simulate)
      java -cp lsh.jar de.uni_potsdam.de.hpi.fgnaumann.art.LSHRunnerImplementation --simulate
      ;;
   --server)
      java -cp lsh.jar de.uni_potsdam.de.hpi.fgnaumann.art.RecommendationServer --port 8282
      ;;
   --client)
      java -cp lsh.jar de.uni_potsdam.de.hpi.fgnaumann.art.RecommendationClient
      ;;
    --extraction)
      java -cp lsh.jar de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration.AllFeaturesDatabaseExtractor --connectionString "jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP"
      ;;      
esac

exit $RETVAL