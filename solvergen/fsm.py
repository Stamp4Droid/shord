#!/usr/bin/env python

import argparse
import bisect
import copy
import FAdo.common
import FAdo.fa
import glob
from itertools import izip, count
import os.path
from os.path import basename, splitext
import re
import string
import sys
import util

class Literal(util.Record):
    # TODO: Track if the symbol is parametric, enforce consistency, use when
    # printing.
    # TODO: Reuse code from cfg_parser.py.

    def __init__(self, reverse, symbol):
        assert re.match(r'^\w+$', symbol) is not None
        self._reverse = reverse
        self.symbol = symbol

    def __key__(self):
        return (self._reverse, self.symbol)

    def __str__(self):
        return ('_' if self._reverse else '') + self.symbol

    def is_terminal(self):
        return self.symbol[0] in string.ascii_lowercase

    def reverse(self):
        return Literal(not self._reverse, self.symbol)

    @staticmethod
    def from_string(str):
        if str == '-':
            return None
        m = re.match(r'^(_)?(\w+)(?:\[\*\])?$', str)
        return Literal(m.group(1) is not None, m.group(2))

class PartialFun(util.Record):
    # TODO: This gets used on states, which aren't always hashable => doing
    # this in a sub-optimal way currently.

    def __init__(self, arg_ret_pairs):
        self._table = util.sort_uniq(arg_ret_pairs)
        # Ensure that no argument appears twice
        assert len(self._table) == len(arg_ret_pairs)

    def __key__(self):
        return (self._table, )

    def apply(self, arg):
        for (x, f_x) in self._table:
            if arg == x:
                return f_x
            elif arg < x:
                raise ValueError
        raise ValueError

    def compose(self, other):
        combo_table = []
        for (x, f_x) in self._table:
            try:
                g_f_x = other.apply(f_x)
                combo_table.append((x, g_f_x))
            except ValueError:
                pass
        return PartialFun(combo_table)

    @staticmethod
    def bottom():
        return PartialFun([])

    @staticmethod
    def id(dom):
        return PartialFun([(x,x) for x in dom])

class FSM(util.BaseClass):
    # TODO: Verify there's at least one start and one end state.

    def __init__(self, nfa=None, literals=None):
        if nfa is None:
            nfa = FAdo.fa.NFA()
        if literals is None:
            literals = []
        self._nfa = nfa
        self._literals = util.IndexDict()
        for lit in literals:
            self._literals.index(lit)
        self._anon_states = 0

    def add_state(self, name, initial, final):
        if name is None:
            name = '#' + str(self._anon_states)
            self._anon_states += 1
        else:
            assert '#' not in name
        index = self._nfa.addState(name)
        if initial:
            self._nfa.addInitial(index)
        if final:
            self._nfa.addFinal(index)
        return name

    def _lit2tok(self, lit):
        if lit is None:
            return FAdo.common.Epsilon
        tok = str(self._literals.index(lit))
        self._nfa.addSigma(tok)
        return tok

    def _tok2lit(self, tok):
        if tok == FAdo.common.Epsilon:
            return None
        return self._literals[int(tok)]

    def add_trans(self, src, lit, dst):
        src_idx = self._nfa.stateName(src)
        dst_idx = self._nfa.stateName(dst)
        tok = self._lit2tok(lit)
        self._nfa.addTransition(src_idx, tok, dst_idx)

    def states(self):
        return self._nfa.States

    def initial(self):
        return [self._nfa.States[i] for i in self._nfa.Initial]

    def final(self):
        return [self._nfa.States[i] for i in self._nfa.Final]

    # Returns a list<(Literal,State)>
    def out_arrows(self, src):
        src_idx = self._nfa.stateName(src)
        idx_trans = self._nfa.delta.get(src_idx, {})
        return [(self._tok2lit(tok), self._nfa.States[i])
                for tok in idx_trans
                for i in idx_trans[tok]]

    # Returns a list<(State,Literal,State)>
    def arrows(self):
        return [(src, lit, dst)
                for src in self.states()
                for (lit, dst) in self.out_arrows(src)]

    def literals(self):
        return self._literals

    def lit_effects(self):
        lit2arrows = util.OrderedMultiDict()
        for (src, lit, dst) in self.arrows():
            lit2arrows.append(lit, (src,dst))
        return lit2arrows

    def insert_fsm(self, src, sub_fsm, dst):
        # TODO: Verify that sub-FSMs are fully expanded.
        state_map = {}
        for s in sub_fsm.states():
            state_map[s] = self.add_state(None, False, False)
        for s in sub_fsm.initial():
            self.add_trans(src, None, state_map[s])
        for s in sub_fsm.final():
            self.add_trans(state_map[s], None, dst)
        for sub_src in sub_fsm.states():
            for (lit, sub_dst) in sub_fsm.out_arrows(sub_src):
                self.add_trans(state_map[sub_src], lit, state_map[sub_dst])

    def reverse(self):
        rev_nfa = self._nfa.reversal()
        rev_map = [l.reverse() for l in self._literals]
        return FSM(rev_nfa, rev_map)

    def dump(self):
        print 'Initial:', self.initial()
        print 'Final:', self.final()
        for src in self.states():
            for (lit, dst) in self.out_arrows(src):
                print '%s --%s--> %s' % (src, lit, dst)

    def dump_tgf(self, out):
        for sidx in range(0, len(self._nfa.States)):
            out.write('%s%s%s\n' %
                      (sidx, ' in' if sidx in self._nfa.Initial else '',
                       ' out' if sidx in self._nfa.Final else '',))
        out.write('#\n')
        for src_idx in range(0, len(self._nfa.States)):
            idx_trans = self._nfa.delta.get(src_idx, {})
            dst2lits = util.OrderedMultiDict()
            for tok in idx_trans:
                lit = self._tok2lit(tok)
                for dst_idx in idx_trans[tok]:
                    dst2lits.append(dst_idx, lit)
            for dst_idx in dst2lits:
                out.write('%s %s %s\n' %
                          (src_idx, dst_idx,
                           ' '.join([str(l) for l in dst2lits.get(dst_idx)])))

    def minimize(self):
        # TODO: Also call complete(), to insert a dummy error state?
        return FSM(self._nfa.minimal().toNFA(),
                   copy.deepcopy(self._literals))

class TransTable(util.BaseClass):
    # Can only be called on a deterministic, non-epsilon FSM.
    def __init__(self, fsm):
        self.funs = util.IndexDict()
        self.lit2fidx = {}
        self.comp_tab = []
        self.accepting = set()

        bot_fidx = self.funs.index(PartialFun.bottom())
        self.funs.index(PartialFun.id(fsm.states()))
        lit_effects = fsm.lit_effects()
        for lit in lit_effects:
            assert lit is not None
            eff_fun = PartialFun(lit_effects.get(lit))
            self.lit2fidx[lit] = self.funs.index(eff_fun)
            rev_lit = lit.reverse()
            if rev_lit not in self.lit2fidx:
                self.lit2fidx[rev_lit] = bot_fidx

        i = 0
        while i < len(self.funs):
            f = self.funs[i]
            for (g, g_row) in zip(self.funs[:i], self.comp_tab):
                g_row.append(self.funs.index(g.compose(f)))
            i += 1
            self.comp_tab.append([self.funs.index(f.compose(h))
                                  for h in self.funs[:i]])

        for src in fsm.initial():
            for f in self.funs:
                try:
                    if f.apply(src) in fsm.final():
                        self.accepting.add(self.funs.index(f))
                except ValueError:
                    pass

    def dump(self):
        idx_width = len(str(len(self.funs)))
        print 'Literal signatures:'
        print ', '.join([str(l) + ':' + str(self.lit2fidx[l])
                         for l in self.lit2fidx])
        print
        print 'Transition functions (%s total):' % len(self.funs)
        for (f_idx, f) in izip(count(), self.funs):
            print (' %s %*s: %s' %
                   ('*' if f_idx in self.accepting else ' ',
                    idx_width, f_idx, f))
        print
        ratio = sum([len([x for x in row if x > 0])
                     for row in self.comp_tab]) / float(len(self.funs)**2)
        print 'Composition table (%s%% full):' % int(ratio * 100)
        for row in self.comp_tab:
            print ' '.join(['%*s' % (idx_width, str(r) if r > 0 else '.')
                            for r in row])

def parse_dir(dir_name):
    fsm_files = glob.glob(os.path.join(dir_name, '*.fsm.tgf'))
    # TODO: Just taking the FSM files in alphabetic order, and assuming that
    # also reflects their stratification.
    return parse_files(sorted(fsm_files))

def parse_files(fsm_files):
    fsm_table = {}
    fsm_name = None

    for fname in fsm_files:
        # TODO: Check file extensions
        fsm_name = splitext(splitext(basename(fname))[0])[0]
        fsm_lit = Literal(False, fsm_name)
        assert not fsm_lit.is_terminal() and fsm_lit not in fsm_table
        fsm = FSM()

        with open(fname) as f:
            states_done = False
            for line in f:
                if line == '#\n':
                    states_done = True
                    continue
                parts = line.split()
                if not states_done:
                    # TODO: Check that all qualifiers are expected.
                    fsm.add_state(parts[0], 'in' in parts[1:],
                                  'out' in parts[1:])
                    continue
                src = parts[0]
                dst = parts[1]
                for s in parts[2:]:
                    lit = Literal.from_string(s)
                    if lit is None or lit.is_terminal():
                        fsm.add_trans(src, lit, dst)
                    else:
                        sub_fsm = fsm_table[lit]
                        fsm.insert_fsm(src, sub_fsm, dst)

        fsm_table[fsm_lit] = fsm
        rev_fsm = fsm.reverse()
        rev_fsm_lit = Literal(True, fsm_name)
        assert rev_fsm_lit not in fsm_table
        fsm_table[rev_fsm_lit] = rev_fsm

    # Return the final FSM after minimization, along with the corresponding
    # transition function table.
    top_fsm = fsm_table[Literal(False, fsm_name)]
    min_fsm = top_fsm.minimize()
    return (min_fsm, TransTable(min_fsm))

if __name__ == '__main__':
    # Just dump information about the last grammar.
    # TODO: Document calling convention.
    parser = argparse.ArgumentParser()
    parser.add_argument('fsms_dir')
    args = parser.parse_args()

    (min_fsm, trans_tab) = parse_dir(args.fsms_dir)
    print 'Minimal FSM:'
    min_fsm.dump()
    print
    trans_tab.dump()
    print
    print 'TGF for minimal FSM:'
    min_fsm.dump_tgf(sys.stdout)
