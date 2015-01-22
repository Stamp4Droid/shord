#!/usr/bin/env python

import argparse
import os
import os.path
import sys

def convert(dname, fout):
    nodes = set()
    param_edges = []
    non_param_edges = []

    for fname in os.listdir(dname):
        if fname.endswith(".pdat"):
            parametric = True
        elif fname.endswith(".ndat"):
            parametric = False
        else:
            continue
        symbol = os.path.splitext(fname)[0]
        edge_list = []
        if parametric:
            param_edges.append((symbol, edge_list))
        else:
            non_param_edges.append((symbol, edge_list))
        with open(os.path.join(dname, fname)) as fin:
            for line in fin:
                toks = line.split()
                assert(parametric and len(toks) == 3 or
                       not parametric and len(toks) == 2)
                nodes.add(toks[0])
                nodes.add(toks[1])
                edge_list.append(toks)

    for n in nodes:
        fout.write(n + '\n')
    fout.write('#\n')
    for (symbol,edges) in param_edges:
        for [src,dst,tag] in edges:
            fout.write('%s %s %s[%s]\n' % (src, dst, symbol, tag))
    for (symbol,edges) in non_param_edges:
        for [src,dst] in edges:
            fout.write('%s %s %s\n' % (src, dst, symbol))

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('facts_dir')
    args = parser.parse_args()
    convert(args.facts_dir, sys.stdout)
