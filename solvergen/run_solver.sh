#!/bin/bash

SGEN_DIR="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" && pwd)"

if [ $# -ne 1 ]; then
    echo "Usage: $(basename "${BASH_SOURCE[0]}") <grammar>"
    exit 1
fi
GRAMMAR="$1"
SOLVER="bin/$GRAMMAR#-DLOGGING-DPATH_RECORDING"

make -C "$SGEN_DIR" "$SOLVER"

"$SGEN_DIR/$SOLVER"
