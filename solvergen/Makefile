PY_SCRIPTS = cfg_parser.py fsm.py util.py

.PHONY: default clean

.SECONDARY:

default:

clean:
	rm -f bin/* gen/*.cpp gen/*.dat

%.fsm.tgf: %.fsm.graphml
	$(error $@ out of date, manually re-export from $?)

.SECONDEXPANSION:

# gen/foo^bar.cpp: ... analyses/foo.cfg fsm/bar/*.fsm.tgf
gen/%.cpp: $(PY_SCRIPTS) analyses/$$(firstword $$(subst ^, ,$$*)).cfg \
	   $$(wildcard fsm/$$(lastword $$(subst ^, ,$$*))/*.fsm.tgf)
	python cfg_parser.py analyses/$(firstword $(subst ^, ,$*)).cfg \
	    fsm/$(lastword $(subst ^, ,$*)) gen/

# bin/foo^bar#-DFLAG1-DFLAG2: ... gen/foo^bar.cpp
# compiled with -DFLAG1 -DFLAG2
bin/%: engine.cpp solvergen.hpp gen/$$(firstword $$(subst \#, ,$$*)).cpp
	g++-4.8 -std=c++11 -Wall -Wextra -pedantic -O2 -g \
	    -o $@ $(subst -, -,$(lastword $(subst #, ,$*))) \
	    -I . $(filter %.cpp,$^)
