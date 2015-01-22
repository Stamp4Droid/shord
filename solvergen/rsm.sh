#!/bin/bash -eu

g++ -std=c++11 -Wall -Wextra -pedantic -g \
    -o rsm.out rsm.cpp \
    -lboost_system -lboost_filesystem -lboost_program_options -lboost_regex
./rsm.out "$@"
