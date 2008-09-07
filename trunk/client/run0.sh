#!/bin/bash
set -x
#java -jar table.jar $(cat server.txt) "$(cat tableId.txt)" 0_Rhywek -d
"$(cat browser)" "http://localhost:8080/thousand.jsp?p=55555&n=0_Rhywek&v=d&t=$(cat tableId.txt)"
