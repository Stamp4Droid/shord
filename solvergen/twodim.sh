#!/bin/bash -eu

SGEN_DIR="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" && pwd)"

make -C "$SGEN_DIR" bin/twodim.out
ulimit -v 10000000
export LD_LIBRARY_PATH="$SGEN_DIR/amore/"; "$SGEN_DIR/bin/twodim.out" "$@"
