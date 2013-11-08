FLAGS=-DPATH_RECORDING

.PHONY: default clean

.SECONDARY:

default:

bin/%: engine.cpp solvergen.hpp gen/%.cpp
	g++-4.8 -std=c++11 -Wall -Wextra -pedantic -O2 -g $(FLAGS) \
	    -I . engine.cpp gen/$*.cpp -o bin/$*

gen/%.cpp: cfg_parser.py util.py analyses/%.cfg
	python cfg_parser.py analyses/$*.cfg gen

clean:
	rm -f bin/* gen/*.cpp
