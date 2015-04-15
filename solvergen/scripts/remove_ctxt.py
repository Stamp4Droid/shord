#!/usr/bin/env python

import argparse
import re
import os
import util

parser = argparse.ArgumentParser()
parser.add_argument('in_dir')
parser.add_argument('out_dir')
parser.add_argument('l_map_file', type=argparse.FileType('r'))
parser.add_argument('cl_map_file', type=argparse.FileType('r'))
args = parser.parse_args()

l_map = util.DomMap(args.l_map_file)
cl_map = util.DomMap(args.cl_map_file)
cli2li = []
for cl_val in cl_map:
    l_val = cl_val.split(',')[0][1:]
    [l_idx] = l_map.val2idx(l_val)
    cli2li.append(l_idx)
unindexed_symbols = ['label2Ref', 'label2Prim', 'prim2Sink', 'ref2Sink',
                     'statLoadRef', 'statLoadPrim', 'new']

def unctxt_endpoint(endp):
    m = re.match(r'^l([0-9]+)$', endp)
    return endp if m is None else ('l%s' % cli2li[int(m.group(1))])

def unctxt_edge(edge):
    src = unctxt_endpoint(edge.src)
    dst = unctxt_endpoint(edge.dst)
    index = None if edge.symbol in unindexed_symbols else edge.index
    return util.Edge(edge.symbol, src, dst, index)

def unctxt_step(step):
    return util.Step(step.reverse, unctxt_edge(step.edge))

paths_per_edge = {}
for c_base in os.listdir(args.in_dir):
    m = re.match(r'^(.*)\.[0-9]+\.tr$', c_base)
    c_top_edge = util.Edge.from_file_base(m.group(1))
    i_top_edge = unctxt_edge(c_top_edge)
    num_paths = paths_per_edge.get(i_top_edge, 0)
    paths_per_edge[i_top_edge] = num_paths + 1
    i_base = '%s.%s.tr' % (i_top_edge.to_file_base(), num_paths)
    with open(os.path.join(args.in_dir, c_base)) as c_trace:
        with open(os.path.join(args.out_dir, i_base), 'w') as i_trace:
            for line in c_trace:
                step = util.Step.from_string(line)
                i_trace.write(str(unctxt_step(step)) + '\n')
        print 'DONE with', c_base
