#!/usr/bin/env python

import argparse
from os.path import basename, splitext
import sys

def convert(tgf_fname, fout):
    comp = splitext(splitext(basename(tgf_fname))[0])[0]
    states_done = False
    trans = {}

    fout.write('digraph %s {\n' % comp)
    fout.write('id="%s";\n' % comp)
    fout.write('rankdir="LR";\n')
    fout.write('node [style="filled",fillcolor="white"];\n')

    with open(tgf_fname) as fin:
        for line in fin:
            toks = line.split()
            if toks == []:
                pass
            elif states_done:
                src = toks[0]
                dst = toks[1]
                trans[(src,dst)] = trans.get((src,dst), []) + toks[2:]
            elif toks == ['#']:
                states_done = True
            else:
                state = toks[0]
                weight = ''
                shape = 'circle'
                label = ' '
                for t in toks[1:]:
                    if t == 'in':
                        shape = 'octagon'
                    elif t == 'out':
                        weight = 'double'
                    else:
                        shape = 'box'
                        label = t
                fout.write('%s [id="%s",tooltip="%s",shape="%s",label="%s"];\n'
                           % (state, comp + ':' + state, comp + ':' + state,
                              weight + shape, label))

    for (src,dst) in trans:
        edge_id = comp + ':' + src + '->' + comp + ':' + dst
        fout.write('%s -> %s [id="%s",tooltip="%s"];\n' %
                   (src, dst, edge_id, ' '.join(trans[(src,dst)])))
    fout.write('}\n')

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('tgf_file')
    args = parser.parse_args()
    convert(args.tgf_file, sys.stdout)
