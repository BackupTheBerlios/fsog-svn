#!/bin/bash
set -x
java -jar table.jar $(cat server.txt) "$(cat tableId.txt)" Rhywek -d
