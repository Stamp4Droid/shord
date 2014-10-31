#!/usr/bin/env python

import FAdo.fa
import FAdo.reex
import string
import util

# TODO:
# remaining functionality:
# - parsing system of equations
# - converting to set of xforms
# - web interface (input form & CGI), with timeout setting
# - solving
# cleanup:
# - denote used constants (e.g. bottom)
# - special-case empty xforms? currently they don't have a normalized repr
# - add union of xforms with inccompatible domains as standard operation
# correctness:
# - (random) testing (may have bugs in writeup)
# - try constrcuted examples, then real systems
# - verify axioms, operator closure
# - ok to have empty languages in matches/results? still sound?
# - Xform.ID is the neutral element for composition
# performance:
# - do caching, uniquing
# - defunctionalize cases (profile first)
# - special-case operations for singleton languages?
# - return None to save time instead of returning an empty language?
# - subset operations on xforms, throwing out redundant cases early

# ===== USAGE =================================================================

# Be careful with precedence when using overloaded operators.
# Language.from_regex deviates from standard syntax: '+' means union.

# ===== LITERALS ==============================================================

# CAUTION: This set shouldn't include regex operators:
# '*', '+', '(', ')', '[', ']', '|'
ALPHABET = set(string.ascii_lowercase)

class AlphabetError(Exception):
    pass

class Language(util.Record):
    def __init__(self, m=None):
        if m is None:
            m = FAdo.fa.DFA()
            m.setInitial(m.addState())
        else:
            if not isinstance(m, FAdo.fa.DFA):
                m = m.toDFA()
            m = m.minimal(complete=False)
            m.renameStates(range(0, len(m)))
            if not m.Sigma <= ALPHABET:
                raise AlphabetError
        m.setSigma(ALPHABET) # required for DFA equality to function correctly
        self._dfa = m

    @staticmethod
    def singleton(seq):
        m = FAdo.fa.DFA()
        prev = m.addState()
        m.setInitial(prev)
        for c in seq.string:
            curr = m.addState()
            m.addTransition(prev, c, curr)
            prev = curr
        m.addFinal(prev)
        return Language(m)

    @staticmethod
    def from_regex(re):
        if re.strip() == '':
            # Special case that str2regexp can't handle
            return Language.singleton(Sequence(''))
        return Language(FAdo.reex.str2regexp(re).toDFA())

    def __add__(self, other):
        return self.concat(other)

    def concat(self, other):
        return Language(self._dfa.concat(other._dfa))

    def __and__(self, other):
        return self.disjunction(other)

    def disjunction(self, other):
        return Language(self._dfa & other._dfa)

    def __or__(self, other):
        return self.union(other)

    def union(self, other):
        return Language(self._dfa | other._dfa)

    def reverse(self):
        return Language(self._dfa.reversal())

    def empty(self):
        return self._dfa.witness() is None

    def __contains__(self, seq):
        return self.contains(seq)

    def contains(self, seq):
        return self._dfa.evalWordP(seq.string)

    def __le__(self, other):
        return self.subseteq(other)

    def subseteq(self, other):
        return (self._dfa & ~(other._dfa)).witness() is None

    def __div__(self, other):
        return self.rquot(other)

    def rquot(self, other):
        m = self._dfa.dup()
        initial = m.Initial
        finals = []
        for sidx in range(0, len(m)):
            m.setInitial(sidx)
            if (m & other._dfa).witness() is not None:
                finals.append(sidx)
        m.setInitial(initial)
        m.setFinal(finals)
        return Language(m)

    def lquot(self, other):
        m = self._dfa.toNFA()
        initials = []
        finals = m.Final
        for sidx in range(0, len(m)):
            m.setFinal([sidx])
            if (m & other._dfa).witness() is not None:
                initials.append(sidx)
        m.setInitial(initials)
        m.setFinal(finals)
        return Language(m)

    def rm_suffix(self, seq):
        m = self._dfa.dup()
        initial = m.Initial
        finals = []
        for sidx in range(0, len(m)):
            m.setInitial(sidx)
            if m.evalWordP(seq.string):
                finals.append(sidx)
        m.setInitial(initial)
        m.setFinal(finals)
        return Language(m)

    def star(self):
        return Language(self._dfa.star())

    def plus(self):
        return Language(self._dfa.star(True))

    def __str__(self):
        return '(%s)' % self._dfa.reCG()

class Sequence(util.Record):
    def __init__(self, string):
        if not set(string) <= ALPHABET:
            raise AlphabetError
        self.string = string

    def __add__(self, other):
        return self.concat(other)

    def concat(self, other):
        return Sequence(self.string + other.string)

    def reverse(self):
        return Sequence(self.string[::-1])

    def splits(self, ne_first=False):
        lim = len(self.string)
        for i in range(0, lim+1):
            if ne_first:
                ne_first = False
                continue
            yield (Sequence(self.string[:i]), Sequence(self.string[i:]))

    def rm_suffix(self, other):
        if self.string.endswith(other.string):
            return Sequence(self.string[:len(self.string)-len(other.string)])
        return None

    def endswith(self, other):
        return self.string.endswith(other.string)

    def __str__(self):
        return self.string

def parse_lang_pair(s):
    [c_str,o_str] = s.split('|')
    return (Language.from_regex(c_str).reverse(), Language.from_regex(o_str))

# ==== XFORM LHS ==============================================================

class MatchExact(util.Record):
    def __init__(self, seq):
        self.seq = seq

    # Returns the portion of 'l' that matches.
    def match(self, l):
        if self.seq in l:
            return Language.singleton(self.seq)
        return Language()

    def symb_match(self, rhs):
        res = []
        if isinstance(rhs, SetLang):
            if self.seq in rhs.lang:
                res.append((lambda x: x, lambda x: x))
        elif isinstance(rhs, AddSuffix):
            for (a,b) in self.seq.splits():
                if b not in rhs.suffix:
                    continue
                res.append((lambda x: x.specialize(a), lambda x: x))
        elif isinstance(rhs, Surround):
            for (a,b) in self.seq.splits():
                if a not in rhs.prefix:
                    continue
                for (c,d) in b.splits():
                    if d not in rhs.suffix:
                        continue
                    res.append((lambda x: x.specialize(c), lambda x: x))
        else:
            assert False
        return res

    def restrict(self, other):
        if isinstance(other, MatchExact):
            if self.seq == other.seq:
                return (self, lambda x: x)
        elif isinstance(other, MatchSuffix):
            if self.seq.endswith(other.suffix):
                return (self, lambda x: x)
        elif isinstance(other, MatchOneOf):
            if self.seq in other.lang:
                return (self, lambda x: x)
        else:
            assert False
        return None

    def limit(self, rhs):
        assert isinstance(rhs, SetLang)
        if self.seq in rhs.lang:
            return [(self, SetLang(Language.singleton(self.seq)))]
        return []

    def satisfiable(self):
        return True

    def upcast(self):
        return MatchOneOf(Language.singleton(self.seq))

    def to_string(self, reverse):
        return '%s' % (self.seq.reverse() if reverse else self.seq)

    def __str__(self):
        return self.to_string(False)

class MatchSuffix(util.Record):
    def __init__(self, suffix):
        self.suffix = suffix

    def specialize(self, c):
        if isinstance(c, Sequence):
            return MatchExact(c + self.suffix)
        elif isinstance(c, Language):
            return MatchOneOf(c + Language.singleton(self.suffix))
        else:
            assert False

    def grow_suffix(self, seq):
        return MatchSuffix(seq + self.suffix)

    def match(self, l):
        return l.rm_suffix(self.suffix)

    def symb_match(self, rhs):
        res = []
        if isinstance(rhs, SetLang):
            matched = rhs.lang.rm_suffix(self.suffix)
            if not matched.empty():
                res.append((lambda x: x, lambda x: x.specialize(matched)))
        elif isinstance(rhs, AddSuffix):
            matched = rhs.suffix.rm_suffix(self.suffix)
            if not matched.empty():
                res.append((lambda x: x, lambda x: x.grow_suffix(matched)))
            for (a,b) in self.suffix.splits(True):
                if b not in rhs.suffix:
                    continue
                res.append((lambda x: x.grow_suffix(a), lambda x: x))
        elif isinstance(rhs, Surround):
            matched = rhs.suffix.rm_suffix(self.suffix)
            if not matched.empty():
                res.append((lambda x: x,
                            lambda x: (x.grow_prefix(rhs.prefix)
                                       .grow_suffix(matched))))
            for (a,b) in self.suffix.splits(True):
                if b not in rhs.suffix:
                    continue
                res.append((lambda x: x.grow_suffix(a),
                            lambda x: x.grow_prefix(rhs.prefix)))
                for (c,d) in a.splits(True):
                    matched = rhs.prefix.rm_suffix(c)
                    if matched.empty():
                        continue
                    res.append((lambda x: x.specialize(d),
                                lambda x: x.specialize(matched)))
        else:
            assert False
        return res

    def restrict(self, other):
        if isinstance(other, MatchExact):
            rest = other.seq.rm_suffix(self.suffix)
            if rest is not None:
                return (other,
                        lambda x: x.specialize(Language.singleton(rest)))
        elif isinstance(other, MatchSuffix):
            rest = self.suffix.rm_suffix(other.suffix)
            if rest is not None:
                return (self, lambda x: x)
            rest = other.suffix.rm_suffix(self.suffix)
            if rest is not None:
                return (other,
                        lambda x: x.grow_suffix(Language.singleton(rest)))
        elif isinstance(other, MatchOneOf):
            matched = other.lang.rm_suffix(self.suffix)
            if not matched.empty():
                return (MatchOneOf(matched + Language.singleton(self.suffix)),
                        lambda x: x.specialize(matched))
        else:
            assert False
        return None

    def limit(self, rhs):
        res = []
        if isinstance(rhs, AddSuffix):
            matched = rhs.suffix.rm_suffix(self.suffix)
            if not matched.empty():
                lim_suffix = matched.star() + Language.singleton(self.suffix)
                res.append((self, AddSuffix(lim_suffix)))
        elif isinstance(rhs, Surround):
            matched = rhs.suffix.rm_suffix(self.suffix)
            if not matched.empty():
                lim_prefix = rhs.prefix.star()
                lim_suffix = matched.star() + Language.singleton(self.suffix)
                res.append((self, Surround(lim_prefix, lim_suffix)))
            for (t,k) in self.suffix.splits(True):
                if k not in rhs.suffix:
                    continue
                for (m,n) in t.splits(True):
                    n_mnplus = (Language.singleton(n) +
                                (Language.singleton(m) +
                                 Language.singleton(n)).plus())
                    n_mnplus_m = n_mnplus + Language.singleton(m)
                    if (rhs.prefix & n_mnplus_m).empty():
                        continue
                    n_mnplus_k = n_mnplus + Language.singleton(k)
                    res.append((MatchOneOf(n_mnplus_k), SetLang(n_mnplus_k)))
        else:
            assert False
        return res

    def satisfiable(self):
        return True

    def to_string(self, reverse):
        if reverse:
            return '%s_' % self.suffix.reverse()
        else:
            return '_%s' % self.suffix

    def __str__(self):
        return self.to_string(False)

class MatchOneOf(util.Record):
    def __init__(self, lang):
        self.lang = lang

    def match(self, l):
        return l & self.lang

    def symb_match(self, rhs):
        res = []
        if isinstance(rhs, SetLang):
            if not (self.lang & rhs.lang).empty():
                res.append((lambda x: x, lambda x: x))
        elif isinstance(rhs, AddSuffix):
            matched = self.lang / rhs.suffix
            if not matched.empty():
                res.append((lambda x: x.specialize(matched), lambda x: x))
        elif isinstance(rhs, Surround):
            matched = (self.lang / rhs.suffix).lquot(rhs.prefix)
            if not matched.empty():
                res.append((lambda x: x.specialize(matched), lambda x: x))
        else:
            assert False
        return res

    def restrict(self, other):
        if isinstance(other, MatchExact):
            if other.seq in self.lang:
                return (other, lambda x: x)
        elif isinstance(other, MatchSuffix):
            matched = self.lang.rm_suffix(other.suffix)
            if not matched.empty():
                return (MatchOneOf(matched + Language.singleton(other.suffix)),
                        lambda x: x)
        elif isinstance(other, MatchOneOf):
            common = self.lang & other.lang
            if not common.empty():
                return (MatchOneOf(common), lambda x: x)
        else:
            assert False
        return None

    def limit(self, rhs):
        assert isinstance(rhs, SetLang)
        if not (self.lang & rhs.lang).empty():
            return [(self, rhs)]
        return []

    def satisfiable(self):
        return not self.lang.empty()

    def to_string(self, reverse):
        return '%s' % (self.lang.reverse() if reverse else self.lang)

    def __str__(self):
        return self.to_string(False)

def parse_matcher(s, reverse):
    if len(s) > 0 and s[-1 if reverse else 0] == '_':
        s = s[:-1] if reverse else s[1:]
        seq = Sequence(s)
        return MatchSuffix(seq.reverse() if reverse else seq)
    try:
        seq = Sequence(s)
        return MatchExact(seq.reverse() if reverse else seq)
    except AlphabetError:
        lang = Language.from_regex(s)
        return MatchOneOf(lang.reverse() if reverse else lang)

# ==== XFORM RHS ==============================================================

class SetLang(util.Record):
    def __init__(self, lang):
        self.lang = lang

    def apply(self, l):
        if l.empty():
            return Language()
        return self.lang

    def __or__(self, other):
        return self.union(other)

    def union(self, other):
        assert isinstance(other, SetLang)
        return SetLang(self.lang | other.lang)

    def always_empty(self):
        return self.lang.empty()

    def to_string(self, reverse):
        return '%s' % (self.lang.reverse() if reverse else self.lang)

    def __str__(self):
        return self.to_string(False)

class AddSuffix(util.Record):
    def __init__(self, suffix):
        self.suffix = suffix

    def apply(self, lang):
        return lang + self.suffix

    def specialize(self, lang):
        return SetLang(lang + self.suffix)

    def grow_suffix(self, lang):
        return AddSuffix(lang + self.suffix)

    def grow_prefix(self, lang):
        return Surround(lang, self.suffix)

    def __or__(self, other):
        return self.union(other)

    def union(self, other):
        assert isinstance(other, AddSuffix)
        return AddSuffix(self.suffix | other.suffix)

    def always_empty(self):
        return False

    def upcast(self):
        return Surround(Language(), self.suffix)

    def to_string(self, reverse):
        if reverse:
            return '%s_' % self.suffix.reverse()
        else:
            return '_%s' % self.suffix

    def __str__(self):
        return self.to_string(False)

class Surround(util.Record):
    def __init__(self, prefix, suffix):
        self.prefix = prefix
        self.suffix = suffix

    def apply(self, lang):
        return self.prefix + lang + self.suffix

    def specialize(self, lang):
        return SetLang(self.prefix + lang + self.suffix)

    def grow_suffix(self, lang):
        return Surround(self.prefix, lang + self.suffix)

    def grow_prefix(self, lang):
        return Surround(self.prefix + lang, self.suffix)

    def __or__(self, other):
        return self.union(other)

    def union(self, other):
        assert isinstance(other, Surround)
        return Surround(self.prefix | other.prefix, self.suffix | other.suffix)

    def always_empty(self):
        return False

    def to_string(self, reverse):
        if reverse:
            return '%s_%s' % (self.suffix.reverse(), self.prefix.reverse())
        else:
            return '%s_%s' % (self.prefix, self.suffix)

    def __str__(self):
        return self.to_string(False)

def parse_result(s, reverse):
    toks = s.split('_')
    if len(toks) == 1:
        lang = Language.from_regex(toks[0])
        return SetLang(lang.reverse() if reverse else lang)
    elif len(toks) == 2:
        pre_str = toks[1 if reverse else 0]
        suf_str = toks[0 if reverse else 1]
        suf_lang = Language.from_regex(suf_str)
        if pre_str == '':
            return AddSuffix(suf_lang.reverse() if reverse else suf_lang)
        else:
            pre_lang = Language.from_regex(pre_str)
            return Surround(pre_lang.reverse() if reverse else pre_lang,
                            suf_lang.reverse() if reverse else suf_lang)
    else:
        assert False

# ==== XFORMS =================================================================

class Xform(util.Record):
    VALID_SIGS = {
        (MatchSuffix, MatchSuffix, AddSuffix, AddSuffix):
            lambda lc,lo,rc,ro: (lc,lo,rc,ro),
        (MatchExact,  MatchSuffix, SetLang,   Surround):
            lambda lc,lo,rc,ro: (lc,lo,rc,ro),
        (MatchSuffix, MatchExact,  Surround,  SetLang):
            lambda lc,lo,rc,ro: (lc,lo,rc,ro),
        (MatchOneOf,  MatchOneOf,  SetLang,   SetLang):
            lambda lc,lo,rc,ro: (lc,lo,rc,ro),
        # Can up-cast certain cases to valid signatures:
        (MatchExact,  MatchSuffix, SetLang,   AddSuffix):
            lambda lc,lo,rc,ro: (lc,lo,rc,ro.upcast()),
        (MatchSuffix, MatchExact,  AddSuffix,  SetLang):
            lambda lc,lo,rc,ro: (lc,lo,rc.upcast(),ro),
        (MatchExact,  MatchOneOf,  SetLang,   SetLang):
            lambda lc,lo,rc,ro: (lc.upcast(),lo,rc,ro),
        (MatchOneOf,  MatchExact,  SetLang,   SetLang):
            lambda lc,lo,rc,ro: (lc,lo.upcast(),rc,ro),
        (MatchExact,  MatchExact,  SetLang,   SetLang):
            lambda lc,lo,rc,ro: (lc.upcast(),lo.upcast(),rc,ro)}

    class SigError(Exception):
        pass

    def __init__(self, l_closes, l_opens, r_closes, r_opens):
        sig = (type(l_closes), type(l_opens), type(r_closes), type(r_opens))
        # TODO: Could compare types with 'is', or 'isinstance'.
        fix = Xform.VALID_SIGS.get(sig)
        if fix is None:
            raise Xform.SigError
        (self.l_closes,self.l_opens,self.r_closes,self.r_opens) = fix(l_closes,
                                                                      l_opens,
                                                                      r_closes,
                                                                      r_opens)

    def apply(self, lang):
        (c,o) = lang
        mc = self.l_closes.match(c)
        mo = self.l_opens.match(o)
        return (self.r_closes.apply(mc), self.r_opens.apply(mo))

    def __or__(self, other):
        return self.union(other)

    def union(self, other):
        assert (self.l_closes == other.l_closes and
                self.l_opens  == other.l_opens)
        return Xform(self.l_closes,
                     self.l_opens,
                     self.r_closes | other.r_closes,
                     self.r_opens | other.r_opens)

    def __div__(self, other):
        return self.restrict(other)

    def restrict(self, other):
        c_res = self.l_closes.restrict(other.l_closes)
        if c_res is None:
            return Xform.BOTTOM
        o_res = self.l_opens.restrict(other.l_opens)
        if o_res is None:
            return Xform.BOTTOM
        (lc,rc_act) = c_res
        (lo,ro_act) = o_res
        return Xform(lc, lo, rc_act(self.r_closes), ro_act(self.r_opens))

    def __rshift__(self, other):
        return self.compose(other)

    def compose(self, other):
        return [Xform(lc_act(self.l_closes), lo_act(self.l_opens),
                      rc_act(other.r_closes), ro_act(other.r_opens))
                for (lc_act,rc_act) in other.l_closes.symb_match(self.r_closes)
                for (lo_act,ro_act) in other.l_opens.symb_match(self.r_opens)]

    def limit(self):
        return [Xform(lc, lo, rc, ro)
                for (lc,rc) in self.l_closes.limit(self.r_closes)
                for (lo,ro) in self.l_opens.limit(self.r_opens)]

    def is_bottom(self):
        return (not self.l_closes.satisfiable() or
                not self.l_opens.satisfiable() or
                self.r_closes.always_empty() or
                self.r_opens.always_empty())

    @staticmethod
    def parse(s):
        s = s.replace(' ', '')
        [lhs_str,rhs_str] = s.split('->')
        [lc_str,lo_str] = lhs_str.split('|')
        [rc_str,ro_str] = rhs_str.split('|')
        return Xform(parse_matcher(lc_str, True), parse_matcher(lo_str, False),
                     parse_result(rc_str, True), parse_result(ro_str, False))

    def __str__(self):
        return '%s|%s -> %s|%s' % (self.l_closes.to_string(True),
                                   self.l_opens.to_string(False),
                                   self.r_closes.to_string(True),
                                   self.r_opens.to_string(False))

Xform.ID = Xform(MatchSuffix(Sequence('')),
                 MatchSuffix(Sequence('')),
                 AddSuffix(Language.singleton(Sequence(''))),
                 AddSuffix(Language.singleton(Sequence(''))))

Xform.BOTTOM = Xform(MatchOneOf(Language()), MatchOneOf(Language()),
                     SetLang(Language()), SetLang(Language()))

# ===== TESTS =================================================================

def test_sequence_splits():
    for (a,b) in Sequence('abcd').splits():
        print '*' + a.string + '*' + b.string + '*'
        for (c,d) in a.splits():
            print '  ' + '&' + c.string + ' ' + d.string + '&'
    for (a,b) in Sequence('abcd').splits(True):
        print '*' + a.string + '*' + b.string + '*'

def test_language_ops():
    print Language.from_regex('abbb*c').lquot(Language.from_regex('ab'))
    print Language.from_regex('abb*') <= Language.from_regex('abb*')

def test_xform_examples():
    t = {}
    t[0] = Xform(MatchExact(Sequence('abc')),
                 MatchSuffix(Sequence('d')),
                 SetLang(Language.from_regex('a*')),
                 Surround(Language.from_regex('ef*'),
                          Language.from_regex('dd')))
    t[1] = Xform.parse('cba|_d -> a*|ef*_dd')
    assert t[0] == t[1]
    t[2] = Xform.parse('(@epsilon)|(@epsilon) -> (@epsilon)|(@epsilon)')
    t[3] = Xform.parse('| -> |')
    t[4] = Xform.parse('_|_ -> _|_d')
    for k in t:
        print t[k]
    return t

def test_xform_ops(t):
    res = t[0].apply((Language.from_regex('a*b*c'), Language.from_regex('kd')))
    print res[0]
    print res[1]
    print '====='
    print t[4] / t[3]
    print '====='
    for x in t[3] >> t[4]:
        print x
    print '====='
    for x in t[4] >> t[3]:
        print x

