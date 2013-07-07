#!/usr/bin/env python
# -*- coding: utf-8 -*-

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
- Each non-terminal may have one or more @Production%s associated with it. A
  production is any sequence of @Symbol%s (terminals and/or non-terminals)
  separated by whitespace, e.g. `B c D`. Productions cannot span multiple
  lines. Empty Productions can be declared using a single `-` character.
- A set of @Production%s is associated with some non-terminal `A` using the
  notation `A :: ...`. The right-hand side of `::` must contain one or more
  productions (as defined above) separated by `|` marks. A series of
  productions may extend to subsequent lines, as long as each line starts a new
  production (the line must begin with `|`). A series of productions ends at
  the first blank line, or at the start of another `::` declaration (there is
  no dedicated end-of-production mark). The same non-terminal may be associated
  with any number of `::` declarations.
- Any instance of a @Symbol in a @Production be prefixed with `_` to signify
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
  specific @Symbol. Any @Production can only generate a specific @Index for an
  indexed @Symbol, i.e. `[*]` cannot appear at the LHS of a @Production (this
  corresponds to "unsafe" rules in Datalog).
- Special directives can be added to the grammar. These are declared using the
  syntax `.<directive> <params> ...`, and must be placed alone in a line.
  Directives break preceding @Production%s. The following directives are
  recognized:
  - `.paths <symbol> <num-paths>`: Instructs the generated solver to print out
    the selected number of witness paths for each of the Edge%s of the
    selected non-terminal @Symbol in the final graph.

Example of a valid grammar specification:

    S :: - | koo # this is a comment
       | A[j] _A[j]
    A[i] :: foo[i] S _S bar[*]
"""

class SymbolStore(util.FinalAttrs):
    """
    A container for @Symbol%s encountered in the input grammar.
    """

    def __init__(self):
        self._num_temps = 0
        self._num_symbols = 0
        self._symbol_list = []
        self._symbol_dict = {}

    def __make_symbol(self, name, parametric):
        symbol = Symbol(name, self._num_symbols, parametric)
        self._num_symbols += 1
        self._symbol_list.append(symbol)
        self._symbol_dict[name] = symbol
        return symbol

    def find_symbol(self, name):
        """
        Return the @Symbol with the specified @a name, if it exists, otherwise
        return @e None.
        """
        return self._symbol_dict.get(name)

    def get_symbol(self, name, parametric):
        """
        Return the @Symbol with the specified @a name and properties. Create
        the @Symbol if it doesn't already exist.
        """
        symbol = self.find_symbol(name)
        if symbol is not None:
            assert symbol.parametric == parametric, \
                "Symbol %s encountered both with and without " % name + \
                "index expression"
        else:
            assert SymbolStore.valid_user_name(name), \
                "Invalid user-defined symbol: %s" % name
            symbol = self.__make_symbol(name, parametric)
        return symbol

    def make_temporary(self, parametric):
        """
        Generate a unique non-terminal @Symbol, guaranteed to never clash with
        any user-defined @Symbol.
        """
        temp_name = '%' + str(self._num_temps)
        self._num_temps += 1
        return self.__make_symbol(temp_name, parametric)

    def __iter__(self):
        """
        Iterate over all @Symbol%s encountered so far, sorted by their @Kind.
        """
        for s in self._symbol_list:
            yield s

    def kind2symbol(self, kind):
        """
        Get the @Symbol corresponding to some @Kind.
        """
        return self._symbol_list[kind]

    def num_symbols(self):
        """
        Get the number of distinct @Symbol%s encountered so far.
        """
        return self._num_symbols

    @staticmethod
    def valid_user_name(name):
        """
        Check if @a name is a valid name for a user-defined @Symbol.
        """
        return re.match(r'^[a-zA-Z]\w*$', name) is not None

class Symbol(util.FinalAttrs):
    """
    A symbol in the input grammar.
    """

    def __init__(self, name, kind, parametric):
        """
        Do not call this function directly; use
        cfg_parser::SymbolStore::get_symbol() instead.
        """
        ## The @Symbol<!-- -->'s string in the input grammar.
        self.name = name
        ## A unique number associated with this @Symbol.
        self.kind = kind
        ## Whether Edge%s of this @Symbol are parameterized by @Indices.
        self.parametric = parametric
        ## The number of paths we wish the solver to print for each Edge of
        #  this @Symbol.
        self.num_paths = 0
        self.min_length = None
        self._mutables = ['num_paths', 'min_length']

    def is_terminal(self):
        """
        Check if this is a terminal symbol of the input grammar.

        Terminal @Symbol names start with lowercase characters.
        """
        return self.name[0] in string.ascii_lowercase

    def __key__(self):
        return self.kind

    def __eq__(self, other):
        return type(other) == Symbol and self.__key__() == other.__key__()

    def __hash__(self):
        return hash(self.__key__())

    def __str__(self):
        return self.name

    def as_lhs(self):
        return self.name + ('[i]' if self.parametric else '')

class Literal(util.FinalAttrs):
    """
    An instance of some @Symbol in the RHS of a @Production.

    May optionally contain a 'reverse' modifier and/or an @Index expression.
    """

    def __init__(self, symbol, indexed, reversed=False):
        ## The @Symbol represented by this @Literal.
        self.symbol = symbol
        ## Whether this @Literal contains an @Index expression.
        #
        #  Parametric @Symbol%s with a `[*]` index expression are considered to
        #  be non-indexed.
        #
        #  We don't have to store the actual index character, since all the
        #  indexed @Literal%s in the same @Production must use the same
        #  character anyway.
        self.indexed = indexed
        assert not indexed or symbol.parametric, \
            "Index modifier on non-parametric symbol %s" % symbol
        ## Whether this @Literal has a 'reverse' modifier.
        self.reversed = reversed

    def __str__(self):
        return (('_' if self.reversed else '') + str(self.symbol) +
                ('' if not self.symbol.parametric else
                 '[i]' if self.indexed else '[*]'))

class Production(util.FinalAttrs):
    """
    A production of the input grammar.
    """

    def __init__(self, result, used):
        Production._check_production(result, used)
        ## The @Symbol on the LHS of this @Production.
        self.result = result
        ## An ordered list of the @Literal%s on the RHS of this @Production.
        self.used = used

    def split(self, store):
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
        num_used = len(self.used)
        if num_used == 0:
            return [NormalProduction(self.result, None, None)]
        elif num_used == 1:
            return [NormalProduction(self.result, self.used[0], None)]
        elif num_used == 2:
            return [NormalProduction(self.result, self.used[0], self.used[1])]
        r_used = self.used[1:]
        num_temps = len(self.used) - 2
        temp_parametric = [True for _ in range(0, num_temps)]
        # The only intermediate symbols that need to be indexed are those
        # between the first and the last indexed literals in the original
        # production (the result of the production counts as the rightmost
        # literal for this purpose).
        if not self.result.parametric:
            for i in range(num_temps-1, -1, -1):
                if r_used[i+1].indexed:
                    break
                else:
                    temp_parametric[i] = False
        if not self.used[0].indexed:
            for i in range(0, num_temps):
                if r_used[i].indexed:
                    break
                else:
                    temp_parametric[i] = False
        temp_symbols = [store.make_temporary(p) for p in temp_parametric]
        temp_literals = [Literal(s, s.parametric) for s in temp_symbols]
        l_used = [self.used[0]] + temp_literals
        results = temp_symbols + [self.result]
        return [NormalProduction(r, ls, rs)
                for (r, ls, rs) in zip(results, l_used, r_used)]

    @staticmethod
    def _check_production(result, used):
        assert not result.is_terminal(), "Can't produce non-terminals"
        indexed_elements = ([result] if result.parametric else []) + \
            [s for s in used if s.indexed]
        assert indexed_elements == [] or len(indexed_elements) >= 2, \
            "At least two indexed elements required per production"

class NormalProduction(util.FinalAttrs):
    """
    A normalized @Production, with up to 2 @Literal%s on the RHS.
    """

    def __init__(self, result, left, right):
        assert not(left is None and right is not None)
        used = (([] if left is None else [left]) +
                ([] if right is None else [right]))
        Production._check_production(result, used)
        ## The @Symbol on the LHS of this @NormalProduction.
        self.result = result
        ## The first of up to 2 @Literal%s on the RHS. Is @e None for empty
        #  @NormalProduction%s.
        self.left = left
        ## The second of up to 2 @Literal%s on the RHS. Is @e None for empty
        #  or single @NormalProduction%s.
        self.right = right

    def update_result_min_length(self):
        newlen = ((0 if self.left is None else self.left.symbol.min_length) +
                  (0 if self.right is None else self.right.symbol.min_length))
        if newlen < self.result.min_length:
            self.result.min_length = newlen
            return True
        else:
            return False

    def get_rev_prods(self):
        """
        Get all the @ReverseProduction%s corresponding to this
        @NormalProduction.
        """
        if self.left is None:
            return [ReverseProduction(self.result, None)]
        elif self.right is None:
            return [ReverseProduction(self.result, self.left)]
        else:
            rev_l = ReverseProduction(self.result, self.left, self.right,
                                      True)
            rev_r = ReverseProduction(self.result, self.right, self.left,
                                      False)
            return [rev_l, rev_r]

    def outer_search_direction(self):
        assert self.left is not None
        if self.right is not None and self.left.reversed:
            return 'in'
        else:
            return 'out'

    def outer_search_source(self):
        assert self.left is not None
        if self.right is None and self.left.reversed:
            return 'e->to'
        else:
            return 'e->from'

    def outer_search_target(self):
        assert self.right is None
        return 'e->from' if self.left.reversed else 'e->to'

    def outer_condition(self):
        if self.left.indexed and self.result.parametric:
            return 'l->index == e->index'
        else:
            return None

    def inner_search_source(self):
        if self.right.reversed:
            return 'e->to'
        elif self.left.reversed:
            return 'l->from'
        else:
            return 'l->to'

    def inner_search_target(self):
        if not self.right.reversed:
            return 'e->to'
        elif self.left.reversed:
            return 'l->from'
        else:
            return 'l->to'

    def inner_condition(self):
        if not self.right.indexed:
            return None
        elif self.left.indexed:
            return 'l->index == r->index'
        else:
            return 'r->index == e->index'

    def __str__(self):
        rhs = ('-' if self.left is None
               else str(self.left) if self.right is None
               else str(self.left) + ' ' + str(self.right))
        return self.result.as_lhs() + ' :: ' + rhs

class ReverseProduction(util.FinalAttrs):
    """
    A @Production as seen from the point of view of some element on the RHS.

    A regular @Production (e.g. `S :: T R`) specifies what we can combine (a
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

    def __init__(self, result, base, reqd=None, comes_after=True):
        assert not(base is None and reqd is not None), \
            "Empty productions can't take a required literal"
        used = (([] if base is None else [base]) +
                ([] if reqd is None else [reqd]))
        Production._check_production(result, used)
        ## The @Symbol generated by this @ReverseProduction.
        self.result = result
        ## The @Literal we assume to be present.
        #
        #  Is @e None if this corresponds to an empty @Production.
        self.base = base
        ## The additional @Literal we need to complete the @Production.
        #
        #  Is @e None if this corresponds to a single-element @Production.
        self.reqd = reqd
        ## Whether the required @Literal needs to come after or before the one
        #  we already have.
        self.comes_after = comes_after

    def __check_need_to_search(self):
        assert self.base is not None, \
            "No need to search for empty productions"
        assert self.reqd is not None, \
            "No need to search for single-element productions"

    # TODO: The following methods return strings, which are tied to a
    #       particular variable and function naming. It might be more robust to
    #       return True/False and have the caller pick the strings to use.

    def search_endpoint(self):
        """
        On which endpoint of the Edge being processed we should search for an
        Edge that can complete this @ReverseProduction.

        In our running example, we need to search on the source Node.
        """
        self.__check_need_to_search()
        if self.base.reversed ^ self.comes_after:
            return 'to'
        else:
            return 'from'

    def search_direction(self):
        """
        On which set of Edge%s (incoming or outgoing) of the
        @link ReverseProduction::search_endpoint() search endpoint@endlink
        we should search for an Edge that can complete this @ReverseProduction.

        In our running example, we need to search within the incoming Edge%s.
        """
        self.__check_need_to_search()
        if self.reqd.reversed ^ self.comes_after:
            return 'out'
        else:
            return 'in'

    def result_source(self):
        """
        Where we should place the source Node of any Edge produced by this
        @ReverseProduction.

        In our running example, we would place it on the source Node of the
        'other' Edge (the one representing @Symbol `A`).
        """
        if self.reqd is None:
            edge = 'base'
            endpoint = 'to' if self.base.reversed else 'from'
        else:
            edge = 'base' if self.comes_after else 'other'
            if (not self.base.reversed and self.comes_after or
                not self.reqd.reversed and not self.comes_after):
                endpoint = 'from'
            else:
                endpoint = 'to'
        return edge + '->' + endpoint

    def result_target(self):
        """
        Where we should place the target Node of any Edge produced by this
        @ReverseProduction.

        In our running example, we would place it on the target Node of the
        'base' Edge (the one representing @Symbol `B`).
        """
        if self.reqd is None:
            edge = 'base'
            endpoint = 'from' if self.base.reversed else 'to'
        else:
            edge = 'other' if self.comes_after else 'base'
            if (not self.base.reversed and not self.comes_after or
                not self.reqd.reversed and self.comes_after):
                endpoint = 'to'
            else:
                endpoint = 'from'
        return edge + '->' + endpoint

    def result_index_source(self):
        """
        Whose @Index we should set on any Edge produced by this
        @ReverseProduction.

        In our running example, the @Indices on the combined Edge%s must match,
        so we can copy from either one of them. We arbitrarily choose to copy
        from the base Edge.
        """
        assert self.result.parametric
        if self.reqd is None:
            assert self.base.indexed
            return 'base'
        else:
            base_idx = self.base.indexed
            reqd_idx = self.reqd.indexed
            if not base_idx and reqd_idx:
                return 'other'
            elif base_idx and not reqd_idx:
                return 'base'
            elif base_idx and reqd_idx:
                return 'base'
            else:
                assert False

    def must_check_for_common_index(self):
        """
        Whether we need to add an additional @Index compatibility check for the
        two combined Edge%s.

        In our running example, we do.
        """
        return (self.base.indexed and self.reqd.indexed)

    def left_edge(self):
        if self.base is None:
            return 'NULL'
        elif self.reqd is None:
            return 'base'
        elif self.comes_after:
            return 'base'
        else:
            return 'other'

    def left_reverse(self):
        if self.base is None:
            return False
        elif self.reqd is None:
            return self.base.reversed
        elif self.comes_after:
            return self.base.reversed
        else:
            return self.reqd.reversed

    def right_edge(self):
        if self.base is None:
            return 'NULL'
        elif self.reqd is None:
            return 'NULL'
        elif self.comes_after:
            return 'other'
        else:
            return 'base'

    def right_reverse(self):
        if self.base is None:
            return False
        elif self.reqd is None:
            return False
        elif self.comes_after:
            return self.reqd.reversed
        else:
            return self.base.reversed

    def __str__(self):
        have = '-' if self.base is None else str(self.base)
        if self.reqd is None:
            need = ''
        elif self.comes_after:
            need = ' + (* %s)' % self.reqd
        else:
            need = ' + (%s *)' % self.reqd
        return have + need + ' => ' + self.result.as_lhs()

class Grammar(util.FinalAttrs):
    """
    A representation of the input grammar. Can be built incrementally by
    feeding it a text representation of a grammar line-by-line.
    """

    def __init__(self):
        self._lhs = None
        self._lhs_index_char = None
        ## All the @Symbol%s encountered so far, stored in a specialized
        #  @SymbolStore container.
        self.symbols = SymbolStore()
        ## All the @NormalProduction%s encountered so far, grouped by result
        #  @Symbol.
        self.prods = util.OrderedMultiDict()
        ## All the @ReverseProduction%s encountered so far, grouped by result
        #  @Symbol.
        self.rev_prods = util.OrderedMultiDict()

    def calc_min_lengths(self):
        for symbol in self.symbols:
            # TODO: Arbitrary value for "infinite length"
            symbol.min_length = 1 if symbol.is_terminal() else 10000
        fixpoint = False
        while not fixpoint:
            fixpoint = True
            for symbol in self.prods:
                for p in self.prods.get(symbol):
                    if p.update_result_min_length():
                        fixpoint = False

    def parse_line(self, line):
        """
        Parse the next line of the grammar specification.
        """
        line_wo_comment = (line.split('#'))[0]
        toks = line_wo_comment.split()
        if toks == [] or toks[0].startswith("."):
            # This is a special, non-production line, so we interrupt any
            # ongoing productions.
            self._lhs = None
            self._lhs_index_char = None
            if toks != []:
                self.__parse_directive(toks[0][1:], toks[1:])
            return
        # This line contains one or more production definitions.
        if toks[0] == '|':
            # This line continues a series of productions.
            assert self._lhs is not None, "| without preceding production"
            toks = toks[1:]
        elif len(toks) >= 2 and toks[1] == '::':
            # This line starts a new production.
            (self._lhs, self._lhs_index_char) = self.__parse_lhs(toks[0])
            toks = toks[2:]
        else:
            assert False, "Malformed production"
        while '|' in toks:
            split_pos = toks.index('|')
            self.__parse_production(toks[:split_pos])
            toks = toks[split_pos+1:]
        self.__parse_production(toks)

    def __parse_directive(self, directive, params):
        if directive == 'paths' and len(params) == 2:
            symbol = self.symbols.find_symbol(params[0])
            assert symbol is not None, \
                "Symbol %s not declared (yet)" % params[0]
            assert not symbol.is_terminal(), \
                "Paths can only be printed for non-terminals"
            symbol.num_paths = int(params[1])
        else:
            assert False, "Unknown directive: %s/%s" % (directive, len(params))

    def __parse_production(self, toks):
        assert toks != [], "Empty production not marked with '-'"
        used = []
        if toks != ['-']:
            prod_index = self._lhs_index_char
            for t in toks:
                (lit, i) = self.__parse_literal(t)
                used.append(lit)
                if i is not None:
                    assert prod_index is None or i == prod_index, \
                        "Production contains more than one distinct indices"
                    prod_index = i
        prod = Production(self._lhs, used)
        for p in prod.split(self.symbols):
            self.prods.append(p.result, p)
            for rp in p.get_rev_prods():
                base_symbol = None if rp.base is None else rp.base.symbol
                self.rev_prods.append(base_symbol, rp)

    def __parse_literal(self, str):
        matcher = re.match(r'^(_?)([a-zA-Z]\w*)(?:\[([a-zA-Z\*])\])?$', str)
        assert matcher is not None, "Malformed literal: %s" % str
        reversed = matcher.group(1) != ''
        index_char = matcher.group(3)
        symbol = self.symbols.get_symbol(matcher.group(2),
                                         index_char is not None)
        if index_char == '*':
            index_char = None
        indexed = index_char is not None
        return (Literal(symbol, indexed, reversed), index_char)

    def __parse_lhs(self, str):
        matcher = re.match(r'^([a-zA-Z]\w*)(?:\[([a-zA-Z])\])?$', str)
        assert matcher is not None, "Malformed production LHS: %s" % str
        index_char = matcher.group(2)
        symbol = self.symbols.get_symbol(matcher.group(1),
                                         index_char is not None)
        return (symbol, index_char)

def parse(fin, fout):
    """
    Read a grammar specification from @a fin and print out the corresponding
    solver code to @a fout.

    @param [in] fin A File-like object to read from.
    @param [out] fout A File-like object to write to.
    """
    grammar = Grammar()
    pr = util.CodePrinter(fout)

    pr.write('#include <assert.h>')
    pr.write('#include <list>')
    pr.write('#include <stdbool.h>')
    pr.write('#include <string.h>')
    pr.write('#include "solvergen.hpp"')
    pr.write('')

    pr.write('/* Original Grammar:')
    for line in fin:
        grammar.parse_line(line)
        pr.write(line, False)
    pr.write('*/')
    pr.write('')

    grammar.calc_min_lengths()
    pr.write('PATH_LENGTH static_min_length(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in grammar.symbols:
        pr.write('case %s: return %s;' % (s.kind, s.min_length))
    pr.write('default: assert(false);')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('/* Normalized Grammar:')
    pr.write('%s' % grammar.prods)
    pr.write('*/')
    pr.write('')

    pr.write('/* Reverse Productions:')
    pr.write('%s' % grammar.rev_prods)
    pr.write('*/')
    pr.write('')

    pr.write('bool is_terminal(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in grammar.symbols:
        if s.is_terminal():
            pr.write('case %s:' % s.kind)
    pr.write('return true;')
    pr.write('default:')
    pr.write('return false;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('bool is_parametric(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in grammar.symbols:
        if s.parametric:
            pr.write('case %s:' % s.kind)
    pr.write('return true;')
    pr.write('default:')
    pr.write('return false;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('bool has_empty_prod(EDGE_KIND kind) {')
    empty_prod_symbols = [r.result for r in grammar.rev_prods.get(None)]
    pr.write('switch (kind) {')
    if empty_prod_symbols != []:
        for s in empty_prod_symbols:
            pr.write('case %s: /* %s */' % (s.kind, s))
        pr.write('return true;')
    pr.write('default:')
    pr.write('return false;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('EDGE_KIND num_kinds() {')
    pr.write('return %s;' % grammar.symbols.num_symbols())
    pr.write('}')
    pr.write('')

    pr.write('EDGE_KIND symbol2kind(const char *symbol) {')
    for s in grammar.symbols:
        pr.write('if (strcmp(symbol, "%s") == 0) return %s;' % (s, s.kind))
    pr.write('assert(false);')
    pr.write('}')
    pr.write('')

    pr.write('const char *kind2symbol(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in grammar.symbols:
        pr.write('case %s: return "%s";' % (s.kind, s))
    pr.write('default: assert(false);')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('void main_loop(Edge *base) {')
    pr.write('Edge *other;')
    # TODO: Could cache base->index
    pr.write('switch (base->kind) {')
    for base_symbol in grammar.symbols:
        rev_prods = grammar.rev_prods.get(base_symbol)
        if rev_prods == []:
            # This symbol doesn't appear on the RHS of any production.
            continue
        pr.write('case %s: /* %s */' % (base_symbol.kind, base_symbol))
        for rp in rev_prods:
            pr.write('/* %s */' % rp)
            res_src = rp.result_source()
            res_tgt = rp.result_target()
            res_kind = rp.result.kind
            l_edge = rp.left_edge()
            l_rev = util.to_c_bool(rp.left_reverse())
            r_edge = rp.right_edge()
            r_rev = util.to_c_bool(rp.right_reverse())
            res_idx = ('%s->index' % rp.result_index_source()
                       if rp.result.parametric else 'INDEX_NONE')
            add_edge_stmt = ('add_edge(%s, %s, %s, %s, %s, %s, %s, %s);'
                             % (res_src, res_tgt, res_kind, res_idx,
                                l_edge, l_rev, r_edge, r_rev))
            if rp.reqd is None:
                pr.write(add_edge_stmt)
            else:
                search_node = 'base->' + rp.search_endpoint()
                search_dir = rp.search_direction()
                reqd_kind = rp.reqd.symbol.kind
                pr.write('other = get_%s_edges(%s, %s);'
                         % (search_dir, search_node, reqd_kind))
                pr.write('for (; other != NULL; other = next_%s_edge(other)) {'
                         % search_dir)
                if rp.must_check_for_common_index():
                    pr.write('if (base->index == other->index) {')
                    pr.write(add_edge_stmt)
                    pr.write('}')
                else:
                    pr.write(add_edge_stmt)
                pr.write('}')
        pr.write('break;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('std::list<Derivation> all_derivations(Edge *e) {')
    pr.write('Edge *l, *r;')
    pr.write('std::list<Derivation> derivs;')
    pr.write('switch (e->kind) {')
    for e_symbol in grammar.prods:
        pr.write('case %s: /* %s */' % (e_symbol.kind, e_symbol))
        for p in grammar.prods.get(e_symbol):
            pr.write('/* %s */' % p)
            if p.left is None:
                # Empty production
                pr.write('if (e->from == e->to) {')
                pr.write('derivs.push_back(derivation_empty());')
                pr.write('}')
                continue
            is_single = p.right is None
            out_dir = p.outer_search_direction()
            out_src = p.outer_search_source()
            pr.write('l = get_%s_edges%s(%s%s, %s);'
                     % (out_dir, '_to_target' if is_single else '', out_src,
                        (', ' + p.outer_search_target()) if is_single else '',
                        p.left.symbol.kind))
            pr.write('for (; l != NULL; l = next_%s_edge(l)) {' % out_dir)
            out_cond = p.outer_condition()
            if out_cond is not None:
                pr.write('if (%s) {' % out_cond)
            if is_single:
                pr.write('derivs.push_back(derivation_single(l, %s));'
                         % util.to_c_bool(p.left.reversed))
            else:
                pr.write('r = get_out_edges_to_target(%s, %s, %s);'
                         % (p.inner_search_source(), p.inner_search_target(),
                            p.right.symbol.kind))
                pr.write('for (; r != NULL; r = next_out_edge(r)) {')
                in_cond = p.inner_condition()
                if in_cond is not None:
                    pr.write('if (%s) {' % in_cond)
                pr.write('derivs.push_back(derivation_double(l, %s, r, %s));'
                         % (util.to_c_bool(p.left.reversed),
                            util.to_c_bool(p.right.reversed)))
                if in_cond is not None:
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

    pr.write('unsigned int num_paths_to_print(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in grammar.symbols:
        if s.num_paths > 0:
            pr.write('case %s: return %s;' % (s.kind, s.num_paths))
    pr.write('default: return 0;')
    pr.write('}')
    pr.write('}')

# TODO: More user-friendly error output than assertion failure
# TODO: More structured way to synthesize code: specialized C-code synthesis
#       class, or put base program text in a large triple-quoted string and
#       leave %s's for places to fill in.

## Help message describing the calling convention for this script.
usage_string = """Usage: %s <input-file> [<output-dir>]
Produce CFL-Reachability solver code for a Context-Free Grammar.
<input-file> must contain a grammar specification (see the main project docs
for details), and have a .cfg extension.
Output is printed to a file inside <output-dir> with the same name as
<input-file>, but with the .cfg extension stripped.
If no output directory is given, print generated code to stdout.
"""

def _main():
    if (len(sys.argv) < 2 or sys.argv[1] == '-h' or sys.argv[1] == '--help' or
        os.path.splitext(sys.argv[1])[1] != '.cfg'):
        script_name = os.path.basename(__file__)
        sys.stderr.write(usage_string % script_name)
        exit(1)
    with open(sys.argv[1], 'r') as fin:
        if len(sys.argv) >= 3:
            base_outfile = os.path.basename(os.path.splitext(sys.argv[1])[0])
            outfile = os.path.join(sys.argv[2], base_outfile + '.cpp')
            with open(outfile, 'w') as fout:
                parse(fin, fout)
        else:
            parse(fin, sys.stdout)

if __name__ == '__main__':
    _main()
