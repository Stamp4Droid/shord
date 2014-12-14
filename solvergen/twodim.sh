#!/bin/bash -eu

g++ -std=c++11 -Wall -Wextra -pedantic -g \
    -I amore/include/ -L amore/ \
    -o twodim.out twodim.cpp \
    -lAMoRE \
    -lboost_system -lboost_filesystem -lboost_program_options -lboost_regex
export LD_LIBRARY_PATH=amore/; ./twodim.out "$@"
