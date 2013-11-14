.PHONY: default clean

.SECONDARY:

default:

clean:
	rm -f bin/* gen/*.cpp gen/*.dat

gen/%.cpp: cfg_parser.py util.py analyses/%.cfg
	python cfg_parser.py analyses/$*.cfg gen/

.SECONDEXPANSION:

bin/%: engine.cpp solvergen.hpp gen/$$(firstword $$(subst \#, ,$$*)).cpp
	g++-4.8 -std=c++11 -Wall -Wextra -pedantic -O2 -g \
	    -o $@ $(subst -, -,$(lastword $(subst #, ,$*))) \
	    -I . $(filter %.cpp,$^)
