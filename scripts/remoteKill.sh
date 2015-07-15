#!/bin/bash

CONTAINER_ID=$1
HOST=$2

echo "Killing $CONTAINER_ID on host $HOST"

ssh -n $HOST -- "killall $CONTAINER_ID"