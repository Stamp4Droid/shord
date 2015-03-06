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
#include <utility>

#define LIBAMORE_LIBRARY_COMPILATION
#include "amore/dfa.h"
#include "amore/global.h"
#include "amore/testBinary.h"
#include "amore++/deterministic_finite_automaton.h"

#include "util.hpp"

namespace fs = boost::filesystem;
namespace po = boost::program_options;

// AUTOMATA ===================================================================

TUPLE_TAG(FROM);
TUPLE_TAG(LETTER);
TUPLE_TAG(TO);

typedef struct dfauto DfaBackend;
typedef amore::deterministic_finite_automaton DfaWrapper;

template<class Symbol> class DFA;

template<class T> class LightSet {
private:
    std::vector<T> array_;
public:
    explicit LightSet() {}
    explicit LightSet(const std::set<T>& src)
        : array_(src.begin(), src.end()) {}
    LightSet(const LightSet&) = delete;
    LightSet(LightSet&&) = default;
    LightSet& operator=(const LightSet&) = delete;
    bool insert(const T& val) {
        auto pos = std::equal_range(array_.begin(), array_.end(), val);
        if (pos.first != pos.second) {
            return false;
        }
        array_.insert(pos.first, val);
        return true;
    }
    unsigned size() const {
        return array_.size();
    }
    typename std::vector<T>::const_iterator begin() const {
        return array_.begin();
    }
    typename std::vector<T>::const_iterator end() const {
        return array_.end();
    }
    bool operator<(const LightSet& rhs) const {
        return set_compare(*this, rhs) < 0;
    }
};

// Doesn't require a pre-defined number of states.
template<class Symbol> class NFA {
private:
    std::vector<Symbol> alphabet_;
    std::set<posint> initial_;
    std::set<posint> final_;
    mi::Index<FROM, posint,
        mi::Index<LETTER, posint,
            mi::Table<TO, posint> > > trans_;
public:
    template<typename C>
    explicit NFA(const C& alphabet)
        : alphabet_(alphabet.begin(), alphabet.end()) {
        // Sort the set of symbols, and remove duplicates.
        std::sort(alphabet_.begin(), alphabet_.end());
        alphabet_.erase(std::unique(alphabet_.begin(), alphabet_.end()),
                        alphabet_.end());
        // No initial or final states set, no transitions added
    }
    NFA(const NFA&) = delete;
    NFA(NFA&& rhs) = default;
    NFA& operator=(const NFA&) = delete;
    const std::vector<Symbol>& alphabet() const {
        return alphabet_;
    }
    // letters: 1..N
    // 0 is reserved for epsilon
    posint num_letters() const {
        return alphabet_.size();
    }
    const std::set<posint> initial() const {
        return initial_;
    }
    const std::set<posint> final() const {
        return final_;
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
        assert(letter <= num_letters());
        trans_.insert(src, letter, tgt);
    }
    void set_initial(posint state) {
        initial_.insert(state);
    }
    void set_final(posint state) {
        final_.insert(state);
    }
    // TODO: Could cache eps_close results (but without emiting d-states for
    // the non-closed n-state sets).
    void eps_close(LightSet<posint>& states) const {
        std::deque<posint> worklist(states.begin(), states.end());
        while (!worklist.empty()) {
            posint src = worklist.front();
            for (posint tgt : trans_[src][0]) {
                if (states.insert(tgt)) {
                    worklist.push_back(tgt);
                }
            }
            worklist.pop_front();
        }
    }
    DfaWrapper* determinize() const {
        // TODO: Can check if the NFA already contains no epsilons before
        // going through this process.
        // TODO: More efficient way to store sets of sets of states?
        Worklist<LightSet<posint>,false> worklist;
        // Enqueue initial state set.
        LightSet<posint> init_ns_set(initial_);
        eps_close(init_ns_set);
        posint init_ds = worklist.enqueue(std::move(init_ns_set))->second;
        // Discover reachable state sets and fill out transitions.
        std::vector<std::vector<posint> > dtrans;
        while (!worklist.empty()) {
            auto src_nsds = worklist.dequeue();
            // dstates processed in order
            assert(src_nsds->second == dtrans.size());
            dtrans.emplace_back(num_letters() + 1,
                                std::numeric_limits<posint>::max());
            for (posint letter = 1; letter <= num_letters(); letter++) {
                LightSet<posint> tgt_ns_set;
                for (posint src_ns : src_nsds->first) {
                    for (posint tgt_ns : trans_[src_ns][letter]) {
                        tgt_ns_set.insert(tgt_ns);
                    }
                }
                eps_close(tgt_ns_set);
                posint tgt_ds =
                    worklist.enqueue(std::move(tgt_ns_set))->second;
                dtrans.back()[letter] = tgt_ds;
            }
        }
        assert(dtrans.size() == worklist.num_reached());
        // Construct product DFA in AMoRE format.
        DfaBackend* res =
            DFA<Symbol>::alloc_backend(num_letters(), worklist.num_reached());
        res->init = init_ds;
        for (const auto& nsds : worklist.reached()) {
            if (!empty_intersection(nsds.first, final_)) {
                setfinal(res->final[nsds.second], true);
            }
        }
        for (posint src = 0; src < worklist.num_reached(); src++) {
            const std::vector<posint>& from_src = dtrans[src];
            for (posint letter = 1; letter <= num_letters(); letter++) {
                res->delta[letter][src] = from_src[letter];
            }
        }
        return new DfaWrapper(res);
    }
    template<class... Rest>
    void print(std::ostream& os, const Rest&... rest) const {
        os << "Initial: ";
        for (posint state : initial_) {
            os << state << " ";
        }
        os << std::endl;
        os << "Final: ";
        for (posint state : final_) {
            os << state << " ";
        }
        os << std::endl;
        FOR(t, trans_) {
            os << t.template get<FROM>() << " --";
            posint letter = t.template get<LETTER>();
            if (letter > 0) {
                alphabet_[letter - 1].print(os, rest...);
            } else {
                os << "@epsilon";
            }
            os << "-> " << t.template get<TO>() << std::endl;
        }
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
        NFA<Symbol> res(alphabet_);
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
        //                      alphabet_.begin(), alphabet_.end()));
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
        backend_ = alloc_backend(alphabet_.size(), num_states + 1);
        wrapper_ = new DfaWrapper(backend_);
    }
    // The last state is assumed to be the sink state.
    static DfaBackend* alloc_backend(posint num_letters, posint num_states) {
        posint sink = num_states - 1;
        // no final state set
        // reserves 1 extra slot for sink state
        char* fin_bitset = newfinal(num_states - 1);
        // reserves 1 extra row for epsilon, and 1 extra slot for sink state
        // doesn't allocate an actual row for epsilon (letter #0)
        posint** delta = newddelta(num_letters, num_states - 1);
        // all valid transitions move to the sink state
        for (posint i = 1; i <= num_letters; i++) {
            std::fill_n(delta[i], num_states, sink);
        }
        DfaBackend* res = newdfa();
        res->highest_state = sink;
        res->init = sink;
        res->alphabet_size = num_letters;
        res->final = fin_bitset;
        res->delta = delta;
        res->minimal = false;
        return res;
    }
    explicit DFA(const NFA<Symbol>& nfa)
        : DFA(std::vector<Symbol>(nfa.alphabet()), nfa.determinize()) {
        // This creates a copy of the NFA's alphabet
        assert(!backend_->minimal); // ensures minimal() => no useless letters
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
    friend void swap(DFA& a, DFA& b) {
        using std::swap;
        swap(a.alphabet_, b.alphabet_);
        swap(a.backend_,  b.backend_);
        swap(a.wrapper_,  b.wrapper_);
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
    posint follow(posint src, posint letter) const {
        return backend_->delta[letter][src];
    }
    const std::vector<Symbol>& alphabet() const {
        return alphabet_;
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
        SetPartition part = k_equiv(depth);
        std::cout << "    Partition calculated" << std::endl;
        NFA<Symbol> folded = state_merge(part);
        std::cout << "    States merged" << std::endl;
        wrapper_ = folded.determinize();
        std::cout << "    Widened FSM determinized" << std::endl;
        backend_ = wrapper_->get_dfa();
        assert(!backend_->minimal); // ensures minimal() => no useless letters
    }
    // TODO: Doing simple product construction; could try a more efficient
    // algorithm.
    // We assume that most combinations of states won't be reachable, so we
    // don't immediately take the cartesian product of states.
    DFA operator|(const DFA& other) const {
        // Combine the alphabets of the two machines.
        std::vector<Symbol> joint_alph;
        std::set_union(alphabet_.begin(), alphabet_.end(),
                       other.alphabet_.begin(), other.alphabet_.end(),
                       std::back_inserter(joint_alph));
        // Extend machines to the common alphabet.
        DfaWrapper* a_wrap = extend_alph(joint_alph);
        DfaWrapper* b_wrap = other.extend_alph(joint_alph);
        assert(a_wrap->get_alphabet_size() == joint_alph.size());
        assert(b_wrap->get_alphabet_size() == joint_alph.size());
        DfaBackend* a = a_wrap->get_dfa();
        DfaBackend* b = b_wrap->get_dfa();
        // Produce union machine.
        Worklist<std::pair<posint,posint>,false> worklist;
        posint aUb_init =
            worklist.enqueue(std::make_pair(a->init, b->init))->second;
        std::vector<std::vector<posint> > aUb_trans;
        while (!worklist.empty()) {
            auto pU_src = worklist.dequeue();
            posint a_src = pU_src->first.first;
            posint b_src = pU_src->first.second;
            posint aUb_src = pU_src->second;
            // union states processed in order
            assert(aUb_src == aUb_trans.size());
            aUb_trans.emplace_back(joint_alph.size() + 1,
                                   std::numeric_limits<posint>::max());
            for (posint letter = 1; letter <= joint_alph.size(); letter++) {
                posint a_tgt = a->delta[letter][a_src];
                posint b_tgt = b->delta[letter][b_src];
                posint aUb_tgt =
                    worklist.enqueue(std::make_pair(a_tgt, b_tgt))->second;
                aUb_trans.back()[letter] = aUb_tgt;
            }
        }
        assert(aUb_trans.size() == worklist.num_reached());
        // Convert result DFA to AMoRE format.
        DfaBackend* aUb = DFA<Symbol>::alloc_backend(joint_alph.size(),
                                                     worklist.num_reached());
        aUb->init = aUb_init;
        for (const auto& pU : worklist.reached()) {
            if (isfinal(a->final[pU.first.first]) ||
                isfinal(b->final[pU.first.second])) {
                setfinal(aUb->final[pU.second], true);
            }
        }
        for (posint src = 0; src < worklist.num_reached(); src++) {
            const std::vector<posint>& from_src = aUb_trans[src];
            for (posint letter = 1; letter <= joint_alph.size(); letter++) {
                aUb->delta[letter][src] = from_src[letter];
            }
        }
        // Clean up temporary machines before returning.
        delete a_wrap;
        delete b_wrap;
        return DFA(std::move(joint_alph), new DfaWrapper(aUb));
    }
    // TODO: Only defined for minimal DFAs
    bool operator==(const DFA& other) const {
        assert(minimal() && other.minimal());
        // XXX: really want alphabet_ == other.alphabet_
        if (num_letters() != other.num_letters()) {
            // We trim letters before minimizing, therefore the presence of
            // some letter in the alphabet means there's at least one sentence
            // in the automaton that contains it. Thus, a difference in the
            // alphabet automatically means that the automata are different.
            return false;
        }
        return equiv(backend_, other.backend_);
    }
    bool operator<=(const DFA& other) const {
        // Combine the alphabets of the two machines.
        std::vector<Symbol> joint_alph;
        std::set_union(alphabet_.begin(), alphabet_.end(),
                       other.alphabet_.begin(), other.alphabet_.end(),
                       std::back_inserter(joint_alph));
        // Extend machines to the common alphabet.
        DfaWrapper* a_wrap = extend_alph(joint_alph);
        DfaWrapper* b_wrap = other.extend_alph(joint_alph);
        assert(a_wrap->get_alphabet_size() == joint_alph.size());
        assert(b_wrap->get_alphabet_size() == joint_alph.size());
        DfaBackend* a = a_wrap->get_dfa();
        DfaBackend* b = b_wrap->get_dfa();
        // Traverse union, stop immediatelly if you reach a state pair <q1,q2>
        // where q1 is final and q2 is not (on their respective machines).
        bool is_subset = true;
        Worklist<std::pair<posint,posint>,false> worklist;
        worklist.enqueue(std::make_pair(a->init, b->init));
        while (!worklist.empty()) {
            auto pU_src = worklist.dequeue();
            posint a_src = pU_src->first.first;
            posint b_src = pU_src->first.second;
            if (isfinal(a->final[a_src]) && !isfinal(b->final[b_src])) {
                is_subset = false;
                break;
            }
            for (posint letter = 1; letter <= joint_alph.size(); letter++) {
                posint a_tgt = a->delta[letter][a_src];
                posint b_tgt = b->delta[letter][b_src];
                worklist.enqueue(std::make_pair(a_tgt, b_tgt));
            }
        }
        // Clean up temporary machines before returning.
        delete a_wrap;
        delete b_wrap;
        return is_subset;
    }
    template<class... Rest>
    void to_tgf(std::ostream& os, const Rest&... rest) const {
        posint sink = sink_state();
        for (posint state = 0; state < num_states(); state++) {
            if (state == sink) {
                continue;
            }
            os << state;
            if (state == initial()) {
                os << " in";
            }
            if (is_final(state)) {
                os << " out";
            }
            os << std::endl;
        }
        os << "#" << std::endl;
        posint** delta = backend_->delta;
        for (posint letter = 1; letter <= num_letters(); letter++) {
            for (posint src = 0; src < num_states(); src++) {
                if (src == sink) {
                    continue;
                }
                posint tgt = delta[letter][src];
                if (tgt == sink) {
                    continue;
                }
                os << src << " " << tgt << " ";
                alphabet_[letter - 1].print(os, rest...);
                os << std::endl;
            }
        }
    }
    std::string to_dot() const {
        return wrapper_->visualize();
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
public:
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Field> ref;
public:
    explicit Field(const std::string* name_ptr, Ref<Field> ref)
        : name(*name_ptr), ref(ref) {
        EXPECT(boost::regex_match(name, boost::regex("\\w+")));
    }
    Field(const Field&) = delete;
    Field(Field&&) = default;
    Field& operator=(const Field&) = delete;
    bool merge() {
        return false;
    }
};

class Variable {
public:
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Variable> ref;
public:
    explicit Variable(const std::string* name_ptr, Ref<Variable> ref)
        : name(name_ptr != NULL ? *name_ptr
               : std::string("#") + std::to_string(ref.value())),
          ref(ref) {
        if (name_ptr != NULL) {
            EXPECT(boost::regex_match(name, boost::regex("\\w+")));
        }
    }
    Variable(const Variable&) = default;
    Variable(Variable&&) = default;
    Variable& operator=(const Variable&) = delete;
    bool merge() {
        return false;
    }
};

class Delimiter {
public:
    bool is_open;
    Ref<Field> fld;
public:
    explicit Delimiter(bool is_open, Ref<Field> fld)
        : is_open(is_open), fld(fld) {}
    Delimiter(const Delimiter&) = default;
    Delimiter& operator=(const Delimiter&) = default;
    friend int compare(const Delimiter& lhs, const Delimiter& rhs) {
        int open_comp = compare(lhs.is_open, rhs.is_open);
        if (open_comp != 0) {
            return open_comp;
        }
        return compare(lhs.fld, rhs.fld);
    }
    bool operator<(const Delimiter& rhs) const {
        return compare(*this, rhs) < 0;
    }
    bool operator==(const Delimiter& rhs) const {
        return compare(*this, rhs) == 0;
    }
    void print(std::ostream& os, const Registry<Field>& fld_reg) const {
        os << (is_open ? "(" : ")") << fld_reg[fld].name;
    }
};

typedef DFA<Delimiter> Signature;

// Only covers intra-method edges.
class CodeGraph {
public:
    Registry<Variable> vars;
    Ref<Variable> entry;
    std::set<Ref<Variable> > exits;
    mi::MultiIndex<mi::Index<SRC, Ref<Variable>,
                             mi::Table<TGT, Ref<Variable> > >,
                   mi::Index<TGT, Ref<Variable>,
                             mi::Table<SRC, Ref<Variable> > > > epsilons;
    mi::MultiIndex<mi::Index<TGT, Ref<Variable>,
                             mi::Index<FLD, Ref<Field>,
                                       mi::Table<SRC, Ref<Variable> > > >,
                   mi::Table<FLD, Ref<Field> > > opens;
    mi::MultiIndex<mi::Index<SRC, Ref<Variable>,
                             mi::Index<FLD, Ref<Field>,
                                       mi::Table<TGT, Ref<Variable> > > >,
                   mi::Table<FLD, Ref<Field> > > closes;
public:
    explicit CodeGraph() {}
    CodeGraph(const CodeGraph&) = default;
    CodeGraph(CodeGraph&&) = default;
    CodeGraph& operator=(const CodeGraph&) = delete;
    friend void swap(CodeGraph& a, CodeGraph& b) {
        using std::swap;
        swap(a.vars,     b.vars);
        swap(a.entry,    b.entry);
        swap(a.exits,    b.exits);
        swap(a.epsilons, b.epsilons);
        swap(a.opens,    b.opens);
        swap(a.closes,   b.closes);
    }
    void embed(Ref<Variable> src, Ref<Variable> tgt, const Signature& callee) {
        // Create temporary variables for all states in callee's signature
        // (except the sink state).
        // TODO: Could do this more efficiently, since Ref's are allocated
        // serially.
        std::vector<Ref<Variable> > state2var;
        state2var.reserve(callee.num_states());
        for (posint state = 0; state < callee.num_states(); state++) {
            // TODO: Emitting a state for the sink.
            state2var.push_back(vars.mktemp().ref);
        }
        // Copy all transitions, except those to/from the sink state.
        posint sink = callee.sink_state();
        for (posint letter = 1; letter <= callee.num_letters(); letter++) {
            const Delimiter& delim = callee.alphabet()[letter - 1];
            for (posint src = 0; src < callee.num_states(); src++) {
                if (src == sink) {
                    continue;
                }
                posint tgt = callee.follow(src, letter);
                if (tgt == sink) {
                    continue;
                }
                if (delim.is_open) {
                    opens.insert(state2var[tgt], delim.fld, state2var[src]);
                } else {
                    closes.insert(state2var[src], delim.fld, state2var[tgt]);
                }
            }
        }
        // Connect the newly constructed subgraph to the rest of the code.
        epsilons.insert(src, state2var[callee.initial()]);
        for (posint state = 0; state < callee.num_states(); state++) {
            if (callee.is_final(state)) {
                epsilons.insert(state2var[state], tgt);
            }
        }
    }
    // TODO: Could do this more efficiently using a rescheduling approach.
    void close() {
        // Initialize reachability worklist
        std::deque<std::pair<Ref<Variable>,Ref<Variable> > > worklist;
        for (const Variable& v : vars) {
            // Each variable is reachable from itself.
            worklist.emplace_back(v.ref, v.ref);
        }
        FOR(e, epsilons) {
            // A base epsilon transition (a,b) means b is reachable from a.
            worklist.emplace_back(e.get<SRC>(), e.get<TGT>());
        }
        // Combine base reachability information up to fixpoint.
        auto add_pair = [&](Ref<Variable> src, Ref<Variable> tgt) {
            if (src == tgt || epsilons.contains(src, tgt)) {
                return;
            }
            epsilons.insert(src, tgt);
            worklist.emplace_back(src, tgt);
        };
        auto add_product = [&](const mi::Table<SRC,Ref<Variable> >& srcs,
                               const mi::Table<TGT,Ref<Variable> >& tgts,
                               Ref<Field>) {
            for (Ref<Variable> s : srcs) {
                for (Ref<Variable> t : tgts) {
                    add_pair(s, t);
                }
            }
        };
        while (!worklist.empty()) {
            Ref<Variable> src, tgt;
            std::tie(src, tgt) = worklist.front();
            // Extend to the left:
            // pre_src --> src --> tgt => pre_src --> tgt
            for (Ref<Variable> pre_src : epsilons.sec<0>()[src]) {
                add_pair(pre_src, tgt);
            }
            // Extend to the right:
            // src --> tgt --> post_tgt => src --> post_tgt
            for (Ref<Variable> post_tgt : epsilons.pri()[tgt]) {
                add_pair(src, post_tgt);
            }
            // Match closes and opens:
            // a --(--> src --> tgt --)--> b => a --> b
            join_zip<1>(opens.pri()[src], closes.pri()[tgt], add_product);
            worklist.pop_front();
        }
    }
    // Simply make a copy of each state, and leave it to the DFA library to
    // remove any unreachable states.
    void pn_extend() {
        using std::swap;
        // The original set of variables form the P-partition. Make a copy of
        // each variable for the N-partition.
        posint orig_vars = vars.size();
        for (posint i = 0; i < orig_vars; i++) {
            vars.mktemp();
        }
        auto to_nvar = [orig_vars](Ref<Variable> pvar) -> Ref<Variable> {
            // XXX: Assumes Ref's are allocated serially.
            return Ref<Variable>(pvar.value() + orig_vars);
        };
        // Only the P-partition entry remains an entry on the PN-extended
        // machine (no change needed).
        // Both P-partition and N-partition exits are valid.
        std::set<Ref<Variable> > new_exits;
        for (Ref<Variable> v : exits) {
            new_exits.insert(v);
            new_exits.insert(to_nvar(v));
        }
        swap(exits, new_exits);
        // Epsilon transitions are copied on the N-partition.
        decltype(epsilons) new_epsilons;
        FOR(e, epsilons) {
            new_epsilons.insert(e.get<SRC>(), e.get<TGT>());
            new_epsilons.insert(to_nvar(e.get<SRC>()), to_nvar(e.get<TGT>()));
        }
        swap(epsilons, new_epsilons);
        // Opens are moved to the N-partition, and also serve as transitions
        // from P to N.
        decltype(opens) new_opens;
        FOR(o, opens) {
            new_opens.insert(to_nvar(o.get<TGT>()), o.get<FLD>(),
                             to_nvar(o.get<SRC>()));
            new_opens.insert(to_nvar(o.get<TGT>()), o.get<FLD>(),
                             o.get<SRC>());
        }
        swap(opens, new_opens);
        // Closes remain only between variables on the P-partition.
    }
    Signature to_sig() const {
        // Assumes at least an entry variable has been set.
        assert(entry.valid());
        // Collect all used delimiters, to form the FSM's alphabet.
        std::vector<Delimiter> delims;
        for (Ref<Field> fld : opens.sec<0>()) {
            delims.emplace_back(true, fld);
        }
        for (Ref<Field> fld : closes.sec<0>()) {
            delims.emplace_back(false, fld);
        }
        // Build the FSM.
        // XXX: Assumes Ref's are allocated serially.
        NFA<Delimiter> nfa(delims);
        nfa.set_initial(entry.value());
        for (Ref<Variable> v : exits) {
            nfa.set_final(v.value());
        }
        FOR(e, epsilons) {
            nfa.add_eps_trans(e.get<SRC>().value(), e.get<TGT>().value());
        }
        FOR(o, opens) {
            Delimiter d(true, o.get<FLD>());
            nfa.add_symb_trans(o.get<SRC>().value(), d, o.get<TGT>().value());
        }
        FOR(c, closes) {
            Delimiter d(false, c.get<FLD>());
            nfa.add_symb_trans(c.get<SRC>().value(), d, c.get<TGT>().value());
        }
        // Determinize and minimize the FSM.
        std::cout << "    NFA constructed" << std::endl;
        Signature res(nfa);
        std::cout << "    NFA determinized" << std::endl;
        res.minimize();
        std::cout << "    DFA minimized" << std::endl;
        return res;
    }
    void to_dot(std::ostream& os, const Registry<Field>& fld_reg) const {
        os << "digraph function {" << std::endl;
        os << "  rankdir=LR;" << std::endl;
        os << "  node [shape=plaintext,label=\"\"] __phantom__;" << std::endl;
        for (const Variable& v : vars) {
            os << "  node [shape="
               << (exits.count(v.ref) > 0 ? "double" : "")
               << "circle,color=black,label=\"" << v.name << "\"] \""
               << v.name << "\";" << std::endl;
        }
        os << "  __phantom__ -> " << vars[entry].name
           << " [color=blue];" << std::endl;
        FOR(e, epsilons) {
            os << "  \"" << vars[e.get<SRC>()].name << "\" -> \""
               << vars[e.get<TGT>()].name << "\";" << std::endl;
        }
        FOR(o, opens) {
            os << "  \"" << vars[o.get<SRC>()].name << "\" -> \""
               << vars[o.get<TGT>()].name << "\" [label=\"("
               << fld_reg[o.get<FLD>()].name << "\"];" << std::endl;
        }
        FOR(c, closes) {
            os << "  \"" << vars[c.get<SRC>()].name << "\" -> \""
               << vars[c.get<TGT>()].name << "\" [label=\")"
               << fld_reg[c.get<FLD>()].name << "\"];" << std::endl;
        }
        os << "}" << std::endl;
    }
};

class Function {
public:
    typedef std::string Key;
    const posint WIDENING_K = 2;
public:
    const std::string name;
    const Ref<Function> ref;
private:
    CodeGraph code_;
    mi::Index<FUN, Ref<Function>,
        mi::Index<SRC, Ref<Variable>,
            mi::Table<TGT, Ref<Variable> > > > calls_;
    std::set<Ref<Function> > callers_;
    Signature sig_; // Initially set to the empty automaton.
public:
    explicit Function(const std::string* name_ptr, Ref<Function> ref)
        : name(*name_ptr), ref(ref), sig_(std::vector<Delimiter>(), 1) {
        EXPECT(boost::regex_match(name, boost::regex("\\w+")));
        sig_.set_initial(0);
        sig_.minimize();
    }
    Function(const Function&) = delete;
    Function(Function&&) = default;
    Function& operator=(const Function&) = delete;
    bool merge() {
        return false;
    }
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
                EXPECT(code_.entry.valid());
                vars_done = true;
            } else if (!vars_done) {
                EXPECT(toks.size() >= 1);
                Variable& v = code_.vars.make(toks[0]);
                for (auto it = toks.begin() + 1; it != toks.end(); ++it) {
                    if (*it == "in") {
                        EXPECT(!code_.entry.valid());
                        code_.entry = v.ref;
                    } else if (*it == "out") {
                        code_.exits.insert(v.ref);
                    } else {
                        EXPECT(false);
                    }
                }
            } else {
                EXPECT(toks.size() == 2 || toks.size() == 3);
                Ref<Variable> src = code_.vars.find(toks[0]).ref;
                Ref<Variable> tgt = code_.vars.find(toks[1]).ref;
                if (toks.size() == 2) {
                    code_.epsilons.insert(src, tgt);
                } else if (toks[2][0] == '(') {
                    Ref<Field> fld = fld_reg.add(toks[2].substr(1)).ref;
                    code_.opens.insert(tgt, fld, src);
                } else if (toks[2][0] == ')') {
                    Ref<Field> fld = fld_reg.add(toks[2].substr(1)).ref;
                    code_.closes.insert(src, fld, tgt);
                } else {
                    Function& callee = fun_reg.add(toks[2]);
                    callee.callers_.insert(ref);
                    calls_.insert(callee.ref, src, tgt);
                }
            }
        }
        EXPECT(fin.eof());
        EXPECT(vars_done);
    }
    bool complete() const {
        return code_.entry.valid();
    }
    const std::set<Ref<Function> >& callers() const {
        return callers_;
    }
    const Signature& sig() const {
        return sig_;
    }
    // Returns 'false' if we've reached fixpoint, and sig_ didn't need to be
    // updated. Otherwise updates sig_ and returns 'true'.
    bool update_sig(const Registry<Function>& fun_reg) {
        // Current sig_ is step i on the fixpoint process, S(i).
        const Signature& si = sig_;
        // Embed the latest signatures of callees, and produce minimal FSM to
        // form step i+1, F(S(i)).
        CodeGraph stage(code_);
        FOR(c, calls_) {
            // TODO: Assumes the numbering of the original vars doesn't change
            // on the clone.
            stage.embed(c.get<SRC>(), c.get<TGT>(),
                        fun_reg[c.get<FUN>()].sig_);
        }
        std::cout << "    Embedded calls" << std::endl;
        stage.close();
        std::cout << "    Internal matching done" << std::endl;
        stage.pn_extend();
        std::cout << "    PN-Extended" << std::endl;
        std::cout << "    " << stage.vars.size() << " vars, "
                  << (stage.epsilons.size() + stage.opens.size() +
                      stage.closes.size() + calls_.size()) << " edges"
                  << std::endl;
        Signature fsi = stage.to_sig();
        std::cout << "    New signature calculated" << std::endl;
        // Check if we've reached fixpoint.
        Signature siUfsi = si | fsi;
        std::cout << "    Unioned with previous sig" << std::endl;
        siUfsi.minimize();
        std::cout << "    Union minimized" << std::endl;
        if (siUfsi == si) { // equivalent to F(Si) <= Si
            return false;
        }
        std::cout << "    New sig was larger" << std::endl;
        // If not, widen S(i) U F(S(i)) and set as latest signature.
        siUfsi.fold(WIDENING_K);
        std::cout << "    Sig folded" << std::endl;
        siUfsi.minimize();
        swap(sig_, siUfsi);
        std::cout << "    Widened sig minimized" << std::endl;
        return true;
    }
    void to_dot(std::ostream& os, const Registry<Function>& fun_reg,
                const Registry<Field>& fld_reg) const {
        os << "digraph " << name << " {" << std::endl;
        os << "  rankdir=LR;" << std::endl;
        os << "  node [shape=plaintext,label=\"\"] __phantom__;" << std::endl;
        for (const Variable& v : code_.vars) {
            os << "  node [shape="
               << (code_.exits.count(v.ref) > 0 ? "double" : "")
               << "circle,color=black] " << v.name << ";" << std::endl;
        }
        os << "  __phantom__ -> " << code_.vars[code_.entry].name
           << " [color=blue];" << std::endl;
        FOR(e, code_.epsilons) {
            os << "  " << code_.vars[e.get<SRC>()].name << " -> "
               << code_.vars[e.get<TGT>()].name << ";" << std::endl;
        }
        FOR(o, code_.opens) {
            os << "  " << code_.vars[o.get<SRC>()].name << " -> "
               << code_.vars[o.get<TGT>()].name << " [label=\"("
               << fld_reg[o.get<FLD>()].name << "\"];" << std::endl;
        }
        FOR(c, code_.closes) {
            os << "  " << code_.vars[c.get<SRC>()].name << " -> "
               << code_.vars[c.get<TGT>()].name << " [label=\")"
               << fld_reg[c.get<FLD>()].name << "\"];" << std::endl;
        }
        FOR(c, calls_) {
            os << "  " << code_.vars[c.get<SRC>()].name << " -> "
               << code_.vars[c.get<TGT>()].name << " [label=\""
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
    std::vector<std::string> alph_abcde = {"A", "B", "C", "D", "E"};

    DFA<std::string> d1(alph_ab, 3);
    d1.set_initial(0);
    d1.set_final(1);
    d1.add_symb_trans(0, "alpha", 1);
    d1.add_symb_trans(1, "alpha", 1);
    d1.add_symb_trans(1, "beta", 2);
    d1.add_symb_trans(2, "alpha", 2);
    std::cout << "d1:" << std::endl;
    test_minimization(d1);

    NFA<std::string> n(alph_ab);
    n.set_initial(3);
    n.set_final(1);
    n.add_eps_trans(3, 0);
    n.add_symb_trans(0, "alpha", 1);
    n.add_symb_trans(1, "alpha", 1);
    n.add_symb_trans(1, "beta", 2);
    n.add_symb_trans(2, "alpha", 2);
    std::cout << "n:" << std::endl;
    DFA<std::string> nd(n);
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
    std::string indir_name;
    std::string outdir_name;

    // Parse options
    po::options_description desc("Options");
    desc.add_options()
        ("help,h", "Print help message")
        ("in", po::value<std::string>(&indir_name)->required(),
         "Directory of function graphs")
        ("out", po::value<std::string>(&outdir_name)->required(),
         "Directory to output signatures");
    po::positional_options_description pos_desc;
    pos_desc.add("in", 1);
    pos_desc.add("out", 1);
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
    for (const fs::path& path : Directory(indir_name)) {
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

    fs::path outdir = fs::path(outdir_name);
    fs::create_directory(outdir);
    // Update signatures up to fixpoint.
    Worklist<Ref<Function>,true> worklist;
    for (const Function& f : funs) {
        worklist.enqueue(f.ref);
    }
    while (!worklist.empty()) {
        Function& f = funs[worklist.dequeue()];
        std::cout << "Processing " << f.name << std::endl;
        std::cout << "    " << worklist.size() << " left in queue" << std::endl;
        if (!f.update_sig(funs)) {
            std::cout << "    No change" << std::endl;
            continue;
        }
        std::cout << "    Updated, rescheduling callers" << std::endl;
        for (Ref<Function> c : f.callers()) {
            if (worklist.enqueue(c)) {
                std::cout << "    rescheduled " << funs[c].name << std::endl;
            }
        }
        fs::path fpath(outdir/(f.name + ".sig.tgf"));
        // TODO: Empty signatures won't be printed.
        std::cout << "    Printing signature" << std::endl;
        std::ofstream fout(fpath.string());
        EXPECT((bool) fout);
        f.sig().to_tgf(fout, flds);
    }
}
