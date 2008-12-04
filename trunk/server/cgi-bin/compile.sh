#!/bin/bash
set -x
rm -f create_table play

#Create table creator:
g++ -ggdb -pedantic -Wall -Wextra -o create_table create_table.cpp
result="${result}${?}"

#Create play:
g++ -ggdb -pedantic -Wall -Wextra -o play play.cpp
result="${result}${?}"

#Create echo:
g++ -ggdb -pedantic -Wall -Wextra -o echo echo.cpp
result="${result}${?}"

echo "Compilation result: ${result}"
exit $result
