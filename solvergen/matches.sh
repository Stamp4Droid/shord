#!/bin/bash

SGEN_DIR="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" && pwd)"

if [ $# -ne 3 ]; then
    echo "Usage: $(basename "${BASH_SOURCE[0]}") <grammar> <symbol> <path-xml>"
    exit 1
fi
GRAMMAR="$1"
SOLVER="bin/$GRAMMAR#-DPATH_RECORDING"
SYMBOL="$2"
PATH_XML="$3"

make -C "$SGEN_DIR" -s "$SOLVER"

TEMP_DIR="$(mktemp -d)"
mkdir "$TEMP_DIR/input"
mkdir "$TEMP_DIR/output"
"$SGEN_DIR/get_trace.py" "$PATH_XML" | "$SGEN_DIR/trace2facts.py" - "$TEMP_DIR/input"
while read TERM; do
    touch "$TEMP_DIR/input/$TERM.dat"
done < "$SGEN_DIR/gen/$GRAMMAR.terms.dat"

CURR_DIR="$(pwd)"
cd "$TEMP_DIR"
"$SGEN_DIR/$SOLVER"
cd "$CURR_DIR"

if "$SGEN_DIR/edge_exists.py" "$PATH_XML" "$TEMP_DIR/output/$SYMBOL.dat"; then
    echo -e "$PATH_XML\tOK"
else
    echo -e "$PATH_XML\tFAIL"
fi

rm -rf "$TEMP_DIR"
