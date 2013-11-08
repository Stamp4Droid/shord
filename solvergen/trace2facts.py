#!/usr/bin/env python

import argparse
import os
import re
import util

class Edge(util.Hashable):
    def __init__(self, str):
        step_pat = r'^(STR|REV) ([a-z]\w*) (\w+) (\w+)(?: ([0-9]+))?$'
        matcher = re.match(step_pat, str)
        assert matcher is not None
        self.symbol = matcher.group(2)
        self.src = matcher.group(3)
        self.dst = matcher.group(4)
        self.index = matcher.group(5)

    def __key__(self):
        return (self.symbol, self.src, self.dst, self.index)

    def to_tuple(self):
        return ('%s %s%s' %
                (self.src, self.dst,
                 '' if self.index is None else (' %s' % self.index)))

parser = argparse.ArgumentParser()
parser.add_argument('trace_file', type=argparse.FileType('r'))
parser.add_argument('out_dir')
args = parser.parse_args()

sym2edges = util.UniqueMultiDict()
for line in args.trace_file:
    edge = Edge(line)
    sym2edges.append(edge.symbol, edge)
for symbol in sym2edges:
    out_file = os.path.join(args.out_dir, symbol + '.dat')
    with open(out_file, 'w') as f:
        for edge in sym2edges.get(symbol):
            f.write(edge.to_tuple() + '\n')
