#!/usr/bin/env bash

curl -d "$cloudevent" -H "Content-Type: application/json" -s -o /dev/null -w "%{http_code}\n" -X POST http://${INGEST_URL}/clnr/ce
