# -*- coding: utf-8 -*-
"""**Context Free Grammars Manipulation.**

Basic context-free grammars manipulation for building uniform random generetors

.. *Authors:* Rogério Reis & Nelma Moreira

.. *This is part of FAdo project* http://fado.dcc.fc.up.pt

.. *Copyright:* 1999-2013 Rogério Reis & Nelma Moreira {rvr,nam}@dcc.fc.up.pt


.. This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as published
   by the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
   or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
   for more details.

   You should have received a copy of the GNU General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   675 Mass Ave, Cambridge, MA 02139, USA."""

#__package__ = "FAdo"

import re
import string
from types import StringType
from random import randint
import common


class CFGrammar(object):
    """ Class for context-free grammars

    :var rules: grammar rules
    :var terminals: terminals symbols
    :var nonterminals: nonterminals symbols
    :var start: start symbol
    :type start: string
    :var ntr: dictionary of rules for each nonterminal"""

    def __init__(self, grammar):
        """

        :param grammar: is a list for productions; each production is a tuple (LeftHandside,
            RightHandside) with LeftHandside nonterminal, RightHandside list of symbols,
            First production is for start symbol"""
        self.rules = grammar
        self.makenonterminals()
        self.maketerminals()
        self.start = self.rules[0][0]
        """ ntr[A] is the set of rules which has A as left side"""
        self.ntr = {}
        for i in xrange(len(self.rules)):
            if self.rules[i][0] not in self.ntr:
                self.ntr[self.rules[i][0]] = {i}
            else:
                self.ntr[self.rules[i][0]].add(i)

    def __str__(self):
        """Grammar rules

        :return: a string representing the grammar rules"""
        s = ""
        for n in xrange(len(self.rules)):
            lhs = self.rules[n][0]
            rhs = self.rules[n][1]
            s += "%s | %s -> %s \n" % (n, lhs, string.join(rhs))
        return "Grammar Rules:\n\n%s" % s

    def makeFFN(self):
        self.NULLABLE()
        self.FIRST_ONE()
        self.FOLLOW()

    def maketerminals(self):
        """Extracts C{terminals} from the rules. Nonterminals must already exist"""
        self.terminals = set([])
        for r in self.rules:
            if type(r[1]) is StringType:
                if r[1] not in self.nonterminals:
                    self.terminals.add(r[1])
            else:
                for s in r[1]:
                    if s not in self.nonterminals:
                        self.terminals.add(s)

    def makenonterminals(self):
        """Extracts C{nonterminals}  from grammar rules."""
        self.nonterminals = set([])
        for r in self.rules:
            self.nonterminals.add(r[0])

    def terminalrules(self):
        self.tr = {}
        for a in self.terminals:
            for i in xrange(len(self.rules)):
                if self.rules[i][1] == a:
                    if a not in self.tr:
                        self.tr[a] = {i}
                    else:
                        self.tr[a].add(i)

    def nonterminalrules(self):
        self.ntr = {}
        for i in xrange(len(self.rules)):
            if self.rules[i][0] not in self.ntr:
                self.ntr[self.rules[i][0]] = {i}
            else:
                self.ntr[self.rules[i][0]].add(i)

    def NULLABLE(self):
        """Determines which nonterminals X ->* [] """
        self.nullable = {}
        for s in self.terminals:
            self.nullable[s] = 0
        for s in self.nonterminals:
            self.nullable[s] = 0
            if s in self.ntr:
                for i in self.ntr[s]:
                    if not self.rules[i][1]:
                        self.nullable[s] = 1
                        break
        k = 1
        while k == 1:
            k = 0
            for r in self.rules:
                e = 0
                for i in r[1]:
                    if not self.nullable[i]:
                        e = 1
                        break
                if e == 0 and not self.nullable[r[0]]:
                    self.nullable[r[0]] = 1
                    k = 1


class CNF(CFGrammar):
    """No useless nonterminals or epsilon rules are ALLOWED... Given a CFG grammar description generates one in CNF
    Then its possible to random generate words of a given size. Before some pre-calculations are nedded."""

    def __init__(self, grammar, mark="A@"):
        CFGrammar.__init__(self, grammar)
        self.mark = mark
        self.newnt = 0
        self.unitary = self.get_unitary()
        self.Chomsky()

    def get_unitary(self):
        return set([r for r in self.rules if
                    (type(r[1]) is StringType and
                     r[1] in self.nonterminals) or
                    (len(r[1]) == 1 and r[1][0] in self.nonterminals)])

    def elim_unitary(self):
        """Elimination of unitary rules """
        f = 1
        while f:
            f = 0
            self.unitary = self.get_unitary()

            for u in self.unitary:
                if type(u[1]) is StringType:
                    ui = u[1]
                else:
                    ui = u[1][0]
                if ui in self.ntr:
                    for i in self.ntr[ui]:
                        if (u[0], self.rules[i][1]) not in self.rules:
                            f = 1
                            self.rules.append((u[0], self.rules[i][1]))
                            self.ntr[u[0]].add(len(self.rules) - 1)

        for u in self.unitary:
            self.rules.remove(u)

    def get_ntr_tr(self, a):
        nta = self.mark + a
        self.nonterminals.add(nta)
        self.rules.append((nta, a))
        return nta

    def iter_rule(self, lhs, rhs, i):
        if type(rhs) is not StringType and len(rhs) == 2:
            self.rules[i] = ((lhs, rhs))
            return
        nta = self.mark + "_" + str(self.newnt)
        self.nonterminals.add(nta)
        self.newnt += 1
        self.rules.append((lhs, (rhs[0], nta)))
        self.iter_rule(nta, rhs[1:], i)

    def Chomsky(self):
        """ Transform to CNF """
        self.elim_unitary()
        self.nttr = {}
        # terminal a is replaced by A@_a in all rules > 2
        for a in self.terminals:
            for i in xrange(len(self.rules)):
                if type(self.rules[i][1]) is not StringType and len(self.rules[i][1]) >= 2 and a in self.rules[i][1]:
                    if a not in self.nttr:
                        self.nttr[a] = self.get_ntr_tr(a)
                    rr = list(self.rules[i][1])
                    for k in xrange(len(rr)):
                        if rr[k] == a:
                            rr[k] = self.nttr[a]
                    self.rules[i] = (self.rules[i][0], tuple(rr))
        n = len(self.rules)
        for i in xrange(n):
            if type(self.rules[i][1]) is not StringType and len(self.rules[i][1]) > 2:
                self.iter_rule(self.rules[i][0], self.rules[i][1], i)


class cfgGenerator(object):
    """CFG uniform genetaror"""

    def __init__(self, grammar, size):
        """Object initialization
        :arg grammar: grammar for the random objects
        :type grammar: CNF
        :arg size: size of objects
        :type size: integer"""
        self.grammar = grammar
        self.size = size
        self._eval_densities(size)

    def generate(self):
        """Generates a new random object generated from the start symbol

        :returns: object
        :rtype: string"""
        return self._gen(self.grammar.start, self.size)

    def _gen(self, nt, n):
        """Generates a new random object generated from the nonterminal

        :arg nt: nonterminal
        :type nt: string
        :arg n: object size
        :type n: integer
        :returns: object
        :rtype: string"""
        g = self.grammar
        if n in self.density[nt] and self.density[nt][n] > 0:
            u = randint(1, self.density[nt][n])
            r = 1
            if n == 1:
                for i in g.ntr[nt]:
                    if g.rules[i][1] in g.terminals:
                        r += 1
                        if r > u:
                            ic = i
                            break
                try:
                    return (g.rules[ic][1])
                except KeyError:
                    raise KeyError
            for i in g.ntr[nt]:
                if len(g.rules[i][1]) == 2:
                    if n in self.density_r[i]:
                        r += self.density_r[i][n]
                        if r > u:
                            ic = i
                            break
            uk = randint(1, self.density_r[ic][n])
            rk = 1
            for k in xrange(1, n):
                if (k in self.density[g.rules[ic][1][0]] and self.density[g.rules[ic][1][0]][k] > 0 and
                   n - k in self.density[g.rules[ic][1][1]] and self.density[g.rules[ic][1][1]][n - k] > 0):
                    rk += self.density[g.rules[ic][1][0]][k] * self.density[g.rules[ic][1][1]][n - k]
                    if rk > uk:
                        kk = k
                        break
            return self._gen(g.rules[ic][1][0], kk) + self._gen(g.rules[ic][1][1], n - kk)

    def _eval_densities(self, n):
        """Evaluates densities

        :arg n: object size
        :type n: integer"""
        g = self.grammar
        self.density = {}
        self.density_r = {}
        for nt in g.nonterminals:
            self.density[nt] = {}
            self.density[nt][1] = 0
        g.terminalrules()
        g.nonterminalrules()
        for t in g.tr:
            for i in g.tr[t]:
                self.density[g.rules[i][0]][1] += 1
        for l in xrange(2, n + 1):
            for nt in g.ntr:
                r = 0
                for i in g.ntr[nt]:
                    if len(g.rules[i][1]) == 2:
                        if i not in self.density_r:
                            self.density_r[i] = {}
                        self.density_r[i][l] = sum(
                            [self.density[g.rules[i][1][0]][k] * self.density[g.rules[i][1][1]][l - k] for k in
                             xrange(1, l) if
                             k in self.density[g.rules[i][1][0]] and l - k in self.density[g.rules[i][1][1]]])
                        r += self.density_r[i][l]
                if r:
                    self.density[nt][l] = r


class reStringRGenerator(cfgGenerator):
    """Uniform random Generator for reStrings"""

    def __init__(self, Sigma=["a", "b"], size=10, grammar=None, epsilon=None, empty=None):
        """ Uniform random generator for regular expressions. Used without arguments generates an uncollapsible re
        over {a,b} with size 10. For generate an arbitary re over an alphabet of 10 symbols of size 100:
        reStringRGenerator (small_alphabet(10),100,reStringRGenerator.g_regular_base)

        :arg Sigma: re alphabet (that will be the set of grammar terminals)
        :type Sigma: list or set
        :arg size: word size
        :type size: integer
        :arg grammar: base grammar
        :arg epsilon: if not None is added to a grammar terminals
        :arg empty: if not None is added to a grammar terminals

        .. note::
           the grammar can have already this symbols"""
        if grammar is None:
            self.base = grules(reGrammar["g_regular_uncollaps"])
        else:
            self.base = grules(grammar)
        self.Sigma = Sigma
        for i in self.Sigma:
            self.base.append(("i", i))
        if epsilon is not None:
            self.base.append(("i", common.Epsilon))
        if empty is not None:
            self.base.append(("i", common.EmptySet))
        self.gen = cfgGenerator(CNF(self.base), size)

    def generate(self):
        """Generates a new random RE string"""
        return self.gen.generate()


def CYKParserTable(gramm, word):
    """Evaluates CYK parser table

    :arg gramm: grammar
    :type gramm: CNF
    :arg word: word to be parsed
    :type word: string
    :returns: the CYK table
    :rtype: list of lists of symbols"""
    pass


def grules(rules_list, rulesym="->", rhssep=None, rulesep='|'):
    """Transforms a list of rules in a grammar description.

    :arg rules_list: is a list of rule where rule is a string  of the form: Word rulesym Word1 ... Word2 or  Word
        rulesym []
    :arg rulesym: LHS and RHS rule separator
    :arg rhssep: RHS values separator (None for white chars)
    :return: a grammar description """
    gr = []
    sep = re.compile(rulesym)
    rsep = re.compile(rulesep)
    for r in rules_list:
        if type(r) is StringType:
            rule = r
        else:
            rule = r[0]
        m = sep.search(rule)
        if not m:
            continue
        else:
            if m.start() == 0:
                raise common.CFGgrammarError(rule)
            else:
                lhs = rule[0:m.start()].strip()
            if m.end() == len(rule):
                raise common.CFGgrammarError(rule)
            else:
                rest = string.strip(rule[m.end():])
                if rest == "[]":
                    rhs = []
                else:
                    multi = string.split(rest, rulesep)
                    rhs = []
                    for i in multi:
                        l = string.split(i, rhssep)
                        if len(l) > 1:
                            l = tuple(string.split(i, rhssep))
                        else:
                            l = l[0]
                        gr.append((lhs, l))
    return gr


def small_alphabet(k, sigma_base="a"):
    """Easy way to have small alphabets

    :arg k: alphabet size (must be less than 52)
    :arg sigma_base: initial symbol
    :returns: alphabet
    :rtype: list"""
    Sigma = []
    if k >= 52:
        raise common.CFGterminalError(k)
    lim = min(26, k)
    sigma_base = 'a'
    for i in xrange(lim):
        Sigma.append(chr(ord(sigma_base) + i))
    if k >= 26:
        sigma_base = 'A'
        for i in xrange(k - lim):
            Sigma.append(chr(ord(sigma_base) + i))
    return Sigma


"""Some regular expressions grammars
   i stands for alphabetic symbols
"""
reGrammar = {"g_regular_base": ["r -> r + c | c", "c -> c s |  s",
                                "s -> s * | i | ( r ) "],

             "g_regular_wredund": ["r -> rs | f ", "rs -> rs + f | f + f",
                                   "f -> c | e | i",
                                   "c -> c s |  s s",
                                   "s -> e | i | ( rs ) ",
                                   "e -> ( rs ) * | ( c ) * | i *"],

             "g_regular_uncollaps": ["s ->  rs | cc  | ee | i | %s | %s" % (common.Epsilon, common.EmptySet),
                                     "cc -> cc r | r r",
                                     "r -> ( rs ) | ee | i ",
                                     "ee -> ( rs ) * | ( cc ) * | i *",
                                     " rs -> %s + x | y + z" % common.Epsilon,
                                     "x -> t | t + x",
                                     "t -> cc | i ",
                                     "y -> z | y + z",
                                     "z -> cc | ee | i "],
             "g_rpn": ["x -> %s | i | +  x  x  | . x x  | * x" % (common.Epsilon)],
             "g_sha": ["s  -> ed | ec | es | i |%s" % (common.Epsilon),
                       "ec -> . ec r | . r r ",
                       "r -> ed | es | i",
                       "es -> * ed | * ec | * i",
                       "ed -> + %s x | + y z" % (common.Epsilon),
                       "x -> t | + t x",
                       "t -> ec | i",
                       "y -> z | + y z",
                       "z -> ec | es | i"],
             "g_rpn_pi": [
                 "p -> i | +  p  x | + np p | . x p | . p %s" % (common.Epsilon),
                 "x -> %s | i | +  x  x  | . x x  | * x" % (common.Epsilon),
                 "np -> %s | +  np  np | . np np | . p np" % (common.Epsilon)
             ]
}
