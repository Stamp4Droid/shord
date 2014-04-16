#!/bin/bash -eu

SGEN_DIR="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" && pwd)"

function quit() {
    echo "Usage: $(basename "${BASH_SOURCE[0]}") cfg|rsm <class> <pri> <sec>"
    exit 1
}

if [ $# -ne 4 ]; then
    quit
elif [ "$1" != "cfg" -a "$1" != "rsm" ]; then
    quit
fi
VERSION="$1"
CLASS="$2"
PRI="$3"
SEC="$4"

if [ "$VERSION" == "cfg" ]; then
    REL_SOLVER="bin/cfg/$CLASS@$PRI^$SEC#-DLOGGING-DPATH_RECORDING"
else
    REL_SOLVER="bin/rsm#-O3"
fi
make -C "$SGEN_DIR" "$REL_SOLVER"
SOLVER="$SGEN_DIR/$REL_SOLVER"

IN_DIR="input"
OUT_DIR="output"
mkdir -p "$OUT_DIR"

if [ "$VERSION" == "cfg" ]; then
    "$SOLVER" "$IN_DIR" "$OUT_DIR"
else
    REL_PRI_DIR="rsm/$CLASS/$PRI"
    (cd "$SGEN_DIR" && make "$REL_PRI_DIR/"*.rsm.tgf)
    PRI_DIR="$SGEN_DIR/$REL_PRI_DIR"

    REL_SEC_FSM="fsm/$CLASS/$SEC.fsm.tgf"
    SEC_FSM="$SGEN_DIR/$REL_SEC_FSM"
    make -C "$SGEN_DIR" "$REL_SEC_FSM"

    "$SOLVER" "$PRI_DIR" "$SEC_FSM" "$IN_DIR" "$OUT_DIR"
fi
