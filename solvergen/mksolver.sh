#!/bin/bash

SGEN_DIR="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" && pwd)"

if [ $# -lt 1 ]; then
    echo "Usage: $(basename "${BASH_SOURCE[0]}") <grammar> [<options> ...]"
    exit 1
fi

GRAMMAR="$1"
shift
python "$SGEN_DIR/cfg_parser.py" "$SGEN_DIR/analyses/$GRAMMAR.cfg" .
g++-4.8 -std=c++11 -Wall -Wextra -pedantic -O2 -g "$@" \
    -I "$SGEN_DIR" "$SGEN_DIR/engine.cpp" "$GRAMMAR.cpp" -o "$GRAMMAR"
