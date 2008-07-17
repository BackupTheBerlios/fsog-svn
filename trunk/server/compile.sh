#!/bin/bash
#set -x
rm -f fsog_server
g++ -ggdb -pedantic -Wall -Wextra -o fsog_server -lboost_system -lboost_thread *.cpp
result=$?
xterm -e "echo -en '\a'; sleep '0.2';echo -en '\a'; sleep '0.2';echo -en '\a'; sleep '0.2';echo -en '\a'; echo 'Compilation FINISHED!'; sleep '0.3';"
exit $result
