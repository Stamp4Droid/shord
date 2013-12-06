# -*- coding: utf-8 -*-
"""**Random DFA generation**

ICDFA Random generation binding

.. *Authors:* Rogério Reis & Nelma Moreira

.. *This is part of FAdo project*  http://fado.dcc.fc.up.pt

.. *Version:* 0.9.5

.. *Copyright:* 1999-2012 Rogério Reis & Nelma Moreira {rvr,nam}@dcc.fc.up.pt

.. versionchanged:: 0.9.4 Interface python to the C code

.. Contributions by
  - Marco Almeida

..  This program is free software; you can redistribute it and/or modify
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

import generator
import fa

class ICDFArnd(object):
  """ICDFA Random Generator class

  This is the class for the uniform random generator for Initially Connected DFAs

  :var n: number of states
  :type n: integer
  :var k: size of the alphabet
  :type k: integer

  .. seealso:: Marco Almeida, Nelma Moreira, and Rogério Reis. Enumeration and generation with a string automata
     representation. Theoretical Computer Science, 387(2):93-102, 2007

  .. versionchanged:: 0.9.6 Working with incomplete automata"""
  def __init__(self,n,k,complete=True):
    """
    :param n: number of states
    :type n: integer
    :param k: size of alphabet
    :type k: integer
    :param complete: sould ICDFAs be complete
    :type comple: Boolean"""
    i = 0 if complete else 1
    self.gen = generator.icdfaRndGen(n,k,i)
    self.n, self.k = n, k

  def __str__(self):
    return "ICDFArnd %d %d"%(self.n,self.k)

  def next(self):
    """
    :returns: a random generated ICDFA
    :rtype: DFA"""
    a = self.gen.next()
    return fa.stringToDFA(a[0],a[1],self.n,self.k)
