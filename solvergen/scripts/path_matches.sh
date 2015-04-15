#!/bin/bash

SGEN_DIR="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" && pwd)"

if [ $# -ne 4 ]; then
    echo "Usage: $(basename "${BASH_SOURCE[0]}") <grammar> <symbol> <trace> <orig_in_dir>"
    exit 1
fi
GRAMMAR="$1"
SOLVER="bin/$GRAMMAR#-DPATH_RECORDING"
SYMBOL="$2"
TRACE="$3"
ORIG_IN_DIR="$4"

make -C "$SGEN_DIR" -s "$SOLVER"

TEMP_DIR="$(mktemp -d)"
mkdir "$TEMP_DIR/input"
mkdir "$TEMP_DIR/output"
"$SGEN_DIR/trace2facts.py" "$TRACE" "$TEMP_DIR/input" "$TEMP_DIR/node.map"

# Instantiate the empty base fact files.
while read TERM; do
    touch "$TEMP_DIR/input/$TERM.dat"
done < "$SGEN_DIR/gen/$GRAMMAR.terms.dat"

# We need at least those supporting facts that $GRAMMAR would use for this
# input graph. If the results were produced by another grammar, which doesn't
# use those supporting facts, we need to produce them just for the comparison.

# We don't change the indices on the edges, so we can just copy the relation
# files verbatim.
while read REL; do
    cp "$ORIG_IN_DIR/$REL.rel" "$TEMP_DIR/input"
done < "$SGEN_DIR/gen/$GRAMMAR.rels.dat"

# Predicate-supporting terminals are separate from main-path terminals. Thus,
# we can just keep that portion of the graph intact, and just connect the new
# nodes to any node their counterparts are connected to. We essentially keep
# a copy of the old graph, so as not to modify the predicate reachability.
while read SUPP; do
    "$SGEN_DIR/map_nodes.py" "$ORIG_IN_DIR/$SUPP.dat" "$TEMP_DIR/node.map" > "$TEMP_DIR/input/$SUPP.dat"
done < "$SGEN_DIR/gen/$GRAMMAR.supp.dat"

CURR_DIR="$(pwd)"
cd "$TEMP_DIR"
"$SGEN_DIR/$SOLVER"
cd "$CURR_DIR"

if "$SGEN_DIR/edge_exists.py" "$TRACE" "$TEMP_DIR/output/$SYMBOL.dat"; then
    echo -e "$TRACE\tOK"
else
    echo -e "$TRACE\tFAIL"
fi

rm -rf "$TEMP_DIR"

# TODO: Technically, we should have also stored a mapping during context
# removal, but we can get away with it for now, because the only nodes that
# change are label nodes, and those don't affect the only predicate we're
# using, type filters.
