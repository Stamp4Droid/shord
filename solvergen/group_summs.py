#!/usr/bin/env python

import argparse
import itertools
import re
import sys
import util

def rm_box(s):
    m = re.match(r'\w+:\w+{(\w+)}', s)
    if m is None:
        return s
    return m.group(1)

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

def print_stacks(s):
    (l,r) = s
    print ('<' + ''.join([e + ' ' for e in l]) +
           '|' + ''.join([' ' + e for e in r]) + '>')

# =============================================================================

class SigSet(util.BaseClass):
    def __init__(self):
        self.full_sigs = []
        self.str2full = {}

parser = argparse.ArgumentParser()
parser.add_argument('summs_file', type=argparse.FileType('r'))
parser.add_argument('tgt_ctxt', nargs='?')
parser.add_argument('-v', '--verbose', action='count')
args = parser.parse_args()

ctxt2sigs = {}

for line in args.summs_file:
    toks = line.split('<')
    assert len(toks) == 2
    ctxt = toks[0].strip()
    if args.tgt_ctxt is not None and ctxt != args.tgt_ctxt:
        continue
    sig_set = util.index(ctxt2sigs, ctxt, SigSet())

    (l_str,r_str) = toks[1][:-2].split('|')
    l_toks = tuple(l_str.split())
    r_toks = tuple(r_str.split())
    sig = (l_toks, r_toks)
    # sigs assumed unique
    sig_set.full_sigs.append(sig)

    l_tags = tuple([rm_box(t) for t in l_toks])
    r_tags = tuple([rm_box(t) for t in r_toks])
    stripped = (l_tags, r_tags)
    util.index(sig_set.str2full, stripped, []).append(sig)

total_full = 0
total_str = 0
total_grp_full = 0
total_grp_str = 0

for ctxt in ctxt2sigs:
    sig_set = ctxt2sigs[ctxt]
    full2cov = group(sig_set.full_sigs)
    str2cov = group(sig_set.str2full)

    total_full += len(sig_set.full_sigs)
    total_str += len(sig_set.str2full)
    total_grp_full += len(full2cov)
    total_grp_str += len(str2cov)

    if args.verbose >= 1:
        print ctxt + ':'
        print 4 * ' ' + 'Full sigs:', len(sig_set.full_sigs)
        print 4 * ' ' + 'Stripped sigs:', len(sig_set.str2full)
        if args.verbose >= 2:
            for s in sig_set.str2full:
                sys.stdout.write(8 * ' ')
                print_stacks(s)
                for f in sig_set.str2full[s]:
                    sys.stdout.write(12 * ' ')
                    print_stacks(f)
        print 4 * ' ' + 'Grouped full sigs:', len(full2cov)
        if args.verbose >= 2:
            for m in full2cov:
                sys.stdout.write(8 * ' ')
                print_stacks(m)
                for c in full2cov[m]:
                    sys.stdout.write(12 * ' ')
                    print_stacks(c)
        print 4 * ' ' + 'Grouped stripped sigs:', len(str2cov)
        if args.verbose >= 2:
            for m in str2cov:
                sys.stdout.write(8 * ' ')
                print_stacks(m)
                for c in str2cov[m]:
                    sys.stdout.write(12 * ' ')
                    print_stacks(c)

print 'Totals:'
print 4 * ' ' + 'Full sigs:', total_full
print 4 * ' ' + 'Stripped sigs:', total_str
print 4 * ' ' + 'Grouped full sigs:', total_grp_full
print 4 * ' ' + 'Grouped stripped sigs:', total_grp_str
