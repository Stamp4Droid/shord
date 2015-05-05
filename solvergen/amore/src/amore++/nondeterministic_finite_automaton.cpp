/* $Id: nondeterministic_finite_automaton.cpp 1481 2011-04-08 15:07:26Z neider $
 * vim: fdm=marker
 *
 * This file is part of libAMoRE++
 *
 * libAMoRE++ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * libAMoRE++ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with libAMoRE++.  If not, see <http://www.gnu.org/licenses/>.
 *
 * (c) 2008,2009 Lehrstuhl Softwaremodellierung und Verifikation (I2), RWTH Aachen University
 *           and Lehrstuhl Logik und Theorie diskreter Systeme (I7), RWTH Aachen University
 * Author: David R. Piegdon <david-i2@piegdon.de>
 *
 */

#include <string>
#include <queue>
#include <stdio.h>
#include <set>

#include <ostream>
#include <fstream>

// for htonl / ntohl
#ifdef _WIN32
# include <winsock.h>
#else
# include <arpa/inet.h>
#endif

# define LIBAMORE_LIBRARY_COMPILATION
# include "amore++/nondeterministic_finite_automaton.h"
# include "amore++/deterministic_finite_automaton.h"

# include <amore/nfa.h>
# include <amore/dfa.h>
# include <amore/nfa2dfa.h>
# include <amore/dfa2nfa.h>
# include <amore/dfamdfa.h>
# include <amore/testBinary.h>
# include <amore/unaryB.h>
# include <amore/binary.h>
# include <amore/rexFromString.h>
# include <amore/rex2nfa.h>
# include <amore/nfa2rex.h>
# include <amore/nfa2mnfa.h>

namespace amore {

using namespace std;

// implementation notes:
//
// libAMoRE is using '0' as epsilon, thus in amore, he alphabet is [1 .. size]
// and not [0 .. size-1]
//
// libalf uses (in construct) -1 to indicate an epsilon transition and
// uses [0 .. size-1] as the alphabet.


nondeterministic_finite_automaton::nondeterministic_finite_automaton()
{{{
	nfa_p = NULL;
}}}

nondeterministic_finite_automaton::nondeterministic_finite_automaton(nfa a)
{{{
	nfa_p = a;
}}}

static void amore_insanitize_regex(char* regex)
{{{
	while(*regex) {
		switch(*regex) {
			case '|':
				*regex = 'U';
				break;
			default:
				break;
		}
		regex++;
	}
}}}

nondeterministic_finite_automaton::nondeterministic_finite_automaton(const char *rex, bool &success)
{{{
	const char *p;
	char c = 'a';
	int alphabet_size;
	regex r;
	char *local_rex = strdup(rex);

	// calculate alphabet size
	p = rex;
	while(*p) {
		if(*p > c && *p <= 'z')
			c = *p;
		p++;
	}
	alphabet_size = 1 + c - 'a';

	// transform rex to nfa
	amore_insanitize_regex(local_rex);
	r = rexFromString(alphabet_size, local_rex);
	free(local_rex);

	if(!r) {
		success = false;
		nfa_p = NULL;
	} else {
		nfa_p = rex2nfa(r);
		freerex(r);
		free(r);
		success = (nfa_p != NULL);
	}
}}}
nondeterministic_finite_automaton::nondeterministic_finite_automaton(int alphabet_size, const char *rex, bool &success)
{{{
	regex r;
	char *local_rex = strdup(rex);

	amore_insanitize_regex(local_rex);
	r = rexFromString(alphabet_size, local_rex);
	free(local_rex);

	if(!r) {
		success = false;
		nfa_p = NULL;
	} else {
		nfa_p = rex2nfa(r);
		freerex(r);
		free(r);
		success = (nfa_p != NULL);
	}
}}}

nondeterministic_finite_automaton::~nondeterministic_finite_automaton()
{{{
	if(nfa_p) {
		freenfa(nfa_p);
		free(nfa_p);
	}
}}}

nondeterministic_finite_automaton * nondeterministic_finite_automaton::clone() const
{{{
	if(nfa_p)
		return new nondeterministic_finite_automaton(clonenfa(nfa_p));
	else
		return new nondeterministic_finite_automaton();
}}}

string nondeterministic_finite_automaton::to_regex() const
{{{
	if(this->is_empty()) {
		// for empty languages,
		//	* either nfa2rex generates invalid regular expressions,
		//	* or freerex may not be called.
		// i don't know which of both...
		return "%";
	} else {
		regex r = nfa2rex(nfa_p);

		string s;

		char *p = r->rex;
		while(*p != 0) {
			if(*p != ' ') {
				if(*p == 'U')
					s += '|';
				else
					s += *p;
			}
			++p;
		}

		freerex(r);
		free(r);

		return s;
	};
}}}

unsigned int nondeterministic_finite_automaton::get_state_count() const
{{{
	if(nfa_p)
		return nfa_p->highest_state + 1;
	else
		return 0;
}}}

bool nondeterministic_finite_automaton::is_empty() const
{{{
	bool ret;
	// libAMoRE-1.0 has empty_full_lan(), but it requires
	// a minimized DFA as input
	get_sample_word(ret);
	return ret;
}}}

bool nondeterministic_finite_automaton::is_universal() const
{{{
	bool ret;
	finite_automaton * d;

	d = this->determinize();
	ret = d->is_universal();
	delete d;

	return ret;
}}}

set<int> nondeterministic_finite_automaton::get_initial_states() const
{{{
	set<int> ret;
	for(unsigned int i = 0; i <= nfa_p->highest_state; i++)
		if(isinit(nfa_p->infin[i]))
			ret.insert(i);
	return ret;
}}}
set<int> nondeterministic_finite_automaton::get_final_states() const
{{{
	set<int> ret;
	for(unsigned int i = 0; i <= nfa_p->highest_state; i++)
		if(isfinal(nfa_p->infin[i]))
			ret.insert(i);
	return ret;
}}}

void nondeterministic_finite_automaton::set_initial_states(set<int> &states)
{{{
	for(unsigned int s = 0; s <= nfa_p->highest_state; s++)
		if(states.find(s) != states.end())
			setinit(nfa_p->infin[s]);
		else
			rminit(nfa_p->infin[s]);
}}}
void nondeterministic_finite_automaton::set_final_states(set<int> &states)
{{{
	for(unsigned int s = 0; s <= nfa_p->highest_state; s++)
		if(states.find(s) != states.end())
			setfinalT(nfa_p->infin[s]);
		else
			setfinalF(nfa_p->infin[s]);
}}}

bool nondeterministic_finite_automaton::contains_initial_states(set<int> states) const
{{{
	set<int>::const_iterator si;

	epsilon_closure(states);

	for(si = states.begin(); si != states.end(); si++)
		if(isinit(nfa_p->infin[*si]))
			return true;

	return false;
}}}
bool nondeterministic_finite_automaton::contains_final_states(set<int> states) const
{{{
	set<int>::const_iterator si;

	epsilon_closure(states);

	for(si = states.begin(); si != states.end(); si++)
		if(isfinal(nfa_p->infin[*si]))
			return true;

	return false;
}}}

set<int> nondeterministic_finite_automaton::successor_states(set<int> states) const
{{{
	set<int> ret;
	set<int>::const_iterator si;
	unsigned int sigma;
	unsigned int dst;

	epsilon_closure(states);

	for(dst = 0; dst <= nfa_p->highest_state; dst++) {
		for(si = states.begin(); si != states.end(); si++) {
			for(sigma = 0; sigma < nfa_p->alphabet_size; sigma++) {
				if(testcon((nfa_p->delta), sigma+1, *si, dst)) {
					ret.insert(dst);
					goto abort;
				}
			}
		}
abort:		;
	}

	epsilon_closure(ret);

	return ret;
}}}
set<int> nondeterministic_finite_automaton::successor_states(set<int> states, int label) const
{{{
	// very much like successor_states
	set<int> ret;
	set<int>::const_iterator si;
	unsigned int dst;

	if(label < 0 || label >= (int)this->get_alphabet_size())
		return ret;

	epsilon_closure(states);

	for(dst = 0; dst <= nfa_p->highest_state; dst++)
		for(si = states.begin(); si != states.end(); si++)
			if(testcon((nfa_p->delta), label+1, *si, dst))
				ret.insert(dst);

	epsilon_closure(ret);

	return ret;
}}}
set<int> nondeterministic_finite_automaton::predecessor_states(set<int> states) const
{{{
	set<int> ret;
	set<int>::const_iterator si;
	unsigned int sigma;
	unsigned int src;

	inverted_epsilon_closure(states);

	for(src = 0; src <= nfa_p->highest_state; src++) {
		for(si = states.begin(); si != states.end(); si++) {
			for(sigma = 0; sigma < nfa_p->alphabet_size; sigma++) {
				if(testcon((nfa_p->delta), sigma+1, src, *si)) {
					ret.insert(src);
					goto abort;
				}
			}
		}
abort:		;
	}

	inverted_epsilon_closure(ret);

	return ret;
}}}
set<int> nondeterministic_finite_automaton::predecessor_states(set<int> states, int label) const
{{{
	set<int> ret;
	set<int>::const_iterator si;
	unsigned int src;

	if(label < 0 || label >= (int)this->get_alphabet_size())
		return ret;

	inverted_epsilon_closure(states);

	for(src = 0; src <= nfa_p->highest_state; src++) {
		for(si = states.begin(); si != states.end(); si++) {
			if(testcon((nfa_p->delta), label+1, src, *si)) {
				ret.insert(src);
				goto abort;
			}
		}
abort:		;
	}

	inverted_epsilon_closure(ret);

	return ret;
}}}

unsigned int nondeterministic_finite_automaton::get_alphabet_size() const
{{{
	if(nfa_p)
		return nfa_p->alphabet_size;
	else
		return 0;
}}}

list<int> nondeterministic_finite_automaton::shortest_run(set<int> from, set<int> & to, bool &reachable) const
{{{
	set<int>::const_iterator si;
	list<automaton_run> run_fifo;
	automaton_run current, next;
	unsigned int s, l;

	// put initial states into fifo
	for(si = from.begin(); si != from.end(); si++) {
		current.state = *si;
		run_fifo.push_back(current);
	}

	from.clear();

	while(!run_fifo.empty()) {
		current = run_fifo.front();
		run_fifo.pop_front();

		// skip visited states
		if(from.find(current.state) != from.end())
			continue;

		// mark state as visited
		from.insert(current.state);

		// if final, we got the shortest run
		if(to.find(current.state) != to.end()) {
			reachable = true;
			return current.prefix;
		}

		// epsilon-close
		if(nfa_p->is_eps == TRUE) {
			next.prefix = current.prefix;
			for(s = 0; s <= nfa_p->highest_state; s++) {
				if(testcon((nfa_p->delta), 0, current.state, s)) {
					if(from.find(s) == from.end()) {
						next.state = s;
						run_fifo.push_front(next);
					}
				}
			}
		}

		// advance to new states
		for(s = 0; s <= nfa_p->highest_state; s++) { // dst state
			if(from.find(s) == from.end()) {
				for(l = 0; l < nfa_p->alphabet_size; l++) { // label
					if(testcon((nfa_p->delta), l+1, current.state, s)) {
						next.prefix = current.prefix;
						next.prefix.push_back(l);
						next.state = s;
						run_fifo.push_back(next);
					}
				}
			}
		}
	}
	list<int> ret;
	reachable = false;
	return ret; // empty word
}}}

bool nondeterministic_finite_automaton::is_reachable(set<int> &from, set<int> &to) const
{{{
	bool reachable;
	shortest_run(from, to, reachable);
	return reachable;
}}}

list<int> nondeterministic_finite_automaton::get_sample_word(bool & is_empty) const
{{{
	unsigned int s;
	list<int> ret;
	set<int> initial_states, final_states;
	bool reachable;

	// get initial and final states
	for(s = 0; s <= nfa_p->highest_state; s++) {
		if(isinit(nfa_p->infin[s]))
			initial_states.insert(s);
		if(isfinal(nfa_p->infin[s]))
			final_states.insert(s);
	}

	ret = shortest_run(initial_states, final_states, reachable);
	is_empty = !reachable;

	return ret;
}}}

bool nondeterministic_finite_automaton::operator==(const finite_automaton &other) const
{{{
	bool ret;
	finite_automaton * d;

	d = this->determinize();
	ret = (*d == other);
	delete d;

	return ret;
}}}

bool nondeterministic_finite_automaton::lang_subset_of(const finite_automaton &other) const
{{{
	bool ret;

	finite_automaton * d;

	d = this->determinize();
	ret = d->lang_subset_of(other);
	delete d;

	return ret;
}}}

bool nondeterministic_finite_automaton::lang_disjoint_to(const finite_automaton &other) const
{{{
	bool ret;

	finite_automaton * d;

	d = this->determinize();
	ret = d->lang_disjoint_to(other);
	delete d;

	return ret;
}}}

void nondeterministic_finite_automaton::epsilon_closure(set<int> & states) const
// add states reachable via an epsilon-transition
{{{
	if(nfa_p->is_eps == FALSE)
		return;

	queue<int> new_states;
	set<int>::const_iterator sti;

	int current;

	for(sti = states.begin(); sti != states.end(); sti++)
		new_states.push(*sti);

	while(!new_states.empty()) {
		// find all new epsilon states
		current = new_states.front();
		new_states.pop();

		for(unsigned int s = 0; s <= nfa_p->highest_state; s++) // state
			if(testcon(nfa_p->delta, 0, current, s))
				if(states.find(s) == states.end()) {
					states.insert(s);
					new_states.push(s);
				};
	};
}}}

void nondeterministic_finite_automaton::inverted_epsilon_closure(set<int> & states) const
// add states from whom these states can be reached via an epsilon-transition
{{{
	if(nfa_p->is_eps == FALSE)
		return;

	queue<int> new_states;
	set<int>::const_iterator sti;

	int current;

	for(sti = states.begin(); sti != states.end(); sti++)
		new_states.push(*sti);

	while(!new_states.empty()) {
		current = new_states.front();
		new_states.pop();

		for(unsigned int s = 0; s <= nfa_p->highest_state; s++) // state
			if(testcon(nfa_p->delta, 0, s, current))
				if(states.find(s) == states.end()) {
					states.insert(s);
					new_states.push(s);
				};
	};
}}}

bool nondeterministic_finite_automaton::contains(list<int> &word) const
{{{
	if(nfa_p) {
		set<int> states;
		set<int>::const_iterator si;

		states = get_initial_states();

		states = run(states, word.begin(), word.end());

		for(si = states.begin(); si != states.end(); si++)
			if(isfinal(nfa_p->infin[*si]))
				return true;
		return false;
	} else {
		return false;
	}
}}}

void nondeterministic_finite_automaton::minimize()
{{{
	dfa d;
	nfa n;

	d = nfa2dfa(nfa_p);
	d = dfamdfa(d, true);

	n = nfa2mnfa(nfa_p, d);

	freedfa(d);
	free(d);
	if(nfa_p != n) {
		freenfa(nfa_p);
		free(nfa_p);
	}

	nfa_p = n;
}}}

void nondeterministic_finite_automaton::lang_complement()
{{{
	dfa a,b;

	a = nfa2dfa(nfa_p);
	b = compldfa(a);

	freedfa(a);
	free(a);

	freenfa(nfa_p);
	free(nfa_p);

	nfa_p = dfa2nfa(b);

	freedfa(b);
	free(b);
}}}

nondeterministic_finite_automaton * nondeterministic_finite_automaton::reverse_language() const
{{{
	nfa rev_p;

	rev_p = newnfa();
	rev_p->alphabet_size = nfa_p->alphabet_size;
	rev_p->highest_state = nfa_p->highest_state;
	rev_p->infin = newfinal(nfa_p->highest_state);
	if(nfa_p->is_eps == TRUE) {
		rev_p->is_eps = TRUE;
		rev_p->delta = newendelta(nfa_p->alphabet_size, nfa_p->highest_state);
	} else {
		rev_p->is_eps = FALSE;
		rev_p->delta = newndelta(nfa_p->alphabet_size, nfa_p->highest_state);
	}

	unsigned int src, sigma, dst;

	for(src = 0; src <= nfa_p->highest_state; src++) {
		// copy reversed transitions
		for(sigma = (nfa_p->is_eps == TRUE) ? 0 : 1; sigma <= nfa_p->alphabet_size; sigma++)
			for(dst = 0; dst <= nfa_p->highest_state; dst++)
				// add reversed transition if exists.
				if(testcon(nfa_p->delta, sigma, src, dst))
					connect(rev_p->delta, sigma, dst, src);
		// reverse initial and final states
		if(isinit(nfa_p->infin[src]))
			setfinalT(rev_p->infin[src]);
		if(isfinal(nfa_p->infin[src]))
			setinit(rev_p->infin[src]);
	}

	return new nondeterministic_finite_automaton(rev_p);
}}}

nondeterministic_finite_automaton * nondeterministic_finite_automaton::lang_union(const finite_automaton &other) const
// libAMoRE says: alphabets need to be the same
{{{
	nondeterministic_finite_automaton * ret;
	const nondeterministic_finite_automaton * o_n;
	bool had_to_nfa = false;

	o_n = dynamic_cast<const nondeterministic_finite_automaton *> (&other);

	if(!o_n) {
		had_to_nfa = true;
		o_n = dynamic_cast<const nondeterministic_finite_automaton *>(other.nondeterminize());
	}

	nfa a;

	a = unionfa(nfa_p, o_n->nfa_p);
	ret = new nondeterministic_finite_automaton(a);

	if(had_to_nfa)
		delete o_n;

	return ret;
}}}

finite_automaton * nondeterministic_finite_automaton::lang_intersect(const finite_automaton &other) const
{{{
	finite_automaton * ret;
	finite_automaton * d;

	d = this->determinize();
	ret = d->lang_intersect(other);
	delete d;

	return ret;
}}}

finite_automaton * nondeterministic_finite_automaton::lang_difference(const finite_automaton &other) const
{{{
	finite_automaton * ret;
	finite_automaton * d;

	d = this->determinize();
	ret = d->lang_difference(other);
	delete d;

	return ret;
}}}

finite_automaton * nondeterministic_finite_automaton::lang_symmetric_difference(const finite_automaton &other) const
{{{
	finite_automaton * L1_without_L2;
	finite_automaton * L2_without_L1;
	finite_automaton * ret = NULL;

	L1_without_L2 = lang_difference(other);
	L2_without_L1 = other.lang_difference(*this);

	ret = L1_without_L2->lang_union(*L2_without_L1);

	delete L1_without_L2;
	delete L2_without_L1;

	return ret;
}}}

nondeterministic_finite_automaton * nondeterministic_finite_automaton::lang_concat(const finite_automaton &other) const
{{{
	nondeterministic_finite_automaton * ret;
	const nondeterministic_finite_automaton * o_n;

	bool had_to_nondeterminize = false;

	o_n = dynamic_cast<const nondeterministic_finite_automaton*> (&other);

	if(!o_n) {
		had_to_nondeterminize = true;
		o_n = dynamic_cast<const nondeterministic_finite_automaton*> (other.nondeterminize());
	}

	ret = new nondeterministic_finite_automaton(concatfa(nfa_p, o_n->nfa_p));

	if(had_to_nondeterminize)
		delete o_n;

	return ret;
}}}

std::basic_string<int32_t> nondeterministic_finite_automaton::serialize() const
{{{
	basic_string<int32_t> ret;
	basic_string<int32_t> temp;
	unsigned int s;
	int l;

	if(!nfa_p) {
		return ret; // empty basic_string
	}

	// stream length; will be filled in later
	ret += 0;

	// is not deterministic
	ret += htonl(0);
	// alphabet size
	ret += htonl(nfa_p->alphabet_size);
	// state count
	ret += htonl(nfa_p->highest_state+1);

	for(s = 0; s <= nfa_p->highest_state; s++)
		if(isinit(nfa_p->infin[s]))
			temp += htonl(s);
	// number of initial states
	ret += htonl(temp.length());
	// initial states
	ret += temp;

	temp.clear();
	for(s = 0; s <= nfa_p->highest_state; s++)
		if(isfinal(nfa_p->infin[s]))
			temp += htonl(s);
	// number of final states
	ret += htonl(temp.length());
	// final states
	ret += temp;

	temp.clear();
	for(l = (nfa_p->is_eps == TRUE) ? 0 : 1; l <= ((int)nfa_p->alphabet_size); l++) { // label
		for(s = 0; s <= nfa_p->highest_state; s++) { // source state id
			for(unsigned int d = 0; d <= nfa_p->highest_state; d++) {
				// boolx testcon(delta, label, src, dst); in libAMoRE
				if(testcon((nfa_p->delta), l, s, d)) {
					temp += htonl(s);
					temp += htonl(l-1);
					temp += htonl(d);
				}
			}
		}
	}
	// number of transitions
	ret += htonl(temp.length() / 3);
	// transitions
	ret += temp;

	ret[0] = htonl(ret.length() - 1);

	return ret;
}}}

bool nondeterministic_finite_automaton::deserialize(basic_string<int32_t>::const_iterator &it, basic_string<int32_t>::const_iterator limit)
{{{
	int size;
	int s, count;

	if(nfa_p) {
		freenfa(nfa_p);
		free(nfa_p);
	}

	if(it == limit)
		goto nfaa_deserialization_failed_fast;

	// string length (excluding length field)
	size = ntohl(*it);
	it++;
	if(size <= 0 || limit == it) goto nfaa_deserialization_failed_fast;

	nfa_p = newnfa();

	// deterministic flag
	s = ntohl(*it);
	if(s != 0 && s != 1) goto nfaa_deserialization_failed;

	// alphabet size
	size--, it++; if(size <= 0 || limit == it) goto nfaa_deserialization_failed;
	s = ntohl(*it);
	if(s < 1)
		return false;
	nfa_p->alphabet_size = s;

	// state count
	size--, it++; if(size <= 0 || limit == it) goto nfaa_deserialization_failed;
	s = ntohl(*it);
	if(s < 1)
		return false;
	nfa_p->highest_state = s - 1;

	// allocate data structures
	nfa_p->infin = newfinal(nfa_p->highest_state);
	nfa_p->delta = newendelta(nfa_p->alphabet_size, nfa_p->highest_state);
	nfa_p->is_eps = TRUE;
	if(!(nfa_p->infin) || !(nfa_p->delta))
		goto nfaa_deserialization_failed;

	// initial states
	size--, it++; if(size <= 0 || limit == it) goto nfaa_deserialization_failed;
	count = ntohl(*it);
	if(count < 0)
		return false;

	for(s = 0; s < count; s++) {
		size--, it++; if(size <= 0 || limit == it) goto nfaa_deserialization_failed;
		if(ntohl(*it) > nfa_p->highest_state)
			goto nfaa_deserialization_failed;
		setinit(nfa_p->infin[ntohl(*it)]);
	}

	// final states
	size--, it++; if(size <= 0 || limit == it) goto nfaa_deserialization_failed;
	count = ntohl(*it);
	if(count < 0)
		return false;

	for(s = 0; s < count; s++) {
		size--, it++; if(size <= 0 || limit == it) goto nfaa_deserialization_failed;
		if(ntohl(*it) > nfa_p->highest_state)
			goto nfaa_deserialization_failed;
		setfinalT(nfa_p->infin[ntohl(*it)]);
	}

	// transitions
	size--, it++; if(size <= 0 || limit == it) goto nfaa_deserialization_failed;
	count = ntohl(*it);
	if(count < 0)
		return false;
	for(s = 0; s < count; s++) {
		int32_t src, label, dst;

		size--, it++; if(size <= 0 || limit == it) goto nfaa_deserialization_failed;
		src = ntohl(*it);
		size--, it++; if(size <= 0 || limit == it) goto nfaa_deserialization_failed;
		label = ntohl(*it);
		size--, it++; if(size <= 0 || limit == it) goto nfaa_deserialization_failed;
		dst = ntohl(*it);

		if(   (label < -1) || (label >= (int)nfa_p->alphabet_size)
		   || (src < 0) || (src > (int)nfa_p->highest_state)
		   || (dst < 0) || (dst > (int)nfa_p->highest_state) ) {
			goto nfaa_deserialization_failed;
		}

		connect(nfa_p->delta, label+1, src, dst);
	}

	size--, it++;

	if(size != 0)
		goto nfaa_deserialization_failed;

	return true;

nfaa_deserialization_failed:
	freenfa(nfa_p);
	free(nfa_p);
nfaa_deserialization_failed_fast:
	nfa_p = NULL;
	return false;
}}}

bool nondeterministic_finite_automaton::is_deterministic() const
{ return false; };

finite_automaton * nondeterministic_finite_automaton::determinize() const
{{{
	deterministic_finite_automaton *a;
	a = new deterministic_finite_automaton( nfa2dfa(nfa_p) );
	return a;
}}}

nondeterministic_finite_automaton * nondeterministic_finite_automaton::nondeterminize() const
{{{
	return this->clone();
}}}

void nondeterministic_finite_automaton::set_nfa(nfa a)
{{{
	if(nfa_p) {
		freenfa(nfa_p);
		free(nfa_p);
	}
	nfa_p = clonenfa(a);
}}}

nfa nondeterministic_finite_automaton::get_nfa()
{{{
	return nfa_p;
}}}

} // end namespace amore

