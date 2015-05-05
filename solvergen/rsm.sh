#!/bin/bash -eu

SGEN_DIR="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" && pwd)"

make -C "$SGEN_DIR" bin/rsm.out
ulimit -v 10000000
"$SGEN_DIR/bin/rsm.out" "$@"
