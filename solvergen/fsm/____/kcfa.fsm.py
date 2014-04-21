#!/usr/bin/env python

import argparse
import os
import re

def insert(d, k, v):
    if k not in d:
        d[k] = v
        return v
    return d[k]

# =============================================================================

var_ctxts = set()
obj_ctxts = set()
idx2type = {} # this is only used for sanity checking

def record_ctxt(ctxt, typ):
    if typ == 'c':
        var_ctxts.add(ctxt)
    elif typ == 'a':
        obj_ctxts.add(ctxt)
    else:
        assert False
    m = re.match(r'^%s([0-9]+)$' % typ, ctxt)
    assert m is not None
    stored = insert(idx2type, m.group(1), typ)
    assert stored == typ

call_trans = {}
_call_trans = {}

def record_call(c1, i, c2):
    record_ctxt(c1, 'c')
    record_ctxt(c2, 'c')
    insert(insert(call_trans, c1, {}), c2, set()).add(i)
    insert(insert(_call_trans, c2, {}), c1, set()).add(i)

new_trans = {}
_new_trans = {}

def record_allocation(c, o):
    record_ctxt(c, 'c')
    record_ctxt(o, 'a')
    insert(new_trans, o, set()).add(c)
    insert(_new_trans, c, set()).add(o)

# =============================================================================

neutral_syms = ['asgnRef', 'loadRef[*]', 'storeRef[*]', 'asgnPrim',
                'loadPrim[*]', 'storePrim[*]', 'prim2Prim', 'prim2Ref',
                'ref2Prim', 'ref2Ref', 'prim2SinkByPrim', 'prim2SinkByRef',
                'ref2SinkByPrim', 'ref2SinkByRef', 'label2Prim', 'label2Ref',
                'prim2Sink', 'ref2Sink', 'new']
neutral_lits = ' '.join(neutral_syms + ['_' + n for n in neutral_syms])

enter_lits = 'srcLabel'
exit_lits = 'sinkLabel'

lits_to_glob = 'statStoreRef statStorePrim _statLoadRef _statLoadPrim'
lits_from_glob = '_statStoreRef _statStorePrim statLoadRef statLoadPrim'

def call_lits(i):
    return 'paramRef[%s] paramPrim[%s] _retRef[%s] _retPrim[%s]' % (i,i,i,i)
def _call_lits(i):
    return '_paramRef[%s] _paramPrim[%s] retRef[%s] retPrim[%s]' % (i,i,i,i)

# =============================================================================

parser = argparse.ArgumentParser()
parser.add_argument('indir')
parser.add_argument('outfile')
args = parser.parse_args()

with open(os.path.join(args.indir, 'cic.rel')) as f:
    for line in f:
        [c1,i,c2] = line.split()
        record_call(c1, i, c2)

with open(os.path.join(args.indir, 'co.rel')) as f:
    for line in f:
        [c,o] = line.split()
        record_allocation(c, o)

with open(args.outfile, 'w') as f:
    f.write('START in\n') # dummy "start" state
    f.write('END out\n')  # dummy "end" state
    f.write('GLOBAL\n')   # "global" context state
    for c in var_ctxts:
        f.write('%s\n' % c)

    f.write('#\n')

    # Global transitions
    f.write('* GLOBAL %s\n' % lits_to_glob)
    f.write('GLOBAL * %s\n' % lits_from_glob)
    for c1 in var_ctxts:
        # Neutral symbols
        f.write('%s %s %s\n' % (c1, c1, neutral_lits))
        # Entry/exit labels
        f.write('START %s %s\n' % (c1, enter_lits))
        f.write('%s END %s\n' % (c1, exit_lits))
        # Calls & returns
        tgt2invks = call_trans.get(c1, {})
        for c2 in tgt2invks:
            for i in tgt2invks[c2]:
                f.write('%s %s %s\n' % (c1, c2, call_lits(i)))
        src2invks = _call_trans.get(c1, {})
        for c2 in src2invks:
            for i in src2invks[c2]:
                f.write('%s %s %s\n' % (c1, c2, _call_lits(i)))
