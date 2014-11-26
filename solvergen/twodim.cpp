#include <cassert>
#include <cstdlib>
#include <limits>
#include <iostream>
#define LIBAMORE_LIBRARY_COMPILATION
#include "amore/dfa.h"
#include "amore/global.h"
#include "amore++/deterministic_finite_automaton.h"
#include "amore++/nondeterministic_finite_automaton.h"

class DFA {
private:
    struct dfauto* backend_;
    amore::deterministic_finite_automaton* m_;
public:
    // an extra sink state is added: the final FSM will have N+1 states
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
	m_ = new amore::deterministic_finite_automaton(backend_);
	assert(m_->get_state_count() == num_states + 1);
	assert(m_->get_alphabet_size() == num_letters);
    }
    ~DFA() {
	delete m_; // also deallocates backend_
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
    posint initial() const {
	return backend_->init;
    }
    bool is_final(posint state) const {
	return backend_->final[state];
    }
    // TODO: Might be more than one sink states
    // Return num_states() if no sink state exists.
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
	backend_->final[state] = final;
	backend_->minimal = false;
    }
    void set_initial(posint state) {
	assert(state < num_states());
	backend_->init = state;
	backend_->minimal = false;
    }
    void minimize() {
	m_->minimize();
	backend_ = m_->get_dfa(); // update the cached backend pointer
    }
    std::string to_dot() const {
	return m_->visualize(true);
    }
    std::string to_regex() const {
	return m_->to_regex();
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
    DFA m(2, 3);
    m.set_initial(0);
    m.set_final(1);
    m.add_trans(0, 'a', 1);
    m.add_trans(1, 'a', 1);
    m.add_trans(1, 'b', 2);
    m.add_trans(2, 'a', 2);
    std::cout << m << std::endl;
    m.minimize();
    std::cout << m << std::endl;
}
