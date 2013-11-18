#!/bin/bash

SGEN_DIR="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" && pwd)"

if [ $# -ne 3 ]; then
    echo "Usage: $(basename "${BASH_SOURCE[0]}") <grammar> <symbol> <trace>"
    exit 1
fi
GRAMMAR="$1"
SOLVER="bin/$GRAMMAR#-DPATH_RECORDING"
SYMBOL="$2"
TRACE="$3"

make -C "$SGEN_DIR" -s "$SOLVER"
if [ -s "$SGEN_DIR/gen/$GRAMMAR.rels.dat" ]; then
    echo "ERROR: Need rels to properly decide for grammar $GRAMMAR"
    exit 1
fi

TEMP_DIR="$(mktemp -d)"
mkdir "$TEMP_DIR/input"
mkdir "$TEMP_DIR/output"
"$SGEN_DIR/trace2facts.py" "$TRACE" "$TEMP_DIR/input"
while read TERM; do
    touch "$TEMP_DIR/input/$TERM.dat"
done < "$SGEN_DIR/gen/$GRAMMAR.terms.dat"

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
