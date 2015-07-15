#!/bin/bash

CONTAINER_ID=$1
HOST=$2
HOST_SHORT=$3

echo "Killing $CONTAINER_ID on host $HOST"

PID=`ssh  $HOST_SHORT -- ps aux | grep $CONTAINER_ID | grep java | tr -s ' ' | cut -d ' ' -f 2 | head -n 1`

echo "PID = $PID"
ssh  $HOST_SHORT -- sudo kill $PID


