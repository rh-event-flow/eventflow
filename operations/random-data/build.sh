#!/bin/bash

#Need to login first
 OPENSHIFT_TOKEN=$(oc whoami -t)
 docker login -u system -p ${OPENSHIFT_TOKEN} 172.30.1.1:5000

echo 'Building CONTAINER'
docker build -t cef-ops-random-data .
docker tag cef-ops-random-data:latest 172.30.1.1:5000/myproject/cef-ops-random-data:latest
docker push 172.30.1.1:5000/myproject/cef-ops-random-data:latest
