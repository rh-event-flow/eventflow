#!/usr/bin/env bash

mvn -f ./pom.xml clean install
./manager/build.sh;
./operations/log-data/build.sh;
./operations/nodejs/filter-events/build.sh;
./operations/random-data/build.sh;
