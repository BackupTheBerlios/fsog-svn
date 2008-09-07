#!/bin/bash
set -x
#java -jar table.jar $(cat server.txt) "$(cat tableId.txt)" 2_Superman -d
"$(cat browser)" "http://localhost:8080/thousand.jsp?p=55555&n=2_Superman&v=d&t=$(cat tableId.txt)"
