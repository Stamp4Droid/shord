# -*- coding: utf-8 -*-
""" Witness language families for operations on DFAs

@author: Rogério Reis & Nelma Moreira

This is part of U{FAdo project <http://www.ncc.up.pt/FAdo>}.

Deterministic and non-deterministic automata manipulation, conversion and
evaluation.

@copyright: 1999-2012 Rogério Reis & Nelma Moreira {rvr,nam}@ncc.up.pt

Contributions by
 - Marco Almeida
 - Hugo Gouveia 
 - Davide Nabais
 - Eva Maia

B{Naming convention:} methods suffixed by P have boolean return.

This program is free software; you can redistribute it and/or modify
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

from FAdo.fa import *
from FAdo.comboperations import *

### Worst case automata for each operation

witnessDFA = {"toDFA": [("toDFAWCMF","int"),
                ("toDFAWC2","int"),
                ("toDFAWC3","int")
                ],
      "reversal": [("reversalWC3M","int"), ("reversalMB","int"), ("reversalWC3L","int"), ("reversalternaryWC","int"),("reversalbinaryWC","int")],
      "star": [("starWC","int"),("starWCM","int")],
      "concat": [("concatWC","int","int"),("concatWCM","int", "int")],
      "conjunction": [("interWC","int","int")],
      "__or__": [("disjWC","int","int")],
      "shuffle":[("shuffleWC","int","int")], 
      "starDisj":[("starDisjWC","int","int")],          
      "starInter":[("starInterBC","int","int")],          
      "disjWStar":[("disjWStarWC","int","int")]         
}

def toDFAWC2MF(m=5):
    """ Worst case automata for toDFA(NFA) with n > 2, k=2
    @see: A. R. Meyer and M. J. Fischer. Economy of description by automata,
    grammars, and formal systems. Twelfth Annual Symposium on
    Switching and Automata Theory, 1971,  188–191. IEEE Society Press.
    :arg m: number of states
    :type m: integer
    :returns: a dfa
    :rtype: DFA"""

    if m < 3:
        raise TestsError("number of states must be greater than 2")
    f = NFA()
    f.setSigma(["a","b"])
    f.States = range(m)
    f.setInitial([0])
    f.addFinal(0)
    f.addTransition(0,"a",1)
    for i in range(1,m):
        f.addTransition(i,"a",(i+1)%m)
        f.addTransition(i,"b",i)
        f.addTransition(i,"b",0)
    return f

def toDFAWC2(m=5):
    """ Worst case automata for toDFA(NFA) with n > 2, k=2
    @see: F.R. Moore. On the bounds for state-set size in the proofs
    of equivalence between deterministic, nondeterministic, and
    two-way finite automata. IEEE Transactions on computers, 2:1211–1214, 1971.

    :arg m: number of states
    :type m: integer
    :returns: a dfa
    :rtype: DFA"""
    
    if m < 3:
        raise TestsError("number of states must be greater than 2")
    f = NFA()
    f.setSigma(["a","b"])
    f.States = range(m)
    f.setInitial([0])
    f.addFinal(m-1)
    f.addTransition(0,"a",1)
    f.addTransition(0,"b",0)
    f.addTransition(m-1,"a",0)
    f.addTransition(m-1,"a",1)
    for i in range(1,m-1):
        f.addTransition(i,"a",i+1)
        f.addTransition(i,"b",i+1)
    return f

def toDFAWC3(m=5):
    """ Worst case automata for toDFA(NFA) with n > 2, k=3.
    @see: O. B. Lupanov. A comparison of two types of finite sources.
    Problemy Kibernetiki, 9:321–326, 1963.
    
    :arg m: number of states
    :type m: integer
    :returns: a dfa
    :rtype: DFA"""
 
    if m < 3:
       raise TestsError("number of states must be greater than 2")
    f = NFA()
    f.setSigma(["a","b","c"])
    f.States = range(m)
    f.setInitial([0])
    f.addFinal(0)
    f.addTransition(0,"a",1)
    f.addTransition(0,"b",1)
    f.addTransition(1,"b",0)
    f.addTransition(1,"c",0)
    f.addTransition(1,"c",1)
    f.addTransition(1,"a",2)
    f.addTransition(m-1,"a",0)
    f.addTransition(m-1,"b",n-1)
    f.addTransition(m-1,"c",n-1)
    for i in range(2,m-1):
        f.addTransition(i,"a",i+1)
        f.addTransition(i,"b",i)
        f.addTransition(i,"c",i)
    return f

def reversalWC3M(m=5):
    """ Worst case automata for reversal(DFA) with m > 2, k=3.

    @see: Boris G. Mirkin. On dual automata. Kibernetika, 2:7–10, 1966.
   
    :arg m: number of states
    :type m: integer
    :returns: a dfa
    :rtype: DFA"""


    if m < 3:
        raise TestsError("number of states  must be greater than 2")
    return toDFAWC3(m).reversal()


def starSC(m=5):
    """ Worst case state complexity for star
    :arg m: number of states
    :type m: integer
    :returns: state complexity
    :rtype: integer"""

    if m>1:
        return 3*2**(m-2)
    return 1

def starWC(m=5):
    """ Worst case automata for star(DFA) with m > 2, k=2
    @see: S. Yu, Q. Zhuang, and K. Salomaa. The state complexities
    of some basic operations on regular languages.
    Theor. Comput. Sci., 125(2):315–328, 1994.

    :arg m: number of states
    :type m: integer
    :returns: a dfa
    :rtype: DFA"""

    if m < 3:
        raise TestsError("number of states must be greater than 2")
        # for m=2, L=\{w\in\{a,b\}*| |w|a odd \}
    f = DFA()
    f.setSigma(["a","b"])
    f.States = range(m)
    f.setInitial(0)
    f.addFinal(m-1)
    f.addTransition(0,"a",1)
    f.addTransition(0,"b",0)
    for i in range(1,m):
        f.addTransition(i,"a",(i+1)%m)
        f.addTransition(i,"b",(i+1)%m)
    return f


def starWCM(m=5):
    """ Worst case automata for star(DFA) with m > 2, k=2

    @see: A. N. Maslov. Estimates of the number of states of
    finite automata. Dokllady Akademii Nauk SSSR, 194:1266–1268, 1970. 
    
    :arg m: number of states
    :type m: integer
    :returns: a dfa
    :rtype: DFA"""   


    if m < 3:
        raise TestsError("number of states must be greater than 2")
    f = DFA()
    f.setSigma(["a","b"])
    f.States = range(m)
    f.setInitial(0)
    f.addFinal(m-1)
    f.addTransition(m-1,"a",0)
    f.addTransition(m-1,"b",m-2)
    f.addTransition(0,"b",0)
    f.addTransition(0,"a",1)
    for i in range(1,m-1):
        f.addTransition(i,"a",(i+1))
        f.addTransition(i,"b",(i-1))
    return f    
                        
def concatSC(m,n,k=1):
    """Worst case state complecity for concatenation
    :arg m: number of states
    :arg n: number of states
    :arg k: number of letters
    :type m: integer
    :type n: integer
    :type k: integer
    :returns: state compelxity
    :rtype: integer"""   

    return m*2**n-k*2**(n-1)



def concatWCM(m=4,n=4):
    """ Worst case automata for catenation(DFA,DFA) with m,n > 1, k=2,

    @see: A. N. Maslov. Estimates of the number of states of
    finite automata. Dokllady Akademii Nauk SSSR, 194:1266–1268, 1970. 
    :arg m: number of states
    :arg n: number of states
    :type m: integer
    :type n: integer
    :returns: two dfas 
    :rtype: (DFA, DFA)"""
    

    if n < 2 or m < 2:
        raise TestsError("number of states must be both greater than 1")
    d1,d2 = DFA(), DFA()
    d1.setSigma(["a","b"])
    d1.States=range(m)
    d1.setInitial(0)
    d1.addFinal(m-1)
    d1.addTransition(m-1,"b",0)
    d1.addTransition(m-1,"a",m-1)
    for i in range(m-1):
        d1.addTransition(i,"a",i)
        d1.addTransition(i,"b",i+1)
    d2.setSigma(["a","b"])
    d2.States=range(n)
    d2.setInitial(0)
    d2.addFinal(n-1)
    d2.addTransition(n-1,"a",n-1)
    d2.addTransition(n-1,"b",n-2)
    d2.addTransition(n-2,"b",n-1)
    d2.addTransition(n-2,"a",n-1)
    for i in range(n-2):
        d2.addTransition(i,"a",i+1)
        d2.addTransition(i,"b",i)
            
    return d1,d2    

def concatWC(m=6,n=6):
    """ Worst case automata for catenation(DFA,DFA) with m,n > 1
    @see: S. Yu, Q. Zhuang, and K. Salomaa. The state complexities
    of some basic operations on regular languages.
    Theor. Comput. Sci., 125(2):315–328, 1994.
    :arg m: number of states
    :arg n: number of states
    :type m: integer
    :type n: integer
    :returns: two dfas 
    :rtype: (DFA, DFA)"""
    if n < 2 or m < 2:
        raise TestsError("number of states must both  greater than 1")
    d1,d2 = DFA(), DFA()
    d1.setSigma(["a","b","c"])
    d1.States=range(m)
    d1.setInitial(0)
    d1.addFinal(m-1)
    for i in range(m):
        d1.addTransition(i,"a",(i+1)%m)
        d1.addTransition(i,"b",0)
        d1.addTransition(i,"c",i)
    d2.setSigma(["a","b","c"])
    d2.States=range(n)
    d2.setInitial(0)
    d2.addFinal(n-1)
    for i in range(n):
        d2.addTransition(i,"b",(i+1)%n)
        d2.addTransition(i,"a",i)
        d2.addTransition(i,"c",1)
    return d1, d2    

def interWC(m=6,n=5):
    """ Worst case automata for intersection(DFA,DFA) with m,n >1

    @see: S. Yu, Q. Zhuang, and K. Salomaa. The state complexities
    of some basic operations on regular languages.
    Theor. Comput. Sci., 125(2):315–328, 1994.
    :arg m: number of states
    :arg n: number of states
    :type m: integer
    :type n: integer
    :returns: two dfas 
    :rtype: (DFA, DFA)"""
    if n < 2 or m < 2:
        raise TestsError("number of states must be both greater than 1")
    d1,d2 = DFA(), DFA()
    d1.setSigma(["a","b"])
    d1.States=range(m)
    d1.setInitial(0)
    d1.addFinal(0)
    for i in range(m):
        d1.addTransition(i,"a",(i+1)%m)
        d1.addTransition(i,"b",i)
    d2.setSigma(["a","b"])
    d2.States=range(m)
    d2.setInitial(0)
    d2.addFinal(0)
    for i in range(n):
        d2.addTransition(i,"b",(i+1)%n)
        d2.addTransition(i,"a",i)
    return d1,d2

def disjWC(m=6,n=5):
    """ Worst case automata for disjunction(DFA,DFA) with m,n >1
    @see: S. Yu, Q. Zhuang, and K. Salomaa. The state complexities
    of some basic operations on regular languages.
    Theor. Comput. Sci., 125(2):315–328, 1994.
    :arg m: number of states
    :arg n: number of states
    :type m: integer
    :type n: integer
    :returns: two dfas 
    :rtype: (DFA, DFA)"""


    if n < 2 or m < 2:
        raise TestsError("number of states must be both greater than 1")
    d1,d2 = DFA(), DFA()
    d1.setSigma(["a","b"])
    d1.States=range(m)
    d1.setInitial(0)
    d1.addTransition(0,"a",1)
    d1.addTransition(0,"b",0)
    for i in range(1,m):
        d1.addTransition(i,"a",(i+1)%m)
        d1.addTransition(i,"b",i)
        d1.addFinal(i)
    d2.setSigma(["a","b"])
    d2.States=range(m)
    d2.setInitial(0)
    d2.addTransition(0,"b",1)
    d2.addTransition(0,"a",0)
    for i in range(n):
        d2.addTransition(i,"b",(i+1)%n)
        d2.addTransition(i,"a",i)
    d2.addFinal(0)
    return d1,d2

def reversalMB(m=8):
    """Worst case automata for reversal(DFA)

    @see: S. Yu, Q. Zhuang, and K. Salomaa. The state complexities
    of some basic operations on regular languages.
    Theor. Comput. Sci., 125(2):315–328, 1994.
    :arg m: number of states
    :type m: integer
    :returns: a dfa
    :rtype: DFA"""
    if m < 3:
        raise TestsError("number of states must be greater than 2")
    d = DFA()
    d.setSigma(["a","b"])
    d.States = range(m)
    d.setInitial(0)
    for i in range(m):
        if i==m-1:
            d.addTransition(m-1,"a",0)
        else:
            d.addTransition(i,"a",i+1)
        if i == 2:
            d.addTransition(2,"b",0)
        elif i == 3:    
            d.addTransition(3,"b",2)
        else:
            d.addTransition(i,"b",i)
    return d  

def reversalWC3L(m=5):
    """ Worst case automata for reversal(DFA) with m > 2, k=3

    @see: E. L. Leiss. Succinct representation of regular languages
        by boolean automata ii. Theor. Comput. Sci., 38:133–136, 1985.
    :arg m: number of states
    :type m: integer
    :returns: a dfa
    :rtype: DFA"""

    if m < 3:
        raise TestsError("number of states must be greater than 2")
    f = DFA()
    f.setSigma(["a","b","c"])
    f.States = range(m)
    f.setInitial(0)
    f.addFinal(0)
    f.addTransition(0,"b",1)
    f.addTransition(1,"b",0)
    f.addTransition(0,"a",1)
    f.addTransition(1,"a",2)
    f.addTransition(0,"c",m-1)
    f.addTransition(1,"c",1)
    for i in range(2,m):
        f.addTransition(i,"a",(i+1)%m)
        f.addTransition(i,"b",i)
        f.addTransition(i,"c",i)
    return f

def reversalternaryWC(m=5):
    """Worst case automata for reversal(DFA) ternary alphabet
       
        :arg m: number of states
        :type m: integer
        :returns: a dfa
        :rtype: DFA"""
    if m<3:
        raise TestsError("number of states must be greater than 2")
    d=DFA()
    d.setSigma(["a","b","c"])
    d.setInitial(0)
    d.addFinal(0)
    d.States=range(m)
    d.addTransition(0,"a",m-1)
    d.addTransition(0,"c",0)
    d.addTransition(0,"b",0)
    d.addTransition(1,"c",m-1)
    d.addTransition(1,"b",0)
    d.addTransition(1,"a",0)
    for i in range(2,m):
        d.addTransition(i,"a",i-1)
        d.addTransition(i,"c",i-1)
        d.addTransition(i,"b",i)
    return d     


def reversalbinaryWC(m=5):
    """Worst case automata for reversal(DFA) binary
    @see: G. Jir{\'a}skov{\'a} and J. S\v ebej. Note on Reversal of binary regular languages. Proc. DCFS 2011, 
    LNCS 6808, Springer, pp 212-221.
    @arg m: number of states
    @type m: integer
    @returns: a dfa
    @rtype: DFA"""

    if m<2:
        raise TestsError("number of states must be greater than 1")
    d=DFA()
    d.setSigma(["a","b"])
    d.States=range(m)
    d.setInitial(0)
    d.addFinal(m-1)
    d.addTransition(0,"a",1)
    d.addTransition(0,"b",0)
    d.addTransition(1,"b",0)
    if m == 2: 
            d.addTransition(1,"a",0)
    else:        
        d.addTransition(1,"a",2)
        d.addTransition(2,"a",0)
        if m == 3:
            d.addTransition(2,"b",2)
        else:    
            d.addTransition(2,"b",3)
            d.addTransition(3,"b",2)
            d.addTransition(3,"a",4)
            d.addTransition(m-1,"a",3)
            d.addTransition(m-1,"b",m-1)
            for i in range(4,m-1):
                d.addTransition(i,"a",i+1)
                d.addTransition(i,"b",i)
    return d


def shuffleWC(m=3,n=3):
    """Worst case automata for shuffle(DFA,DFA) with m.n>1
    @see:C. Campeanu, K. Salomaa, and S. Yu. Tight lower bound for
    the state complexity of shuffle of regular languages.
    Journal of Automata, Languages and Combinatorics, 7(3):303–310, 2002.
    :arg m: number of states
    :arg n: number of states
    :type m: integer
    :type n: integer
    :returns: two dfas 
    :rtype: (DFA, DFA)"""
    if n < 2 or m < 2:
        raise TestsError("number of states must be both greater than 1")
    d1,d2 = DFA(), DFA()
    d1.States = range(m)
    d1.setSigma(["a","b","c","d","f"])
    d1.setInitial(0)
    d1.addFinal(0)
    for i in range(m):
        d1.addTransition(i,"a",(i+1)%m)
        if i != m-1:
            d1.addTransition(i,"c",i+1)
        d1.addTransition(i,"d",i)
        if i !=0:
            d1.addTransition(i,"f",i)
    d2.States = range(n)
    d2.setSigma(["a","b","c","d","f"])
    d2.setInitial(0)
    d2.addFinal(0)
    for i in range(n):
        d2.addTransition(i,"b",(i+1)%n)
        d2.addTransition(i,"c",i)
        if i != n-1:
                d2.addTransition(i,"d",i+1)
        if i !=0:
            d2.addTransition(i,"f",i)
    return d1,d2

def starDisjWC(m=6,n=5):
    """Worst case automata for starDisj(DFA,DFA) with m.n>1
     @see: Arto Salomaa, Kai Salomaa, and Sheng Yu. 'State complexity of
    combined operations'. Theor. Comput. Sci., 383(2-3):140–152, 2007.
    :arg m: number of states
    :arg n: number of states
    :type m: integer
    :type n: integer
    :returns: two dfas 
    :rtype: (DFA,DFA)"""

    if n < 2 or m < 2:
        raise TestsError("number of states must be both greater than 1")
    d1,d2 = DFA(), DFA()
    d1.States = range(m)
    d1.setSigma(["a","b","c"])
    d1.setInitial(0)
    d1.addFinal(0)
    for i in range(m):
        d1.addTransition(i,"a",(i+1)%m)
        d1.addTransition(i,"b",i)
        if i != 0:
            d1.addTransition(i,"c",i)
    d1.addTransition(0,"c",1)
    d2.States = range(n)
    d2.setSigma(["a","b","c"])
    d2.setInitial(0)
    d2.addFinal(0)
    for i in range(n):
        d2.addTransition(i,"b",(i+1)%n)
        d2.addTransition(i,"a",i)
        if i != 0:
            d2.addTransition(i,"c",i)
    d2.addTransition(0,"c",1)
    return d1,d2

def starInterBC(m=3,n=3):
    """Bad case automata for starInter(DFA,DFA) with m,n>1
    @see: Arto Salomaa, Kai Salomaa, and Sheng Yu. 'State complexity of
    combined operations'. Theor. Comput. Sci., 383(2-3):140–152, 2007.
    :arg m: number of states
    :arg n: number of states
    :type m: integer
    :type n: integer
    :returns: two dfas 
    :rtype: (DFA,DFA)"""

    if n < 2 or m < 2:
        raise TestsError("number of states must be both greater than 1")
    d1,d2 = DFA(),DFA()
    d1.setSigma(["a","b","c","d","e"])
    d1.States = range(m)
    d1.setInitial(0)
    d1.addFinal(m-1)
    for i in range(m):
       d1.addTransition(i,"a",(i+1)%m)
       d1.addTransition(i,"b",i)
       d1.addTransition(i,"c",i)
       d1.addTransition(i,"d",i)
       d1.addTransition(i,"e",i)
    d2.setSigma(["a","b","c","d","e"])
    d2.States = range(n)          
    d2.setInitial(0)
    d2.addFinal(n-1)
    for i in range(n):
       d2.addTransition(i,"b",(i+1)%n)
       d2.addTransition(i,"a",i)
       d2.addTransition(i,"c",n-2)
       if i == n-2: d2.addTransition(i,"d",n-1)
       elif i == n-1: d2.addTransition(i,"d",n-2)
       else:
           d2.addTransition(i,"d",i)
       if i> n-4: d2.addTransition(i,"e",i)
       else:
           d2.addTransition(i,"e",i+1)
    return d1,d2

def disjWStarWC(m=6,n=5):
    """
     @see: Yuan Gao and Sheng Yu. 'State complexity of union and intersection
  combined with star and reversal'. CoRR, abs/1006.3755, 2010.
  :arg m: number of states
  :arg n: number of states
  :type m: integer
  :type n: integer
  :returns: two dfas 
  :rtype: (DFA,DFA)"""

    if n < 3 or m < 3:
      raise TestsError("number of states must be greater than 2")
    f1 = DFA()
    f1.setSigma(["a","b","c"])
    f1.States = range(m)
    f1.setInitial(0)
    f1.addFinal(m-1)
    f1.addTransition(0,"a",1)
    f1.addTransition(0,"b",0)
    f1.addTransition(0,"c",0)
    for i in range(1,m):
        f1.addTransition(i,"a",(i+1)%m)
        f1.addTransition(i,"b",(i+1)%m)
        f1.addTransition(i,"c",i)
    f2 = DFA()
    f2.setSigma(["a","b","c"])
    f2.States = range(n)
    f2.setInitial(0)
    f2.addFinal(n-1)
    for i in range(n):
        f2.addTransition(i,"a",i)
        f2.addTransition(i,"b",i)
        f2.addTransition(i,"c",(i+1)%n)
    return f1,f2


### worst cases for transition complexity


#######     UNION   ######

def unionWCTk2(m=6,n=6):
    """ @ worst-case family union where
    @m>=2 and n>=2 and k=2
    @see: Gao, Y., Salomaa, K., Yu, S.: Transition complexity of
    incomplete dfas. Fundam. Inform.  110(1-4), 143–158 (2011)
    @ the conjecture in this article fails for this family
    :arg m: number of states
    :arg n: number of states
    :type m: integer
    :type n: integer
    :returns: two dfas 
    :rtype: (DFA,DFA)"""

    if n < 2 or m < 2:
        raise TestsError("number of states must both  greater than 1")
    d1,d2 = DFA(), DFA()
    d1.setSigma(["a","b"])
    d1.States=range(m)
    d1.setInitial(0)
    d1.addFinal(0)
    d1.addTransition(m-1, "a", 0)
    for i in range(0,m-1):
        d1.addTransition(i,"b",i+1)
    d2.setSigma(["a","b"])
    d2.States=range(n)
    d2.setInitial(0)
    d2.addFinal(n-1)
    d2.addTransition(n-1, "b", n-1)
    for i in range(0,n-1):
        d2.addTransition(i,"a",i+1)
        d2.addTransition(i,"b",i)
    return d1, d2    



def unionWCT2(n=6):
    """ @ worst-case family union where
    @m=1 and n>=2 and k=3
    @ Note that the same happens to m>=2 and n=1
    :arg n: number of states
    :type n: integer
    :returns: two dfas 
    :rtype: (DFA,DFA)"""
    m=1
    if n < 2:
        raise TestsError("number of states must both  greater than 1")
    d1,d2 = DFA(), DFA()
    d1.setSigma(["a","b","c"])
    d1.States=range(m)
    d1.setInitial(0)
    d1.addFinal(0)
    d1.addTransition(0, "b", 0)
    d1.addTransition(0, "c", 0)

    d2.setSigma(["a","b","c"])
    d2.States=range(n)
    d2.setInitial(0)
    d2.addFinal(n-1)
    d2.addTransition(0, "a", 0)
    d2.addTransition(0, "b", 1)
    for i in range(1,n):
        d2.addTransition(i,"b",(i+1)%n)
        d2.addTransition(i,"a",i)
        d2.addTransition(i,"c",1)
    return d1, d2    




def unionWCT(m=6,n=6):
    """ @ worst-case family union where
    @m>=2 and n>=2 and k=3
    :arg m: number of states
    :arg n: number of states
    :type m: integer
    :type n: integer
    :returns: two dfas 
    :rtype: (DFA,DFA)"""

    if n < 2 or m < 2:
        raise TestsError("number of states must both  greater than 1")
    d1,d2 = DFA(), DFA()
    d1.setSigma(["a","b","c"])
    d1.States=range(m)
    d1.setInitial(0)
    d1.addFinal(m-1)
    d1.addTransition(0, "a", 1)
    d1.addTransition(0, "c", 0)
    for i in range(1,m):
        d1.addTransition(i,"a",(i+1)%m)
        d1.addTransition(i,"b",0)
        d1.addTransition(i,"c",i)
    d2.setSigma(["a","b","c"])
    d2.States=range(n)
    d2.setInitial(0)
    d2.addFinal(n-1)
    d2.addTransition(0, "a", 0)
    d2.addTransition(0, "b", 1)
    for i in range(1,n):
        d2.addTransition(i,"b",(i+1)%n)
        d2.addTransition(i,"a",i)
        d2.addTransition(i,"c",1)
    return d1, d2  
    

### CONCAT 

def concatWCT2(n=6):
    """ @ worst-case family concatenation where
    @m=1 and n>=2 and k=3
    :arg n: number of states
    :type n: integer
    :returns: two dfas 
    :rtype: (DFA,DFA)"""
    m=1
    if n < 2:
        raise TestsError("number of states must both  greater than 1")
    d1,d2 = DFA(), DFA()
    d1.setSigma(["a","b","c"])
    d1.States=range(m)
    d1.setInitial(0)
    d1.addFinal(0)
    d1.addTransition(0, "b", 0)
    d1.addTransition(0, "c", 0)

    d2.setSigma(["a","b","c"])
    d2.States=range(n)
    d2.setInitial(0)
    d2.addFinal(n-1)
    d2.addTransition(0, "a", 0)
    d2.addTransition(0, "b", 1)
    for i in range(1,n):
        d2.addTransition(i,"b",(i+1)%n)
        d2.addTransition(i,"a",i)
        d2.addTransition(i,"c",(i+1)%n)
    return d1, d2    




def concatWCT3(m=6):
    """ @ worst-case family concatenation where
    @m>=2 and n=1 and k=3
    :arg m: number of states
    :type m: integer
    :returns: two dfas 
    :rtype: (DFA,DFA)"""
    n=1
    if m < 2:
        raise TestsError("number of states must both  greater than 1")
    d1,d2 = DFA(), DFA()
    d1.setSigma(["a","b","c"])
    d1.States=range(m)
    d1.setInitial(0)
    d1.addFinal(m-1)
    d1.addTransition(0, "a", 0)
    d1.addTransition(0, "b", 1)
    d1.addTransition(0, "c", 1)
    d1.addTransition(1, "a", 1)
    d1.addTransition(1, "b", 2)
    for i in range(2,m):
        d1.addTransition(i,"b",(i+1)%m)
        d1.addTransition(i,"c",(i+1)%m)
        d1.addTransition(i,"a",i)
    d2.setSigma(["a","b","c"])
    d2.States=range(n)
    d2.setInitial(0)
    d2.addFinal(0)
    d2.addTransition(0, "c", 0)
    d2.addTransition(0, "b", 0)

    return d1, d2    



def concatWCT(m=6,n=6):
    """ @ worst-case family concatenation where
    @m>=2 and n>=2 and k=3
    :arg m: number of states
    :arg n: number of states
    :type m: integer
    :type n: integer
    :returns: two dfas 
    :rtype: (DFA,DFA)"""
    
    if n < 2 or m < 2:
        raise TestsError("number of states must both  greater than 1")
    d1,d2 = DFA(), DFA()
    d1.setSigma(["a","b","c"])
    d1.States=range(m)
    d1.setInitial(0)
    d1.addFinal(m-1)
    d1.addTransition(0, "a", 1)
    d1.addTransition(0, "c", 0)
    for i in range(1,m):
        d1.addTransition(i,"a",(i+1)%m)
        d1.addTransition(i,"b",0)
        d1.addTransition(i,"c",i)
    d2.setSigma(["a","b","c"])
    d2.States=range(n)
    d2.setInitial(0)
    d2.addFinal(n-1)
    d2.addTransition(0, "a", 0)
    d2.addTransition(0, "b", 1)
    for i in range(1,n):
        d2.addTransition(i,"b",(i+1)%n)
        d2.addTransition(i,"a",i)
        d2.addTransition(i,"c",1)
    return d1, d2    


#####  Star

def starWCT(m=5):
    """ @ worst-case family star where
    @m>=2 and k=2
    :arg m: number of states
    :type m: integer
    :returns: dfa 
    :rtype: DFA"""
    if m < 3:
        raise TestsError("number of states must be greater than 2")
    f = DFA()
    f.setSigma(["a","b"])
    f.States = range(m)
    f.setInitial(0)
    f.addFinal(m-1)
    f.addTransition(0,"a",1)
    for i in range(1,m):
        f.addTransition(i,"a",(i+1)%m)
        f.addTransition(i,"b",(i+1)%m)
    return f

def starWCT1(m=5):
    """ @ worst-case family star where
    @m>=2 and k=2
    :arg m: number of states
    :type m: integer
    :returns: dfa 
    :rtype: DFA"""
    if m < 3:
        raise TestsError("number of states must be greater than 2")
    f = DFA()
    f.setSigma(["a","b"])
    f.States = range(m)
    f.setInitial(0)
    f.addFinal(m-1)
    f.addTransition(0,"b",0)
    f.addTransition(0,"a",1)
    f.addTransition(m-2,"a",m-1)
    f.addTransition(m-1,"a",0)
    for i in range(1,m-2):
        f.addTransition(i,"a",(i+1)%m)
        f.addTransition(i,"b",(i+1)%m)
    return f
