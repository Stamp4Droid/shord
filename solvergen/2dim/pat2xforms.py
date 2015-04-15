#!/usr/bin/env python

import argparse

# =============================================================================

OPEN_CHARS  = ['(', '[', '{', '<']
CLOSE_CHARS = [')', ']', '}', '>']

def is_open(c):
    return c in OPEN_CHARS

def is_close(c):
    return c in CLOSE_CHARS

def to_open(c):
    return OPEN_CHARS[CLOSE_CHARS.index(c)]

def to_close(c):
    return CLOSE_CHARS[OPEN_CHARS.index(c)]

def parse(s):
    closes = []
    opens = []
    for c in s:
        if is_close(c):
            assert len(opens) == 0
            closes.append(c)
        elif is_open(c):
            opens.append(c)
        else:
            assert False
    return (closes, opens)

def matches(a, b):
    return OPEN_CHARS.index(a) == CLOSE_CHARS.index(b)

# =============================================================================

def match_empty(before, after):
    if len(before) == 0:
        # - . -|- . ]) => ])|-
        return [([], [], after, [])]
    elif len(after) == 0:
        # ([ . -|- . - => -|([
        return [([], [], [], before)]
    elif matches(before[-1], after[0]):
        # ([ . -|- . ]) --> ( . -|- . )
        return match_empty(before[:-1], after[1:])
    else:
        # ([ . -|- . }) => fail
        return []

def match_no_closes(before, after):
    if len(after) == 0:
        # ([ . -|y . - => -|([y
        return [([], ['y'], [], before + ['y'])]
    return (
        # case 1:
        # ([ . -|y[ . ]) --> ([ . -|y . )
        # then put [ back
        [(pat_closes, pat_opens + [to_open(after[0])],
          res_closes, res_opens)
         for (pat_closes, pat_opens, res_closes, res_opens)
         in match_no_closes(before, after[1:])] +
        # case 2:
        # ([ . -|- . ])
        match_empty(before, after))

def match(before, after):
    if len(before) == 0:
        # - . x|y . ]) --> - . -|y . ])
        # then put x back
        return [(['x'], pat_opens, ['x'] + res_closes, res_opens)
                for ([], pat_opens, res_closes, res_opens)
                in match_no_closes(before, after)]
    return (
        # case 1:
        # ([ . ]x|y . ]) --> ( . x|y . ])
        # then put ] back
        [([to_close(before[-1])] + pat_closes, pat_opens,
          res_closes, res_opens)
         for (pat_closes, pat_opens, res_closes, res_opens)
         in match(before[:-1], after)] +
        # case 2:
        # ([ . -|y . ])
        match_no_closes(before, after))

# =============================================================================

parser = argparse.ArgumentParser()
parser.add_argument('before')
parser.add_argument('after')
args = parser.parse_args()

(bc, bo) = parse(args.before)
(ac, ao) = parse(args.after)
rules = [(pat_closes, pat_opens, bc + res_closes, res_opens + ao)
         for (pat_closes, pat_opens, res_closes, res_opens)
         in match(bo, ac)]

for (pat_closes, pat_opens, res_closes, res_opens) in rules:
    print '%10s|%-10s -> %10s|%s' % (''.join(pat_closes), ''.join(pat_opens),
                                     ''.join(res_closes), ''.join(res_opens))
