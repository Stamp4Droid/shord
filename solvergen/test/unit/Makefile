CC = g++ -std=c++1y -Wall -Wextra -pedantic -g
LIBS = -lboost_system -lboost_filesystem -lboost_program_options -lboost_regex

.PHONY: test

test: bin/util_unit.out
	bin/util_unit.out

bin/util_unit.out: util_unit.cpp ../../util.hpp
	$(CC) -I ../../ -o $@ $< $(LIBS)
