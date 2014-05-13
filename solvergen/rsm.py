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

def label2str(label):
    if label is None:
        return '-'
    return str(label)

# =============================================================================

class Machine(util.BaseClass):
    def __init__(self, orig=None, keep_eps=False):
        if orig is None:
            self._nfa = FAdo.fa.NFA()
            self._labels = []
            # States not in self._refd are regular states (only those can be
            # initial or final).
            self._refd = {} # state name -> Reference
            self._anon_states = 0
        else:
            self._nfa = orig._nfa.reversal()
            self._labels = [l.reverse() for l in orig._labels]
            self._refd = dict([(s, orig._refd[s].reverse())
                               for s in orig._refd])
            self._anon_states = orig._anon_states
            self.finalize(keep_eps)
            # TODO: Probably OK if rev_states is a subset of str_states
            assert self._nfa.States == orig._nfa.States

    def finalize(self, keep_eps):
        # TODO: Include a finalized flag, which is set here and checked on
        # every destructive operation.
        assert len(self._nfa.Initial) > 0
        assert len(self._nfa.Final) > 0
        if not keep_eps:
            self._nfa.eliminateEpsilonTransitions()
            self._nfa.trim()
            for s in self._refd:
                if s not in self._nfa.States:
                    del self._refd[s]

    def _add_state(self, name, initial, final, ref):
        if name is None:
            name = '__' + str(self._anon_states)
            self._anon_states += 1
        else:
            assert '__' not in name
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

    def add_trans(self, src, label, dst):
        if src in self._refd or dst in self._refd:
            assert not (src in self._refd and dst in self._refd)
            assert label is not None
            assert label.indexed
        else:
            assert (label is None
                    or isinstance(label, Reference)
                    or not label.indexed)
        src_idx = self._nfa.stateName(src)
        dst_idx = self._nfa.stateName(dst)
        tok = self._record_label(label)
        self._nfa.addTransition(src_idx, tok, dst_idx)

    def states(self):
        return self._nfa.States

    def refd_comps(self):
        return set([ref.comp for ref in self._refd.itervalues()]
                   + [label.comp for label in self._labels
                      if isinstance(label, Reference)])

    def initial(self):
        return [self._nfa.States[i] for i in self._nfa.Initial]

    def final(self):
        return [self._nfa.States[i] for i in self._nfa.Final]

    # Returns a list<(Label,State)>
    def out_arrows(self, src):
        src_idx = self._nfa.stateName(src)
        idx_trans = self._nfa.delta.get(src_idx, {})
        return [(self._tok2label(tok), self._nfa.States[i])
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
            for (label, sub_dst) in other.out_arrows(sub_src):
                self.add_trans(state_map[sub_src], label, state_map[sub_dst])

    def _record_label(self, label):
        if label is None:
            return FAdo.common.Epsilon
        tok = str(len(self._labels))
        self._labels.append(label)
        self._nfa.addSigma(tok)
        return tok

    def _tok2label(self, tok):
        if tok == FAdo.common.Epsilon:
            return None
        return self._labels[int(tok)]

    # Returns a src_idx -> dst_idx -> list<Label>
    def _flipped_delta(self):
        res = {}
        for src_idx in range(0, len(self._nfa.States)):
            res[src_idx] = util.OrderedMultiDict()
            idx_trans = self._nfa.delta.get(src_idx, {})
            for tok in idx_trans:
                label = self._tok2label(tok)
                for dst_idx in idx_trans[tok]:
                    res[src_idx].append(dst_idx, label)
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
                labels = fd[src_idx].get(dst_idx)
                out.write('%s %s %s\n' %
                          (self._nfa.States[src_idx],
                           self._nfa.States[dst_idx],
                           ' '.join([label2str(l) for l in labels])))

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
                labels = fd[src_idx].get(dst_idx)
                label = r'\n'.join([label2str(l) for l in labels])
                out.write('  %s -> %s [label = "%s"];\n' %
                          (src_idx, dst_idx, label))
        out.write('}\n')

    def dump_cfg(self, out, comp):
        sidx2nt = {}
        for sidx in range(0, len(self._nfa.States)):
            if self._nfa.States[sidx] in self._refd:
                continue
            sidx2nt[sidx] = comp.name + '__' + str(sidx)

        for i in self._nfa.delta:
            if self._nfa.States[i] in self._refd:
                continue
            for t1 in self._nfa.delta[i]:
                l1 = self._tok2label(t1)
                for j in self._nfa.delta[i][t1]:
                    if self._nfa.States[j] in self._refd:
                        ref = self._refd[self._nfa.States[j]]
                        assert not ref.comp.name.startswith('S_')
                        for t2 in self._nfa.delta[j]:
                            l2 = self._tok2label(t2)
                            for k in self._nfa.delta[j][t2]:
                                out.write('%s :: %s %s %s %s\n' %
                                          (sidx2nt[k], sidx2nt[i],
                                           l1, ref, l2))
                    elif l1 is None:
                        out.write('%s :: %s\n' % (sidx2nt[j], sidx2nt[i]))
                    else:
                        out.write('%s :: %s %s\n' %
                                  (sidx2nt[j], sidx2nt[i], l1))

        for sidx in self._nfa.Initial:
            out.write('%s :: -\n' % sidx2nt[sidx])
        for sidx in self._nfa.Final:
            out.write('%s :: %s\n' % (Reference(False, comp), sidx2nt[sidx]))

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

    def finalize(self, keep_eps):
        self.str.finalize(keep_eps)
        self.rev = Machine(self.str, keep_eps)

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

    def add_trans(self, src, label, dst):
        self.str.add_trans(src, label, dst)

    def inline(self, src, machine, dst):
        self.str.inline(src, machine, dst)

    def write(self, fmt, out_dir):
        if out_dir is None:
            print '%s:' % Reference(False, self)
            print
            self.dump(fmt, sys.stdout)
            print
        else:
            out_file = os.path.join(out_dir, self.name + '.rsm.' + fmt)
            with open(out_file, 'w') as out:
                self.dump(fmt, out)

    def dump(self, fmt, out):
        if fmt == 'tgf':
            self.str.dump_tgf(out)
        elif fmt == 'dot':
            self.str.dump_dot(out)
        elif fmt == 'cfg':
            self.str.dump_cfg(out, self)
        else:
            assert False

# =============================================================================

class RSM(util.BaseClass):
    def __init__(self, dir_name, keep_refs, keep_eps):
        self._symbols = SymbolStore()
        self._components = ComponentStore()
        self._curr_comp = None
        self._keep_refs = keep_refs
        self._keep_eps = keep_eps
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
        self._curr_comp.finalize(self._keep_eps)
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
                if self._keep_refs:
                    self._curr_comp.add_trans(src, ref, dst)
                else:
                    machine = ref.comp.rev if ref.reversed else ref.comp.str
                    self._curr_comp.inline(src, machine, dst)
            else:
                lit = self._parse_lit(s)
                self._curr_comp.add_trans(src, lit, dst)

    def write(self, fmt, out_dir):
        for comp in self._out_comps:
            comp.write(fmt, out_dir)

if __name__ == '__main__':
    # Just dump all components.
    parser = argparse.ArgumentParser()
    parser.add_argument('-r', '--keep-refs', action='store_true')
    parser.add_argument('-e', '--keep-eps', action='store_true')
    parser.add_argument('-f', '--format', choices=['tgf', 'dot', 'cfg'],
                        default='tgf')
    parser.add_argument('rsm_dir')
    parser.add_argument('out_dir', nargs='?')
    args = parser.parse_args()

    rsm = RSM(args.rsm_dir, args.keep_refs, args.keep_eps)
    rsm.write(args.format, args.out_dir)
