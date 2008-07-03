#!/bin/bash -x
rm *.class
javac -Xlint *.java || exit 1
jar -cf table.jar *.class || exit 2
