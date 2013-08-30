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

class Symbol(util.FinalAttrs):
    """
    A symbol in the input grammar.
    """

    def __init__(self, name, kind, parametric):
        """
        Objects of this class are managed by the cfg_parser::SymbolStore class.
        Do not call this constructor directly, use
        cfg_parser::SymbolStore::get() instead.
        """
        ## The @Symbol<!-- -->'s string in the input grammar.
        self.name = name
        ## A unique number assigned to this @Symbol by its manager class.
        self.kind = kind
        ## Whether Edge%s of this @Symbol are parameterized by @Indices.
        self.parametric = parametric
        self.min_length = None
        self._predicate = False
        self._num_paths = 0
        self._mutables = ['min_length']

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

    def is_predicate(self):
        """
        Check if this @Symbol is used as a predicate on a production.
        """
        return self._predicate

    def make_predicate(self):
        """
        Declare that this @Symbol is used as a predicate on a production.
        """
        assert not self.is_terminal(), \
            "Terminals are implicitly usable as predicates"
        assert self.num_paths() == 0, \
            "Can't output paths for predicate symbols"
        assert not self.is_temporary(), \
            "Intermediate symbols can't be used as predicates"
        self._predicate = True

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
        assert not self.is_predicate(), \
            "Can't output paths for predicate symbols"
        self._num_paths = num_paths

    def __key__(self):
        return (self.name, self.parametric)

    def __eq__(self, other):
        return type(other) == Symbol and self.__key__() == other.__key__()

    def __hash__(self):
        return hash(self.__key__())

    def __str__(self):
        return self.name

class Result(util.FinalAttrs):
    """
    An instance of some @Symbol on the LHS of a production.
    """

    def __init__(self, symbol, index):
        assert not symbol.is_terminal(), "Can't produce non-terminals"
        assert (index is None) ^ symbol.parametric, \
            "Indexing mismatch on LHS of production for %s" % symbol
        ## The @Symbol represented by this @Result.
        self.symbol = symbol
        ## The @Index carried by this @Result, as a 0-based position in the
        #  containing production's @Relation. Is @e None for non-parametric
        #  @Symbol%s, and @e 0 for the @Result of indexed productions that
        #  don't carry a @Relation.
        self.index = index

    def indexed(self):
        """
        Check whether this @Result carries a non-wildcard @Index expression.
        """
        return self.index is not None

    def __str__(self):
        idx_str = ('' if not self.indexed() else
                   ('[%s]' % util.idx2char(self.index)))
        return str(self.symbol) + idx_str

class Literal(util.FinalAttrs):
    """
    An instance of some @Symbol on the RHS of a production.

    May optionally contain a 'reverse' modifier and/or an @Index expression.
    """

    def __init__(self, symbol, index, reversed=False):
        assert index is None or symbol.parametric, \
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
        self.index = index
        ## Whether this @Literal has a 'reverse' modifier.
        self.reversed = reversed

    def indexed(self):
        """
        Check whether this @Literal carries a non-wildcard @Index expression.
        """
        return self.index is not None

    def __str__(self):
        idx_str = ('' if not self.symbol.parametric else
                   '[*]' if not self.indexed() else
                   ('[%s]' % util.idx2char(self.index)))
        return ('_' if self.reversed else '') + str(self.symbol) + idx_str

class RelationStore(util.UniqueNameMap):
    """
    A container for all @Relation%s encountered in the input grammar.
    """

    def managed_class(self):
        return Relation

class Relation(util.FinalAttrs):
    """
    A relation describing the combinations of @Indices that are allowed to
    trigger a production.
    """

    def __init__(self, name, ref, arity):
        """
        Objects of this class are managed by the cfg_parser::RelationStore
        class. Do not call this constructor directly, use
        cfg_parser::RelationStore::get() instead.
        """
        assert arity == 3, "Only 3-parameter relations allowed"
        ## The @Relation<!-- -->'s string in the input grammar.
        self.name = name
        ## A unique number assigned to this @Relation by its manager class.
        self.ref = ref
        ## The @Relation<!-- -->'s arity (number of parameters).
        self.arity = arity

    def __key__(self):
        return (self.name, self.arity)

    def __eq__(self, other):
        return type(other) == Relation and self.__key__() == other.__key__()

    def __str__(self):
        params_str = ','.join([util.idx2char(i) for i in range(0, self.arity)])
        return '%s(%s)' % (self.name, params_str)

class Production(util.FinalAttrs):
    """
    A production of the input @Grammar.
    """

    def __init__(self, result, used, relation, predicate):
        Production._check_production(result, used, relation, predicate)
        ## The @Result on the LHS of this @Production.
        self.result = result
        ## An ordered list of the @Literal%s on the RHS of this production.
        self.used = used
        ## The @Relation describing how the @Indices on the RHS of this
        #  production are to be matched. Is @e None if there is no such
        #  @Relation.
        self.relation = relation
        ## The @Symbol under which the endpoints of any Edge generated by this
        #  @Production must be connected. Is @e None if there is no such
        #  predicate.
        self.predicate = predicate

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
            return [NormalProduction(self.result, None, None,
                                     self.relation, self.predicate)]
        elif num_used == 1:
            return [NormalProduction(self.result, self.used[0], None,
                                     self.relation, self.predicate)]
        elif num_used == 2:
            return [NormalProduction(self.result, self.used[0], self.used[1],
                                     self.relation, self.predicate)]
        # TODO: Longer productions cannot (currently) carry relations, so we
        # can ignore this complication in the splitting algorithm below.
        assert self.relation is None
        r_used = self.used[1:]
        num_temps = len(self.used) - 2
        temp_parametric = [True for _ in range(0, num_temps)]
        # The only intermediate symbols that need to be indexed are those
        # between the first and the last indexed literals in the original
        # production (the result of the production counts as the rightmost
        # literal for this purpose).
        if not self.result.indexed():
            for i in range(num_temps-1, -1, -1):
                if r_used[i+1].indexed():
                    break
                else:
                    temp_parametric[i] = False
        if not self.used[0].indexed():
            for i in range(0, num_temps):
                if r_used[i].indexed():
                    break
                else:
                    temp_parametric[i] = False
        temp_symbols = [store.make_temporary(p) for p in temp_parametric]
        temp_results = [Result(s, 0 if s.parametric else None)
                        for s in temp_symbols]
        temp_literals = [Literal(s, 0 if s.parametric else None)
                         for s in temp_symbols]
        l_used = [self.used[0]] + temp_literals
        results = temp_results + [self.result]
        # The predicate only applies to the NormalProduction that produces the
        # final result, i.e. the last one.
        preds = [None for t in temp_symbols] + [self.predicate]
        return [NormalProduction(r, ls, rs, self.relation, p)
                for (r, ls, rs, p) in zip(results, l_used, r_used, preds)]

    @staticmethod
    def _check_production(result, used, relation, predicate):
        """
        Test that a production with the given properties is valid.
        """
        indices = [e.index for e in [result] + used if e.index is not None]
        assert indices == [] or len(indices) >= 2, \
            "At least two indexed elements required per production"
        if relation is None:
            assert all([i == 0 for i in indices]), \
                "Non-zero index on non-relation carrying production"
        else:
            assert len(used) == 2, \
                "Only binary productions are allowed to carry relations"
            assert result.indexed(), \
                "The result of a relation-carrying production must be indexed"
            assert sorted(indices) == range(0, relation.arity), \
                "Duplicate index, index out-of-bounds or missing index"
        if predicate is not None:
            assert used != [], "Empty productions can't carry predicates"
            assert not(predicate.parametric and not result.indexed()), \
                "Indexing mismatch between predicate and result symbol"

class NormalProduction(util.FinalAttrs):
    """
    A normalized @Production, with up to 2 @Literal%s on the RHS.
    """

    def __init__(self, result, left, right, relation, predicate):
        assert not(left is None and right is not None)
        used = (([] if left is None else [left]) +
                ([] if right is None else [right]))
        Production._check_production(result, used, relation, predicate)
        ## The @Result on the LHS of this @NormalProduction.
        self.result = result
        ## The first of up to 2 @Literal%s on the RHS. Is @e None for empty
        #  @NormalProduction%s.
        self.left = left
        ## The second of up to 2 @Literal%s on the RHS. Is @e None for empty
        #  or single @NormalProduction%s.
        self.right = right
        ## The @Relation describing how the @Indices on the RHS of this
        #  production are to be matched. Is @e None if there is no such
        #  @Relation.
        self.relation = relation
        ## The @Symbol under which the endpoints of any Edge generated by this
        #  @NormalProduction must be connected. Is @e None if there is no such
        #  predicate.
        self.predicate = predicate

    def _update_result_min_length(self):
        """
        Propagate the minimum length estimates from the @Symbol%s on the RHS
        to the @Symbol on the LHS. To be used by
        cfg_parser::Grammar::calc_min_lengths().

        @return Whether this call updated the estimate for the result @Symbol.
        """
        left_len = 0 if self.left is None else self.left.symbol.min_length
        right_len = 0 if self.right is None else self.right.symbol.min_length
        newlen = left_len + right_len
        if newlen < self.result.symbol.min_length:
            self.result.symbol.min_length = newlen
            return True
        else:
            return False

    def get_rev_prods(self):
        """
        Get all the @ReverseProduction%s corresponding to this
        @NormalProduction.
        """
        if self.right is None:
            return [ReverseProduction(self.result, self.left, None,
                                      Position.FIRST, self.relation,
                                      self.predicate)]
        else:
            return [ReverseProduction(self.result, self.left, self.right,
                                      Position.FIRST, self.relation,
                                      self.predicate),
                    ReverseProduction(self.result, self.right, self.left,
                                      Position.SECOND, self.relation,
                                      self.predicate)]

    def only_terminals(self):
        return ((self.left is None or self.left.symbol.is_terminal()) and
                (self.right is None or self.right.symbol.is_terminal()))

    def outer_search_direction(self):
        assert self.left is not None
        if self.right is None:
            return 'out'
        return 'in' if self.left.reversed else 'out'

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

    def outer_condition(self):
        assert self.left is not None
        if self.relation is not None:
            if self.relation.ref != 0:
                return None
            indices = (self.result.index, self.left.index, self.right.index)
            if indices == (2,0,1):
                return 'l->index == (index >> 14)'
            elif indices == (2,1,0):
                return 'l->index == (index & 0x3fff)'
            elif indices == (0,2,1):
                return 'index == (l->index >> 14)'
            elif indices == (1,2,0):
                return 'index == (l->index & 0x3fff)'
            elif indices == (0,1,2):
                return None
            elif indices == (1,0,2):
                return None
            else:
                assert False
        if self.left.indexed() and self.result.indexed():
            return 'l->index == index'
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

    def inner_loop_header(self):
        if self.relation is None:
            return None
        assert self.left is not None and self.left.indexed()
        assert self.right is not None and self.right.indexed()
        assert self.result.indexed()
        if self.relation.ref == 0:
            return None
        # We need to feed the selection parameters in column order.
        if self.left.index < self.right.index:
            (edge_1, edge_2) = ('l', 'r')
        else:
            (edge_1, edge_2) = ('r', 'l')
        return ('INDEX i : rel_select(%s, %s, %s->index, %s->index)'
                % (self.relation.ref, self.result.index, edge_1, edge_2))

    def inner_condition(self):
        assert self.left is not None and self.right is not None
        if self.relation is not None:
            if self.relation.ref != 0:
                return 'i == index'
            indices = (self.result.index, self.left.index, self.right.index)
            if indices == (2,0,1):
                return 'r->index == (index & 0x3fff)'
            elif indices == (2,1,0):
                return 'r->index == (index >> 14)'
            elif indices == (0,2,1):
                return 'r->index == (l->index & 0x3fff)'
            elif indices == (1,2,0):
                return 'r->index == (l->index >> 14)'
            elif indices == (0,1,2):
                er_check = 'index == (r->index >> 14)'
                lr_check = 'l->index == (r->index & 0x3fff)'
                return er_check + ' && ' + lr_check
            elif indices == (1,0,2):
                er_check = 'index == (r->index & 0x3fff)'
                lr_check = 'l->index == (r->index >> 14)'
                return er_check + ' && ' + lr_check
            else:
                assert False
        if not self.right.indexed():
            return None
        elif self.left.indexed():
            return 'l->index == r->index'
        assert self.result.indexed()
        return 'r->index == index'

    def __str__(self):
        rhs = ('-' if self.left is None
               else str(self.left) if self.right is None
               else str(self.left) + ' ' + str(self.right))
        rel = '' if self.relation is None else (' .%s' % self.relation)
        if self.predicate is None:
            pred = ''
        elif not self.predicate.parametric:
            pred = ' //%s' % self.predicate
        else:
            pred = ' //%s[%s]' % (self.predicate,
                                  util.idx2char(self.result.index))
        return str(self.result) + ' :: ' + rhs + rel + pred

class Position(util.FinalAttrs):
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

class ReverseProduction(util.FinalAttrs):
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

    def __init__(self, result, base, reqd, base_pos, relation, predicate):
        assert not(base is None and reqd is not None), \
            "Empty productions can't take a required literal"
        assert not(reqd is None and base_pos != Position.FIRST)
        assert Position.valid_position(base_pos)
        used = (([] if base is None else [base]) +
                ([] if reqd is None else [reqd]))
        Production._check_production(result, used, relation, predicate)
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
        ## The @Relation describing how the @Indices on the combined Edge%s
        #  are to be matched. Is @e None if there is no such @Relation.
        self.relation = relation
        ## The @Symbol under which the endpoints of any Edge generated by this
        #  @ReverseProduction must be connected. Is @e None if there is no such
        #  predicate.
        self.predicate = predicate

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
            return 'in' if self.reqd.reversed else 'out'
        elif self.base_pos == Position.SECOND:
            return 'out' if self.reqd.reversed else 'in'
        else:
            assert False

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
        if (self.relation is not None and self.relation.ref == 0 and
            self.result.index == 2):
            indices = (self.base.index, self.reqd.index)
            if indices == (0,1):
                return 'base->index < 0x40000 && other->index < 0x4000'
            elif indices == (1,0):
                return 'other->index < 0x40000 && base->index < 0x4000'
            else:
                assert False
        return None

    def result_index(self):
        """
        What @Index we should set on any Edge produced by this
        @ReverseProduction.

        In our running example, the @Indices on the combined Edge%s must match,
        so we can copy from either one of them. We arbitrarily choose to copy
        from the base Edge.
        """
        if not self.result.indexed():
            return 'INDEX_NONE'
        assert self.base is not None
        if self.relation is not None:
            assert self.base.indexed()
            assert self.reqd is not None and self.reqd.indexed()
            if self.relation.ref != 0:
                return 'i'
            indices = (self.result.index, self.base.index, self.reqd.index)
            if indices == (2,0,1):
                return '(base->index << 14) | other->index'
            elif indices == (2,1,0):
                return '(other->index << 14) | base->index'
            elif indices == (0,2,1):
                return 'base->index >> 14'
            elif indices == (1,2,0):
                return 'base->index & 0x3fff'
            elif indices == (0,1,2):
                return 'other->index >> 14'
            elif indices == (1,0,2):
                return 'other->index & 0x3fff'
            else:
                assert False
        if self.reqd is None:
            assert self.base.indexed()
            return 'base->index'
        elif self.base.indexed():
            return 'base->index'
        else:
            assert self.reqd.indexed()
            return 'other->index'

    def loop_header(self):
        if self.relation is None:
            return None
        assert self.base is not None and self.base.indexed()
        assert self.reqd is not None and self.reqd.indexed()
        assert self.result.indexed()
        if self.relation.ref == 0:
            return None
        # We need to feed the selection parameters in column order.
        if self.base.index < self.reqd.index:
            (edge_1, edge_2) = ('base', 'other')
        else:
            (edge_1, edge_2) = ('other', 'base')
        return ('INDEX i : rel_select(%s, %s, %s->index, %s->index)'
                % (self.relation.ref, self.result.index, edge_1, edge_2))

    def condition(self):
        """
        Any additional @Index compatibility check we need to perform before we
        record the combination of the Edge%s.

        In our running example, we need to check that they two Edge%s have the
        same @Index.
        """
        if self.relation is not None:
            if self.relation.ref != 0:
                return None
            indices = (self.result.index, self.base.index, self.reqd.index)
            if indices == (2,0,1):
                return None
            elif indices == (2,1,0):
                return None
            elif indices == (0,2,1):
                return 'other->index == (base->index & 0x3fff)'
            elif indices == (1,2,0):
                return 'other->index == (base->index >> 14)'
            elif indices == (0,1,2):
                return 'base->index == (other->index & 0x3fff)'
            elif indices == (1,0,2):
                return 'base->index == (other->index >> 14)'
            else:
                assert False
        if self.base.indexed() and self.reqd.indexed():
            return 'base->index == other->index'
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
        rel = '' if self.relation is None else (' && %s' % self.relation)
        if self.predicate is None:
            pred = ''
        elif not self.predicate.parametric:
            pred = ' // %s' % self.predicate
        else:
            pred = ' // %s[%s]' % (self.predicate,
                                   util.idx2char(self.result.index))
        return have + need + rel + pred + ' => ' + str(self.result)

class Grammar(util.FinalAttrs):
    """
    A representation of the input grammar. Can be built incrementally by
    feeding it a text representation of a grammar line-by-line.
    """

    def __init__(self):
        self._lhs_symbol = None
        self._lhs_idx_char = None
        self._relation = None
        self._param_chars = None
        self._pred_symbol = None
        self._pred_idx_char = None
        ## All the @Symbol%s encountered so far, stored in a specialized
        #  @SymbolStore container.
        self.symbols = SymbolStore()
        ## All the @Relation%s encountered so far, stored in a specialized
        #  @RelationStore container.
        self.rels = RelationStore()
        # HACK: Using a special 0-th relation for index concatenation.
        # HACK: Currently cramming the two indices into one, using 18 bits for
        # the first and 14 for the second; this may overflow the index. We've
        # added assertion checks, to make sure we're notified when that
        # happens.
        # TODO: Implement correctly and document.
        self.rels.get('concat', 3)
        ## All the @NormalProduction%s encountered so far, grouped by result
        #  @Symbol.
        self.prods = util.OrderedMultiDict()
        ## All the @ReverseProduction%s encountered so far, grouped by base
        #  @Symbol.
        self.rev_prods = util.OrderedMultiDict()

    def finalize(self):
        """
        Run final sanity checks and calculations, which require all productions
        to be present.
        """
        for s in self.symbols:
            if not s.is_terminal() and self.prods.get(s) == []:
                assert False, "Non-terminal %s can never be produced" % s
            if s.is_predicate():
                assert self.rev_prods.get(s) == [], \
                    "Predicate %s used on the RHS of a production" % s
                for p in self.prods.get(s):
                    assert p.only_terminals(), \
                        "Predicate %s constructed from non-terminals" % s
                    assert p.predicate is None, \
                        "Predicates not allowed on predicate-generating rules"
        self._calc_min_lengths()
        # TODO: Also disable parse_line().

    def _calc_min_lengths(self):
        for symbol in self.symbols:
            # TODO: Arbitrary value for "infinite length"
            symbol.min_length = 1 if symbol.is_terminal() else 10000
        fixpoint = False
        while not fixpoint:
            fixpoint = True
            for symbol in self.prods:
                for p in self.prods.get(symbol):
                    if p._update_result_min_length():
                        fixpoint = False

    def parse_line(self, line):
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
        if toks != [] and self._parse_predicate(toks[-1]):
            toks = toks[:-1]
        if toks != [] and self._parse_relation(toks[-1]):
            toks = toks[:-1]
        assert toks != [], "Empty production not marked with '-'"
        used = []
        all_chars = [self._lhs_idx_char]
        if toks != ['-']:
            for t in toks:
                (lit, i) = self._parse_literal(t)
                used.append(lit)
                all_chars.append(i)
        if self._relation is None:
            assert util.all_same([i for i in all_chars if i is not None]), \
                "Relation-less production with multiple distinct indices"
        result = Result(self._lhs_symbol, self._char2idx(self._lhs_idx_char))
        full_prod = Production(result, used, self._relation, self._pred_symbol)
        prods = full_prod.split(self.symbols)
        for p in prods:
            self.prods.append(p.result.symbol, p)
            for rp in p.get_rev_prods():
                base_symbol = None if rp.base is None else rp.base.symbol
                self.rev_prods.append(base_symbol, rp)
        self._relation = None
        self._param_chars = None
        self._pred_symbol = None
        self._pred_idx_char = None

    def _parse_predicate(self, str):
        if not str.startswith('//'):
            return False
        (self._pred_symbol, self._pred_idx_char) = self._parse_symbol(str[2:])
        if not self._pred_symbol.is_terminal():
            self._pred_symbol.make_predicate()
        if self._pred_idx_char is not None:
            assert self._pred_idx_char == self._lhs_idx_char, \
                "Index mismatch between predicate and result symbol"
        return True

    def _parse_literal(self, str):
        matcher = re.match(r'^(_?)([a-zA-Z]\w*)(?:\[([a-zA-Z\*])\])?$', str)
        assert matcher is not None, "Malformed literal: %s" % str
        reversed = matcher.group(1) != ''
        idx_char = matcher.group(3)
        symbol = self.symbols.get(matcher.group(2), idx_char is not None)
        if idx_char == '*':
            idx_char = None
        index = self._char2idx(idx_char)
        return (Literal(symbol, index, reversed), idx_char)

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

    def _parse_relation(self, str):
        matcher = re.match(r'^\.(\w+)\(((?:[a-zA-Z](?:,[a-zA-Z])*)?)\)$', str)
        if matcher is None:
            return False
        param_chars = matcher.group(2).split(',')
        assert util.all_different(param_chars), \
            "Duplicate parameter on relation declaration: %s" % str
        assert self._lhs_idx_char is not None, \
            "Relation-carrying production with unindexed LHS"
        assert self._lhs_idx_char in param_chars, \
            "Index on LHS not present in relation parameters"
        self._relation = self.rels.get(matcher.group(1), len(param_chars))
        self._param_chars = param_chars
        return True

    def _char2idx(self, idx_char):
        return (None if idx_char is None else
                0 if self._relation is None else
                # This will throw an error if idx_char is not present as a
                # parameter of the relation.
                self._param_chars.index(idx_char))

def parse(grammar_in, code_out, terms_out, rels_out):
    """
    Read a grammar specification and generate the corresponding solver code.
    Accepts File-like objects for its input and output parameters.
    """
    grammar = Grammar()
    pr = util.CodePrinter(code_out)

    pr.write('#include <assert.h>')
    pr.write('#include <list>')
    pr.write('#include <stdbool.h>')
    pr.write('#include <string.h>')
    pr.write('#include "solvergen.hpp"')
    pr.write('')

    pr.write('/* Original Grammar:')
    for line in grammar_in:
        grammar.parse_line(line)
        pr.write(line, False)
    grammar.finalize()
    pr.write('*/')
    pr.write('')

    pr.write('/* Normalized Grammar:')
    pr.write('%s' % grammar.prods)
    pr.write('*/')
    pr.write('')

    pr.write('/* Reverse Productions:')
    pr.write('%s' % grammar.rev_prods)
    pr.write('*/')
    pr.write('')

    pr.write('PATH_LENGTH static_min_length(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in grammar.symbols:
        pr.write('case %s: return %s; /* %s */' % (s.kind, s.min_length, s))
    pr.write('default: assert(false);')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('bool is_terminal(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in grammar.symbols:
        if s.is_terminal():
            pr.write('case %s: return true; /* %s */' % (s.kind, s))
    pr.write('default: return false;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('bool is_parametric(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in grammar.symbols:
        if s.parametric:
            pr.write('case %s: return true; /* %s */' % (s.kind, s))
    pr.write('default: return false;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('bool has_empty_prod(EDGE_KIND kind) {')
    empty_prod_symbols = [r.result.symbol for r in grammar.rev_prods.get(None)]
    pr.write('switch (kind) {')
    for s in set(empty_prod_symbols):
        pr.write('case %s: return true; /* %s */' % (s.kind, s))
    pr.write('default: return false;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('EDGE_KIND num_kinds() {')
    pr.write('return %s;' % grammar.symbols.size())
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

    pr.write('unsigned int num_paths_to_print(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in grammar.symbols:
        if s.num_paths() > 0:
            pr.write('case %s: return %s; /* %s */'
                     % (s.kind, s.num_paths(), s))
    pr.write('default: return 0;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('bool is_predicate(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in grammar.symbols:
        if s.is_predicate():
            pr.write('case %s: return true; /* %s */' % (s.kind, s))
    pr.write('default: return false;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('bool is_temporary(EDGE_KIND kind) {')
    pr.write('switch (kind) {')
    for s in grammar.symbols:
        if s.is_temporary():
            pr.write('case %s: return true; /* %s */' % (s.kind, s))
    pr.write('default: return false;')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('RELATION_REF num_rels() {')
    pr.write('return %s;' % grammar.rels.size())
    pr.write('}')
    pr.write('')

    pr.write('RELATION_REF rel2ref(const char *rel) {')
    for r in grammar.rels:
        pr.write('if (strcmp(rel, "%s") == 0) return %s;' % (r.name, r.ref))
    pr.write('assert(false);')
    pr.write('}')
    pr.write('')

    pr.write('const char *ref2rel(RELATION_REF ref) {')
    pr.write('switch (ref) {')
    for r in grammar.rels:
        pr.write('case %s: return "%s";' % (r.ref, r.name))
    pr.write('default: assert(false);')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('ARITY rel_arity(RELATION_REF ref) {')
    pr.write('switch (ref) {')
    for r in grammar.rels:
        pr.write('case %s: return %s; /* %s */' % (r.ref, r.arity, r.name))
    pr.write('default: assert(false);')
    pr.write('}')
    pr.write('}')
    pr.write('')

    pr.write('void main_loop(Edge *base) {')
    pr.write('Edge *other;')
    # TODO: Local iterator variables may go unused: use per-case variables,
    # enclosed in {} blocks.
    pr.write('OutEdgeIterator *out_iter;')
    pr.write('InEdgeIterator *in_iter;')
    # TODO: Could cache base->index
    pr.write('switch (base->kind) {')
    for base_symbol in grammar.symbols:
        rev_prods = grammar.rev_prods.get(base_symbol)
        if rev_prods == []:
            # This symbol doesn't appear on the RHS of any production.
            continue
        pr.write('case %s: /* %s */' % (base_symbol.kind, base_symbol))
        if base_symbol.is_predicate():
            # Edges for predicate symbols should never get produced, and thus
            # never enter the worklist.
            pr.write('assert(false);')
            continue
        for rp in rev_prods:
            if rp.result.symbol.is_predicate():
                # Skip productions that generate predicate symbols.
                continue
            pr.write('/* %s */' % rp)
            res_src = rp.result_source()
            res_tgt = rp.result_target()
            res_kind = rp.result.symbol.kind
            l_edge = rp.left_edge()
            l_rev = util.to_c_bool(rp.left_reverse())
            r_edge = rp.right_edge()
            r_rev = util.to_c_bool(rp.right_reverse())
            res_idx = rp.result_index()
            if rp.reqd is not None:
                search_src = 'base->' + rp.search_source_endp()
                search_dir = rp.search_direction()
                reqd_kind = rp.reqd.symbol.kind
                pr.write('%s_iter = get_%s_edge_iterator(%s, %s);'
                         % (search_dir, search_dir, search_src, reqd_kind))
                pr.write('while ((other = next_%s_edge(%s_iter)) != NULL) {'
                         % (search_dir, search_dir))
                loop_header = rp.loop_header()
                if loop_header is not None:
                    pr.write('for (%s) {' % loop_header)
                cond = rp.condition()
                if cond is not None:
                    pr.write('if (%s) {' % cond)
            if rp.predicate is not None:
                # TODO: If the predicate doesn't actually use the index on the
                # result edge, we could switch the order of the loops.
                pred_idx = res_idx if rp.predicate.parametric else 'INDEX_NONE'
                pr.write('if (reachable(%s, %s, %s, %s)) {'
                         % (res_src, res_tgt, rp.predicate.kind, pred_idx))
            asrt = rp.assertion()
            if asrt is not None:
                pr.write('assert(%s);' % asrt)
            pr.write('add_edge(%s, %s, %s, %s, %s, %s, %s, %s);'
                     % (res_src, res_tgt, res_kind, res_idx,
                        l_edge, l_rev, r_edge, r_rev))
            if rp.predicate is not None:
                pr.write('}')
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

    emit_derivs_or_reachable(grammar, pr, True)

    emit_derivs_or_reachable(grammar, pr, False)

    if terms_out is not None:
        for s in grammar.symbols:
            if s.is_terminal():
                terms_out.write('%s\n' % s)

    if rels_out is not None:
        for r in grammar.rels:
            if r.ref == 0:
                continue
            rels_out.write('%s\n' % r.name)

def emit_derivs_or_reachable(grammar, pr, emit_derivs):
    if emit_derivs:
        pr.write('std::list<Derivation> all_derivations(Edge *e) {')
        pr.write('std::list<Derivation> derivs;')
        pr.write('NODE_REF from = e->from;')
        pr.write('NODE_REF to = e->to;')
        pr.write('EDGE_KIND kind = e->kind;')
        pr.write('INDEX index = e->index;')
    else:
        pr.write('bool reachable(NODE_REF from, NODE_REF to, EDGE_KIND kind, '
                 + 'INDEX index) {')
        pr.write('assert(is_parametric(kind) ^ (index == INDEX_NONE));')
        pr.write('bool reached = false;')
    pr.write('Edge *l, *r;')
    # TODO: Local iterator variables may go unused: use per-case variables,
    # enclosed in {} blocks.
    # TODO: Paths for predicate witness edges are not used during path
    # reconstruction, so we don't have to consider that case when emitting
    # all_derivations.
    # TODO: We could stop the reachability search at the first hit, without
    # exhausting the iterators, but we must make sure we deallocate them
    # correctly.
    # When finding all derivations of an edge, we already know the edge has
    # passed any predicate checks, so we don't need to check those again.
    # Similarly, any rules producing predicate symbols are not allowed to
    # carry predicates themselves, so we don't need to emit predicate checks
    # for those either.
    pr.write('OutEdgeIterator *l_out_iter, *r_out_iter;')
    pr.write('InEdgeIterator *l_in_iter;')
    pr.write('switch (kind) {')
    for e_symbol in grammar.symbols:
        pr.write('case %s: /* %s */' % (e_symbol.kind, e_symbol))
        if (e_symbol.is_predicate() or e_symbol.is_terminal()) == emit_derivs:
            # Derivation re-construction should never be requested for
            # predicate witness edges, because none are ever explicitly
            # produced. Similarly, it doesn't make sense to request the
            # derivations for a terminal symbol.
            # Conversely, reachability queries only need to be performed for
            # predicate (and terminal) symbols.
            pr.write('assert(false);')
            pr.write('break;')
            continue
        if e_symbol.is_terminal():
            assert not emit_derivs
            pr.write('l_out_iter = get_out_edge_iterator_to_target(from, to, '
                     + '%s);' % e_symbol.kind)
            pr.write('while ((l = next_out_edge(l_out_iter)) != NULL) {')
            if e_symbol.parametric:
                pr.write('if (l->index == index) {')
            pr.write('reached = true;')
            if e_symbol.parametric:
                pr.write('}')
            pr.write('}')
            pr.write('break;')
            continue
        for p in grammar.prods.get(e_symbol):
            pr.write('/* %s */' % p)
            if p.left is None:
                # Empty production
                pr.write('if (from == to) {')
                if emit_derivs:
                    pr.write('derivs.push_back(derivation_empty());')
                else:
                    pr.write('reached = true;')
                pr.write('}')
                continue
            l_rev = util.to_c_bool(p.left.reversed)
            out_dir = p.outer_search_direction()
            out_src = p.outer_search_source()
            out_tgt = p.outer_search_target()
            pr.write('l_%s_iter = get_%s_edge_iterator%s(%s%s, %s);'
                     % (out_dir, out_dir,
                        '_to_target' if out_tgt is not None else '',
                        out_src,
                        (', ' + out_tgt) if out_tgt is not None else '',
                        p.left.symbol.kind))
            pr.write('while ((l = next_%s_edge(l_%s_iter)) != NULL) {'
                     % (out_dir, out_dir))
            out_cond = p.outer_condition(emit_derivs)
            if out_cond is not None:
                pr.write('if (%s) {' % out_cond)
            if p.right is None:
                # single production
                if emit_derivs:
                    pr.write('derivs.push_back(derivation_single(l, %s));'
                             % l_rev)
                else:
                    pr.write('reached = true;')
            else:
                # double production
                r_rev = util.to_c_bool(p.right.reversed)
                pr.write('r_out_iter = get_out_edge_iterator_to_target' +
                         ('(%s, %s, %s);' % (p.inner_search_source(),
                                             p.inner_search_target(),
                                             p.right.symbol.kind)))
                pr.write('while ((r = next_out_edge(r_out_iter)) != NULL) {')
                in_loop_header = p.inner_loop_header()
                if in_loop_header is not None:
                    pr.write('for (%s) {' % in_loop_header)
                in_cond = p.inner_condition(emit_derivs)
                if in_cond is not None:
                    pr.write('if (%s) {' % in_cond)
                if emit_derivs:
                    pr.write('derivs.push_back(derivation_double' +
                             '(l, %s, r, %s));' % (l_rev, r_rev))
                else:
                    pr.write('reached = true;')
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
    if emit_derivs:
        pr.write('return derivs;')
    else:
        pr.write('return reached;')
    pr.write('}')
    pr.write('')

# TODO: More user-friendly error output than assertion failure. Should use
# assertions only for the internal sanity checks, and do something different
# for input errors.
# TODO: More structured way to synthesize code: specialized C-code synthesis
# class, or put base program text in a large triple-quoted string and leave
# %s's for places to fill in.

## Help message describing the calling convention for this script.
usage_string = """Usage: %s <input-file> [<output-dir>]
Produce CFL-Reachability solver code for a Context-Free Grammar.

<input-file> must contain a grammar specification (see the main project docs
for details), and have a .cfg extension.

If <output-dir> is not provided, print generated code to stdout.

If <output-dir> is provided, and assuming that <input-file> is foo.cfg, then
nothing is printed on stdout, and the following files are created:
- foo.cpp: the generated code
- foo.terms.dat: all terminal symbols of the grammar, one per line
- foo.rels.dat: all relations used in the grammar, one per line
"""

if __name__ == '__main__':
    if (len(sys.argv) < 2 or
        sys.argv[1] == '-h' or sys.argv[1] == '--help' or
        os.path.splitext(sys.argv[1])[1] != '.cfg' or
        len(sys.argv) >= 3 and (not os.path.exists(sys.argv[2]) or
                                not os.path.isdir(sys.argv[2]))):
        script_name = os.path.basename(__file__)
        sys.stderr.write(usage_string % script_name)
        exit(1)
    with open(sys.argv[1], 'r') as grammar_in:
        if len(sys.argv) >= 3:
            outdir = sys.argv[2]
            grammar = os.path.basename(os.path.splitext(sys.argv[1])[0])
            code_out_name = os.path.join(outdir, grammar + '.cpp')
            terms_out_name = os.path.join(outdir, grammar + '.terms.dat')
            rels_out_name = os.path.join(outdir, grammar + '.rels.dat')
            with open(code_out_name, 'w') as code_out:
                with open(terms_out_name, 'w') as terms_out:
                    with open(rels_out_name, 'w') as rels_out:
                        parse(grammar_in, code_out, terms_out, rels_out)
        else:
            parse(grammar_in, sys.stdout, None, None)
