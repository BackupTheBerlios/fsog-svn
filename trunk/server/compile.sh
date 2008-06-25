#!/bin/bash

g++ -ggdb -pedantic -Wall -Wextra -o thousandServer -lboost_system -lboost_thread *.cpp
