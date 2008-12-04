#!/bin/bash
set -x
rm -f fsog_server
#g++ -ggdb -pedantic -Wall -Wextra -o fsog_server -lboost_system -lboost_thread *.cpp
#Create server:
g++ -ggdb -pedantic -Wall -Wextra -o fsog_server `cat fsog_server_cpps`
result="$?"
echo "Compilation result: ${result}"
exit $result
