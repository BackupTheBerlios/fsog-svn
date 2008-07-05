#!/bin/bash
set -x
pkill fsog_server
sleep 2
pkill -9 fsog_server
sleep 2
./fsog_server -p 80 --print-packets
