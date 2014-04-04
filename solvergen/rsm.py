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

# =============================================================================

class Symbol(util.Hashable):
    def __init__(self, name, ref, parametric):
        self.name = name
        self.ref = ref
        self.parametric = parametric

    def __key__(self):
        return (self.name, self.parametric)

    def __str__(self):
        return self.name

class SymbolStore(util.UniqueNameMap):
    def __init__(self):
        super(SymbolStore, self).__init__()

    def managed_class(self):
        return Symbol

    def valid_name(self, name):
        return re.match(r'^[a-z]\w*$', name) is not None

class Reference(util.Record):
    def __init__(self, reversed, comp):
        self.reversed = reversed
        self.comp = comp

    def __key__(self):
        return (self.reversed, self.comp)

    def __str__(self):
        return ('_' if self.reversed else '') + str(self.comp)

    def reverse(self):
        return Reference(not self.reversed, self.comp)

class Literal(util.Record):
    def __init__(self, reversed, symbol, indexed):
        assert not indexed or symbol.parametric
        self.reversed = reversed
        self.symbol = symbol
        self.indexed = indexed

    def __key__(self):
        return (self.reversed, self.symbol, self.indexed)

    def __str__(self):
        idx_str = ('' if not self.symbol.parametric
                   else '[*]' if not self.indexed
                   else '[i]')
        return ('_' if self.reversed else '') + str(self.symbol) + idx_str

    def reverse(self):
        return Literal(not self.reversed, self.symbol, self.indexed)

def lit2str(lit):
    if lit is None:
        return '-'
    return str(lit)

# =============================================================================

class Machine(util.BaseClass):
    def __init__(self, orig=None):
        if orig is None:
            self._nfa = FAdo.fa.NFA()
            self._lits = []
            # States not in self._refd are regular states (only those can be
            # initial or final).
            self._refd = {} # state name -> Reference
            self._anon_states = 0
        else:
            self._nfa = orig._nfa.reversal()
            self._lits = [l.reverse() for l in orig._lits]
            self._refd = dict([(s, orig._refd[s].reverse())
                               for s in orig._refd])
            self._anon_states = orig._anon_states
            self.finalize()
            # TODO: Probably OK if rev_states is a subset of str_states
            assert self._nfa.States == orig._nfa.States

    def finalize(self):
        # TODO: Include a finalized flag, which is set here and checked on
        # every destructive operation.
        assert len(self._nfa.Initial) > 0
        assert len(self._nfa.Final) > 0
        self._nfa.eliminateEpsilonTransitions()
        self._nfa.trim()
        for s in self._refd:
            if s not in self._nfa.States:
                del self._refd[s]

    def _add_state(self, name, initial, final, ref):
        if name is None:
            name = '#' + str(self._anon_states)
            self._anon_states += 1
        else:
            assert '#' not in name
        # Guarantees no name duplication
        index = self._nfa.addState(name)
        if initial:
            self._nfa.addInitial(index)
        if final:
            self._nfa.addFinal(index)
        if ref is not None:
            self._refd[name] = ref
        return name

    def add_node(self, name, initial, final):
        assert name is not None
        self._add_state(name, initial, final, None)

    def add_box(self, name, ref):
        assert name is not None
        assert ref is not None
        self._add_state(name, False, False, ref)

    def add_trans(self, src, lit, dst):
        if src in self._refd or dst in self._refd:
            assert not (src in self._refd and dst in self._refd)
            assert lit is not None
            assert lit.indexed
        else:
            assert lit is None or not lit.indexed
        src_idx = self._nfa.stateName(src)
        dst_idx = self._nfa.stateName(dst)
        tok = self._record_lit(lit)
        self._nfa.addTransition(src_idx, tok, dst_idx)

    def states(self):
        return self._nfa.States

    def refd_comps(self):
        return set([ref.comp for ref in self._refd.itervalues()])

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

    def inline(self, src, other, dst):
        assert src not in self._refd and dst not in self._refd
        # TODO: Check that the machine to inline is finalized.
        state_map = {}
        for s in other.states():
            new_s = self._add_state(None, False, False, None)
            state_map[s] = new_s
            if s in other._refd:
                self._refd[new_s] = other._refd[s]
        for s in other.initial():
            self.add_trans(src, None, state_map[s])
        for s in other.final():
            self.add_trans(state_map[s], None, dst)
        for sub_src in other.states():
            for (lit, sub_dst) in other.out_arrows(sub_src):
                self.add_trans(state_map[sub_src], lit, state_map[sub_dst])

    def _record_lit(self, lit):
        if lit is None:
            return FAdo.common.Epsilon
        tok = str(len(self._lits))
        self._lits.append(lit)
        self._nfa.addSigma(tok)
        return tok

    def _tok2lit(self, tok):
        if tok == FAdo.common.Epsilon:
            return None
        return self._lits[int(tok)]

    # Returns a src_idx -> dst_idx -> list<Literal>
    def _flipped_delta(self):
        res = {}
        for src_idx in range(0, len(self._nfa.States)):
            res[src_idx] = util.OrderedMultiDict()
            idx_trans = self._nfa.delta.get(src_idx, {})
            for tok in idx_trans:
                lit = self._tok2lit(tok)
                for dst_idx in idx_trans[tok]:
                    res[src_idx].append(dst_idx, lit)
        return res

    def dump_tgf(self, out):
        for sidx in range(0, len(self._nfa.States)):
            s = self._nfa.States[sidx]
            out.write('%s%s%s%s\n' %
                      (s, ' in' if sidx in self._nfa.Initial else '',
                       ' out' if sidx in self._nfa.Final else '',
                       ' %s' % self._refd[s] if s in self._refd else ''))
        out.write('#\n')
        fd = self._flipped_delta()
        for src_idx in fd:
            for dst_idx in fd[src_idx]:
                lits = fd[src_idx].get(dst_idx)
                out.write('%s %s %s\n' %
                          (self._nfa.States[src_idx],
                           self._nfa.States[dst_idx],
                           ' '.join([lit2str(l) for l in lits])))

    def dump_dot(self, out):
        out.write('digraph {\n')
        out.write('  rankdir=LR;\n')
        for sidx in range(0, len(self._nfa.States)):
            s = self._nfa.States[sidx]
            shape = (('double' if sidx in self._nfa.Final else '') +
                     ('octagon' if sidx in self._nfa.Initial else 'circle'))
            out.write('  %s [label = "%s", shape = %s];\n'
                      % (sidx, self._refd.get(s, ''), shape))
        fd = self._flipped_delta()
        for src_idx in fd:
            for dst_idx in fd[src_idx]:
                lits = fd[src_idx].get(dst_idx)
                label = r'\n'.join([lit2str(l) for l in lits])
                out.write('  %s -> %s [label = "%s"];\n' %
                          (src_idx, dst_idx, label))
        out.write('}\n')

class ComponentStore(util.UniqueNameMap):
    def __init__(self):
        super(ComponentStore, self).__init__()

    def managed_class(self):
        return Component

    def valid_name(self, name):
        return re.match(r'^[A-Z]\w*$', name) is not None

class Component(util.Hashable):
    def __init__(self, name, _):
        self.name = name
        self.str = Machine()
        self.rev = None
        self._mutables = ['rev']

    def finalize(self):
        self.str.finalize()
        self.rev = Machine(self.str)

    def refd_comps(self):
        return self.str.refd_comps()

    def __key__(self):
        return (self.name, )

    def __str__(self):
        return self.name

    def add_node(self, name, initial, final):
        self.str.add_node(name, initial, final)

    def add_box(self, name, ref):
        self.str.add_box(name, ref)

    def add_trans(self, src, lit, dst):
        self.str.add_trans(src, lit, dst)

    def inline(self, src, machine, dst):
        self.str.inline(src, machine, dst)

    def write(self, out_dir):
        out_file = os.path.join(out_dir, self.name + '.rsm.tgf')
        with open(out_file, 'w') as out:
            self.str.dump_tgf(out)

    def dump(self, out):
        out.write('%s:\n\n' % Reference(False, self))
        self.str.dump_tgf(out)
        out.write('\n')
        out.write('%s:\n\n' % Reference(True, self))
        self.rev.dump_tgf(out)
        out.write('\n')

# =============================================================================

class RSM(util.BaseClass):
    def __init__(self, dir_name):
        self._symbols = SymbolStore()
        self._components = ComponentStore()
        self._curr_comp = None
        files = glob.glob(os.path.join(dir_name, '*.rsm.tgf'))
        # TODO: Just taking the Components in alphabetic order, and assuming
        # that also reflects their stratification.
        for fname in sorted(files):
            self._parse_comp(fname)
        self._out_comps = set() # set<Component>
        self._add_out_comp(list(self._components)[-1])

    def _add_out_comp(self, comp):
        if comp not in self._out_comps:
            self._out_comps.add(comp)
            for c in comp.refd_comps():
                self._add_out_comp(c)

    def _parse_comp(self, fname):
        name = splitext(splitext(basename(fname))[0])[0]
        assert self._components.find(name) is None
        self._curr_comp = self._components.get(name)
        with open(fname) as f:
            states_done = False
            for line in f:
                if line == '#\n':
                    assert not states_done
                    states_done = True
                elif not states_done:
                    self._parse_state(line)
                else:
                    self._parse_trans(line)
            assert states_done
        self._curr_comp.finalize()
        self._curr_comp = None

    def _parse_state(self, line):
        parts = line.split()
        name = parts[0]
        quals = parts[1:]
        if len(quals) == 0 or 'in' in quals or 'out' in quals:
            initial = False
            final = False
            for q in quals:
                if q == 'in':
                    initial = True
                elif q == 'out':
                    final = True
                else:
                    assert False
            self._curr_comp.add_node(name, initial, final)
        else:
            assert len(quals) == 1
            ref = self._try_parse_reference(quals[0])
            assert ref is not None
            self._curr_comp.add_box(name, ref)

    def _try_parse_reference(self, str):
        m = re.match(r'^(_)?([A-Z]\w*)$', str)
        if m is None:
            return None
        comp = self._components.find(m.group(2))
        assert comp is not None
        return Reference(m.group(1) is not None, comp)

    def _parse_lit(self, str):
        if str == '-':
            return None
        m = re.match(r'^(_)?([a-z]\w*)(?:\[([a-zA-Z\*])\])?$', str)
        assert m is not None
        idx_char = m.group(3)
        symbol = self._symbols.get(m.group(2), idx_char is not None)
        if idx_char == '*':
            idx_char = None
        return Literal(m.group(1) is not None, symbol, idx_char is not None)

    def _parse_trans(self, line):
        parts = line.split()
        src = parts[0]
        dst = parts[1]
        assert len(parts) > 2, "Unlabelled transition"
        for s in parts[2:]:
            ref = self._try_parse_reference(s)
            if ref is not None:
                # References on arcs must be inlinable
                # => can't be self-recursive
                assert ref.comp != self._curr_comp
                machine = ref.comp.rev if ref.reversed else ref.comp.str
                self._curr_comp.inline(src, machine, dst)
            else:
                lit = self._parse_lit(s)
                self._curr_comp.add_trans(src, lit, dst)

    def write(self, out_dir):
        for comp in self._out_comps:
            comp.write(out_dir)

    def dump(self, out):
        for comp in self._components:
            comp.dump(out)

if __name__ == '__main__':
    # Just dump all components.
    parser = argparse.ArgumentParser()
    parser.add_argument('rsm_dir')
    parser.add_argument('out_dir', nargs='?')
    args = parser.parse_args()

    rsm = RSM(args.rsm_dir)
    if args.out_dir is not None:
        rsm.write(args.out_dir)
    else:
        rsm.dump(sys.stdout)
