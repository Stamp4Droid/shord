#!/usr/bin/env python

import argparse

parser = argparse.ArgumentParser()
parser.add_argument('summs_file', type=argparse.FileType('r'))
args = parser.parse_args()

summs = {}
for line in args.summs_file:
    src = line.split()[0]
    if src in summs:
        summs[src] += 1
    else:
        summs[src] = 1

freqs = {}
for src in summs:
    freqs[summs[src]] = freqs.get(summs[src], []) + [src]

for summ_size in sorted(freqs):
    print summ_size, len(freqs[summ_size])
    print '\t' + ' '.join(freqs[summ_size])
