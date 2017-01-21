#!/opt/local/bin/bash

OUTPUT_FILE="$1"
DEFAULT_PRE_JAR="pre-fsfinder.jar"

rm ../${1}
rm -rf main/*
mv ${DEFAULT_PRE_JAR} ./main

jar -cvfm ../${OUTPUT_FILE} boot-manifest.mf .
