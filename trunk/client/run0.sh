#!/bin/bash
set -x
java -jar table.jar $(cat server.txt) "$(cat tableId.txt)" 0_Rhywek -d
