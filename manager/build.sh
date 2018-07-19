#!/bin/bash

#Need to login first
OPENSHIFT_TOKEN=$(oc whoami -t)
docker login -u system -p ${OPENSHIFT_TOKEN} 172.30.1.1:5000
 
echo 'Building CloudEvent Flow Manager'
docker build -t cef-manager .
docker tag cef-manager:latest 172.30.1.1:5000/hardcoded-test/cef-manager:latest
docker push 172.30.1.1:5000/hardcoded-test/cef-manager:latest
