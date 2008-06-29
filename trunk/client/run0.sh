#!/bin/bash -x
java TableJoiner localhost 3000 "$(cat tableId.txt)" Batman -d
