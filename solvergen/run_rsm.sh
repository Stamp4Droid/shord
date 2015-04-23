#!/bin/bash -eu

SGEN_DIR="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" && pwd)"

if [ $# -ne 1 ]; then
    echo "Usage: $(basename "${BASH_SOURCE[0]}") <app>"
    exit 1
fi
APP="$1"

echo -n "$APP"
cd "$APP"
t1=$(date +"%s")
"$SGEN_DIR/rsm.sh" \
    "$SGEN_DIR/rsm/____/i-s--s" "$SGEN_DIR/rsm/____/s-i-p-cn" \
    input/____/ funs/ > rsm.log
t2=$(date +"%s")
printf "\t%dm%02ds" $((($t2-$t1) / 60)) $((($t2-$t1) % 60))
# "$SGEN_DIR/timeout.sh" -t 600 "$SGEN_DIR/twodim.sh" funs/ sigs/ > twodim.log
"$SGEN_DIR/twodim.sh" funs/ sigs/ > twodim.log
t3=$(date +"%s")
printf "\t%dm%02ds\t\n" $((($t3-$t2) / 60)) $((($t3-$t2) % 60))
# "$SGEN_DIR/max_node_label.py" sigs/cg.tgf
# "$SGEN_DIR/count_lines.sh" sigs/ > sig_sizes.csv
