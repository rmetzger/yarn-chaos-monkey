#!/bin/bash

CONTAINER_ID=$1
HOST=$2
HOST_SHORT=$3

echo "Killing $CONTAINER_ID on host $HOST"

yarn container -signal $CONTAINER_ID FORCEFUL_SHUTDOWN
