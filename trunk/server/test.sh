#!/bin/bash

#   FSOG - Free Software Online Games
#   Copyright (C) 2007 Bartlomiej Antoni Szymczak

#   This file is part of FSOG.

#   FSOG is free software: you can redistribute it and/or modify
#   it under the terms of the GNU Affero General Public License as
#   published by the Free Software Foundation, either version 3 of the
#   License, or (at your option) any later version.

#   This program is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU Affero General Public License for more details.

#   You should have received a copy of the GNU Affero General Public License
#   along with this program.  If not, see <http://www.gnu.org/licenses/>.



#   You can contact the author, Bartlomiej Antoni Szymczak, by:
#   - electronic mail: rhywek@gmail.com
#   - paper mail:
#   Bartlomiej Antoni Szymczak
#   Boegesvinget 8, 1. sal
#   2740 Skovlunde
#   Denmark

for file in *.hpp;
do
    if [ -d "tests/${file%.hpp}" ];
    then
        echo Testing class ${file}.
    else
        echo Tests not found for ${file}.
        continue
    fi

    echo "#include \"${file}\"" > test.cpp
    echo "#include <iostream>
using namespace std;" >> test.cpp

    echo "int main()
{
  long passed = 0l;
  long failed = 0l;" >> test.cpp

    for testFile in $(find tests/ -iname '*.cpp'|grep -F "tests/${file%.hpp}/");
    do
    
	echo "{//Tests included from ${testFile}:" >> test.cpp
	
	cat "${testFile}" \
	    | sed 's/^[[:space:]]*\(.*\)[[:space:]]\+EQ[[:space:]]\+\(.*\)[;]$/if(\1==\2){passed++;}else{cout<<\"Equality assertion failed at \"<<__func__<<\" in \"<<__FILE__<<\", line \"<<__LINE__<<\".\"<<endl<<\"\1 evaluates to \"<<\1<<endl<<\"\2 evaluates to \"<<\2<<endl;return 1;}/g' \
	    >> test.cpp

	echo "}//End of tests included from ${testFile}." >> test.cpp

#    cat ${file} | grep "^.* test[_]" | grep -o "test_[[:alpha:]_]*" | sed "s/^/  if(${file%.hpp}::/g" | sed "s/$/()){passed++;}else{failed++;cout<<\"Failure in \"<<__FILE__<<\" at line \"<<__LINE__<<'.'<<endl;return 1;}/g" >> test.cpp
    done

    echo "  cout<<\"Passed: \"<<passed<<\", failed: \"<<failed<<endl;
  return 0;
}" >> test.cpp

    if [ -a "${file%.hpp}.cpp" ];
    then
        g++ -w -o test test.cpp ${file%.hpp}.cpp
    else
        g++ -w -o test test.cpp
    fi

    if [ $? -ne 0 ];
    then
	echo "Couldn't compile tests."
        rm -f test.cpp
	continue
    fi

    ./test

    if [ $? -ne 0 ];
    then
	echo "Test failed. Investigate test.cpp"
	exit 1
    fi

    rm -f test.cpp test
done
