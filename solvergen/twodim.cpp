#include <boost/algorithm/string.hpp>
#include <boost/filesystem.hpp>
#include <boost/program_options.hpp>
#include <boost/regex.hpp>
#include <cassert>
#include <chrono>
#include <cstdlib>
#include <deque>
#include <fstream>
#include <iostream>
#include <iterator>
#include <limits>
#include <map>
#include <stack>
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

const std::string FILE_EXTENSION = ".fun.tgf";

// TIMERS =====================================================================

class Timer {
public:
    typedef std::chrono::milliseconds::rep TimeDiff;
private:
    typedef std::chrono::time_point<std::chrono::steady_clock> TimeStamp;
    struct Phase {
        TimeDiff ms_spent = 0;
        std::map<std::string,Phase> sub_phases;
    };
private:
    std::stack<std::pair<Phase*,TimeStamp> > tstamps_;
    std::map<std::string,Phase> totals_;
private:
    void print_indent() {
        for (unsigned i = 0; i < tstamps_.size(); i++) {
            std::cout << "    ";
        }
    }
    void print_all() {
        std::cout << std::endl;
    }
    template<class T, class... Rest>
    void print_all(const T& first, const Rest&... rest) {
        std::cout << first;
        print_all(rest...);
    }
    void print_rest() {
        std::cout << std::endl;
    }
    template<class T, class... Rest>
    void print_rest(const T& first, const Rest&... rest) {
        std::cout << ": " << first;
        print_all(rest...);
    }
    void print_tier(unsigned depth, const std::map<std::string,Phase>& tier) {
        for (const auto& p : tier) {
            for (unsigned i = 0; i < depth; i++) {
                std::cout << "    ";
            }
            std::cout << p.first << ": " << p.second.ms_spent << "ms"
                      << std::endl;
            print_tier(depth + 1, p.second.sub_phases);
        }
    }
public:
    template<class... Rest>
    void start(const std::string& name, const Rest&... rest) {
        print_indent();
        std::cout << name;
        print_rest(rest...);
        Phase* phase;
        if (tstamps_.empty()) {
            phase = &(totals_[name]);
        } else {
            phase = &(tstamps_.top().first->sub_phases[name]);
        }
        tstamps_.push(std::make_pair(phase, std::chrono::steady_clock::now()));
    }
    TimeDiff done() {
        assert(!tstamps_.empty());
        Phase* phase = tstamps_.top().first;
        TimeStamp t1 = tstamps_.top().second;
        TimeStamp t2 = std::chrono::steady_clock::now();
        TimeDiff d = std::chrono::duration_cast<std::chrono::milliseconds>
            (t2 - t1).count();
        phase->ms_spent += d;
        tstamps_.pop();
        print_indent();
        std::cout << d << "ms" << std::endl;
        return d;
    }
    template<class... Args>
    void log(const Args&... args) {
        print_indent();
        print_all(args...);
    }
    void print_stats() {
        assert(tstamps_.empty());
        print_tier(0, totals_);
    }
};

Timer timer;

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
    std::set<posint> states_;
    std::set<posint> initial_;
    std::set<posint> final_;
    mi::MultiIndex<
        mi::Index<FROM, posint,
            mi::Index<LETTER, posint,
                mi::Table<TO, posint> > >,
        mi::Index<FROM, posint,
            mi::Table<TO, posint> >,
        mi::Index<TO, posint,
            mi::Table<FROM, posint> >,
        mi::Index<TO, posint,
            mi::Index<LETTER, posint,
                mi::Table<FROM, posint> > > > trans_;
private:
    // TODO:
    // - Renumber states, to cover holes in numbering?
    // - Also clean alphabet?
    // - Perform directly on CodeGraph (do generic renumbering on Registries).
    void prune_states(std::set<posint>&& new_states) {
        using std::swap;
        std::set<posint> temp(std::move(new_states));
        swap(states_, temp);
        std::set<posint> new_initial;
        for (posint s : initial_) {
            if (states_.count(s) > 0) {
                new_initial.insert(s);
            }
        }
        swap(initial_, new_initial);
        std::set<posint> new_final;
        for (posint s : final_) {
            if (states_.count(s) > 0) {
                new_final.insert(s);
            }
        }
        swap(final_, new_final);
        decltype(trans_) new_trans;
        for (const auto& from_p : trans_.pri()) {
            posint from = from_p.first;
            if (states_.count(from) == 0) {
                continue;
            }
            FOR(arrow, from_p.second) {
                if (states_.count(arrow.template get<TO>()) == 0) {
                    continue;
                }
                new_trans.insert(from, arrow.template get<LETTER>(),
                                 arrow.template get<TO>());
            }
        }
        swap(trans_, new_trans);
    }
    // TODO: Could cache eps_close results (but without emiting d-states for
    // the non-closed n-state sets).
    void eps_close(LightSet<posint>& states) const {
        std::deque<posint> worklist(states.begin(), states.end());
        while (!worklist.empty()) {
            posint src = worklist.front();
            for (posint tgt : trans_.pri()[src][0]) {
                if (states.insert(tgt)) {
                    worklist.push_back(tgt);
                }
            }
            worklist.pop_front();
        }
    }
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
        states_.insert(src);
        states_.insert(tgt);
        trans_.insert(src, letter, tgt);
    }
    void set_initial(posint state) {
        states_.insert(state);
        initial_.insert(state);
    }
    void set_final(posint state) {
        states_.insert(state);
        final_.insert(state);
    }
    unsigned num_states() const {
        return states_.size();
    }
    unsigned num_trans() const {
        return trans_.size();
    }
    void prune_useless_states() {
        // Perform forward reachability
        std::set<posint> fwd_reached(initial_.begin(), initial_.end());
        std::deque<posint> fwd_list(initial_.begin(), initial_.end());
        while (!fwd_list.empty()) {
            posint from = fwd_list.front();
            for (posint to : trans_.sec<0>()[from]) {
                if (fwd_reached.insert(to).second) {
                    fwd_list.push_back(to);
                }
            }
            fwd_list.pop_front();
        }

        // Perform backward reachability
        std::set<posint> bck_reached;
        std::deque<posint> bck_list;
        for (posint s : final_) {
            if (fwd_reached.count(s) > 0) {
                bck_reached.insert(s);
                bck_list.push_back(s);
            }
        }
        while (!bck_list.empty()) {
            posint to = bck_list.front();
            for (posint from : trans_.sec<1>()[to]) {
                if (fwd_reached.count(from) > 0) {
                    if (bck_reached.insert(from).second) {
                        bck_list.push_back(from);
                    }
                }
            }
            bck_list.pop_front();
        }
        // TODO: Some initial states might now be unreachable => should run
        // until fixpoint?
        // Prune states based on the above information.
        prune_states(std::move(bck_reached));
    }
    // Drop states that are only reachable through epsilons.
    // TODO: Should only do this if it's a single epsilon reaching this
    // variable? Alternatively merge all states in an epsilon-cycle?
    void merge_epsilons() {
        // TODO: Assuming order of processing doesn't matter.
        std::set<posint> new_states;
        for (posint b : states_) {
            // Ignore initial states.
            if (initial_.count(b) > 0) {
                new_states.insert(b);
                continue;
            }
            // Consider states whose incoming transitions are all epsilons.
            bool only_inc_eps = true;
            for (const auto& letter_p : trans_.sec<2>()[b]) {
                if (letter_p.first == 0) {
                    continue;
                }
                if (!letter_p.second.empty()) {
                    only_inc_eps = false;
                    break;
                }
            }
            if (!only_inc_eps) {
                new_states.insert(b);
                continue;
            }
            // Collect all states 'a' reaching 'b' through an epsilon.
            std::list<posint> new_srcs;
            for (posint a : trans_.sec<2>()[b][0]) {
                // Ignore epsilon self-loops.
                if (a == b) {
                    continue;
                }
                new_srcs.push_back(a);
                if (final_.count(b) > 0) {
                    final_.insert(a);
                }
            }
            // Copy all edges (epsilons or not) starting from 'b' over to 'a'.
            for (posint a : new_srcs) {
                FOR(e, trans_.pri()[b]) {
                    trans_.insert(a, e.template get<LETTER>(),
                                  e.template get<TO>());
                }
            }
            // Don't include 'b' in 'new_states', effectively dropping it from
            // the FSM (and all transitions starting or ending at it).
        }
        prune_states(std::move(new_states));
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
                    for (posint tgt_ns : trans_.pri()[src_ns][letter]) {
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
    void to_tgf(std::ostream& os, const Rest&... rest) const {
        for (posint s : states_) {
            os << s;
            if (initial_.count(s) > 0) {
                os << " in";
            }
            if (final_.count(s) > 0) {
                os << " out";
            }
            os << std::endl;
        }
        os << "#" << std::endl;
        FOR(t, trans_) {
            os << t.template get<FROM>() << " " << t.template get<TO>();
            if (t.template get<LETTER>() != 0) {
                os << " ";
                alphabet_[t.template get<LETTER>() - 1].print(os, rest...);
            }
            os << std::endl;
        }
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
private:
    std::vector<Symbol> alphabet_;
    DfaBackend* backend_;
    DfaWrapper* wrapper_;
private:
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
    explicit DFA() : DFA(std::vector<Symbol>(), 1) {
        set_initial(0);
        minimize();
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
    void clear() {
        DFA temp;
        swap(*this, temp);
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
    posint num_trans() const {
        posint count = 0;
        posint sink = sink_state();
        for (posint letter = 1; letter <= num_letters(); letter++) {
            for (posint src = 0; src < num_states(); src++) {
                if (src == sink) {
                    continue;
                }
                posint tgt = backend_->delta[letter][src];
                if (tgt == sink) {
                    continue;
                }
                count++;
            }
        }
        return count;
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
    // TODO: Only defined for minimal DFAs
    bool empty() const {
        assert(minimal());
        return num_states() == 1 && !is_final(0);
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
    template<class... Rest>
    std::string to_regex(const Rest&... rest) const {
        std::string s = wrapper_->to_regex();
        std::ostringstream ss;
        boost::regex e((num_letters() <= 26) ? "[a-z]" : "a([0-9]+)");
        boost::sregex_iterator rit(s.begin(), s.end(), e);
        boost::sregex_iterator rend;
        unsigned skip_idx = 0;
        for (; rit != rend; ++rit) {
            ss << s.substr(skip_idx, rit->position() - skip_idx);
            posint letter = (num_letters() <= 26)
                ? (rit->str(0)[0] - 'a') : (std::stoi(rit->str(1)) - 1);
            ss << alphabet_.at(letter);
            skip_idx = rit->position() + rit->length();
        }
        ss << s.substr(skip_idx, s.length() - skip_idx);
        return ss.str();
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
               : std::string("$") + std::to_string(ref.value())),
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
    friend std::ostream& operator<<(std::ostream& os, const Delimiter& d) {
        os << (d.is_open ? "[" : "]") << d.fld;
        return os;
    }
    void print(std::ostream& os, const Registry<Field>& fld_reg) const {
        os << (is_open ? "(" : ")") << fld_reg[fld].name;
    }
};

typedef DFA<Delimiter> Signature;

TUPLE_TAG(OUT_SRC);
TUPLE_TAG(OUT_TGT);
TUPLE_TAG(IN_SRC);
TUPLE_TAG(IN_TGT);
TUPLE_TAG(CLE_OUT_SRC);
TUPLE_TAG(CLE_OUT_TGT);
TUPLE_TAG(CLR_IN_SRC);
TUPLE_TAG(CLR_IN_TGT);

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
    mi::MultiIndex<mi::Index<FLD, Ref<Field>,
                       mi::Index<SRC, Ref<Variable>,
                           mi::Table<TGT, Ref<Variable> > > >,
                   mi::Index<FLD, Ref<Field>,
                       mi::Table<TGT, Ref<Variable> > >,
                   mi::Index<TGT, Ref<Variable>,
                       mi::Index<FLD, Ref<Field>,
                           mi::Table<SRC, Ref<Variable> > > >,
                   mi::Table<SRC, Ref<Variable> > > opens;
    mi::MultiIndex<mi::Index<FLD, Ref<Field>,
                       mi::Index<SRC, Ref<Variable>,
                           mi::Table<TGT, Ref<Variable> > > >,
                   mi::Index<SRC, Ref<Variable>,
                       mi::Index<FLD, Ref<Field>,
                           mi::Table<TGT, Ref<Variable> > > > > closes;
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
    void clear() {
        CodeGraph temp;
        swap(*this, temp);
    }
    void copy(const CodeGraph& other,
              std::map<Ref<Variable>,Ref<Variable> >& var_map) {
        for (const Variable& v : other.vars) {
            var_map[v.ref] = vars.mktemp().ref;
        }
        FOR (e, other.epsilons) {
            epsilons.insert(var_map[e.get<SRC>()], var_map[e.get<TGT>()]);
        }
        FOR (o, other.opens) {
            opens.insert(o.get<FLD>(), var_map[o.get<SRC>()],
                         var_map[o.get<TGT>()]);
        }
        FOR (c, other.closes) {
            closes.insert(c.get<FLD>(), var_map[c.get<SRC>()],
                          var_map[c.get<TGT>()]);
        }
    }
    static CodeGraph from_sig(const Signature& sig) {
        CodeGraph res;
        Ref<Variable> a = res.vars.mktemp().ref;
        res.entry = a;
        Ref<Variable> b = res.vars.mktemp().ref;
        res.exits.insert(b);
        res.embed(a, b, sig);
        return res;
    }
    void star() {
        for (Ref<Variable> e : exits) {
            epsilons.insert(e, entry);
        }
        exits.insert(entry);
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
                    opens.insert(delim.fld, state2var[src], state2var[tgt]);
                } else {
                    closes.insert(delim.fld, state2var[src], state2var[tgt]);
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
    void close() {
        std::list<std::pair<std::set<Ref<Variable> >,
                            std::set<Ref<Variable> > > > in_bounds;
        mi::Index<CLE_OUT_SRC, Ref<Variable>,
            mi::Table<CLR_IN_SRC, Ref<Variable> > > deps;
        Worklist<Ref<Variable>,true> worklist; // holds in_src's

        // Record the set of matching in-variables.
        auto rec_in_bounds =
            [&](const mi::Table<TGT, Ref<Variable> >& ops,
                const mi::Index<SRC, Ref<Variable>,
                          mi::Table<TGT, Ref<Variable> > >& cls,
                Ref<Field>) {
            in_bounds.emplace_back();
            for (Ref<Variable> in_src : ops) {
                worklist.enqueue(in_src);
                in_bounds.back().first.insert(in_src);
            }
            for (const auto& in_tgt_p : cls) {
                in_bounds.back().second.insert(in_tgt_p.first);
            }
        };
        join_zip<1>(opens.sec<0>(), closes.pri(), rec_in_bounds);

        // Process matching parenthesis pairs up to fixpoint.
        auto emit_eps = [&](const mi::Table<SRC, Ref<Variable> >& out_srcs,
                            const mi::Table<TGT, Ref<Variable> >& out_tgts,
                            Ref<Field>) {
            for (Ref<Variable> out_src : out_srcs) {
                for (Ref<Variable> out_tgt : out_tgts) {
                    if (!epsilons.insert(out_src, out_tgt) ) {
                        continue;
                    }
                    for (Ref<Variable> clr_in_src : deps[out_src]) {
                        worklist.enqueue(clr_in_src);
                    }
                }
            }
        };
        while (!worklist.empty()) {
            Ref<Variable> in_src = worklist.dequeue();
            // Build the set of target variables on the fly.
            std::set<Ref<Variable> > in_tgts;
            for (const auto& bound : in_bounds) {
                if (bound.first.count(in_src) == 0) {
                    continue;
                }
                in_tgts.insert(bound.second.begin(), bound.second.end());
            }
            std::set<Ref<Variable> > reached;
            std::deque<Ref<Variable> > queue;
            reached.insert(in_src);
            queue.push_back(in_src);
            // Perform forward reachability calculation.
            while (!queue.empty()) {
                Ref<Variable> a = queue.front();
                queue.pop_front();
                if (in_tgts.count(a) > 0) {
                    join_zip<1>(opens.sec<1>()[in_src], closes.sec<0>()[a],
                                emit_eps);
                }
                // TODO: Record all imm epsilons?
                for (Ref<Variable> b : epsilons.pri()[a]) {
                    if (reached.insert(b).second) {
                        queue.push_back(b);
                    }
                }
                // Check if any open starts at this variable.
                if (opens.sec<2>().contains(a)) {
                    deps.insert(a, in_src);
                }
            }
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
            new_opens.insert(o.get<FLD>(), to_nvar(o.get<SRC>()),
                             to_nvar(o.get<TGT>()));
            new_opens.insert(o.get<FLD>(), o.get<SRC>(),
                             to_nvar(o.get<TGT>()));
        }
        swap(opens, new_opens);
        // Closes remain only between variables on the P-partition.
    }
    Signature to_sig() const {
        // Assumes at least an entry variable has been set.
        assert(entry.valid());
        timer.start("Building NFA for function");
        // Collect all used delimiters, to form the FSM's alphabet.
        std::vector<Delimiter> delims;
        for (const auto& fld_p : opens.pri()) {
            delims.emplace_back(true, fld_p.first);
        }
        for (const auto& fld_p : closes.pri()) {
            delims.emplace_back(false, fld_p.first);
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
        unsigned n_orig = nfa.num_states();
        unsigned e_orig = nfa.num_trans();
        timer.log("Size: ", n_orig, " states, ", e_orig, " transitions");
        timer.done();
        timer.start("Pruning NFA");
        nfa.prune_useless_states();
        unsigned n_prune = nfa.num_states();
        unsigned e_prune = nfa.num_trans();
        timer.log("Size: ", n_prune, " states, ", e_prune, " transitions");
        typename Timer::TimeDiff dt_prune = timer.done();
        timer.start("Merging epsilons");
        nfa.merge_epsilons();
        unsigned n_merge = nfa.num_states();
        unsigned e_merge = nfa.num_trans();
        timer.log("Size: ", n_merge, " states, ", e_merge, " transitions");
        typename Timer::TimeDiff dt_merge = timer.done();
        timer.start("Determinizing NFA");
        Signature res(nfa);
        unsigned n_det = res.num_states();
        unsigned e_det = res.num_trans();
        timer.log("Size: ", n_det, " states, ", e_det, " transitions");
        typename Timer::TimeDiff dt_det = timer.done();
        timer.start("Minimizing DFA");
        res.minimize();
        unsigned n_min = res.num_states();
        unsigned e_min = res.num_trans();
        timer.log("Size: ", n_min, " states, ", e_min, " transitions");
        typename Timer::TimeDiff dt_min = timer.done();
        timer.log("FSMSizes\t",   n_orig,  "\t", e_orig,  "\t",
                  dt_prune, "\t", n_prune, "\t", e_prune, "\t",
                  dt_merge, "\t", n_merge, "\t", e_merge, "\t",
                  dt_det,   "\t", n_det,   "\t", e_det,   "\t",
                  dt_min,   "\t", n_min,   "\t", e_min);
        return res;
    }
    void to_tgf(std::ostream& os, const Registry<Field>& fld_reg) const {
        for (const Variable& v : vars) {
            os << v.name;
            if (v.ref == entry) {
                os << " in";
            }
            if (exits.count(v.ref) > 0) {
                os << " out";
            }
            os << std::endl;
        }
        os << "#" << std::endl;
        FOR(e, epsilons) {
            os << vars[e.get<SRC>()].name << " "
               << vars[e.get<TGT>()].name << std::endl;
        }
        FOR(o, opens) {
            os << vars[o.get<SRC>()].name << " "
               << vars[o.get<TGT>()].name << " "
               << "(" << fld_reg[o.get<FLD>()].name << std::endl;
        }
        FOR(c, closes) {
            os << vars[c.get<SRC>()].name << " "
               << vars[c.get<TGT>()].name << " "
               << ")" << fld_reg[c.get<FLD>()].name << std::endl;
        }
    }
    unsigned num_vars() const {
        return vars.size();
    }
    unsigned num_ops() const {
        return epsilons.size() + opens.size() + closes.size();
    }
};

Signature concat_sigs(const Signature& l_sig, const Signature& r_sig) {
    CodeGraph res;
    Ref<Variable> a = res.vars.mktemp().ref;
    Ref<Variable> b = res.vars.mktemp().ref;
    Ref<Variable> c = res.vars.mktemp().ref;
    res.entry = a;
    res.exits.insert(c);
    res.embed(a, b, l_sig);
    res.embed(b, c, r_sig);
    res.close();
    res.pn_extend();
    return res.to_sig();
}

Signature star_sig(const Signature& sig) {
    CodeGraph res = CodeGraph::from_sig(sig);
    res.star();
    res.close();
    res.pn_extend();
    return res.to_sig();
}

class SCC;

class Function {
public:
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Function> ref;
    const Ref<SCC> scc;
private:
    Ref<Variable> entry_;
    std::set<Ref<Variable> > exits_;
    Signature sig_; // Initially set to the empty automaton.
public:
    explicit Function(const std::string* name_ptr, Ref<Function> ref,
                      Ref<SCC> scc) : name(*name_ptr), ref(ref), scc(scc) {
        EXPECT(boost::regex_match(name, boost::regex("\\w+")));
    }
    Function(const Function&) = delete;
    Function(Function&&) = default;
    Function& operator=(const Function&) = delete;
    bool merge(Ref<SCC> scc) {
        EXPECT(scc == this->scc);
        return false;
    }
    void clear() {
        sig_.clear();
    }
    void parse_file(const fs::path& fpath, SCC& scc);
    Ref<Variable> entry() const {
        return entry_;
    }
    const std::set<Ref<Variable> >& exits() const {
        return exits_;
    }
    bool complete() const {
        return entry_.valid();
    }
    const Signature& sig() const {
        return sig_;
    }
    void set_sig(Signature&& sig) {
        using std::swap;
        Signature temp(std::move(sig));
        swap(sig_, temp);
    }
};

class SCC {
public:
    typedef std::string Key;
    typedef mi::MultiIndex<
                mi::Index<FUN, Ref<Function>,
                    mi::Index<SRC, Ref<Variable>,
                        mi::Table<TGT, Ref<Variable> > > >,
                mi::Table<FUN, Ref<Function> > > CallStore;
public:
    const std::string name;
    const Ref<SCC> ref;
private:
    CodeGraph code_;
    CallStore calls_;
    std::set<Ref<Function> > funs_;
    std::set<Ref<Function> > entries_;
    std::set<Ref<SCC> > children_;
    std::set<Ref<SCC> > parents_;
public:
    explicit SCC(const std::string* name_ptr, Ref<SCC> ref)
        : name(*name_ptr), ref(ref) {
        EXPECT(boost::regex_match(name, boost::regex("\\w+")));
    }
    SCC(const SCC&) = delete;
    SCC(SCC&&) = default;
    SCC& operator=(const SCC&) = delete;
    bool merge() {
        return false;
    }
    void clear() {
        code_.clear();
        calls_.clear();
    }
    void clear_if_useless(Ref<SCC> curr, Registry<Function>& fun_reg) {
        for (Ref<SCC> p : parents_) {
            if (curr < p) {
                // This SCC is reachable by another SCC, which hasn't been
                // processed yet => we will read it again in the future.
                return;
            }
        }
        // We won't be reading the contents of this SCC in the future, so we can
        // safely clear it.
        clear();
        for (Ref<Function> f : entries_) {
            fun_reg[f].clear();
        }
    }
    const std::set<Ref<SCC> >& children() const {
        return children_;
    }
    const std::set<Ref<SCC> >& parents() const {
        return parents_;
    }
    const std::set<Ref<Function> >& funs() const {
        return funs_;
    }
    const std::set<Ref<Function> >& entries() const {
        return entries_;
    }
    void add_function(Ref<Function> fun) {
        funs_.insert(fun);
    }
    Variable& add_var(const std::string& name) {
        return code_.vars.add(name);
    }
    void parse_file(const fs::path& fpath, Registry<SCC>& scc_reg,
                    const Registry<Function>& fun_reg,
                    Registry<Field>& fld_reg) {
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
                vars_done = true;
            } else if (!vars_done) {
                EXPECT(toks.size() == 1);
                code_.vars.add(toks[0]);
            } else {
                EXPECT(toks.size() == 2 || toks.size() == 3);
                Ref<Variable> src = code_.vars.find(toks[0]).ref;
                Ref<Variable> tgt = code_.vars.find(toks[1]).ref;
                if (toks.size() == 2) {
                    code_.epsilons.insert(src, tgt);
                } else if (toks[2][0] == '(') {
                    Ref<Field> fld = fld_reg.add(toks[2].substr(1)).ref;
                    code_.opens.insert(fld, src, tgt);
                } else if (toks[2][0] == ')') {
                    Ref<Field> fld = fld_reg.add(toks[2].substr(1)).ref;
                    code_.closes.insert(fld, src, tgt);
                } else {
                    const Function& callee = fun_reg.find(toks[2]);
                    // Assuming all intra-SCC calls have been inlined in the
                    // previous step.
                    EXPECT(callee.scc < ref);
                    children_.insert(callee.scc);
                    scc_reg[callee.scc].mark_entry(callee.ref);
                    scc_reg[callee.scc].parents_.insert(ref);
                    calls_.insert(callee.ref, src, tgt);

                }
            }
        }
        EXPECT(fin.eof());
        EXPECT(vars_done);
    }
    const CodeGraph& code() const {
        return code_;
    }
    const CallStore& calls() const {
        return calls_;
    }
    const mi::Table<FUN, Ref<Function> >& callees() const {
        return calls_.sec<0>();
    }
    posint size() const {
        return funs_.size();
    }
    void mark_entry(Ref<Function> f) {
        assert(funs_.count(f) > 0);
        entries_.insert(f);
    }
    // CAUTION: Updates the code directly.
    void inline_callees(const Registry<Function>& fun_reg) {
        // There should be no intra-SCC calls present.
        FOR(c, calls_) {
            code_.embed(c.get<SRC>(), c.get<TGT>(),
                        fun_reg[c.get<FUN>()].sig());
        }
        calls_.clear();
    }
    void close_code() {
        code_.close();
    }
    CodeGraph make_fun_code(const Function& f) const {
        EXPECT(funs_.count(f.ref) > 0);
        EXPECT(f.complete());
        EXPECT(calls_.empty());
        CodeGraph res(code_);
        res.entry = f.entry();
        res.exits = f.exits();
        return res;
    }
    void to_tgf(std::ostream& os, const Registry<Function>& fun_reg,
                const Registry<Field>& fld_reg) const {
        code_.to_tgf(os, fld_reg);
        FOR(c, calls_) {
            os << code_.vars[c.get<SRC>()].name << " "
               << code_.vars[c.get<TGT>()].name << " "
               << fun_reg[c.get<FUN>()].name << std::endl;
        }
    }
    posint num_vars() const {
        return code_.vars.size();
    }
    posint num_ops() const {
        return (code_.epsilons.size() + code_.opens.size() +
                code_.closes.size() + calls_.size());
    }
};

void Function::parse_file(const fs::path& fpath, SCC& scc) {
    assert(!entry_.valid());
    assert(scc.ref == this->scc);
    std::ifstream fin(fpath.string());
    EXPECT((bool) fin);
    std::string line;
    while (std::getline(fin, line)) {
        boost::trim(line);
        if (line.empty()) {
            continue; // Empty lines are ignored.
        }
        std::vector<std::string> toks;
        boost::split(toks, line, boost::is_any_of(" "),
                     boost::token_compress_on);
        EXPECT(toks.size() >= 1);
        // TODO: The variable might not be present in the SCC, if the member
        // function is empty.
        Ref<Variable> v = scc.add_var(toks[0]).ref;
        for (auto it = toks.begin() + 1; it != toks.end(); ++it) {
            if (*it == "in") {
                EXPECT(!entry_.valid());
                entry_ = v;
            } else if (*it == "out") {
                exits_.insert(v);
            } else {
                EXPECT(false);
            }
        }
    }
    EXPECT(entry_.valid());
}

// TOP-LEVEL CODE =============================================================

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
    fs::path indir(indir_name);
    fs::path outdir(outdir_name);
    fs::create_directory(outdir);

    // Registries
    Registry<SCC> sccs;
    Registry<Function> funs;
    Registry<Field> flds;

    timer.start("Collecting SCC and function names");
    // SCCs were printed out in reverse topological order, so we need to parse
    // them in that order.
    std::vector<fs::path> scc_paths;
    std::copy(Directory(indir).begin(), Directory(indir).end(),
              std::back_inserter(scc_paths));
    std::sort(scc_paths.begin(), scc_paths.end());
    for (const fs::path& scc_path : scc_paths) {
        std::string scc_base(scc_path.filename().string());
        if (!boost::algorithm::ends_with(scc_base, FILE_EXTENSION)) {
            continue;
        }
        size_t scc_name_len = scc_base.size() - FILE_EXTENSION.size();
        std::string scc_name(scc_base.substr(0, scc_name_len));
        SCC& scc = sccs.make(scc_name);
        // Parse function names from files in the corresponding directory.
        fs::path funs_dir(indir/scc_name);
        for (const fs::path& fun_path : Directory(funs_dir)) {
            std::string fun_base(fun_path.filename().string());
            if (!boost::algorithm::ends_with(fun_base, FILE_EXTENSION)) {
                continue;
            }
            size_t fun_name_len = fun_base.size() - FILE_EXTENSION.size();
            std::string fun_name(fun_base.substr(0, fun_name_len));
            Ref<Function> fun = funs.make(fun_name, scc.ref).ref;
            scc.add_function(fun);
        }
        if (scc.size() < 10) {
            for (Ref<Function> f : scc.funs()) {
                scc.mark_entry(f);
            }
        }
    }
    timer.log(sccs.size(), " SCCs");
    timer.done();

    timer.start("Parsing SCCs");
    for (SCC& scc : sccs) {
        fs::path scc_path(indir/(scc.name + FILE_EXTENSION));
        scc.parse_file(scc_path, sccs, funs, flds);
    }
    timer.done();

    timer.start("Parsing functions");
    for (Function& f : funs) {
        SCC& scc = sccs[f.scc];
        fs::path fun_path(indir/scc.name/(f.name + FILE_EXTENSION));
        f.parse_file(fun_path, scc);
    }
    timer.done();

    timer.start("Processing SCCs bottom-up");
    for (SCC& scc : sccs) {
        timer.start("Processing SCC", scc.ref, " of ", sccs.size());
        timer.log(scc.size(), " functions, ", scc.entries().size(),
                  " entries");
        timer.log("Size: ", scc.num_vars(), " vars, ", scc.num_ops(), " ops");

        timer.start("Inlining callees");
        scc.inline_callees(funs);
        timer.done();
        timer.log("Size: ", scc.num_vars(), " vars, ", scc.num_ops(), " ops");

        timer.start("Closing SCC code");
        scc.close_code();
        timer.done();
        timer.log("Size: ", scc.num_vars(), " vars, ", scc.num_ops(), " ops");

        // Emit signatures by repurposing the full-SCC code graph.
        posint f_num = 0;
        for (Ref<Function> f_ref : scc.entries()) {
            Function& f = funs[f_ref];
            timer.start("Emitting sig for entry", f_num, " of ",
                        scc.entries().size(), ": ", f.name);

            timer.start("Copying SCC code");
            CodeGraph f_code = scc.make_fun_code(f);
            timer.done();
            timer.log("Size: ", f_code.num_vars(), " vars, ",
                      f_code.num_ops(), " ops");

            timer.start("PN-extending function code");
            f_code.pn_extend();
            timer.done();
            timer.log("Size: ", f_code.num_vars(), " vars, ",
                      f_code.num_ops(), " ops");

            timer.start("Emitting function signature");
            f.set_sig(f_code.to_sig());
            timer.done();
            timer.log("Size: ", f.sig().num_states(), " states, ",
                      f.sig().num_trans(), " transitions");

            timer.start("Printing signature");
            fs::path fpath(outdir/(f.name + ".sig.tgf"));
            std::ofstream fout(fpath.string());
            EXPECT((bool) fout);
            f.sig().to_tgf(fout, flds);
            timer.done();

            f_num++;
            timer.done();
        }

        timer.start("Pre-emptively clearing useless SCCs");
        scc.clear_if_useless(scc.ref, funs);
        for (Ref<SCC> child : scc.children()) {
            // Re-check all children of this SCC.
            sccs[child].clear_if_useless(scc.ref, funs);
        }
        timer.done();

        timer.done();
    }
    timer.done();

    timer.log("Time breakdown:");
    timer.print_stats();
}
