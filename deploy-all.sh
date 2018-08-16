#!/usr/bin/env bash

mvn -f ./pom.xml clean install
cd manager
./build.sh;
cd ../log-data/
./build.sh
cd ../nodejs/filter-events/
./build.sh

cd ../../

