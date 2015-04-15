#!/usr/bin/env python

import argparse
import os
import re
import sys
import util

parser = argparse.ArgumentParser()
parser.add_argument('trace_file')
parser.add_argument('out_dir')
parser.add_argument('out_map_file', type=argparse.FileType('w'))
args = parser.parse_args()

m = re.match(r'^(.*)\.[0-9]+\.tr$', os.path.basename(args.trace_file))
top_edge = util.Edge.from_file_base(m.group(1))
if top_edge.src == top_edge.dst:
    sys.stderr.write('Same source and destination currently unsupported.\n')
    sys.exit(1)

# TODO: We're assuming the real graph doesn't use node names using this format.
num_nodes = 0
def make_node():
    global num_nodes
    new_node = '_%s' % num_nodes
    num_nodes += 1
    return new_node

node_map = util.UniqueMultiDict()
prev_node = top_edge.src
def move_edge_to_line(step, is_last):
    global prev_node
    next_node = top_edge.dst if is_last else make_node()
    (new_src, new_dst) = ((next_node, prev_node) if step.reverse else
                          (prev_node, next_node))
    prev_node = next_node
    if step.edge.src != new_src:
        node_map.append(step.edge.src, new_src)
    if step.edge.dst != new_dst:
        node_map.append(step.edge.dst, new_dst)
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
    out_dat = os.path.join(args.out_dir, symbol + '.dat')
    with open(out_dat, 'w') as f:
        for edge in sym2edges.get(symbol):
            f.write(edge.to_tuple() + '\n')
for orig_node in node_map:
    args.out_map_file.write(orig_node)
    for mapped_node in node_map.get(orig_node):
        args.out_map_file.write(' ' + mapped_node)
    args.out_map_file.write('\n')
