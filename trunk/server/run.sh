#!/bin/bash
set -x
pkill fsog_server
sleep 1
pkill -9 fsog_server
sleep 1
if [ "$(whoami)" == "root" ];
then
    ./fsog_server -p 80 --print-packets
else
    ./fsog_server -p 8080 --print-packets
fi
