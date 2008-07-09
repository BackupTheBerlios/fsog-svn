#!/bin/bash -x
rm *.class
javac -Xlint -Xlint:-serial *.java || exit 1
jar -cfm table.jar manifest *.class || exit 2
