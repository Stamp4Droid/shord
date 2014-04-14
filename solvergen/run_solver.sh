#!/bin/bash

SGEN_DIR="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" && pwd)"

if [ $# -ne 4 -o "$1" != "cfg" -a "$1" != "rsm" ]; then
    echo "Usage: $(basename "${BASH_SOURCE[0]}") cfg|rsm <class> <pri> <sec>"
    exit 1
fi
VERSION="$1"
CLASS="$2"
PRI="$3"
SEC="$4"

if [ "$VERSION" == "cfg" ]; then
    SOLVER="bin/cfg/$CLASS@$PRI^$SEC#-DLOGGING-DPATH_RECORDING"
else
    SOLVER="bin/rsm#-O3"
fi
make -C "$SGEN_DIR" "$SOLVER"

mkdir -p output
if [ "$VERSION" == "cfg" ]; then
    "$SGEN_DIR/$SOLVER"
else
    "$SGEN_DIR/$SOLVER" "$SGEN_DIR/rsm/$CLASS/$PRI/" \
	"$SGEN_DIR/fsm/$CLASS/$SEC.fsm.tgf" input/ output/
fi
