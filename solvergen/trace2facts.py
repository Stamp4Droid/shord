#!/usr/bin/env python

import argparse
import os
import re
import sys
import util

parser = argparse.ArgumentParser()
parser.add_argument('trace_file')
parser.add_argument('out_dir')
args = parser.parse_args()

m = re.match(r'^(.*)\.[0-9]+\.tr$', os.path.basename(args.trace_file))
top_edge = util.Edge.from_file_base(m.group(1))
if top_edge.src == top_edge.dst:
    sys.stderr.write('Same source and destination currently unsupported.\n')
    sys.exit(1)

num_nodes = 0
def make_node():
    global num_nodes
    new_node = top_edge.src
    while new_node == top_edge.src or new_node == top_edge.dst:
        new_node = 'n%s' % num_nodes
        num_nodes += 1
    return new_node

prev_node = top_edge.src
def move_edge_to_line(step, is_last):
    global prev_node
    next_node = top_edge.dst if is_last else make_node()
    (new_src, new_dst) = ((next_node, prev_node) if step.reverse else
                          (prev_node, next_node))
    prev_node = next_node
    return util.Edge(step.edge.symbol, new_src, new_dst, step.edge.index)

sym2edges = util.OrderedMultiDict()
def process_trace_line(line, is_last):
    step = util.Step.from_string(line)
    new_edge = move_edge_to_line(step, is_last)
    sym2edges.append(new_edge.symbol, new_edge)

with open(args.trace_file) as f:
    all_lines = f.readlines()
    for line in all_lines[:-1]:
        process_trace_line(line, False)
    process_trace_line(all_lines[-1], True)
for symbol in sym2edges:
    out_file = os.path.join(args.out_dir, symbol + '.dat')
    with open(out_file, 'w') as f:
        for edge in sym2edges.get(symbol):
            f.write(edge.to_tuple() + '\n')
