#include <boost/algorithm/string.hpp>
#include <boost/filesystem.hpp>
#include <boost/program_options.hpp>
#include <boost/regex.hpp>
#include <cassert>
#include <cstdlib>
#include <deque>
#include <fstream>
#include <iostream>
#include <iterator>
#include <limits>
#include <map>
#include <vector>

#define LIBAMORE_LIBRARY_COMPILATION
#include "amore/dfa.h"
#include "amore/enfa2nfa.h"
#include "amore/global.h"
#include "amore/nfa2dfa.h"
#include "amore/testBinary.h"
#include "amore++/deterministic_finite_automaton.h"
#include "amore++/nondeterministic_finite_automaton.h"

#include "util.hpp"

namespace fs = boost::filesystem;
namespace po = boost::program_options;

// AMORE AUTOMATA =============================================================

typedef struct dfauto DfaBackend;
typedef amore::deterministic_finite_automaton DfaWrapper;
typedef struct nfauto NfaBackend;
typedef amore::nondeterministic_finite_automaton NfaWrapper;

template<class Symbol> class DFA;

template<class Symbol> class NFA {
    friend DFA<Symbol>;
private:
    std::vector<Symbol> alphabet_;
    NfaBackend* backend_;
    // Not using a wrapper, because AMoRE++ NFAs can't handle epsilons.
private:
    DfaWrapper* determinize() const {
	NfaBackend* wo_eps = enfa2nfa(backend_);
	DfaWrapper* res = new DfaWrapper(nfa2dfa(wo_eps));
	freenfa(wo_eps);
	free(wo_eps);
	return res;
    }
public:
    template<typename C>
    explicit NFA(const C& alphabet, posint num_states)
	: alphabet_(alphabet.begin(), alphabet.end()) {
	// Sort the set of symbols, and remove duplicates.
	std::sort(alphabet_.begin(), alphabet_.end());
	alphabet_.erase(std::unique(alphabet_.begin(), alphabet_.end()),
			alphabet_.end());
	posint num_letters = alphabet_.size();
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
    }
    ~NFA() {
	if (backend_ != NULL) {
	    freenfa(backend_);
	    free(backend_);
	}
    }
    NFA(const NFA&) = delete;
    NFA(NFA&& rhs)
	: alphabet_(std::move(rhs.alphabet_)), backend_(rhs.backend_) {
	rhs.backend_ = NULL;
    }
    NFA& operator=(const NFA&) = delete;
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
    void add_symb_trans(posint src, Symbol symbol, posint tgt) {
	// TODO: Might be more efficient to keep a map from symbols to letters.
	const auto iter_p =
	    std::equal_range(alphabet_.cbegin(), alphabet_.cend(), symbol);
	// Assumes the symbol exists in the FSM's alphabet.
	assert(iter_p.first != iter_p.second);
	// Add 1 to the alphabet offset, because 0 is reserved for epsilon.
	posint letter = iter_p.first - alphabet_.cbegin() + 1;
	add_trans(src, letter, tgt);
    }
    void add_eps_trans(posint src, posint tgt) {
	add_trans(src, 0, tgt);
    }
    void add_trans(posint src, posint letter, posint tgt) {
	assert(src < num_states());
	assert(letter <= num_letters());
	assert(tgt < num_states());
	connect(backend_->delta, letter, src, tgt);
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
	for (posint letter = 0; letter <= nfa.num_letters(); letter++) {
	    for (posint src = 0; src < nfa.num_states(); src++) {
		for (posint tgt = 0; tgt < nfa.num_states(); tgt++) {
		    if (testcon(nfa.backend_->delta, letter, src, tgt)) {
			os << src << " --";
			if (letter > 0) {
			    os << nfa.alphabet_[letter - 1];
			} else {
			    os << "@epsilon";
			}
			os << "-> " << tgt << std::endl;
		    }
		}
	    }
	}
	return os;
    }
};

// Maintains a map from items to blocks, which gets traversed on every merge,
// to perform renumbering.
// TODO: Could delay the renumbering (leaving holes temporarily), or use a more
// efficient data structure (e.g. union-find).
class SetPartition {
private:
    const posint num_items_;
    posint num_blocks_;
    posint* map_;
public:
    explicit SetPartition(posint n)
	: num_items_(n), num_blocks_(n), map_(new posint[n]) {
	for (posint i = 0; i < n; i++) {
	    // Each element forms a singleton block.
	    map_[i] = i;
	}
    }
    ~SetPartition() {
	delete map_;
    }
    posint num_blocks() const {
	return num_blocks_;
    }
    void merge(posint blk_1, posint blk_2) {
	assert(blk_1 < num_blocks() && blk_2 < num_blocks());
	if (blk_1 == blk_2) {
	    return;
	}
	if (blk_1 > blk_2) {
	    merge(blk_2, blk_1);
	    return;
	}
	for (posint i = 0; i < num_items_; i++) {
	    if (map_[i] == blk_2) {
		map_[i] = blk_1;
	    } else if (map_[i] > blk_2) {
		map_[i]--;
	    }
	}
	num_blocks_--;
    }
    // Return the block where item i belongs.
    posint operator[](posint i) const {
	assert(i < num_items_);
	return map_[i];
    }
};

template<class T> class Matrix2D {
private:
    const posint x_len_;
    const posint y_len_;
    T* array_;
public:
    Matrix2D(posint x_len, posint y_len, const T& val)
	: x_len_(x_len), y_len_(y_len), array_(new T[x_len*y_len]) {
	std::fill_n(array_, x_len * y_len, val);
    }
    ~Matrix2D() {
	delete array_;
    }
    T& operator()(posint i, posint j) {
	assert(i < x_len_ && j < y_len_);
	return array_[i*y_len_ + j];
    }
};

// Lower-left triangle (without the main diagonal): only care about elements
// M[i][j] with i > j.
// TODO: Could store only the required elements: ((n - 1) * n / 2) cells in
// total, use formula (i * (i - 1) / 2 + j) to calculate the index.
// TODO: Could specialize for bools: store 8 bools in a byte.
template<class T> class TriangleMatrix {
private:
    posint size_;
    T* array_;
public:
    TriangleMatrix(posint n, const T& val) : size_(n), array_(new T[n*n]) {
	for (posint i = 0; i < n; i++) {
	    for (posint j = 0; j < i; j++) {
		array_[i*n + j] = val;
	    }
	}
    }
    ~TriangleMatrix() {
	delete array_;
    }
    friend void swap(TriangleMatrix& a, TriangleMatrix& b) {
	using std::swap;
	swap(a.size_, b.size_);
	swap(a.array_, b.array_);
    }
    T& operator()(posint i, posint j) {
	assert(i < size_ && j < size_ && i != j);
	return (i > j) ? array_[i*size_ + j] : array_[j*size_ + i];
    }
};

// Symbols must be comparable (the alphabet gets sorted at construction).
template<class Symbol> class DFA {
    enum class StateColor {NONE, INITIAL, FINAL, BOTH};
private:
    std::vector<Symbol> alphabet_;
    DfaBackend* backend_;
    DfaWrapper* wrapper_;
private:
    posint follow(posint src, posint letter) const {
	return backend_->delta[letter][src];
    }
    StateColor color(posint state) const {
	assert(state < num_states());
	if (state == initial()) {
	    if (is_final(state)) {
		return StateColor::BOTH;
	    } else {
		return StateColor::INITIAL;
	    }
	}
	if (is_final(state)) {
	    return StateColor::FINAL;
	} else {
	    return StateColor::NONE;
	}
    }
    NFA<Symbol> state_merge(const SetPartition& part) const {
	// TODO: Copying alphabet, but not really using it.
	NFA<Symbol> res(alphabet_, part.num_blocks());
	for (posint letter = 1; letter <= num_letters(); letter++) {
	    for (posint src = 0; src < num_states(); src++) {
		posint tgt = follow(src, letter);
		res.add_trans(part[src], letter, part[tgt]);
	    }
	}
	res.set_initial(part[initial()]);
	for (posint state = 0; state < num_states(); state++) {
	    if (is_final(state)) {
		res.set_final(part[state]);
	    }
	}
	return res;
    }
    // Equivalent to the partitioning step of the table-filling variant of the
    // DFA minimization algorithm, run for k iterations (not to fixpoint).
    // TODO:
    // - might converge faster than that
    // - don't have to traverse all elements on every iteration
    // - can adapt Hopcroft's improved partitioning algorithm instead?
    SetPartition k_equiv(posint k) const {
	TriangleMatrix<bool> distinct(num_states(), false);
	posint sink = sink_state();
	for (posint i = 0; i < num_states(); i++) {
	    for (posint j = 0; j < i; j++) {
		if (i == sink || j == sink || color(i) != color(j)) {
		    distinct(i, j) = true;
		}
	    }
	}
	for (posint len = 1; len <= k; len++) {
	    // We can't update the original array directly; that could
	    // accelerate convergence.
	    TriangleMatrix<bool> temp(num_states(), false);
	    for (posint i = 0; i < num_states(); i++) {
		for (posint j = 0; j < i; j++) {
		    if (distinct(i, j)) {
			temp(i, j) = true;
			continue;
		    }
		    temp(i, j) = false;
		    for (posint l = 1; l <= num_letters(); l++) {
			posint ii = follow(i,l);
			posint jj = follow(j,l);
			if (ii == jj || !distinct(ii, jj)) {
			    continue;
			}
			temp(i, j) = true;
			break;
		    }
		}
	    }
	    swap(distinct, temp);
	}
	SetPartition part(num_states());
	for (posint i = 0; i < num_states(); i++) {
	    for (posint j = 0; j < i; j++) {
		if (!distinct(i, j)) {
		    part.merge(part[i], part[j]);
		}
	    }
	}
	return part;
    }
    void trim_letters() {
	// Compute the full set of letters that can reach each state.
	Matrix2D<bool> reachable(num_states(), num_letters() + 1, false);
	std::deque<posint> worklist = {initial()};
	while (!worklist.empty()) {
	    posint src = worklist.front();
	    worklist.pop_front();
	    for (posint letter = 1; letter <= num_letters(); letter++) {
		posint tgt = follow(src, letter);
		bool updated = false;
		for (posint l = 1; l <= num_letters(); l++) {
		    if (l != letter && !reachable(src, l)) {
			continue;
		    }
		    if (reachable(tgt, l)) {
			continue;
		    }
		    updated = true;
		    reachable(tgt, l) = true;
		}
		if (updated) {
		    worklist.push_back(tgt);
		}
	    }
	}
	// Only letters reaching final states are useful.
	std::vector<bool> is_useful(num_letters() + 1, false);
	for (posint state = 0; state < num_states(); state++) {
	    if (!is_final(state)) {
		continue;
	    }
	    for (posint letter = 1; letter <= num_letters(); letter++) {
		if (reachable(state, letter)) {
		    is_useful[letter] = true;
		}
	    }
	}
	// Collect useful letters in new alphabet.
	std::vector<Symbol> useful_syms;
	for (posint letter = 1; letter <= num_letters(); letter++) {
	    if (is_useful[letter]) {
		useful_syms.push_back(alphabet_[letter - 1]);
	    }
	}
	// No need to continue if all letters are useful.
	if (useful_syms.size() == num_letters()) {
	    return;
	}
	// Set up new transition table (copy rows for useful letters only,
	// deallocate the rest).
	posint** new_delta =
	    (posint**) calloc(useful_syms.size() + 1, sizeof(posint*));
	assert(new_delta != NULL);
	posint next = 1;
	for (posint letter = 1; letter <= num_letters(); letter++) {
	    if (is_useful[letter]) {
		new_delta[next++] = backend_->delta[letter];
	    } else {
		dispose(backend_->delta[letter]);
	    }
	}
	assert(next == useful_syms.size() + 1);
	// Update the data members.
	alphabet_.swap(useful_syms);
	backend_->alphabet_size = alphabet_.size();
	dispose(backend_->delta);
	backend_->delta = new_delta;
	// This CAN break minimality, e.g. if there was a sink state just to
	// cover the useless letters.
	backend_->minimal = false;
    }
    // Returns the newly-created sink state.
    posint add_sink_state() {
	posint new_size = num_states() + 1;
	posint new_sink = new_size - 1;
	backend_->highest_state = new_sink;
	// new state is not marked final
	backend_->final = (posint*) realloc(backend_->final, new_size);
	assert(backend_->final != NULL);
	posint** delta = backend_->delta;
	for (posint letter = 1; letter <= num_letters(); letter++) {
	    delta[letter] = (posint*) realloc(delta[letter], new_size);
	    delta[letter][new_sink] = new_sink;
	}
	backend_->minimal = false;
	return new_sink;
    }
    DfaWrapper* extend_alph(const std::vector<Symbol>& new_alph) const {
	// TODO: Assuming well-formed input:
	// assert(alphabet_.size() <= new_alph.size());
	// assert(std::is_sorted(new_alph.begin(), new_alph.end()));
	// assert(no_duplicates(new_alph.begin(), new_alph.end()));
	// assert(std::includes(new_alph.begin(), new_alph.end(),
	// 		     alphabet_.begin(), alphabet_.end()));
	if (alphabet_.size() == new_alph.size()) {
	    return wrapper_->clone();
	}
	// Locate the sink state.
	posint new_states = num_states();
	posint new_sink = sink_state();
	if (new_sink == num_states()) {
	    // If no sink state exists on the original FSM, insert one as the
	    // last state.
	    new_states++;
	}
	// Allocate & fill out extended transition table.
	posint** new_delta =
	    (posint**) calloc(new_alph.size() + 1, sizeof(posint*));
	assert(new_delta != NULL);
	typename std::vector<Symbol>::const_iterator
	    base_first = alphabet_.cbegin(),
	    base_it    = alphabet_.cbegin(),
	    base_lim   = alphabet_.cend(),
	    ext_first  = new_alph.cbegin(),
	    ext_it     = new_alph.cbegin(),
	    ext_lim    = new_alph.cend();
	for (; ext_it < ext_lim ; ++ext_it) {
	    posint* new_row = (posint*) calloc(new_states, sizeof(posint));
	    assert(new_row != NULL);
	    new_delta[ext_it - ext_first + 1] = new_row;
	    if (base_it < base_lim && *base_it == *ext_it) {
		// existing symbol: copy the corresponding row
		std::copy_n(backend_->delta[base_it - base_first + 1],
			    num_states(), new_row);
		++base_it;
		if (new_states > num_states()) {
		    new_row[new_sink] = new_sink;
		}
	    } else {
		// new symbol: always leads to the sink state
		std::fill_n(new_row, new_states, new_sink);
	    }
	}
	assert(base_it == base_lim);
	// Synthesize a new DFA.
	DfaBackend* new_dfa = newdfa();
	new_dfa->highest_state = new_states - 1;
	new_dfa->init = initial();
	new_dfa->alphabet_size = new_alph.size();
	new_dfa->final = newfinal(new_states - 1);
	for (posint state = 0; state < num_states(); state++) {
	    setfinal(new_dfa->final[state], is_final(state));
	    // Possible new sink state is not marked final.
	}
	new_dfa->delta = new_delta;
	// TODO: Theoretically doesn't break minimality, but it might mess with
	// the library's expectation of what minimized DFAs look like.
	new_dfa->minimal = false;
	return new DfaWrapper(new_dfa);
    }
    // The newly created DFA takes ownership of the provided components.
    explicit DFA(std::vector<Symbol>&& alphabet, DfaWrapper* wrapper)
	: alphabet_(std::move(alphabet)), backend_(wrapper->get_dfa()),
	  wrapper_(wrapper) {
	// TODO: Assuming well-formed input:
	// assert(std::is_sorted(alphabet.begin(), alphabet.end()));
	// assert(no_duplicates(alphabet.begin(), alphabet.end()));
	assert(backend_->alphabet_size == alphabet_.size());
    }
public:
    // An extra sink state is added: the final FSM will have N+1 states.
    template<typename C>
    explicit DFA(const C& alphabet, posint num_states)
	: alphabet_(alphabet.begin(), alphabet.end()) {
	// Sort the set of symbols, and remove duplicates.
	std::sort(alphabet_.begin(), alphabet_.end());
	alphabet_.erase(std::unique(alphabet_.begin(), alphabet_.end()),
			alphabet_.end());
	posint num_letters = alphabet_.size();
	posint sink = num_states;
	// no final state set
	// reserves 1 extra slot for sink state
	char* fin_bitset = newfinal(num_states);
	// reserves 1 extra row for epsilon, and 1 extra slot for sink state
	// doesn't allocate an actual row for epsilon (letter #0)
	posint** delta = newddelta(num_letters, num_states);
	// all valid transitions move to the sink state
	for (posint i = 1; i <= num_letters; i++) {
	    std::fill_n(delta[i], num_states + 1, sink);
	}
	backend_ = newdfa();
	backend_->highest_state = num_states;
	backend_->init = sink;
	backend_->alphabet_size = num_letters;
	backend_->final = fin_bitset;
	backend_->delta = delta;
	backend_->minimal = false;
	wrapper_ = new DfaWrapper(backend_);
    }
    explicit DFA(NFA<Symbol>&& nfa)
	: DFA(std::move(nfa.alphabet_), nfa.determinize()) {
	assert(!backend_->minimal); // ensures minimal() => no useless letters
	// Ensure the NFA is in a valid state, by clearing its contents.
	freenfa(nfa.backend_);
	free(nfa.backend_);
	nfa.backend_ = NULL;
    }
    ~DFA() {
	if (wrapper_ != NULL) {
	    delete wrapper_; // also deallocates backend_
	}
    }
    DFA(const DFA&) = delete;
    DFA(DFA&& rhs) : DFA(std::move(rhs.alphabet_), rhs.wrapper_) {
	rhs.backend_ = NULL;
	rhs.wrapper_ = NULL;
    }
    DFA& operator=(const DFA&) = delete;
    // letters: 1..N
    // 0 is reserved for epsilon
    posint num_letters() const {
	return backend_->alphabet_size;
    }
    // states: 0..N-1
    posint num_states() const {
	return backend_->highest_state + 1;
    }
    // Returns true iff DFA has minimal #states and no useless letters.
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
    posint sink_state() const {
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
    void add_symb_trans(posint src, Symbol symbol, posint tgt) {
	// TODO: Might be more efficient to keep a map from symbols to letters.
	const auto iter_p =
	    std::equal_range(alphabet_.cbegin(), alphabet_.cend(), symbol);
	// Assumes the symbol exists in the FSM's alphabet.
	assert(iter_p.first != iter_p.second);
	// Add 1 to the alphabet offset, because 0 is reserved for epsilon.
	posint letter = iter_p.first - alphabet_.cbegin() + 1;
	add_trans(src, letter, tgt);
    }
    void add_trans(posint src, posint letter, posint tgt) {
	assert(src < num_states());
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
	trim_letters();
	wrapper_->minimize();
	backend_ = wrapper_->get_dfa(); // update the cached backend pointer
    }
    void fold(posint depth) {
	NFA<Symbol> folded = state_merge(k_equiv(depth));
	wrapper_ = folded.determinize();
	backend_ = wrapper_->get_dfa();
	assert(!backend_->minimal); // ensures minimal() => no useless letters
    }
    DFA operator|(const DFA& other) const {
	// Combine the alphabets of the two machines.
	std::vector<Symbol> joint_alph;
	std::set_union(alphabet_.begin(), alphabet_.end(),
		       other.alphabet_.begin(), other.alphabet_.end(),
		       std::back_inserter(joint_alph));
	// Extend machines to the common alphabet, then union them.
	DfaWrapper* dfa1 = extend_alph(joint_alph);
	DfaWrapper* dfa2 = other.extend_alph(joint_alph);
	assert(dfa1->get_alphabet_size() == dfa2->get_alphabet_size());
	// TODO: These downcasts are statically safe.
	NfaWrapper* nfa1U2 =
	    dynamic_cast<NfaWrapper*>(dfa1->lang_union(*dfa2));
	// XXX: Assuming AMoRE++ can handle this correctly.
	DfaWrapper* dfa1U2 = dynamic_cast<DfaWrapper*>(nfa1U2->determinize());
	assert(dfa1U2 != NULL);
	// ensures minimal() => no useless letters
	assert(!dfa1U2->get_dfa()->minimal);
	// Clean up temporary machines before returning.
	delete dfa1;
	delete dfa2;
	delete nfa1U2;
	return DFA(std::move(joint_alph), dfa1U2);
    }
    // TODO: Only defined for minimal DFAs
    bool operator==(const DFA& other) const {
	assert(minimal() && other.minimal());
	// XXX: really want alphabet_ == other.alphabet_
	if (num_letters() != other.num_letters()) {
	    return false;
	}
	return equiv(backend_, other.backend_);
    }
    std::string to_dot() const {
	return wrapper_->visualize(true);
    }
    std::string to_regex() const {
	return wrapper_->to_regex();
    }
    friend std::ostream& operator<<(std::ostream& os, const DFA& dfa) {
	posint sink = dfa.sink_state();
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
	    os << dfa.alphabet_[letter - 1] << "\t";
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

// FUNCTION GRAPHS ============================================================

TUPLE_TAG(SRC);
TUPLE_TAG(TGT);
TUPLE_TAG(FLD);
TUPLE_TAG(FUN);

class Field {
    friend Registry<Field>;
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Field> ref;
private:
    explicit Field(const std::string& name, Ref<Field> ref)
	: name(name), ref(ref) {
	EXPECT(boost::regex_match(name, boost::regex("\\w+")));
    }
    bool merge() {
	return false;
    }
};

class Variable {
    friend Registry<Variable>;
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Variable> ref;
private:
    explicit Variable(const std::string& name, Ref<Variable> ref)
	: name(name), ref(ref) {
	EXPECT(boost::regex_match(name, boost::regex("\\w+")));
    }
    bool merge() {
	return false;
    }
};

class Function {
    friend Registry<Function>;
    typedef std::string Key;
public:
    typedef mi::MultiIndex<
                mi::Index<SRC, Ref<Variable>,
                    mi::Table<TGT, Ref<Variable>>>,
                mi::Index<TGT, Ref<Variable>,
                    mi::Table<SRC, Ref<Variable>>>> VarPairs;
public:
    const std::string name;
    const Ref<Function> ref;
private:
    Registry<Variable> vars;
    Ref<Variable> entry;
    std::set<Ref<Variable>> exits;
    VarPairs epsilons;
    mi::Index<FLD, Ref<Field>, VarPairs> opens;
    mi::Index<FLD, Ref<Field>, VarPairs> closes;
    mi::Index<FUN, Ref<Function>, VarPairs> calls;
private:
    explicit Function(const std::string& name, Ref<Function> ref)
	: name(name), ref(ref) {
	EXPECT(boost::regex_match(name, boost::regex("\\w+")));
    }
    bool merge() {
	return false;
    }
public:
    void parse_file(const fs::path& fpath,
		    Registry<Function>& fun_reg, Registry<Field>& fld_reg) {
	assert(!complete());
	std::ifstream fin(fpath.string());
	EXPECT((bool) fin);

	bool vars_done = false;
	std::string line;
	while (std::getline(fin, line)) {
	    boost::trim(line);
	    if (line.empty()) {
		continue; // Empty lines are ignored.
	    }
	    std::vector<std::string> toks;
	    boost::split(toks, line, boost::is_any_of(" "),
			 boost::token_compress_on);

	    if (toks[0] == "#") {
		EXPECT(toks.size() == 1);
		EXPECT(!vars_done);
		EXPECT(entry.valid());
		EXPECT(!exits.empty());
		vars_done = true;
	    } else if (!vars_done) {
		EXPECT(toks.size() >= 1);
		Variable& v = vars.make(toks[0]);
		for (auto it = toks.begin() + 1; it != toks.end(); ++it) {
		    if (*it == "in") {
			EXPECT(!entry.valid());
			entry = v.ref;
		    } else if (*it == "out") {
			exits.insert(v.ref);
		    } else {
			EXPECT(false);
		    }
		}
	    } else {
		EXPECT(toks.size() == 2 || toks.size() == 3);
		Ref<Variable> src = vars.find(toks[0]).ref;
		Ref<Variable> tgt = vars.find(toks[1]).ref;
		if (toks.size() == 2) {
		    epsilons.insert(src, tgt);
		} else if (toks[2][0] == '(') {
		    opens.insert(fld_reg.add(toks[2].substr(1)).ref,
				 src, tgt);
		} else if (toks[2][0] == ')') {
		    closes.insert(fld_reg.add(toks[2].substr(1)).ref,
				  src, tgt);
		} else {
		    calls.insert(fun_reg.add(toks[2]).ref, src, tgt);
		}
	    }
	}

	EXPECT(fin.eof());
	EXPECT(vars_done);
    }
    bool complete() const {
	return entry.valid();
    }
    void to_dot(std::ostream& os, const Registry<Function>& fun_reg,
		const Registry<Field>& fld_reg) const {
	os << "digraph " << name << " {" << std::endl;
	os << "  rankdir=LR;" << std::endl;
	os << "  node [shape=plaintext,label=\"\"] __phantom__;" << std::endl;
	for (const Variable& v : vars) {
	    os << "  node [shape=" << (exits.count(v.ref) > 0 ? "double" : "")
	       << "circle,color=black] " << v.name << ";" << std::endl;
	}
	os << "  __phantom__ -> " << vars[entry].name << " [color=blue];"
		   << std::endl;
	FOR(e, epsilons) {
	    os << "  " << vars[e.get<SRC>()].name << " -> "
	       << vars[e.get<TGT>()].name << ";" << std::endl;
	}
	FOR(o, opens) {
	    os << "  " << vars[o.get<SRC>()].name << " -> "
	       << vars[o.get<TGT>()].name << " [label=\"("
	       << fld_reg[o.get<FLD>()].name << "\"];" << std::endl;
	}
	FOR(c, closes) {
	    os << "  " << vars[c.get<SRC>()].name << " -> "
	       << vars[c.get<TGT>()].name << " [label=\")"
	       << fld_reg[c.get<FLD>()].name << "\"];" << std::endl;
	}
	FOR(c, calls) {
	    os << "  " << vars[c.get<SRC>()].name << " -> "
	       << vars[c.get<TGT>()].name << " [label=\""
	       << fun_reg[c.get<FUN>()].name << "\"];" << std::endl;
	}
	os << "}" << std::endl;
    }
};

// TOP-LEVEL CODE =============================================================

template<class Symbol>
void test_minimization(DFA<Symbol>& dfa) {
    std::cout << dfa;
    std::cout << dfa.to_regex() << std::endl;
    dfa.minimize();
    std::cout << "Minimized:" << std::endl;
    std::cout << dfa;
    std::cout << dfa.to_regex() << std::endl;
    std::cout << std::endl;
}

template<class Symbol>
void test_folding(DFA<Symbol>& dfa, posint depth) {
    std::cout << dfa;
    std::cout << dfa.to_regex() << std::endl;
    dfa.fold(depth);
    std::cout << "Folded, k = " << depth << ":" << std::endl;
    std::cout << dfa;
    std::cout << dfa.to_regex() << std::endl;
    std::cout << std::endl;
}

void test_dfa_code() {
    std::vector<std::string> alph_ab = {"alpha","beta"};
    std::vector<std::string> alph_abc = {"alpha", "beta", "gamma"};
    std::vector<std::string> alph_abcde = {"A","B", "C", "D", "E"};

    DFA<std::string> d1(alph_ab, 3);
    d1.set_initial(0);
    d1.set_final(1);
    d1.add_symb_trans(0, "alpha", 1);
    d1.add_symb_trans(1, "alpha", 1);
    d1.add_symb_trans(1, "beta", 2);
    d1.add_symb_trans(2, "alpha", 2);
    std::cout << "d1:" << std::endl;
    test_minimization(d1);

    NFA<std::string> n(alph_ab, 4);
    n.set_initial(3);
    n.set_final(1);
    n.add_eps_trans(3, 0);
    n.add_symb_trans(0, "alpha", 1);
    n.add_symb_trans(1, "alpha", 1);
    n.add_symb_trans(1, "beta", 2);
    n.add_symb_trans(2, "alpha", 2);
    std::cout << "n:" << std::endl;
    std::cout << n;
    DFA<std::string> nd(std::move(n));
    std::cout << "Determinized:" << std::endl;
    test_minimization(nd);

    std::cout << "d1 and n are " << ((d1 == nd) ? "" : "NOT ") << "equal"
	      << std::endl;
    std::cout << std::endl;

    DFA<std::string> d2(alph_ab, 2);
    d2.set_initial(1);
    d2.set_final(0);
    d2.add_symb_trans(1, "alpha", 0);
    d2.add_symb_trans(0, "alpha", 0);
    std::cout << "d2:" << std::endl;
    test_minimization(d2);

    std::cout << "d1 and d2 are " << ((d1 == d2) ? "" : "NOT ") << "equal"
	      << std::endl;
    std::cout << std::endl;

    DFA<std::string> d3(alph_abc, 2);
    d3.set_initial(0);
    d3.set_final(1);
    d3.add_symb_trans(0, "alpha", 1);
    d3.add_symb_trans(1, "beta", 1);
    d3.add_symb_trans(1, "gamma", 1);
    std::cout << "d3:" << std::endl;
    test_minimization(d3);

    DFA<std::string> d1U3 = d1 | d3;
    std::cout << "d1 U d3:" << std::endl;
    test_minimization(d1U3);

    DFA<std::string> aaaa(alph_abc, 5);
    aaaa.set_initial(0);
    aaaa.set_final(4);
    aaaa.add_symb_trans(0, "alpha", 1);
    aaaa.add_symb_trans(1, "alpha", 2);
    aaaa.add_symb_trans(2, "alpha", 3);
    aaaa.add_symb_trans(3, "alpha", 4);
    std::cout << "aaaa" << std::endl;
    test_folding(aaaa, 1);

    DFA<std::string> aacUbad(alph_abcde, 6);
    aacUbad.set_initial(0);
    aacUbad.set_final(3);
    aacUbad.add_symb_trans(0, "A", 1);
    aacUbad.add_symb_trans(1, "A", 2);
    aacUbad.add_symb_trans(2, "C", 3);
    aacUbad.add_symb_trans(0, "B", 4);
    aacUbad.add_symb_trans(4, "A", 5);
    aacUbad.add_symb_trans(5, "D", 3);
    std::cout << "aacUbad" << std::endl;
    test_folding(aacUbad, 1);
}

int main(int argc, char* argv[]) {
    // User-defined parameters
    std::string funs_dirname;

    // Parse options
    po::options_description desc("Options");
    desc.add_options()
	("help,h", "Print help message")
	("graph-dir", po::value<std::string>(&funs_dirname)->required(),
	 "Directory of function graphs");
    po::positional_options_description pos_desc;
    pos_desc.add("graph-dir", 1);
    po::variables_map vm;
    try {
	po::store(po::command_line_parser(argc, argv)
		  .options(desc).positional(pos_desc).run(), vm);
	if (vm.count("help") > 0) {
	    // TODO: Also print usage
	    std::cerr << desc << std::endl;
	    return EXIT_FAILURE;
	}
	po::notify(vm);
    } catch (const po::error& e) {
        std::cerr << e.what() << std::endl;
	return EXIT_FAILURE;
    }

    // Parse function graphs
    const std::string FILE_EXTENSION = ".fun.tgf";
    Registry<Function> funs;
    Registry<Field> flds;
    for (const fs::path& path : Directory(funs_dirname)) {
	std::string base(path.filename().string());
	if (!boost::algorithm::ends_with(base, FILE_EXTENSION)) {
	    continue;
	}
	size_t name_len = base.size() - FILE_EXTENSION.size();
	std::string name(base.substr(0, name_len));
	std::cout << "Parsing function " << name << std::endl;
	Function& f = funs.add(name);
	f.parse_file(path, funs, flds);
    }
    for (const Function& f : funs) {
	EXPECT(f.complete());
    }
}
