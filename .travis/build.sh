#!/bin/sh


if [ "$TRAVIS_PULL_REQUEST" != "false" ] ; then
    echo "Building Pull Request - nothing to push"
else
    echo "Login into Docker Hub ..."
    echo "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USERNAME" --password-stdin docker.io

    docker build -t docker.io/streamzi/cef-manager:latest manager
    docker push docker.io/streamzi/cef-manager:latest

    docker build -t docker.io/streamzi/cef-watcher:latest watcher
    docker push docker.io/streamzi/cef-watcher:latest

fi
