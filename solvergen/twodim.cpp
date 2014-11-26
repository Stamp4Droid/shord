#include <cassert>
#include <cstdlib>
#include <limits>
#include <iostream>
#define LIBAMORE_LIBRARY_COMPILATION
#include "amore/dfa.h"
#include "amore/global.h"
#include "amore/testBinary.h"
#include "amore++/deterministic_finite_automaton.h"
#include "amore++/nondeterministic_finite_automaton.h"

typedef struct dfauto DfaBackend;
typedef amore::deterministic_finite_automaton DfaWrapper;
typedef struct nfauto NfaBackend;
typedef amore::nondeterministic_finite_automaton NfaWrapper;

class NFA {
private:
    NfaBackend* backend_;
    NfaWrapper* wrapper_;
private:
    void add_trans_impl(posint src, posint letter, posint tgt) {
	assert(src < num_states());
	assert(letter <= num_letters());
	assert(tgt < num_states());
	connect(backend_->delta, letter, src, tgt);
    }
public:
    explicit NFA(posint num_letters, posint num_states) {
	// no initial or final state set
	char* infin_bitset = newfinal(num_states - 1);
	// reserves 1 extra row for epsilon
	// no transitions are set
	char*** delta = newendelta(num_letters, num_states - 1);
	backend_ = newnfa();
	backend_->highest_state = num_states - 1;
	backend_->alphabet_size = num_letters;
	backend_->infin = infin_bitset;
	backend_->delta = delta;
	// We never minimize NFAs directly, so this is always false; we don't
	// need to update it after mutations.
	backend_->minimal = false;
	backend_->is_eps = true;
	wrapper_ = new NfaWrapper(backend_);
	assert(wrapper_->get_state_count() == num_states);
	assert(wrapper_->get_alphabet_size() == num_letters);
    }
    ~NFA() {
	delete wrapper_; // also deallocates backend_
    }
    // letters: 1..N
    // 0 is reserved for epsilon
    posint num_letters() const {
	return backend_->alphabet_size;
    }
    // states: 0..N-1
    posint num_states() const {
	return backend_->highest_state + 1;
    }
    posint is_initial(posint state) const {
	assert(state < num_states());
	return isinit(backend_->infin[state]);
    }
    bool is_final(posint state) const {
	assert(state < num_states());
	return isfinal(backend_->infin[state]);
    }
    const NfaWrapper* wrapper() const {
	return wrapper_;
    }
    void add_trans(posint src, char symbol, posint tgt) {
	posint letter = symbol - 'a' + 1;
	assert(letter >= 1);
	add_trans_impl(src, letter, tgt);
    }
    void add_eps_trans(posint src, posint tgt) {
	add_trans_impl(src, 0, tgt);
    }
    void set_final(posint state, bool final = true) {
	assert(state < num_states());
	setfinal(backend_->infin[state], final);
    }
    void set_initial(posint state, bool initial = true) {
	assert(state < num_states());
	if (initial) {
	    setinit(backend_->infin[state]);
	} else {
	    rminit(backend_->infin[state]);
	}
    }
    std::string to_dot() const {
	return wrapper_->visualize(true);
    }
    std::string to_regex() const {
	return wrapper_->to_regex();
    }
    friend std::ostream& operator<<(std::ostream& os, const NFA& nfa) {
	os << "Initial: ";
	for (posint state = 0; state < nfa.num_states(); state++) {
	    if (nfa.is_initial(state)) {
		os << state << " ";
	    }
	}
	os << std::endl;
	os << "Final: ";
	for (posint state = 0; state < nfa.num_states(); state++) {
	    if (nfa.is_final(state)) {
		os << state << " ";
	    }
	}
	os << std::endl;
	std::map<int,std::map<int,std::set<int>>> premap;
	std::map<int,std::map<int,std::set<int>>> postmap;
	nfa.wrapper_->get_transition_maps(premap, postmap);
	for (const auto& src_p : postmap) {
	    int src = src_p.first;
	    for (const auto& letter_p : src_p.second) {
		int letter = letter_p.first;
		for (int dst : letter_p.second) {
		    os << src << " --";
		    if (letter >= 0) {
			os << (char) ('a' + letter);
		    } else {
			os << "@epsilon";
		    }
		    os << "-> " << dst << std::endl;
		}
	    }
	}
	return os;
    }
};

class DFA {
private:
    DfaBackend* backend_;
    DfaWrapper* wrapper_;
public:
    // An extra sink state is added: the final FSM will have N+1 states.
    explicit DFA(posint num_letters, posint num_states) {
	posint sink_state = num_states;
	// no final state set
	// reserves 1 extra slot for sink state
	char* fin_bitset = newfinal(num_states);
	// reserves 1 extra row for epsilon, and 1 extra slot for sink state
	// doesn't allocate an actual row for epsilon (letter #0)
	posint** delta = newddelta(num_letters, num_states);
	// all valid transitions move to the sink state
	for (posint i = 1; i <= num_letters; i++) {
	    std::fill_n(delta[i], num_states + 1, sink_state);
	}
	backend_ = newdfa();
	backend_->highest_state = num_states;
	backend_->init = sink_state;
	backend_->alphabet_size = num_letters;
	backend_->final = fin_bitset;
	backend_->delta = delta;
	backend_->minimal = false;
	wrapper_ = new DfaWrapper(backend_);
	assert(wrapper_->get_state_count() == num_states + 1);
	assert(wrapper_->get_alphabet_size() == num_letters);
    }
    explicit DFA(const NFA& nfa) {
	wrapper_ = dynamic_cast<DfaWrapper*>(nfa.wrapper()->determinize());
	backend_ = wrapper_->get_dfa();
    }
    ~DFA() {
	delete wrapper_; // also deallocates backend_
    }
    // letters: 1..N
    // 0 is reserved for epsilon
    posint num_letters() const {
	return backend_->alphabet_size;
    }
    // states: 0..N-1
    posint num_states() const {
	return backend_->highest_state + 1;
    }
    bool minimal() const {
	return backend_->minimal;
    }
    posint initial() const {
	return backend_->init;
    }
    bool is_final(posint state) const {
	assert(state < num_states());
	return isfinal(backend_->final[state]);
    }
    // Returns the first sink state found (TODO: Assumes only one sink state).
    // Returns num_states() if no sink state exists.
    posint sink() const {
	for (posint state = 0; state < num_states(); state++) {
	    if (is_final(state)) {
		continue;
	    }
	    bool is_sink = true;
	    for (posint letter = 1; letter <= num_letters(); letter++) {
		if (backend_->delta[letter][state] != state) {
		    is_sink = false;
		    break;
		}
	    }
	    if (is_sink) {
		return state;
	    }
	}
	return num_states();
    }
    void add_trans(posint src, char symbol, posint tgt) {
	assert(src < num_states());
	posint letter = symbol - 'a' + 1;
	assert(letter >= 1 && letter <= num_letters());
	assert(tgt < num_states());
	backend_->delta[letter][src] = tgt;
	backend_->minimal = false;
    }
    void set_final(posint state, bool final = true) {
	assert(state < num_states());
	setfinal(backend_->final[state], final);
	backend_->minimal = false;
    }
    void set_initial(posint state) {
	assert(state < num_states());
	backend_->init = state;
	backend_->minimal = false;
    }
    void minimize() {
	wrapper_->minimize();
	backend_ = wrapper_->get_dfa(); // update the cached backend pointer
    }
    bool operator==(const DFA& other) {
	assert(minimal() && other.minimal());
	return equiv(backend_, other.backend_);
    }
    std::string to_dot() const {
	return wrapper_->visualize(true);
    }
    std::string to_regex() const {
	return wrapper_->to_regex();
    }
    friend std::ostream& operator<<(std::ostream& os, const DFA& dfa) {
	posint sink = dfa.sink();
	os << "\t";
	for (posint state = 0; state < dfa.num_states(); state++) {
	    if (state == sink) {
		continue;
	    }
	    if (dfa.initial() == state) {
		os << "->";
	    }
	    os << state;
	    if (dfa.is_final(state)) {
		os << "->";
	    }
	    os << "\t";
	}
	os << std::endl;
	posint** delta = dfa.backend_->delta;
	for (posint letter = 1; letter <= dfa.num_letters(); letter++) {
	    os << (char) ('a' + letter - 1) << "\t";
	    for (posint src = 0; src < dfa.num_states(); src++) {
		if (src == sink) {
		    continue;
		}
		posint tgt = delta[letter][src];
		if (tgt != sink) {
		    os << tgt;
		}
		os << "\t";
	    }
	    os << std::endl;
	}
	return os;
    }
};

int main() {
    DFA d1(2, 3);
    d1.set_initial(0);
    d1.set_final(1);
    d1.add_trans(0, 'a', 1);
    d1.add_trans(1, 'a', 1);
    d1.add_trans(1, 'b', 2);
    d1.add_trans(2, 'a', 2);
    std::cout << "d1:" << std::endl;
    std::cout << d1;
    std::cout << d1.to_regex() << std::endl;
    std::cout << std::endl;

    d1.minimize();
    std::cout << "d1 minimized:" << std::endl;
    std::cout << d1;
    std::cout << d1.to_regex() << std::endl;
    std::cout << std::endl;

    NFA n(2, 4);
    n.set_initial(0);
    n.set_final(1);
    n.add_trans(0, 'a', 1);
    n.add_trans(1, 'a', 1);
    n.add_trans(1, 'b', 2);
    n.add_trans(2, 'a', 2);
    std::cout << "n:" << std::endl;
    std::cout << n;
    std::cout << n.to_regex() << std::endl;
    std::cout << std::endl;

    DFA d2(n);
    std::cout << "d2 = n determinized:" << std::endl;
    std::cout << d2;
    std::cout << d2.to_regex() << std::endl;
    std::cout << std::endl;

    d2.minimize();
    std::cout << "d2 minimized:" << std::endl;
    std::cout << d2;
    std::cout << d2.to_regex() << std::endl;
    std::cout << std::endl;

    std::cout << "d1 and d2 are " << ((d1 == d2) ? "" : "NOT ") << "equal"
	      << std::endl;
}
