#!/bin/bash
set -x
rm fsog_server
g++ -ggdb -pedantic -Wall -Wextra -o fsog_server -lboost_system -lboost_thread *.cpp
