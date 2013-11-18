#!/usr/bin/env python

import argparse
import os
import re
import sys
import util

parser = argparse.ArgumentParser()
parser.add_argument('path_or_trace')
parser.add_argument('dat_file', type=argparse.FileType('r'))
args = parser.parse_args()

m = re.match(r'^(.*)\.[0-9]+\.(xml|tr)$', os.path.basename(args.path_or_trace))
edge = util.Edge.from_file_base(m.group(1))
to_find = edge.to_tuple() + '\n'
for line in args.dat_file:
    if line == to_find:
        sys.exit(0)
sys.exit(1)
