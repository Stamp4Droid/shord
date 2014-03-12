#!/usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
from argparse import RawTextHelpFormatter as RawFormatter
import fsm
import os
import re
import string
import sys
import util

""" @file
Infrastructure for producing the functions in @ref Generated from a
Context-Free Grammar.

The accepted format for the input grammar is as follows:

- Except otherwise noted, the parser is insensitive to whitespace, including
  extra blank lines.
- Comments are written using the `#` character, and extend to the end of the
  line.
- A @Symbol can be any alphanumeric string (including underscores). The first
  character must be a letter. @Symbol%s are case-sensitive.
- @Symbol%s don't need to be declared before use.
- Terminal symbols are represented by strings starting with a lowercase
  character. Conversely, names for non-terminals start with an uppercase
  character.
- Each non-terminal may have one or more productions associated with it. A
  production is any sequence of @Symbol%s (terminals and/or non-terminals)
  separated by whitespace, e.g. `B c D`. Productions cannot span multiple
  lines. Empty Productions can be declared using a single `-` character.
- A set of productions is associated with some non-terminal `A` using the
  notation `A :: ...`. The right-hand side of `::` must contain one or more
  productions (as defined above) separated by `|` marks. A series of
  productions may extend to subsequent lines, as long as each line starts a new
  production (the line must begin with `|`). A series of productions ends at
  the first blank line, or at the start of another `::` declaration (there is
  no dedicated end-of-production mark). The same non-terminal may be associated
  with any number of `::` declarations.
- Any instance of a @Symbol in a production can be prefixed with `_` to signify
  that the corresponding Edge should be traversed in reverse during the
  CFL-Reachability computation.
- The Edge%s associated with a @Symbol may additionally be parameterized with
  an @Index. Instances of indexed @Symbol%s in the grammar must be suffixed by
  an index expression, `[i]`, where `i` can be any single latin letter, or the
  special character `*`. A production must contain either 0 or 2+ indexed
  symbols (including the generated @Symbol, on the LHS), and they must all use
  the same index character. The resulting code will only trigger the production
  if the indices on the Edge%s match. The special `[*]` index expression can be
  used in place of a specific @Index character on any @Symbol on the RHS, and
  will cause the generated solver code to ignore indices when matching that
  specific @Symbol. Any production can only generate a specific @Index for an
  indexed @Symbol, i.e. `[*]` cannot appear at the LHS of a production (this
  corresponds to "unsafe" rules in Datalog).
- An indexed production can additionally involve a @Relation, which controls
  how the @Indices on the Edge%s are matched. Instead of trigerring a match
  when all constituent Edge%s have the same @Index, a production with an
  associated @Relation @e rel will only trigger for those combinations of
  @Indices that are members of @e rel. A production can be associated with a
  relation @e rel by adding the directive `.rel(i,j,k)` at the end, where @e i,
  @e j and @e k are all the @Indices on the production's @Symbol%s. In contrast
  to regular productions, the @Indices on the @Symbol%s of relation-carrying
  productions must be different. All the @Index parameters on a relation
  directive must be distinct.
- A production can additionally include a Node predicate, which is declared
  after any @Relation the production may carry, as a (terminal or non-terminal)
  @Symbol prefixed with a `//` symbol, e.g. `T :: A B //Pred`. Whenever such a
  production triggers, the endpoints of the generated Edge must satisfy the
  predicate, otherwise the Edge is not added to the graph. Two Node%s `a` and
  `b` satisfy a predicate `Pred` iff `b` is reachable from `a` over a
  `Pred`-symbol path. Reachability is infered using any productions for symbol
  `Pred` (if it is non-terminal). This reachability calculation is performed on
  the fly for each triggering of the predicate-carrying production, and the
  results are not cached. In order to support this, certain restrictions are
  placed on the rules involving any symbol `Pred` that is used as a predicate:
  - It cannot be used as a regular symbol on the RHS of other productions
    (unless it is a terminal).
  - If indexed, it must always use the same index as the @Result of any
    production it is applied to.
  - It can only be produced directly, from up to 2 terminals (if it's a
    non-terminal).
  - It cannot be added to empty productions.
  - Rules producing it cannot carry predicates of their own.
- Special directives can be added to the grammar. These are declared using the
  syntax `.<directive> <params> ...`, and must be placed alone in a line.
  Directives break preceding productions. The following directives are
  recognized:
  - `.paths <symbol> <num-paths>`: Instructs the generated solver to print out
    the selected number of witness paths for each of the Edge%s of the
    selected non-terminal @Symbol in the final graph.

Example of a valid grammar specification:

    S :: - | koo # this is a comment
       | A[j] _A[j] | t[*]
    A[i] :: foo[i] S _S bar[*]
"""

class SymbolStore(util.UniqueNameMap):
    """
    A container for @Symbol%s encountered in the input grammar.
    """

    def __init__(self):
        super(SymbolStore, self).__init__()
        self._num_temps = 0

    def copy_from(self, src):
        super(SymbolStore, self).copy_from(src)
        self._num_temps = src._num_temps

    def clone(self):
        clone = SymbolStore()
        clone.copy_from(self)
        return clone

    def make_temporary(self, parametric):
        """
        Generate a unique non-terminal @Symbol, guaranteed to never clash with
        any user-defined @Symbol.
        """
        temp_name = '%' + str(self._num_temps)
        self._num_temps += 1
        # We don't use the public interface, to bypass the name check.
        symbol = self._make(temp_name, parametric)
        self._add(temp_name, symbol)
        return symbol

    def managed_class(self):
        return Symbol

    def valid_name(self, name):
        """
        Check if @a name is a valid name for a user-defined @Symbol.
        """
        return re.match(r'^[a-zA-Z]\w*$', name) is not None

class Symbol(util.Hashable):
    """
    A symbol in the input grammar.
    """

    def __init__(self, name, ref, parametric):
        """
        Objects of this class are managed by the cfg_parser::SymbolStore class.
        Do not call this constructor directly, use
        cfg_parser::SymbolStore::get() instead.
        """
        ## The @Symbol<!-- -->'s string in the input grammar.
        self.name = name
        ## A unique number assigned to this @Symbol by its manager class.
        self.ref = ref
        ## Whether Edge%s of this @Symbol are parameterized by @Indices.
        self.parametric = parametric
        self._num_paths = 0

    def is_terminal(self):
        """
        Check if this is a terminal symbol of the input grammar.

        Terminal @Symbol names start with lowercase characters.
        """
        return self.name[0] in string.ascii_lowercase

    def is_temporary(self):
        """
        Check if this is an intermediate symbol introduced by the parser.

        Intermediate @Symbol names start with a `%` character.
        """
        return self.name[0] == '%'

    def num_paths(self):
        """
        Return the number of paths we wish the solver to print for each Edge of
        this @Symbol.
        """
        return self._num_paths

    def set_num_paths(self, num_paths):
        """
        Set the number of paths we wish the solver to print for each Edge of
        this @Symbol.
        """
        assert not self.is_terminal(), \
            "Paths can only be printed for non-terminals"
        assert not self.is_temporary(), \
            "Paths cannot be printed for intermediate symbols"
        self._num_paths = num_paths

    def __key__(self):
        return (self.name, self.parametric)

    def __str__(self):
        return self.name

    def as_result(self):
        return self.name + ('' if not self.parametric else '[i]')

class Literal(util.Record):
    """
    An instance of some @Symbol on the RHS of a production.

    May optionally contain a 'reverse' modifier and/or an @Index expression.
    """

    def __init__(self, symbol, indexed, reversed):
        assert not indexed or symbol.parametric, \
            "Index modifier on non-parametric symbol %s" % symbol
        ## The @Symbol represented by this @Literal.
        self.symbol = symbol
        ## The @Index carried by this @Literal, as a 0-based position in the
        #  containing production's @Relation. Is @e None for non-indexed
        #  @Literal%s, and @e 0 for all indexed @Literal%s of productions that
        #  don't carry a @Relation.
        #
        #  Parametric @Symbol%s with a `[*]` index expression are considered to
        #  be non-indexed.
        self.indexed = indexed
        ## Whether this @Literal has a 'reverse' modifier.
        self.reversed = reversed

    def __key__(self):
        return (self.symbol, self.indexed, self.reversed)

    def __str__(self):
        idx_str = ('' if not self.symbol.parametric else
                   '[*]' if not self.indexed else '[i]')
        return ('_' if self.reversed else '') + str(self.symbol) + idx_str

    def reverse(self):
        return Literal(self.symbol, self.indexed, not self.reversed)

    def relax(self):
        if not self.indexed:
            return self
        return Literal(self.symbol, False, self.reversed)

def check_production(result, used):
    """
    Test that a production with the given properties is valid.
    """
    indices = (([0] if result.parametric else [])
               + [0 for e in used if e.indexed])
    assert indices == [] or len(indices) >= 2, \
        "At least two indexed elements required per production"

class Context(util.Record):
    def __init__(self, lead, follow, result):
        self.lead = lead
        self.follow = follow
        self.result = result

    def empty(self):
        return self.lead.empty() and self.follow.empty()

    def __key__(self):
        return (self.lead, self.follow, self.result)

    def __str__(self):
        return '%s . %s => %s' % (self.lead, self.follow,
                                  self.result.as_result())

class Sequence(util.Record):
    def __init__(self, *lits):
        self.lits = tuple(lits)

    def __key__(self):
        return self.lits

    def __str__(self):
        if self.empty():
            return '-'
        return ' '.join([str(l) for l in self.lits])

    # TODO: Only works for slicing
    def __getitem__(self, key):
        return Sequence(*(self.lits[key]))

    def num_indexed(self):
        return len([0 for l in self.lits if l.indexed])

    def empty(self):
        return self.lits == ()

    def indexed(self):
        return self.num_indexed() > 0

    def reverse(self):
        return Sequence(*[l.reverse() for l in reversed(self.lits)])

    def relax(self):
        if self.num_indexed() != 1:
            return self
        return Sequence(*[l.relax() for l in self.lits])

    def ne_subseqs(self, result):
        num_lits = len(self.lits)
        for n in range(1, num_lits + 1):
            for i in range(0, num_lits - n + 1):
                yield (self[i:i+n],
                       Context(self[0:i], self[i+n:num_lits], result))

    def split(self, result, store):
        """
        Split this @Production into a list of @NormalProduction%s.

        Our splitting strategy works as follows:

            S :: a b c d e =>
            S :: ((((a b) c) d) e) =>
            T0 :: a b
            T1 :: T0 c
            T2 :: T1 d
            S  :: T2 e

        The extra @a store parameter is a @SymbolStore that will be used to
        generate any necessary temporary @Symbol%s.
        """
        num_used = len(self.lits)
        if num_used == 0:
            return [NormalProduction(result, None, None)]
        elif num_used == 1:
            return [NormalProduction(result, self.lits[0], None)]
        elif num_used == 2:
            return [NormalProduction(result, self.lits[0], self.lits[1])]
        r_used = self.lits[1:]
        num_temps = len(self.lits) - 2
        temp_parametric = [True for _ in range(0, num_temps)]
        # The only intermediate symbols that need to be indexed are those
        # between the first and the last indexed literals in the original
        # production (the result of the production counts as the rightmost
        # literal for this purpose).
        if not result.parametric:
            for i in range(num_temps-1, -1, -1):
                if r_used[i+1].indexed:
                    break
                else:
                    temp_parametric[i] = False
        if not self.lits[0].indexed:
            for i in range(0, num_temps):
                if r_used[i].indexed:
                    break
                else:
                    temp_parametric[i] = False
        temp_symbols = [store.make_temporary(p) for p in temp_parametric]
        temp_literals = [Literal(s, s.parametric, False) for s in temp_symbols]
        l_used = [self.lits[0]] + temp_literals
        results = temp_symbols + [result]
        return [NormalProduction(r, ls, rs)
                for (r, ls, rs) in zip(results, l_used, r_used)]

class SequenceMap(util.BaseClass):
    def __init__(self, grammar):
        # frozenset<Sequence> -> (set<Context>,set<Context>,
        #                         set<Context>,set<Context>)
        # TODO: An entry can currently be empty (if the relaxed form of some
        # sequence doesn't appear in any rule).
        self._map = {}
        self._fill_singles(grammar)
        self._fill_combinations()

    def _fill_singles(self, grammar):
        for res in grammar.prods:
            for fs in grammar.prods.get(res):
                for (ss,ctxt) in fs.ne_subseqs(res):
                    self._add_single(ss, ctxt)

    def _add_single(self, seq, ctxt):
        self._init_entries(seq)
        # entries on other maps will be updated automatically
        self._map[frozenset([seq])][0].add(ctxt)

    def _init_entries(self, seq):
        seq_set = frozenset([seq])
        if seq_set in self._map:
            # all other entries will have been set up as well
            return
        r_seq = seq.reverse()
        l_seq = seq.relax()
        lr_seq = r_seq.relax()
        c = set()
        rc = c if r_seq == seq else set()
        lc = c if l_seq == seq else set()
        lrc = rc if lr_seq == r_seq else set()
        # 0: regular instances
        if seq_set not in self._map:
            self._map[seq_set] = (c,rc,lc,lrc)
        # 1: reverse instances
        r_seq_set = frozenset([r_seq])
        if r_seq_set not in self._map:
            self._map[r_seq_set] = (rc,c,lrc,lc)
        # 2: relaxed instances
        l_seq_set = frozenset([l_seq])
        if l_seq_set not in self._map:
            self._map[l_seq_set] = (lc,lrc,lc,lrc)
        # 3: relaxed reverse instances
        lr_seq_set = frozenset([lr_seq])
        if lr_seq_set not in self._map:
            self._map[lr_seq_set] = (lrc,lc,lrc,lc)

    @staticmethod
    def _trivial_entry(entry):
        if len(entry[0]) + len(entry[1]) > 1:
            return False
        if len(entry[2]) + len(entry[3]) > 1:
            return False
        for ctxts in entry:
            for c in ctxts:
                if not c.empty():
                    return False
        # TODO: skipping special case:
        # for s in seqs:
        #     if s != s.relax():
        #         return False
        # Will cause us to miss an optimization opportunity in a case like:
        #   S :: B[i] C[i] D[i] | B[*] C[i] D[i]
        return True

    def _fill_combinations(self):
        pending = self._map
        self._map = {}
        while len(pending) > 0:
            (a, a_entry) = pending.popitem()
            if SequenceMap._trivial_entry(a_entry):
                continue
            for b in self._map:
                c = a | b
                if c in self._map or c in pending:
                    continue
                b_entry = self._map[b]
                c_entry = (a_entry[0] & b_entry[0],
                           a_entry[1] & b_entry[1],
                           a_entry[2] & b_entry[2],
                           a_entry[3] & b_entry[3])
                if SequenceMap._trivial_entry(c_entry):
                    continue
                pending[c] = c_entry
            self._map[a] = a_entry

    def __str__(self):
        return '\n'.join(['\n'.join([', '.join([str(seq) for seq in s]) + ':'] +
                                    ['\tstraight:'] +
                                    ['\t\t%s' % c for c in self._map[s][0]] +
                                    ['\treverse:'] +
                                    ['\t\t%s' % c for c in self._map[s][1]] +
                                    ['\trelaxed:'] +
                                    ['\t\t%s' % c for c in self._map[s][2]] +
                                    ['\treverse relaxed:'] +
                                    ['\t\t%s' % c for c in self._map[s][3]])
                          for s in self._map])

class Grammar(util.BaseClass):
    """
    A representation of the input grammar.
    """

    def __init__(self, cfg_file):
        self._lhs_symbol = None
        self._lhs_idx_char = None
        ## All the @Symbol%s encountered so far, stored in a specialized
        #  @SymbolStore container.
        self.symbols = SymbolStore()
        ## All the @Production%s encountered so far, grouped by result @Symbol.
        self.prods = util.UniqueMultiDict()
        with open(cfg_file) as grammar_in:
            for line in grammar_in:
                self._parse_line(line)
        self._finalize()

    def _finalize(self):
        """
        Run final sanity checks and calculations, which require all productions
        to be present.
        """
        for s in self.symbols:
            if not s.is_terminal() and len(self.prods.get(s)) == 0:
                assert False, "Non-terminal %s can never be produced" % s

    def _parse_line(self, line):
        """
        Parse the next line of the grammar specification.
        """
        line_wo_comment = (line.split('#'))[0]
        toks = line_wo_comment.split()
        if toks == [] or toks[0].startswith("."):
            # This is a special, non-production line, so we interrupt any
            # ongoing series of productions.
            self._end_series()
            if toks != []:
                self._parse_directive(toks[0][1:], toks[1:])
            return
        # This line contains one or more production definitions.
        if toks[0] == '|':
            # This line continues a series of productions.
            assert self._series_in_progress(), "| without preceding production"
            toks = toks[1:]
        elif len(toks) >= 2 and toks[1] == '::':
            # This line starts a new series of productions.
            self._begin_series(toks[0])
            toks = toks[2:]
        else:
            assert False, "Malformed production"
        while '|' in toks:
            split_pos = toks.index('|')
            self._parse_production(toks[:split_pos])
            toks = toks[split_pos+1:]
        self._parse_production(toks)

    def _parse_directive(self, directive, params):
        if directive == 'paths' and len(params) == 2:
            symbol = self.symbols.find(params[0])
            assert symbol is not None, \
                "Symbol %s not declared (yet)" % params[0]
            symbol.set_num_paths(int(params[1]))
        else:
            assert False, "Unknown directive: %s/%s" % (directive, len(params))

    def _parse_production(self, toks):
        assert toks != [], "Empty production not marked with '-'"
        used = []
        all_chars = [self._lhs_idx_char]
        if toks != ['-']:
            for t in toks:
                (lit, i) = self._parse_literal(t)
                used.append(lit)
                all_chars.append(i)
        assert util.all_same([i for i in all_chars if i is not None])
        # XXX: Empty productions temporarily disallowed.
        assert used != []
        check_production(self._lhs_symbol, used)
        self.prods.append(self._lhs_symbol, Sequence(*used))

    def _parse_literal(self, str):
        matcher = re.match(r'^(_?)([a-zA-Z]\w*)(?:\[([a-zA-Z\*])\])?$', str)
        assert matcher is not None, "Malformed literal: %s" % str
        reversed = matcher.group(1) != ''
        idx_char = matcher.group(3)
        symbol = self.symbols.get(matcher.group(2), idx_char is not None)
        if idx_char == '*':
            idx_char = None
        return (Literal(symbol, idx_char is not None, reversed), idx_char)

    def _begin_series(self, str):
        (self._lhs_symbol, self._lhs_idx_char) = self._parse_symbol(str)

    def _parse_symbol(self, str):
        matcher = re.match(r'^([A-Z]\w*)(?:\[([a-zA-Z])\])?$', str)
        assert matcher is not None, "Malformed symbol declaration: %s" % str
        idx_char = matcher.group(2)
        symbol = self.symbols.get(matcher.group(1), idx_char is not None)
        return (symbol, idx_char)

    def _end_series(self):
        self._lhs_symbol = None
        self._lhs_idx_char = None

    def _series_in_progress(self):
        return self._lhs_symbol is not None

#==============================================================================

class NormalProduction(util.BaseClass):
    """
    A normalized @Production, with up to 2 @Literal%s on the RHS.
    """

    def __init__(self, result, left, right):
        assert not(left is None and right is not None)
        used = (([] if left is None else [left]) +
                ([] if right is None else [right]))
        check_production(result, used)
        ## The @Result on the LHS of this @NormalProduction.
        self.result = result
        ## The first of up to 2 @Literal%s on the RHS. Is @e None for empty
        #  @NormalProduction%s.
        self.left = left
        ## The second of up to 2 @Literal%s on the RHS. Is @e None for empty
        #  or single @NormalProduction%s.
        self.right = right

    def get_rev_prods(self):
        """
        Get all the @ReverseProduction%s corresponding to this
        @NormalProduction.
        """
        if self.right is None:
            return [ReverseProduction(self.result, self.left, None,
                                      Position.FIRST)]
        else:
            return [ReverseProduction(self.result, self.left, self.right,
                                      Position.FIRST),
                    ReverseProduction(self.result, self.right, self.left,
                                      Position.SECOND)]

    def only_terminals(self):
        return ((self.left is None or self.left.symbol.is_terminal()) and
                (self.right is None or self.right.symbol.is_terminal()))

    def outer_search_direction(self):
        assert self.left is not None and self.right is not None
        return 'to' if self.left.reversed else 'from'

    def outer_search_source(self):
        assert self.left is not None
        if self.right is None:
            return 'to' if self.left.reversed else 'from'
        return 'from'

    def outer_search_target(self):
        assert self.left is not None
        if self.right is None:
            return 'from' if self.left.reversed else 'to'
        return None

    def outer_search_index(self):
        assert self.left is not None
        if self.left.indexed and self.result.parametric:
            return 'index'
        return None

    def outer_condition(self):
        assert self.left is not None
        return None

    def inner_search_source(self):
        assert self.left is not None and self.right is not None
        if self.right.reversed:
            return 'to'
        elif self.left.reversed:
            return 'l->from'
        else:
            return 'l->to'

    def inner_search_target(self):
        assert self.left is not None and self.right is not None
        if not self.right.reversed:
            return 'to'
        elif self.left.reversed:
            return 'l->from'
        else:
            return 'l->to'

    def inner_search_index(self):
        assert self.left is not None and self.right is not None
        if not self.right.indexed:
            return None
        if self.left.indexed:
            return 'l->index'
        else:
            assert self.result.parametric
            return 'index'

    def inner_loop_header(self):
        return None

    def inner_condition(self):
        assert self.left is not None and self.right is not None
        return None

    def __str__(self):
        rhs = ('-' if self.left is None
               else str(self.left) if self.right is None
               else str(self.left) + ' ' + str(self.right))
        return self.result.as_result() + ' :: ' + rhs

class Position(util.BaseClass):
    """
    An enumeration of relative Edge positions in a @NormalProduction.
    """

    ## The first in a series of two Edge%s being combined through a
    #  @NormalProduction.
    FIRST = 0
    ## The second in a series of two Edge%s being combined through a
    #  @NormalProduction.
    SECOND = 1

    @staticmethod
    def valid_position(pos):
        """
        Check whether @a pos is within the bounds of this enumeration.
        """
        return pos >= Position.FIRST and pos <= Position.SECOND

class ReverseProduction(util.BaseClass):
    """
    A production as seen from the point of view of some element on the RHS.

    A regular production (e.g. `S :: T R`) specifies what we can combine (a
    `T` followed by an `R`) to synthesize the LHS (an `S`). Conversely, a
    @ReverseProduction (e.g. `T + (* R) => S`) assumes we already have the
    'base' (a `T`), and specifies what additional elements are required (a
    subsequent `R`) to produce the 'result' (an `S`).

    This implementation allows up to one additional required @Literal.

    The public API exposed by this class translates the abstract, grammar-level
    relations between @Symbol%s to the corresponding low-level solver
    instructions that implement those relations. In the context of the solver,
    each @Literal is represented by an Edge of the appropriate @Kind, and the
    'base' @Literal corresponds to an Edge of the appropriate @Kind which we
    are currently processing.

    We will use the @ReverseProduction `B[i] + (A[i] *) => C[i]` as a running
    example to illustrate the functionality of the methods in this class. As
    part of this example, we assume we are currently processing an Edge
    compatible with the 'base' of this @ReverseProduction, i.e., an Edge for
    @Symbol `B`.
    """

    def __init__(self, result, base, reqd, base_pos):
        assert not(base is None and reqd is not None), \
            "Empty productions can't take a required literal"
        assert not(reqd is None and base_pos != Position.FIRST)
        assert Position.valid_position(base_pos)
        used = (([] if base is None else [base]) +
                ([] if reqd is None else [reqd]))
        check_production(result, used)
        ## The @Result of this @ReverseProduction.
        self.result = result
        ## The @Literal we assume to be present. Is @e None if this corresponds
        #  to an empty production.
        self.base = base
        ## The additional @Literal we need to complete the production. Is
        #  @e None if this corresponds to a single-element production.
        self.reqd = reqd
        ## What position the base @Literal has in the corresponding
        #  @NormalProduction.
        self.base_pos = base_pos

    def _check_need_to_search(self):
        assert self.base is not None, \
            "No need to search for empty productions"
        assert self.reqd is not None, \
            "No need to search for single-element productions"

    # TODO: The following methods return strings, which are tied to a
    #       particular variable and function naming. It might be more robust to
    #       return True/False and have the caller pick the strings to use.

    def search_source_endp(self):
        """
        On which endpoint of the Edge being processed we should search for an
        Edge that can complete this @ReverseProduction.

        In our running example, we need to search on the source Node.
        """
        self._check_need_to_search()
        if self.base_pos == Position.FIRST:
            return 'from' if self.base.reversed else 'to'
        elif self.base_pos == Position.SECOND:
            return 'to' if self.base.reversed else 'from'
        else:
            assert False

    def search_direction(self):
        """
        On which set of Edge%s (incoming or outgoing) of the
        @link ReverseProduction::search_source_endp() search endpoint@endlink
        we should search for an Edge that can complete this @ReverseProduction.

        In our running example, we need to search within the incoming Edge%s.
        """
        self._check_need_to_search()
        if self.base_pos == Position.FIRST:
            return 'to' if self.reqd.reversed else 'from'
        elif self.base_pos == Position.SECOND:
            return 'from' if self.reqd.reversed else 'to'
        else:
            assert False

    def search_index(self):
        self._check_need_to_search()
        if self.base.indexed and self.reqd.indexed:
            return 'base->index'
        return None

    def result_source(self):
        """
        Where we should place the source Node of any Edge produced by this
        @ReverseProduction.

        In our running example, we would place it on the source Node of the
        'other' Edge (the one representing @Symbol `A`).
        """
        if self.base is None:
            edge = 'base'
            endpoint = 'from'
        elif self.reqd is None:
            edge = 'base'
            endpoint = 'to' if self.base.reversed else 'from'
        elif self.base_pos == Position.FIRST:
            edge = 'base'
            endpoint = 'to' if self.base.reversed else 'from'
        elif self.base_pos == Position.SECOND:
            edge = 'other'
            endpoint = 'to' if self.reqd.reversed else 'from'
        else:
            assert False
        return edge + '->' + endpoint

    def result_target(self):
        """
        Where we should place the target Node of any Edge produced by this
        @ReverseProduction.

        In our running example, we would place it on the target Node of the
        'base' Edge (the one representing @Symbol `B`).
        """
        if self.base is None:
            edge = 'base'
            endpoint = 'to'
        elif self.reqd is None:
            edge = 'base'
            endpoint = 'from' if self.base.reversed else 'to'
        elif self.base_pos == Position.FIRST:
            edge = 'other'
            endpoint = 'from' if self.reqd.reversed else 'to'
        elif self.base_pos == Position.SECOND:
            edge = 'base'
            endpoint = 'from' if self.base.reversed else 'to'
        else:
            assert False
        return edge + '->' + endpoint

    def assertion(self):
        """
        What assertion we should check before triggering this
        @ReverseProduction, if any.
        """
        return None

    def result_index(self):
        """
        What @Index we should set on any Edge produced by this
        @ReverseProduction.

        In our running example, the @Indices on the combined Edge%s must match,
        so we can copy from either one of them. We arbitrarily choose to copy
        from the base Edge.
        """
        if not self.result.parametric:
            return 'INDEX_NONE'
        assert self.base is not None
        if self.reqd is None:
            assert self.base.indexed
            return 'base->index'
        elif self.base.indexed:
            return 'base->index'
        else:
            assert self.reqd.indexed
            return 'other->index'

    def loop_header(self):
        return None

    def condition(self):
        """
        Any additional @Index compatibility check we need to perform before we
        record the combination of the Edge%s.
        """
        return None

    def left_edge(self):
        if self.base is None:
            return 'NULL'
        elif self.reqd is None:
            return 'base'
        elif self.base_pos == Position.FIRST:
            return 'base'
        elif self.base_pos == Position.SECOND:
            return 'other'
        else:
            assert False

    def left_reverse(self):
        if self.base is None:
            return False
        elif self.reqd is None:
            return self.base.reversed
        elif self.base_pos == Position.FIRST:
            return self.base.reversed
        elif self.base_pos == Position.SECOND:
            return self.reqd.reversed
        else:
            assert False

    def right_edge(self):
        if self.base is None:
            return 'NULL'
        elif self.reqd is None:
            return 'NULL'
        elif self.base_pos == Position.FIRST:
            return 'other'
        elif self.base_pos == Position.SECOND:
            return 'base'
        else:
            assert False

    def right_reverse(self):
        if self.base is None:
            return False
        elif self.reqd is None:
            return False
        elif self.base_pos == Position.FIRST:
            return self.reqd.reversed
        elif self.base_pos == Position.SECOND:
            return self.base.reversed
        else:
            assert False

    def __str__(self):
        have = '-' if self.base is None else str(self.base)
        if self.reqd is None:
            need = ''
        elif self.base_pos == Position.FIRST:
            need = ' + (* %s)' % self.reqd
        elif self.base_pos == Position.SECOND:
            need = ' + (%s *)' % self.reqd
        else:
            assert False
        return have + need + ' => ' + self.result.as_result()

def emit_solver(grammar, code_out):
    """
    Generate the solver code for the input grammar. Accepts File-like objects
    for its output parameters.
    """
    pr = util.CodePrinter(code_out)
    norm_prods = util.OrderedMultiDict()
    rev_prods = util.OrderedMultiDict()
    symbols = grammar.symbols.clone()
    for s in grammar.prods:
        for seq in grammar.prods.get(s):
            for p in seq.split(s, symbols):
                norm_prods.append(p.result, p)
                for rp in p.get_rev_prods():
                    base_symbol = None if rp.base is None else rp.base.symbol
                    rev_prods.append(base_symbol, rp)

    pr.write('#include <assert.h>')
    pr.write('#include <list>')
    pr.write('#include <stdbool.h>')
    pr.write('#include <string.h>')
    pr.write('#include "solvergen.hpp"')
    pr.write('')

    pr.write('/* Reverse Productions:')
    pr.write('%s' % rev_prods)
    pr.write('*/')
    pr.write('')

    pr.write('PATH_LENGTH static_min_length(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    # XXX: Overapproximate
    for s in symbols:
        pr.write('case %s: return %s; /* %s */'
                 % (s.ref, 1 if s.is_terminal() else 0, s))
    pr.write('default: assert(false);')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('bool is_terminal(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in symbols:
        if s.is_terminal():
            pr.write('case %s: return true; /* %s */' % (s.ref, s))
    pr.write('default: return false;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('bool is_parametric(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in symbols:
        if s.parametric:
            pr.write('case %s: return true; /* %s */' % (s.ref, s))
    pr.write('default: return false;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('bool has_empty_prod(EDGE_KIND kind) {')
    empty_prod_symbols = [r.result for r in rev_prods.get(None)]
    pr.write('switch (kind) {')
    for s in set(empty_prod_symbols):
        pr.write('case %s: return true; /* %s */' % (s.ref, s))
    pr.write('default: return false;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('EDGE_KIND num_kinds() {')
    pr.write('return %s;' % symbols.size())
    pr.write('}')
    pr.write('')

    pr.write('EDGE_KIND symbol2kind(const char* symbol) {')
    for s in symbols:
        pr.write('if (strcmp(symbol, "%s") == 0) return %s;' % (s, s.ref))
    pr.write('assert(false);')
    pr.write('}')
    pr.write('')

    pr.write('const char* kind2symbol(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in symbols:
        pr.write('case %s: return "%s";' % (s.ref, s))
    pr.write('default: assert(false);')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('unsigned int num_paths_to_print(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in symbols:
        if s.num_paths() > 0:
            pr.write('case %s: return %s; /* %s */'
                     % (s.ref, s.num_paths(), s))
    pr.write('default: return 0;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('bool is_temporary(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in symbols:
        if s.is_temporary():
            pr.write('case %s: return true; /* %s */' % (s.ref, s))
    pr.write('default: return false;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('bool is_valid(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in symbols:
        pr.write('case %s: return true; /* %s */' % (s.ref, s))
    pr.write('default: return false;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('void main_loop(Edge* base) {')
    # TODO: Could cache base->index
    pr.write('switch (base->kind) {')
    for base_symbol in symbols:
        rps = rev_prods.get(base_symbol)
        if rps == []:
            # This symbol doesn't appear on the RHS of any production.
            continue
        pr.write('case %s: /* %s */' % (base_symbol.ref, base_symbol))
        for rp in rps:
            pr.write('/* %s */' % rp)
            res_src = rp.result_source()
            res_tgt = rp.result_target()
            res_kind = rp.result.ref
            res_idx = rp.result_index()
            l_edge = rp.left_edge()
            l_rev = util.to_c_bool(rp.left_reverse())
            r_edge = rp.right_edge()
            r_rev = util.to_c_bool(rp.right_reverse())
            if rp.reqd is not None:
                search_src = 'base->' + rp.search_source_endp()
                search_dir = rp.search_direction()
                search_idx = rp.search_index()
                reqd_kind = rp.reqd.symbol.ref
                pr.write('for (Edge* other : edges_%s(%s, %s%s)) {'
                         % (search_dir, search_src, reqd_kind,
                            '' if search_idx is None else (', %s' % search_idx)))
                loop_header = rp.loop_header()
                if loop_header is not None:
                    pr.write('for (%s) {' % loop_header)
                cond = rp.condition()
                if cond is not None:
                    pr.write('if (%s) {' % cond)
            asrt = rp.assertion()
            if asrt is not None:
                pr.write('assert(%s);' % asrt)
            pr.write('add_edge(%s, %s, %s, %s, %s, %s, %s, %s);'
                     % (res_src, res_tgt, res_kind, res_idx,
                        l_edge, l_rev, r_edge, r_rev))
            if rp.reqd is not None:
                if cond is not None:
                    pr.write('}')
                if loop_header is not None:
                    pr.write('}')
                pr.write('}')
        pr.write('break;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('std::vector<Derivation> all_derivations(Edge* e) {')
    pr.write('std::vector<Derivation> derivs;')
    pr.write('NODE_REF from = e->from;')
    pr.write('NODE_REF to = e->to;')
    pr.write('EDGE_KIND kind = e->kind;')
    pr.write('INDEX index = e->index;')
    pr.write('Edge* l;')
    pr.write('Edge* r;')
    # TODO: Paths for predicate witness edges are not used during path
    # reconstruction, so we don't have to consider that case when emitting
    # all_derivations.
    # When finding all derivations of an edge, we already know the edge has
    # passed any predicate checks, so we don't need to check those again.
    # Similarly, any rules producing predicate symbols are not allowed to
    # carry predicates themselves, so we don't need to emit predicate checks
    # for those either.
    pr.write('switch (kind) {')
    for e_symbol in symbols:
        pr.write('case %s: /* %s */' % (e_symbol.ref, e_symbol))
        if e_symbol.is_terminal():
            pr.write('break;')
            continue
        for p in norm_prods.get(e_symbol):
            pr.write('/* %s */' % p)
            if p.left is None:
                # Empty production
                pr.write('if (from == to) {')
                pr.write('derivs.push_back(Derivation());')
                pr.write('}')
                continue
            l_rev = util.to_c_bool(p.left.reversed)
            out_src = p.outer_search_source()
            out_tgt = p.outer_search_target()
            out_idx = p.outer_search_index()
            if out_tgt is None:
                out_dir = p.outer_search_direction()
                pr.write('for (Edge* l : edges_%s(%s, %s%s)) {'
                         % (out_dir, out_src, p.left.symbol.ref,
                            '' if out_idx is None else (', %s' % out_idx)))
            elif out_idx is None and p.left.symbol.parametric:
                pr.write('for (Edge* l : edges_between(%s, %s, %s)) {'
                         % (out_src, out_tgt, p.left.symbol.ref))
            else:
                pr.write(('if ((l = find_edge(%s, %s, %s, %s)) != NULL) {')
                         % (out_src, out_tgt, p.left.symbol.ref,
                            'INDEX_NONE' if out_idx is None else out_idx))
            out_cond = p.outer_condition()
            if out_cond is not None:
                pr.write('if (%s) {' % out_cond)
            if p.right is None:
                # single production
                pr.write('derivs.push_back(Derivation(l, %s));' % l_rev)
            else:
                # double production
                r_rev = util.to_c_bool(p.right.reversed)
                in_src = p.inner_search_source()
                in_tgt = p.inner_search_target()
                in_idx = p.inner_search_index()
                if in_idx is None and p.right.symbol.parametric:
                    pr.write('for (Edge* r : edges_between(%s, %s, %s)) {'
                             % (in_src, in_tgt, p.right.symbol.ref))
                else:
                    pr.write(('if ((r = find_edge(%s, %s, %s, %s)) != NULL) {')
                             % (in_src, in_tgt, p.right.symbol.ref,
                                'INDEX_NONE' if in_idx is None else in_idx))
                in_loop_header = p.inner_loop_header()
                if in_loop_header is not None:
                    pr.write('for (%s) {' % in_loop_header)
                in_cond = p.inner_condition()
                if in_cond is not None:
                    pr.write('if (%s) {' % in_cond)
                pr.write('derivs.push_back(Derivation(l, %s, r, %s));'
                             % (l_rev, r_rev))
                if in_cond is not None:
                    pr.write('}')
                if in_loop_header is not None:
                    pr.write('}')
                pr.write('}')
            if out_cond is not None:
                pr.write('}')
            pr.write('}')
        pr.write('break;')
    pr.write('default:')
    pr.write('assert(false);')
    pr.write('}')
    pr.write('return derivs;')
    pr.write('}')
    pr.write('')

# TODO: More user-friendly error output than assertion failure. Should use
# assertions only for the internal sanity checks, and do something different
# for input errors.
# TODO: More structured way to synthesize code: specialized C-code synthesis
# class, or put base program text in a large triple-quoted string and leave
# %s's for places to fill in.

prog_desc = 'Produce CFL-Reachability solver code for the input grammar.'
cfg_file_help = '.cfg file describing a context-free grammar'
out_dir_help = """print generated code this directory
if not specified, print to stdout"""

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=prog_desc,
                                     formatter_class=RawFormatter)
    parser.add_argument('cfg_file', help=cfg_file_help)
    parser.add_argument('out_dir', nargs='?', help=out_dir_help)
    args = parser.parse_args()

    grammar = Grammar(args.cfg_file)
    if args.out_dir is not None:
        cfg_name = os.path.splitext(os.path.basename(args.cfg_file))[0]
        code_out = os.path.join(args.out_dir, '%s.cpp' % cfg_name)
        with open(code_out, 'w') as f:
            emit_solver(grammar, f)
    else:
        emit_solver(grammar, sys.stdout)
