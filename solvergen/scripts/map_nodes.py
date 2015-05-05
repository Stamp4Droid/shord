#!/usr/bin/env python

import argparse
import os
import re
import util

parser = argparse.ArgumentParser()
parser.add_argument('--replace', action='store_true')
parser.add_argument('dat_file')
parser.add_argument('node_map', type=argparse.FileType('r'))
args = parser.parse_args()

node_map = {}
for line in args.node_map:
    toks = line.split()
    assert len(toks) >= 2
    assert toks[0] not in node_map
    node_map[toks[0]] = toks[1:]

m = re.match(r'^(\w+).dat$', os.path.basename(args.dat_file))
symbol = m.group(1)

with open(args.dat_file, 'r') as f:
    for line in f:
        old_edge = util.Edge.from_tuple(symbol, line)
        if old_edge.src not in node_map and old_edge.dst not in node_map:
            print line[:-1]
            continue
        mapped = [(s,d)
                  for s in node_map.get(old_edge.src, []) + [old_edge.src]
                  for d in node_map.get(old_edge.dst, []) + [old_edge.dst]
                  if (not args.replace or
                      s != old_edge.src or d != old_edge.dst)]
        for (new_src,new_dst) in mapped:
            new_edge = util.Edge(symbol, new_src, new_dst, old_edge.index)
            print new_edge.to_tuple()
