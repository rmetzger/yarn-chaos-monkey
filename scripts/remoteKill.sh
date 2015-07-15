#!/bin/bash

CONTAINER_ID=$1
HOST=$2
HOST_SHORT=$3

echo "Killing $CONTAINER_ID on host $HOST"

ssh -n $HOST_SHORT -- "killall $CONTAINER_ID"