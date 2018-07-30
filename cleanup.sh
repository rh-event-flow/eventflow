#!/usr/bin/env bash

oc delete dc -l app=${1}
oc delete cm -l streamzi.io/kind=ev
oc delete cm -l strimzi.io/kind=topic