#!/bin/bash -eu

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
    PRI_DIR="rsm/$CLASS/$PRI"
    (cd "$SGEN_DIR" && make "$PRI_DIR/"*.rsm.tgf)
    SEC_FSM="fsm/$CLASS/$SEC.fsm.tgf"
    make -C "$SGEN_DIR" "$SEC_FSM"
    "$SGEN_DIR/$SOLVER" "$SGEN_DIR/$PRI_DIR" "$SGEN_DIR/$SEC_FSM" input output
fi
