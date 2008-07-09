#!/bin/bash
set -x
java TableCreator $(cat server.txt) > tableId.txt
