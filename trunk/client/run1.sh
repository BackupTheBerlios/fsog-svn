#!/bin/bash
set -x
#java -jar table.jar $(cat server.txt) "$(cat tableId.txt)" 1_Lobo -d
"$(cat browser)" "http://localhost:8080/thousand.jsp?p=55555&n=1_Lobo&v=d&t=$(cat tableId.txt)"
