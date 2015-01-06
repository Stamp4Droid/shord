#!/usr/bin/env python

import argparse
from os.path import basename, splitext
import sys

def convert(tgf_fname, fout):
    comp = splitext(splitext(basename(tgf_fname))[0])[0]
    states_done = False
    entries = []
    trans = {}

    fout.write('digraph %s {\n' % comp)
    fout.write('id="%s";\n' % comp)
    fout.write('rankdir="LR";\n')
    fout.write('node [style=filled,fillcolor=white,color=black];\n')
    fout.write('node [shape=plaintext,label=""] __phantom__;\n')

    with open(tgf_fname) as fin:
        for line in fin:
            toks = line.split()
            if toks == []:
                pass
            elif states_done:
                src = toks[0]
                dst = toks[1]
                labels = toks[2:]
                if len(labels) == 0:
                    labels = ['']
                trans[(src,dst)] = trans.get((src,dst), []) + labels
            elif toks == ['#']:
                states_done = True
            else:
                state = toks[0]
                weight = ''
                shape = 'circle'
                label = state
                for t in toks[1:]:
                    if t == 'in':
                        entries.append(state)
                    elif t == 'out':
                        weight = 'double'
                    else:
                        shape = 'box'
                        label = t
                fout.write('%s [id="%s",tooltip="%s",shape="%s",label="%s"];\n'
                           % (state, comp + ':' + state, comp + ':' + state,
                              weight + shape, label))

    for e in entries:
        fout.write('__phantom__ -> %s [color=blue];\n' % e)
    for ((src,dst),labels) in trans.iteritems():
        for l in labels:
            edge_id = comp + ':' + src + '--' + l + '->' + comp + ':' + dst
            fout.write('%s -> %s [id="%s",tooltip="%s",label="%s"];\n' %
                       (src, dst, edge_id, l, l))
    fout.write('}\n')

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('tgf_file')
    args = parser.parse_args()
    convert(args.tgf_file, sys.stdout)
