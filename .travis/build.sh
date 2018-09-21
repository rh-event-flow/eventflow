#!/bin/sh


if [ "$TRAVIS_PULL_REQUEST" != "false" ] ; then
    echo "Building Pull Request - nothing to push"
else
    echo "Login into Docker Hub ..."
    echo "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USERNAME" --password-stdin docker.io

    docker build -t docker.io/streamzi/eventflow-operator:latest operator
    docker push docker.io/streamzi/eventflow-operator:latest

    docker build -t docker.io/streamzi/eventflow-ui:latest ui
    docker push docker.io/streamzi/eventflow-ui:latest

fi
