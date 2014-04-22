CFG_PY = cfg_parser.py fsm.py util.py
CFG_SRC = engine.cpp solvergen.hpp
RSM_PY = rsm.py fsm.py util.py
RSM_SRC = rsm.cpp rsm.hpp util.hpp

CC = g++ -std=c++11 -Wall -Wextra -pedantic -g
RSM_LIBS = -lboost_system -lboost_filesystem -lboost_program_options -lboost_regex

# The following functions assume a name formatted as "cls@pri^sec#flags".
first = $(firstword $(subst $(1), ,$(2)))
last = $(lastword $(subst $(1), ,$(2)))
class = $(call first,@,$(1))
flags = $(subst -, -,$(call last,\#,$(1)))
analysis = $(call first,\#,$(1))
pri = $(call first,^,$(call last,@,$(call first,\#,$(1))))
sec = $(call last,^,$(call last,@,$(call first,\#,$(1))))

.PHONY: default clean

.SECONDARY:

default:

clean:
	rm -f bin/cfg/* bin/rsm* gen/*.cpp gen/*.dat

%.tgf: %.graphml
	$(error $@ out of date, manually re-export from $?)

.SECONDEXPANSION:

# fsm/cls/sec.fsm.tgf : fsm/cls/sec/*.fsm.tgf
# TODO: Might be able to do the processing automatically.
fsm/*/*.fsm.tgf: $$(wildcard $$(basename $$(basename $$@))/*.fsm.tgf)
	$(error $@ out of date, re-combine the components in $(basename $(basename $@)))

# rsm/cls/pri/*.rsm.tgf : rsm/cls/pri/src/*.rsm.tgf
# TODO: Might be able to do the processing automatically.
rsm/*/*/*.rsm.tgf: $$(wildcard $$(dir $$@)src/*.rsm.tgf)
	$(error $@ out of date, re-process the components in $(dir $@)src/)

# gen/cls@pri^sec.cpp: ... cfg/cls/pri.cfg fsm/cls/sec/*.fsm.tgf
gen/%.cpp: $(CFG_PY) cfg/$$(call class,$$*)/$$(call pri,$$*).cfg \
	   $$(wildcard fsm/$$(call class,$$*)/$$(call sec,$$*)/*.fsm.tgf)
	python cfg_parser.py cfg/$(call class,$*)/$(call pri,$*).cfg \
	    fsm/$(call class,$*)/$(call sec,$*) gen/

# bin/cfg/cls@pri^sec#-DFLAG1-DFLAG2: ... gen/cls@pri^sec.cpp
# compiled with -DFLAG1 -DFLAG2
bin/cfg/%: $(CFG_SRC) gen/$$(call class,$$*)@$$(call pri,$$*)^$$(call sec,$$*).cpp
	$(CC) -O3 -o $@ $(call flags,$*) -I . $(filter %.cpp,$^)

# bin/rsm#-DFLAG1-DFLAG2: ...
# compiled with -DFLAG1 -DFLAG2
bin/rsm\#%: $(RSM_SRC)
	$(CC) $(subst -, -,$*) -o $@ -I . $(filter %.cpp,$^) $(RSM_LIBS)
