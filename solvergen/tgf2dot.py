#!/usr/bin/env python

import argparse
from os.path import basename, splitext

parser = argparse.ArgumentParser()
parser.add_argument('tgf_file')
args = parser.parse_args()

comp = splitext(splitext(basename(args.tgf_file))[0])[0]
states_done = False
trans = {}

print 'digraph %s {' % comp
print 'id="%s";' % comp
print 'rankdir="LR";'
print 'node [style="filled",fillcolor="white"];'

with open(args.tgf_file) as fin:
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
            print ('%s [id="%s",tooltip="%s",shape="%s",label="%s"];' %
                   (state, comp + ':' + state, comp + ':' + state,
                    weight + shape, label))

for (src,dst) in trans:
    edge_id = comp + ':' + src + '->' + comp + ':' + dst
    print ('%s -> %s [id="%s",tooltip="%s"];' %
           (src, dst, edge_id, ' '.join(trans[(src,dst)])))
print '}'
