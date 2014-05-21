#!/usr/bin/env python

import argparse
import itertools
import re

def rm_box(s):
    m = re.match(r'\w+:\w+{(\w+)}', s)
    if m is None:
        return s
    return m.group(1)

def append(d, key, val):
    if key not in d:
        d[key] = []
    d[key].append(val)

def reqd_covers(x, y):
    if x == y:
        return True
    if len(x) == 0 or x[-1] != '*':
        return False
    x = x[:-1]
    if len(y) > 0 and y[-1] == '*':
        y = y[:-1]
    if len(x) > len(y):
        return False
    for (i, j) in itertools.izip(x, y):
        if i != j:
            return False
    return True

def push_covers(x, y):
    return reqd_covers(list(reversed(x)), list(reversed(y)))

def covers(a, b):
    return reqd_covers(a[0], b[0]) and push_covers(a[1], b[1])

def group(sigs):
    grouped = {}
    for s in sigs:
        grouped[s] = [s]
    fixpoint = False
    while not fixpoint:
        fixpoint = True
        for a in grouped.keys():
            for b in grouped:
                if a != b and covers(b, a):
                    grouped[b].extend(grouped[a])
                    del grouped[a]
                    fixpoint = False
                    break
    return grouped

full_sigs = []
str2full = {}

parser = argparse.ArgumentParser()
parser.add_argument('summs_file', type=argparse.FileType('r'))
args = parser.parse_args()

for line in args.summs_file:
    line.strip()
    (l_str,r_str) = line.split('|')

    l_toks = tuple(l_str[1:].split())
    r_toks = tuple(r_str[:-2].split())
    sig = (l_toks, r_toks)
    # sigs assumed unique
    full_sigs.append(sig)

    l_tags = tuple([rm_box(t) for t in l_toks])
    r_tags = tuple([rm_box(t) for t in r_toks])
    stripped = (l_tags, r_tags)
    append(str2full, stripped, sig)


full2cov = group(full_sigs)
str2cov = group(str2full)

print 'Full sigs:', len(full_sigs)
print 79 * '-'
print 'Stripped sigs:', len(str2full)
# for s in str2full:
#     print s
#     for f in str2full[s]:
#         print '   ', f
print 79 * '-'
print 'Grouped full sigs:', len(full2cov)
# for m in full2cov:
#     print m
#     for c in full2cov[m]:
#         print '   ', c
print 79 * '-'
print 'Grouped stripped sigs:', len(str2cov)
# for m in str2cov:
#     print m
#     for c in str2cov[m]:
#         print '   ', c
