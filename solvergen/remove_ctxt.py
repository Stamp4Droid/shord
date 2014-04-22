#!/usr/bin/env python

import argparse
import re
import util

parser = argparse.ArgumentParser()
parser.add_argument('ctxt_flows_file', type=argparse.FileType('r'))
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

def unctxt_endpoint(endp):
    m = re.match(r'^l([0-9]+)$', endp)
    return 'l%s' % cli2li[int(m.group(1))]

u_flows = set()
for line in args.ctxt_flows_file:
    [c_src,c_dst] = line.split()
    u_src = unctxt_endpoint(c_src)
    u_dst = unctxt_endpoint(c_dst)
    u_flows.add((u_src, u_dst))
for (u_src, u_dst) in u_flows:
    print u_src, u_dst
