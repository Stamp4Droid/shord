VERSION = 0.3

CFLAGS += -O3 -Wall -g
CFLAGS += -Iinclude/
CFLAGS += -DUNIX -DLINUX
CFLAGS += -DDEBUG -DJAVAEXCEPT -DLIBAMORE -DBIT32
CFLAGS += -DVERSION="\"${VERSION}\""
CFLAGS += -fno-stack-protector

CPPFLAGS += -O3 -Wall -g
CPPFLAGS += -Iinclude/
CPPFLAGS += -D__cplusplus -DVERSION="\"$(VERSION)\""
CPPFLAGS += -fno-stack-protector

# =============================================================================

C_OBJ_BASE = global.o buffer.o liberror.o parse.o fileIO.o \
	nfa.o dfa.o regexp.o sfexp.o mon.o language.o rexFromString.o \
	dfa2nfa.o dfamdfa.o enfa2nfa.o nfa2dfa.o nfa2rex.o  \
	nfa2mnfa_sol.o nfa2mnfa_help.o nfa2mnfa.o rex2nfa.o  \
	regexp-infix.o genrex2nfa.o \
	dfa2mon.o  unaryB.o binary.o mon2dcl.o mon2rel.o mon2sfx.o \
	testUnary.o testBinary.o \
	debugPrint.o
C_OBJ = $(addprefix bin/amore/, $(C_OBJ_BASE))

CPP_OBJ_BASE = amore.o finite_automaton.o \
	nondeterministic_finite_automaton.o deterministic_finite_automaton.o
CPP_OBJ = $(addprefix bin/amore++/, $(CPP_OBJ_BASE))

# =============================================================================

.PHONY: default clean

default: libAMoRE.a

libAMoRE.a: $(C_OBJ) $(CPP_OBJ)
	$(AR) crs $@ $?

bin/amore/%.o: src/amore/%.c
	gcc $(CFLAGS) -c -o $@ $?

bin/amore++/%.o: src/amore++/%.cpp
	g++ $(CPPFLAGS) -c -o $@ $?

finite_automaton.cpp: set.h

clean:
	rm -f $(C_OBJ) $(CPP_OBJ) libAMoRE.a

# CPPFLAGS += -DANTICHAIN_DEBUG
# C/CPPFLAGS += -shared -fpic
# C: LDFLAGS += -fpic -shared
# C++: LDFLAGS += -shared -L${LIBDIR} -lAMoRE
