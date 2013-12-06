# -*- coding: utf-8 -*-
"""Finite languages and related automata manipulation

Finite languages manipulation

.. *Authors:* Rogério Reis & Nelma Moreira

.. *This is part of FAdo project*   <http://fado.dcc.fc.up.pt

.. *Version:* 0.9.5

.. *Copyright*: 1999-2011 Rogério Reis & Nelma Moreira {rvr,nam}@dcc.fc.up.pt

.. This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA."""

import fa
from copy import deepcopy, copy
from common import *
import random


class FL(object):
    """Finite Language Class

    :var Words: the elements of the language
    :var Sigma: the alphabet"""
    def __init__(self, wordsList=None, Sigma=set([])):
        if not wordsList:
            wordsList = []
        self.Words = set(wordsList)
        self.Sigma = Sigma
        for w in self.Words:
            for l in w:
                self.Sigma.add(l)

    def __str__(self):
        return "(%s,%s)" % (list(self.Words), list(self.Sigma))

    def __repr__(self):
        return "FL%s" % self.__str__()

    def reunion(self, other):
        """Reunion of FL:   a | b

        :param other: right hand operand
        :type other: FL
        :rtype: FL
        :raises FAdoGeneralError: if both arguments are not FL"""
        return self.__or__(other)

    def __or__(self, other):
        if type(other) != type(self):
            raise FAdoGeneralError("Incompatible objects")
        new = FL()
        new.Sigma = self.Sigma | other.Sigma
        new.Words = self.Words | other.Words
        return new

    def intersection(self, other):
        """Intersection of FL: a & b

        :param other: right hand operand
        :type other: FL
        :raises FAdoGeneralError: if both arguments are not FL"""
        return self.__and__(other)

    def __and__(self, other):
        if type(other) != type(self):
            raise FAdoGeneralError("Incompatible objects")
        new = FL()
        new.Sigma = self.Sigma | other.Sigma
        new.Words = self.Words & other.Words
        return new

    def diff(self, other):
        """Difference of FL: a - b

        :param other: right hand operand
        :type other: FL
        :rtype: FL
        :raises FAdoGeneralError: if both arguments are not FL"""
        return self.__sub__(other)

    def __sub__(self, other):
        if type(other) != type(self):
            raise FAdoGeneralError("Incompatible objects")
        new = FL()
        new.Sigma = self.Sigma | other.Sigma
        new.Words = self.Words - other.Words
        return new

    def setSigma(self, Sigma, Strict=False):
        """Sets the alphabet of a FL

        :param Sigma: alphabet
        :type Sigma: Set of char
        :param Strict: behaviour
        :type Strict: boolean
     
        .. attention::
           Unless Strict flag is set to True, alphabet can only be enlarged.  The resulting alphabet is  in fact the
           union of the former alphabet with the new one. If flag is set to True, the alphabet is simply replaced."""
        if Strict:
            self.Sigma = Sigma
        else:
            self.Sigma = self.Sigma.union(Sigma)

    def addWords(self, wList):
        """Adds a list of words to a FL

        :param wList: words to add
        :type wList: list of strings"""
        self.Words = self.Words | set(wList)
        for w in wList:
            for c in w:
                self.Sigma.add(c)

    def filter(self, automata):
        """Separates a language in two other using a DFA of NFA as a filter

        :param automata: the automata to be used as a filter
        :type automata: DFA or NFA
        :returns: the accepted/unaccepted pair of languages
        :rtype: tuple of FL"""
        a, b = (FL(), FL())
        a.setSigma(self.Sigma)
        b.setSigma(self.Sigma)
        for w in self.Words:
            if automata.evalWord(w):
                a.addWords([w])
            else:
                b.addWords([w])
        return a, b

    def trieFA(self):
        """Generates the trie automaton that recognises this language

        :returns: the trie automaton
        :rtype: ADFA"""
        new = ADFA()
        new.setSigma(copy(self.Sigma))
        i = new.addState()
        new.setInitial(i)
        for w in self.Words:
            s = i
            for c in w:
                if c not in new.delta.get(s, []):
                    sn = new.addState()
                    new.addTransition(s, c, sn)
                    s = sn
                else:
                    s = new.delta[s][c]
            new.addFinal(s)
        return new

    # noinspection PyUnboundLocalVariable
    def multiLineAutomaton(self):
        """Generates the trivial linear ANFA equivalent to this language
    
        :rtype: ANFA"""
        new = ANFA()
        new.setSigma(copy(self.Sigma))
        for w in self.Words:
            s = new.addState()
            new.addInitial(s)
            for c in w:
                s1 = new.addState()
                new.addTransition(s, c, s1)
                s = s1
            new.addFinal(s1)
        return new


class DCFA(fa.DFA):
    """Deterministic Cover Automata class

    .. inheritance-diagram:: DCFA"""

    def setLength(self, l):
        """Set the maximum length of the words of the language

        :param l: length
        :type l: int"""
        self.Length = l


class AFA(object):
    """Base class for Acyclic Finite Automata

    .. inheritance-diagram:: AFA

    .. note::
       This is just a container for some common methods. **Not to be used directly!!**"""

    def setDeadState(self, sti):
        """Identifies the dead state

        :param sti: index of the dead state
        :type sti: integer
    
        .. attention::
           nothing is done to ensure that the state given is legitimate

        .. note::
           without dead state identified, most of the methods for acyclic automata can not be applied"""
        self.Dead = sti

    def ensureDead(self):
        """Ensures that a state is defined as dead"""
        try:
            _ = self.Dead
        except AttributeError:
            x = self.addState()
            self.setDeadState(x)

    def ordered(self):
        """Orders states names in its topological order
    
        :returns: ordered list of state indexes
        :rtype: list of integers

        .. note::
           one could use the FA.toposort() method, but special care must be taken with the dead state for the
           algorithms related with cover automata."""

        def _dealS(st):
            if st not in torder:
                torder.append(st)
                if st in self.delta.keys():
                    for k in self.delta[st]:
                        for dest in forceIterable(self.delta[st][k]):
                            if dest not in torder and dest != self.Dead:
                                queue.append(dest)

        try:
            dead = self.Dead
        except AttributeError:
            raise FAdoGeneralError("ADFA has not dead state identified")
        torder, queue = [], []
        _dealS(self.Initial)
        while queue:
            st = queue.pop()
            _dealS(st)
        torder.append(dead)
        return torder

    def _getRdelta(self):
        """
        :returns: pair, map of number of sons map, of reverse conectivity
        :rtype: (dictionary int->set of integers, dictionary int->int)"""
        done = set()
        deltaC, rdelta = {}, {}
        notDone = set(forceIterable(self.Initial))
        while notDone:
            sts = uSet(notDone)
            done.add(sts)
            l = set()
            for k in self.delta.get(sts, []):
                for std in forceIterable(self.delta[sts][k]):
                    l.add(std)
                    rdelta.setdefault(std, set([])).add(sts)
                    if std not in done:
                        notDone.add(std)
            deltaC[sts] = len(l)
            notDone.remove(sts)
        for s in forceIterable(self.Initial):
            if s not in rdelta:
                rdelta[s] = set()
        return deltaC, rdelta

    def evalRank(self):
        """Evaluates the rank map of a automaton

        :return: pair sets of states by rank map, reverse delta acessability map
        :rtype: pair of dictionaries int -> set of int"""
        (deltaC, rdelta) = self._getRdelta()
        rank, deltai = {}, {}
        for s in xrange(len(self.States)):
            deltai.setdefault(deltaC[s], set([])).add(s)
        i = -1
        notDone = copy(range(len(self.States)))
        deltaC[self.Dead] = 0
        deltai[1].remove(self.Dead)
        deltai[0] = {self.Dead}
        rdelta[self.Dead].remove(self.Dead)
        while notDone:
            rank[i] = deepcopy(deltai[0])
            deltai[0] = set()
            for s in rank[i]:
                for s1 in rdelta[s]:
                    l = deltaC[s1]
                    deltaC[s1] = l - 1
                    deltai[l].remove(s1)
                    deltai.setdefault(l - 1, set()).add(s1)
                notDone.remove(s)
            i += 1
        return rank, rdelta

    def getLeaves(self):
        """The set of leaves, i.e. final states for last symbols of language words

        :return: set of leaves
        :rtype: set of state names"""

        def _last(s):
            queue, done = {s}, set()
            while queue:
                q = queue.pop()
                done.add(q)
                for k in self.delta.get(q, {}):
                    for s1 in forceIterable(self.delta[q][k]):
                        if self.finalP(s1):
                            return False
                        elif s1 not in done:
                            queue.add(s1)
            return True

        leaves = set()
        for s in self.Final:
            if _last(s):
                leaves.add(self.States[s])
        return leaves


class ADFA(fa.DFA, AFA):
    """Acyclic Deterministic Finite Automata class

    .. inheritance-diagram:: ADFA"""

    def __repr__(self):
        return 'ADFA(%s)' % self.__str__()

    def complete(self, dead=None):
        """Make the ADFA complete

        :param dead: a state to be identified as dead state if one was not identified yet
        :type dead: integer

        .. attention::
           The object is modified in place"""
        if dead is not None:
            self.Dead = dead
        else:
            try:
                _ = self.Dead
            except AttributeError:
                foo = self.addState()
                self.setDeadState(foo)
        for st in range(len(self.States)):
            for k in self.Sigma:
                if k not in self.delta.get(st, {}).keys():
                    self.addTransition(st, k, self.Dead)

    def minimal(self):
        """Finds the minimal equivalent ADFA

        .. seealso:: [TCS 92 pp 181-189] Minimisation of acyclic deterministic automata in linear time, Dominique Revuz

        :returns: the minimal equivalent ADFA
        :rtype: ADFA"""

        def _getListDelta(ss):
            """returns [([sons,final?],s) for s in ss].sort"""
            l = []
            for s in ss:
                dl = [new.delta[s][k] for k in new.Sigma]
                dl.append(s in new.Final)
                l.append((dl, s))
            l.sort()
            return l

        def _collapse(s1, s2):
            """redirects all transitions going to s2 to s1 and adds s2 to toBeDeleted"""
            for s in rdelta[s2]:
                for k in new.delta[s]:
                    if new.delta[s][k] == s2:
                        new.delta[s][k] = s1
            toBeDeleted.append(s2)

        new = deepcopy(self)
        new.trim()
        new.complete()
        (rank, rdelta) = new.evalRank()
        toBeDeleted = []
        maxr = len(rank) - 2
        for r in xrange(maxr + 1):
            ls = _getListDelta(rank[r])
            (d0, s0) = ls[0]
            j = 1
            while j < len(ls):
                (d1, s1) = ls[j]
                if d0 == d1:
                    _collapse(s0, s1)
                else:
                    (d0, s0) = (d1, s1)
                j += 1
        new.deleteStates(toBeDeleted)
        return new

    def trim(self):
        """Remove states that do not lead to a final state, or, inclusively, that can't be reached from the initial
        state. Only useful states remain.

        .. attention:: in place transformation"""
        fa.FA.trim(self)
        try:
            del self.Dead
        except AttributeError:
            pass

    def toANFA(self):
        """Converts the ADFA in a equivalent ANFA
    @rtype: ANFA"""
        new = ANFA()
        new.setSigma(copy(self.Sigma))
        new.States = copy(self.States)
        for s in xrange(len(self.States)):
            for k in self.delta.get(s, {}):
                new.addTransition(s, k, self.delta[s][k])
        new.addInitial(self.Initial)
        for s in self.Final:
            new.addFinal(s)
        return new


class ANFA(fa.NFA, AFA):
    """Acyclic Nondeterministic Finite Automata class

    .. inheritance-diagram:: ANFA"""

    def moveFinal(self, st, stf):
        """Unsets a set as final transfering transition to another final
        :param st: the state to be 'moved'
        :type st: integer
        :param stf: the destination final state
        :type stf: integer

        .. note::
           stf must be a 'last' final state, i.e., must have no out transitions to anywhere but to a possible dead
           state

        .. @attention:: the object is modified in place"""
        (rdelta, _) = self._getRdelta()
        for s in rdelta[st]:
            l = []
            for k in self.delta[s]:
                if st in self.delta[s][k]:
                    l.append(k)
            for k in l:
                self.addTransition(s, k, stf)
            self.delFinal(s)

    def mergeStates(self, s1, s2):
        """Merge state s2 into state s1

        :param s1: state
        :type s1: integer
        :param s2: state
        :type s2: integer

        .. note::
           no attempt is made to check if the merging preserves the language of teh automaton

        .. attention:: the object is modified in place"""
        (_, rdelta) = self._getRdelta()
        for s in rdelta[s2]:
            l = []
            for k in self.delta[s]:
                if s2 in self.delta[s][k]:
                    l.append(k)
            for k in l:
                self.delta[s][k].remove(s2)
                self.addTransition(s, k, s1)
        for k in self.delta.get(s2, {}):
            for ss in self.delta[s2][k]:
                self.delta.setdefault(s1, {}).setdefault(k, set()).add(ss)
        self.deleteState(s2)

    def mergeLeaves(self):
        """Merge leaves

        .. attention:: object is modified in place"""
        l = self.getLeaves()
        if len(l):
            s0n = l.pop()
            while l:
                s0 = self.stateName(s0n)
                s = self.stateName(l.pop())
                self.mergeStates(s0, s)

    def mergeInitial(self):
        """Merge initial states

        .. attention:: object is modified in place"""
        l = copy(self.Initial)
        s0 = self.StateName(l.pop())
        while l:
            s = self.stateName(l.pop())
            self.mergeStates(s0, s)


def sigmaInitialSegment(Sigma, l, exact=False):
    """Generates the ADFA recognizing Sigma^i for i<=l
    :param Sigma: the alphabet
    :type Sigma: set of symbols
    :param l: length
    :type l: integer
    :param exact: only the words with exactly that length?
    :type exact: boolean
    :returns: the automaton
    :rtype: ADFA"""
    new = ADFA()
    new.setSigma(Sigma)
    s = new.addState()
    if not exact:
        new.addFinal(s)
    new.setInitial(s)
    for i in range(l):
        s1 = new.addState()
        if not exact or i == l - 1:
            new.addFinal(s1)
        for k in Sigma:
            new.addTransition(s, k, s1)
        s = s1
    return new


# noinspection PyUnboundLocalVariable
def genRndTrieBalanced(maxL, Sigma, safe=True):
    """Generates a random trie automaton for a binary language of balanced words of a given leght for max word
    :param maxL: length of the max word
    :type maxL: integer
    :param Sigma: alphabet to be used
    :type Sigma: set
    :param safe: should a word of size maxl be present in every language?
    :type safe: boolean
    :return: the generated trie automaton
    :rtype: ADFA"""

    def _genEnsurance(m, Sigma):
        l = len(Sigma)
        fair = m / l
        if m % l == 0:
            odd = 0
        else:
            odd = 1
        pool = copy(Sigma)
        c = {}
        sl = []
        while len(sl) < m:
            s = random.choice(pool)
            c[s] = c.get(s, 0) + 1
            if c[s] == fair + odd:
                pool.remove(s)
            sl.append(s)
        return sl

    def _legal(contab):
        l = [contab[k] for k in contab]
        return max(l) - min(l) <= 1

    def _descend(s, ens, safe, m, contab):
        sons = 0
        if not safe:
            if _legal(contab):
                final = random.randint(0, 1)
            else:
                final = 0
        # noinspection PyUnboundLocalVariable
        if safe:
            trie.addFinal(s)
            final = 1
        elif final == 1:
            trie.addFinal(s)
        if m != 0:
            if safe:
                ks = ens.pop()
            else:
                ks = None
            for k in trie.Sigma:
                ss = trie.addState()
                trie.addTransition(s, k, ss)
                contab[k] = contab.get(k, 0) + 1
                if _descend(ss, ens, k == ks, m - 1, contab):
                    sons += 1
                contab[k] -= 1
        if sons == 0 and final == 0:
            trie.deleteState(s)
            return False
        else:
            return True

    if safe:
        ensurance = _genEnsurance(maxL, Sigma)
    trie = ADFA()
    trie.setSigma(Sigma)
    s = trie.addState()
    trie.setInitial(s)
    contab = {}
    for k in Sigma:
        contab[k] = 0
    _descend(s, ensurance, safe, maxL, contab)
    if random.randint(0, 1) == 1:
        trie.delFinal(s)
    return trie


# noinspection PyUnboundLocalVariable
def genRndTrieUnbalanced(maxL, Sigma, ratio, safe=True):
    """Generates a random trie automaton for a binary language of balanced words of a given leght for max word
    :param maxL: length of the max word
    :type maxL: integer
    :param Sigma: alphabet to be used
    :type Sigma: set
    :param ratio: the ration of the unbalance
    :type ratio: integer
    :param safe: should a word of size maxl be present in every language?
    :type safe: boolean
    :return: the generated trie automaton
    :rtype: ADFA"""

    def _genEnsurance(m, Sigma):
    #    l = len(Sigma)
        chief = uSet(Sigma)
        fair = m / (ratio + 1)
        pool = copy(Sigma)
        c = {}
        sl = []
        while len(sl) < m:
            s = random.choice(pool)
            c[s] = c.get(s, 0) + 1
            if len(sl) - c.get(chief, 0) == fair:
                pool = [chief]
            sl.append(s)
        return sl

    def _legal(contab):
        l = [contab[k] for k in contab]
        return (ratio + 1) * contab[uSet(Sigma)] >= sum(l)

    # noinspection PyUnboundLocalVariable
    def _descend(s, ens, safe, m, contab):
        sons = 0
        if not safe:
            if _legal(contab):
                final = random.randint(0, 1)
            else:
                final = 0
        if safe:
            trie.addFinal(s)
            final = 1
        elif final == 1:
            trie.addFinal(s)
        if m:
            if safe:
                ks = ens.pop()
            else:
                ks = None
            for k in trie.Sigma:
                ss = trie.addState()
                trie.addTransition(s, k, ss)
                contab[k] = contab.get(k, 0) + 1
                if _descend(ss, ens, k == ks, m - 1, contab):
                    sons += 1
                contab[k] -= 1
        if sons == 0 and final == 0:
            trie.deleteState(s)
            return False
        else:
            return True

    if safe:
        ensurance = _genEnsurance(maxL, Sigma)
    trie = ADFA()
    trie.setSigma(Sigma)
    s = trie.addState()
    trie.setInitial(s)
    contab = {}
    for k in Sigma:
        contab[k] = 0
    _descend(s, ensurance, safe, maxL, contab)
    if random.randint(0, 1) == 1:
        trie.delFinal(s)
    return trie


# noinspection PyUnboundLocalVariable
def genRandomTrie(maxL, Sigma, safe=True):
    """Generates a random trie automaton for a finite language with a given length for max word
    :param maxL: length of the max word
    :type maxL: integer
    :param Sigma: alphabet to be used
    :type Sigma: set
    :param safe: should a word of size maxl be present in every language?
    :type safe: boolean
    :return: the generated trie automaton
    :rtype: ADFA"""

    def _genEnsurance(m, Sigma):
        l = len(Sigma)
        sl = list(Sigma)
        return [sl[random.randint(0, l - 1)] for _ in xrange(m)]

    # noinspection PyUnboundLocalVariable
    def _descend(s, ens, safe, m):
        sons = 0
        if not safe:
            final = random.randint(0, 1)
        if safe:
            trie.addFinal(s)
            final = 1
        elif final == 1:
            trie.addFinal(s)
        if m:
            if safe:
                ks = ens.pop()
            else:
                ks = None
            for k in trie.Sigma:
                ss = trie.addState()
                trie.addTransition(s, k, ss)
                if _descend(ss, ens, k == ks, m - 1):
                    sons += 1
        if sons == 0 and final == 0:
            trie.deleteState(s)
            return False
        else:
            return True

    if safe:
        ensurance = _genEnsurance(maxL, Sigma)
    trie = ADFA()
    trie.setSigma(Sigma)
    s = trie.addState()
    trie.setInitial(s)
    _descend(s, ensurance, safe, maxL)
    if random.randint(0, 1) == 1:
        trie.delFinal(s)
    return trie


# noinspection PyUnboundLocalVariable
def genRndTriePrefix(maxL, Sigma, ClosedP=False, safe=True):
    """Generates a random trie automaton for a finite (either prefix free or prefix closed) language with a given
    length for max word
    :param maxL: length of the max word
    :type maxL: integer
    :param Sigma: alphabet to be used
    :type Sigma: set
    :param ClosedP: should it be a prefix closed language?
    :type ClosedP: boolean
    :param safe: should a word of size maxl be present in every language?
    :type safe: boolean
    :return: the generated trie automaton
    :rtype: ADFA"""

    def _genEnsurance(m, Sigma):
        l = len(Sigma)
        sl = list(Sigma)
        return [sl[random.randint(0, l - 1)] for _ in xrange(m)]

    def _descend(s, ens, safe, m):
        sons = ClosedP
        if m is 0:
            final = random.randint(0, 1)
            if safe or final == 1:
                trie.addFinal(s)
                return True
            else:
                return False
        else:
            if safe is True:
                ks = ens.pop()
            else:
                ks = None
            for k in trie.Sigma:
                ss = trie.addState()
                trie.addTransition(s, k, ss)
                r = _descend(ss, ens, k == ks, m - 1)
                if not ClosedP:
                    sons |= r
                else:
                    sons &= 1
            if not ClosedP:
                if not sons:
                    final = random.randint(0, 1)
                    if final == 1:
                        trie.addFinal(s)
                        return True
                    else:
                        return False
                else:
                    return True
            else:
                if not sons:
                    final = random.randint(0, 1)
                    if final == 1:
                        trie.addFinal(s)
                        return True
                    else:
                        return False
                else:
                    trie.addFinal(s)
                    return True

    if safe:
        ensurance = _genEnsurance(maxL, Sigma)
    trie = ADFA()
    trie.setSigma(Sigma)
    s = trie.addState()
    trie.setInitial(s)
    _descend(s, ensurance, safe, maxL)
    return trie


def DFAtoADFA(aut):
    """Transforms an acyclic DFA into a ADFA
 :param aut: the automaton to be transformed
 @type aut: DFA
 @raises notAcyclic: if the DFA is not acyclic
 @returns: the converted automaton
 @rtype: ADFA"""
    new = deepcopy(aut)
    new.trim()
    if not new.acyclicP(True):
        raise notAcyclic()
    afa = ADFA()
    afa.States = copy(new.States)
    afa.Sigma = copy(new.Sigma)
    afa.Initial = new.Initial
    afa.delta = copy(new.delta)
    afa.Final = copy(new.Final)
    afa.complete()
    return afa
