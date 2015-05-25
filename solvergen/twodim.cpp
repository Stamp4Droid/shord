#include <boost/algorithm/string.hpp>
#include <boost/filesystem.hpp>
#include <boost/program_options.hpp>
#include <boost/regex.hpp>
#include <cassert>
#include <chrono>
#include <cstdlib>
#include <deque>
#include <fstream>
#include <functional>
#include <iostream>
#include <iterator>
#include <limits>
#include <map>
#include <stack>
#include <vector>
#include <utility>

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
    std::set<unsigned> states_;
    std::set<unsigned> initial_;
    std::set<unsigned> final_;
    mi::MultiIndex<
        mi::Index<FROM, unsigned,
            mi::Index<LETTER, unsigned,
                mi::Table<TO, unsigned> > >,
        mi::Index<TO, unsigned,
            mi::Index<LETTER, unsigned,
                mi::Table<FROM, unsigned> > > > trans_;
private:
    // TODO:
    // - Renumber states, to cover holes in numbering?
    // - Also clean alphabet?
    // - Perform directly on CodeGraph (do generic renumbering on Registries).
    void prune_states(std::set<unsigned>&& new_states) {
        using std::swap;
        std::set<unsigned> temp(std::move(new_states));
        swap(states_, temp);
        std::set<unsigned> new_initial;
        for (unsigned s : initial_) {
            if (states_.count(s) > 0) {
                new_initial.insert(s);
            }
        }
        swap(initial_, new_initial);
        std::set<unsigned> new_final;
        for (unsigned s : final_) {
            if (states_.count(s) > 0) {
                new_final.insert(s);
            }
        }
        swap(final_, new_final);
        decltype(trans_) new_trans;
        for (const auto& from_p : trans_.pri()) {
            unsigned from = from_p.first;
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
    void eps_close(LightSet<unsigned>& states) const {
        std::deque<unsigned> worklist(states.begin(), states.end());
        while (!worklist.empty()) {
            unsigned src = worklist.front();
            for (unsigned tgt : trans_.pri()[src][0]) {
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
    unsigned num_letters() const {
        return alphabet_.size();
    }
    const std::set<unsigned> initial() const {
        return initial_;
    }
    const std::set<unsigned> final() const {
        return final_;
    }
    void add_symb_trans(unsigned src, Symbol symbol, unsigned tgt) {
        // TODO: Might be more efficient to keep a map from symbols to letters.
        const auto iter_p =
            std::equal_range(alphabet_.cbegin(), alphabet_.cend(), symbol);
        // Assumes the symbol exists in the FSM's alphabet.
        assert(iter_p.first != iter_p.second);
        // Add 1 to the alphabet offset, because 0 is reserved for epsilon.
        unsigned letter = iter_p.first - alphabet_.cbegin() + 1;
        add_trans(src, letter, tgt);
    }
    void add_eps_trans(unsigned src, unsigned tgt) {
        add_trans(src, 0, tgt);
    }
    void add_trans(unsigned src, unsigned letter, unsigned tgt) {
        assert(letter <= num_letters());
        states_.insert(src);
        states_.insert(tgt);
        trans_.insert(src, letter, tgt);
    }
    void set_initial(unsigned state) {
        states_.insert(state);
        initial_.insert(state);
    }
    void set_final(unsigned state) {
        states_.insert(state);
        final_.insert(state);
    }
    unsigned num_states() const {
        return states_.size();
    }
    unsigned num_trans() const {
        return trans_.size();
    }
    // Drop states that are only reachable through epsilons.
    // TODO: Should only do this if it's a single epsilon reaching this
    // variable? Alternatively merge all states in an epsilon-cycle?
    void merge_epsilons() {
        // TODO: Assuming order of processing doesn't matter.
        std::set<unsigned> new_states;
        for (unsigned b : states_) {
            // Ignore initial states.
            if (initial_.count(b) > 0) {
                new_states.insert(b);
                continue;
            }
            // Consider states whose incoming transitions are all epsilons.
            bool only_inc_eps = true;
            for (const auto& letter_p : trans_.sec<0>()[b]) {
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
            std::list<unsigned> new_srcs;
            for (unsigned a : trans_.sec<0>()[b][0]) {
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
            for (unsigned a : new_srcs) {
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
    DFA<Symbol> determinize() const {
        // TODO: Can check if the NFA already contains no epsilons before
        // going through this process.
        // TODO: More efficient way to store sets of sets of states?
        Worklist<LightSet<unsigned>,false> worklist;
        // Enqueue initial state set.
        LightSet<unsigned> init_ns_set(initial_);
        eps_close(init_ns_set);
        unsigned init_ds = worklist.enqueue(std::move(init_ns_set))->second;
        // Discover reachable state sets and fill out transitions.
        std::vector<std::vector<unsigned> > dtrans;
        while (!worklist.empty()) {
            auto src_nsds = worklist.dequeue();
            // dstates processed in order
            assert(src_nsds->second == dtrans.size());
            dtrans.emplace_back(num_letters(),
                                std::numeric_limits<unsigned>::max());
            for (unsigned letter = 1; letter <= num_letters(); letter++) {
                LightSet<unsigned> tgt_ns_set;
                for (unsigned src_ns : src_nsds->first) {
                    for (unsigned tgt_ns : trans_.pri()[src_ns][letter]) {
                        tgt_ns_set.insert(tgt_ns);
                    }
                }
                eps_close(tgt_ns_set);
                unsigned tgt_ds =
                    worklist.enqueue(std::move(tgt_ns_set))->second;
                dtrans.back()[letter - 1] = tgt_ds;
            }
        }
        assert(dtrans.size() == worklist.num_reached());
        // Construct product DFA.
        DFA<Symbol> dfa(alphabet_, init_ds, std::move(dtrans));
        for (const auto& nsds : worklist.reached()) {
            if (!empty_intersection(nsds.first, final_)) {
                dfa.set_final(nsds.second);
            }
        }
        return dfa;
    }
    template<class... Rest>
    void to_tgf(std::ostream& os, const Rest&... rest) const {
        for (unsigned s : states_) {
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
};

class Partitioning {
public:
    typedef unsigned ElemId;
    typedef unsigned PartId;
    typedef unsigned Idx;
private:
    PartId num_parts_;
    const ElemId num_elems_;
    // TODO: Could instead use one doubly-linked list per part.
    std::vector<ElemId> elems_;
    std::vector<std::pair<Idx,Idx> > part2ends_;
    std::vector<PartId> elem2part_;
public:
    explicit Partitioning(ElemId num_elems)
        : num_parts_(1), num_elems_(num_elems), elems_(num_elems),
          elem2part_(num_elems) {
        for (ElemId i = 0; i < num_elems; i++) {
            elems_[i] = i;
        }
        part2ends_.emplace_back(0, num_elems);
        elem2part_.assign(num_elems, 0);
    }
    PartId num_parts() const {
        return num_parts_;
    }
    ElemId num_elems() const {
        return num_elems_;
    }
    unsigned part_size(PartId p) const {
        return part2ends_[p].second - part2ends_[p].first;
    }
    std::list<std::pair<PartId,PartId> > refine(const std::set<ElemId>& xs) {
        std::list<std::pair<PartId,PartId> > updates;
        // TODO: Could use a vector instead of a map.
        std::map<PartId,Idx> part2split;
        for (ElemId x : xs) {
            PartId p = elem2part_[x];
            Idx p_begin = part2ends_[p].first;
            Idx p_end = part2ends_[p].second;
            Idx& p_split = part2split.emplace(p, p_end).first->second;
            // TODO: This performs a lot of work at each step. It could be bad
            // if 'xs' covers a large portion of some part p.
            auto x_pos = std::equal_range(elems_.begin() + p_begin,
                                          elems_.begin() + p_split, x);
            assert(x_pos.second == x_pos.first + 1);
            // The elements to the left of the split remain sorted, and since
            // 'xs' are stored in ascending order in a std::set, the elements
            // to the right of the split will be in descending order.
            std::rotate(x_pos.first, x_pos.first + 1,
                        elems_.begin() + p_split);
            p_split--;
        }
        for (const auto& p_split : part2split) {
            PartId p = p_split.first;
            Idx split = p_split.second;
            // Fix the order to the right of the split point.
            std::reverse(elems_.begin() + split,
                         elems_.begin() + part2ends_[p].second);
            if (split == part2ends_[p].first) {
                // The entire part was covered.
                continue;
            }
            // Record the right half as a new part.
            PartId q = num_parts_++;
            Idx q_begin = split;
            Idx q_end = part2ends_[p].second;
            part2ends_.emplace_back(q_begin, q_end);
            part2ends_[p].second = q_begin;
            for (Idx i = q_begin; i < q_end; i++) {
                elem2part_[elems_[i]] = q;
            }
            updates.emplace_back(p, q);
        }
        return updates;
    }
    typename std::vector<ElemId>::const_iterator begin(PartId p) const {
        return elems_.begin() + part2ends_[p].first;
    }
    typename std::vector<ElemId>::const_iterator end(PartId p) const {
        return elems_.begin() + part2ends_[p].second;
    }
    bool part_contains(PartId p, ElemId x) const {
        auto x_pos
            = std::equal_range(elems_.begin() + part2ends_[p].first,
                               elems_.begin() + part2ends_[p].second, x);
        return x_pos.first != x_pos.second;
    }
    PartId part_of(ElemId x) const {
        return elem2part_[x];
    }
};

template<class Symbol> class DFA {
private:
    std::vector<Symbol> alphabet_;
    unsigned num_states_;
    unsigned initial_;
    std::set<unsigned> finals_;
    std::vector<std::vector<unsigned> > trans_; // src -> letter -> tgt
public:
    explicit DFA(const std::vector<Symbol>& alph, unsigned initial,
                 std::vector<std::vector<unsigned> >&& trans)
        : alphabet_(alph), num_states_(trans.size()), initial_(initial),
          trans_(std::move(trans)) {
        // sorted and unique
        assert(std::is_sorted(alphabet_.begin(), alphabet_.end(),
                              std::less_equal<Symbol>()));
        assert(initial_ < num_states_);
        for (unsigned src = 0; src < num_states_; src++) {
            const std::vector<unsigned>& row = trans_[src];
            assert(row.size() == alphabet_.size());
            for (unsigned letter = 0; letter < alphabet_.size(); letter++) {
                assert(row[letter] < num_states_);
            }
        }
    }
    // Adds a special sink state at the end.
    template<typename C>
    explicit DFA(const C& alph, unsigned real_states)
        : alphabet_(alph.begin(), alph.end()), num_states_(real_states + 1),
          initial_(real_states) {
        // Sort the set of symbols, and remove duplicates.
        std::sort(alphabet_.begin(), alphabet_.end());
        alphabet_.erase(std::unique(alphabet_.begin(), alphabet_.end()),
                        alphabet_.end());
        for (unsigned src = 0; src < num_states_; src++) {
            trans_.emplace_back
                (std::vector<unsigned>(alphabet_.size(), initial_));
        }
    }
    explicit DFA() : DFA(std::vector<Symbol>(), 0) {}
    DFA(const DFA&) = delete;
    DFA(DFA&& rhs) = default;
    DFA& operator=(const DFA&) = delete;
    friend void swap(DFA& a, DFA& b) {
        using std::swap;
        swap(a.alphabet_,   b.alphabet_);
        swap(a.num_states_, b.num_states_);
        swap(a.initial_,    b.initial_);
        swap(a.finals_,     b.finals_);
        swap(a.trans_,      b.trans_);
    }
    void clear() {
        DFA temp;
        swap(*this, temp);
    }
    unsigned num_letters() const {
        return alphabet_.size();
    }
    unsigned num_states() const {
        return num_states_;
    }
    unsigned num_trans() const {
        unsigned count = 0;
        unsigned sink = sink_state();
        for (unsigned src = 0; src < num_states(); src++) {
            if (src == sink) {
                continue;
            }
            for (unsigned letter = 0; letter < num_letters(); letter++) {
                unsigned tgt = follow(src, letter);
                if (tgt == sink) {
                    continue;
                }
                count++;
            }
        }
        return count;
    }
    unsigned follow(unsigned src, unsigned letter) const {
        assert(src < num_states());
        assert(letter < num_letters());
        return trans_[src][letter];
    }
    const std::vector<Symbol>& alphabet() const {
        return alphabet_;
    }
    unsigned initial() const {
        return initial_;
    }
    const std::set<unsigned>& finals() const {
        return finals_;
    }
    // Returns the first sink state found (TODO: Assumes only one sink state).
    // Returns num_states() if no sink state exists.
    unsigned sink_state() const {
        for (unsigned state = 0; state < num_states(); state++) {
            if (finals_.count(state) > 0) {
                continue;
            }
            bool is_sink = true;
            for (unsigned letter = 0; letter < num_letters(); letter++) {
                if (follow(state, letter) != state) {
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
    void add_trans(unsigned src, unsigned letter, unsigned tgt) {
        assert(src < num_states());
        assert(letter < num_letters());
        assert(tgt < num_states());
        trans_[src][letter] = tgt;
    }
    void set_final(unsigned state) {
        assert(state < num_states());
        finals_.insert(state);
    }
    void set_initial(unsigned state) {
        assert(state < num_states());
        initial_ = state;
    }
    DFA minimize() const {
        // Special case: trivially empty FSM.
        if (finals_.empty()) {
            return DFA();
        }
        // Partition states according to the Hopcroft minimization algorithm.
        typedef typename Partitioning::PartId PartId;
        Partitioning P(num_states());
        PartId F = P.refine(finals_).front().second;
        Worklist<PartId,true> worklist;
        worklist.enqueue(F);
        while (!worklist.empty()) {
            PartId A = worklist.dequeue();
            for (unsigned c = 0; c < num_letters(); c++) {
                std::set<unsigned> X;
                for (unsigned src = 0; src < num_states(); src++) {
                    if (P.part_contains(A, follow(src, c))) {
                        X.insert(src);
                    }
                }
                for (auto u : P.refine(X)) {
                    PartId Y = u.first; // == Y\X
                    PartId XnY = u.second;
                    if (worklist.contains(Y)
                        || P.part_size(XnY) <= P.part_size(Y)) {
                        worklist.enqueue(XnY);
                    } else {
                        worklist.enqueue(Y);
                    }
                }
            }
        }
        // Build the FSM.
        std::vector<std::vector<unsigned> >
            min_trans(P.num_parts(), std::vector<unsigned>(num_letters()));
        for (unsigned p = 0; p < P.num_parts(); p++) {
            for (unsigned letter = 0; letter < num_letters(); letter++) {
                min_trans[p][letter] =
                    P.part_of(follow(*(P.begin(p)), letter));
            }
        }
        DFA min(alphabet_, P.part_of(initial_), std::move(min_trans));
        for (unsigned s : finals_) {
            min.set_final(P.part_of(s));
        }
        // TODO: Not removing unreachable states => we will emit a sink state
        // even if the minimal DFA didn't need one.
        // TODO: Also throw out useless letters (need to calculate this set
        // before we allocate the transition table, need to renumber letters).
        return min;
    }
    template<class... Rest>
    void to_tgf(std::ostream& os, const Rest&... rest) const {
        unsigned sink = sink_state();
        for (unsigned state = 0; state < num_states(); state++) {
            if (state == sink) {
                continue;
            }
            os << state;
            if (state == initial_) {
                os << " in";
            }
            if (finals_.count(state) > 0) {
                os << " out";
            }
            os << std::endl;
        }
        os << "#" << std::endl;
        for (unsigned src = 0; src < num_states(); src++) {
            if (src == sink) {
                continue;
            }
            for (unsigned letter = 0; letter < num_letters(); letter++) {
                unsigned tgt = follow(src, letter);
                if (tgt == sink) {
                    continue;
                }
                os << src << " " << tgt << " ";
                alphabet_[letter].print(os, rest...);
                os << std::endl;
            }
        }
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
private:
    bool useful_ = true;
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
    bool useful() const {
        return useful_;
    }
    void mark_useless() {
        useful_ = false;
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
    bool operator<=(const Delimiter& rhs) const {
        return compare(*this, rhs) <= 0;
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
// Invariant: entry, exits, and all endpoints of epsilons, opens and closes are
// useful variables.
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
                       mi::Index<TGT, Ref<Variable>,
                           mi::Table<SRC, Ref<Variable> > > >,
                   mi::Table<SRC, Ref<Variable> >,
                   mi::Table<FLD, Ref<Field> > > opens;
    mi::MultiIndex<mi::Index<FLD, Ref<Field>,
                       mi::Index<SRC, Ref<Variable>,
                           mi::Table<TGT, Ref<Variable> > > >,
                   mi::Index<FLD, Ref<Field>,
                       mi::Index<TGT, Ref<Variable>,
                           mi::Table<SRC, Ref<Variable> > > >,
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
    void clear() {
        CodeGraph temp;
        swap(*this, temp);
    }
    void embed(Ref<Variable> src, Ref<Variable> tgt, const Signature& callee) {
        assert(vars[src].useful());
        assert(vars[tgt].useful());
        // Create temporary variables for all states in callee's signature
        // (except the sink state).
        // TODO: Could do this more efficiently, since Ref's are allocated
        // serially.
        std::vector<Ref<Variable> > state2var;
        state2var.reserve(callee.num_states());
        for (unsigned state = 0; state < callee.num_states(); state++) {
            // TODO: Emitting a state for the sink.
            state2var.push_back(vars.mktemp().ref);
        }
        // Copy all transitions, except those to/from the sink state.
        unsigned sink = callee.sink_state();
        for (unsigned src = 0; src < callee.num_states(); src++) {
            if (src == sink) {
                continue;
            }
            for (unsigned letter = 0; letter < callee.num_letters();
                 letter++) {
                const Delimiter& delim = callee.alphabet()[letter];
                unsigned tgt = callee.follow(src, letter);
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
        for (unsigned state : callee.finals()) {
            epsilons.insert(state2var[state], tgt);
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
            [&](const mi::Index<TGT, Ref<Variable>,
                          mi::Table<SRC, Ref<Variable> > >& ops,
                const mi::Index<SRC, Ref<Variable>,
                          mi::Table<TGT, Ref<Variable> > >& cls,
                Ref<Field>) {
            in_bounds.emplace_back();
            for (const auto& in_src_p : ops) {
                worklist.enqueue(in_src_p.first);
                in_bounds.back().first.insert(in_src_p.first);
            }
            for (const auto& in_tgt_p : cls) {
                in_bounds.back().second.insert(in_tgt_p.first);
            }
        };
        join_zip<1>(opens.sec<0>(), closes.pri(), rec_in_bounds);

        // Process matching parenthesis pairs up to fixpoint.
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
                auto emit_eps =
                    [&](const mi::Index<TGT, Ref<Variable>,
                                  mi::Table<SRC, Ref<Variable> > >& o_edges,
                        const mi::Index<SRC, Ref<Variable>,
                                  mi::Table<TGT, Ref<Variable> > >& c_edges,
                        Ref<Field>) {
                    for (Ref<Variable> out_src : o_edges[in_src]) {
                        for (Ref<Variable> out_tgt : c_edges[a]) {
                            if (!epsilons.insert(out_src, out_tgt) ) {
                                continue;
                            }
                            for (Ref<Variable> clr_in_src : deps[out_src]) {
                                worklist.enqueue(clr_in_src);
                            }
                        }
                    }
                };
                if (in_tgts.count(a) > 0) {
                    join_zip<1>(opens.sec<0>(), closes.pri(), emit_eps);
                }
                // TODO: Record all imm epsilons?
                for (Ref<Variable> b : epsilons.pri()[a]) {
                    if (reached.insert(b).second) {
                        queue.push_back(b);
                    }
                }
                // Check if any open starts at this variable.
                if (opens.sec<1>().contains(a)) {
                    deps.insert(a, in_src);
                }
            }
        }
    }
    // Simply makes a copy of each state; should prune later to remove
    // unreachable states.
    // XXX: Implementation assumes Ref's are allocated serially.
    void pn_extend() {
        using std::swap;
        assert(entry.valid());
        // The original set of variables form the P-partition. Make a copy of
        // each variable for the N-partition.
        RefMap<Variable,Ref<Variable> > to_nvar(vars);
        unsigned orig_vars = vars.size();
        for (unsigned i = 0; i < orig_vars; i++) {
            Ref<Variable> pvar(i);
            if (vars[pvar].useful()) {
                to_nvar[pvar] = vars.mktemp().ref;
            }
        }
        // Only the P-partition entry remains an entry on the PN-extended
        // machine (no change needed).
        // Both P-partition and N-partition exits are valid.
        std::set<Ref<Variable> > new_exits;
        for (Ref<Variable> v : exits) {
            new_exits.insert(v);
            new_exits.insert(to_nvar[v]);
        }
        swap(exits, new_exits);
        // Epsilon transitions are copied on the N-partition.
        decltype(epsilons) new_epsilons;
        FOR(e, epsilons) {
            new_epsilons.insert(e.get<SRC>(), e.get<TGT>());
            new_epsilons.insert(to_nvar[e.get<SRC>()], to_nvar[e.get<TGT>()]);
        }
        swap(epsilons, new_epsilons);
        // Opens are moved to the N-partition, and also serve as transitions
        // from P to N.
        decltype(opens) new_opens;
        FOR(o, opens) {
            new_opens.insert(o.get<FLD>(), to_nvar[o.get<SRC>()],
                             to_nvar[o.get<TGT>()]);
            new_opens.insert(o.get<FLD>(), o.get<SRC>(),
                             to_nvar[o.get<TGT>()]);
        }
        swap(opens, new_opens);
        // Closes remain only between variables on the P-partition.
    }
    // Ignores parenthesis matching.
    void prune_useless_states() {
        assert(entry.valid());
        // Perform forward reachability
        RefSet<Variable> fwd_reached(vars);
        std::deque<Ref<Variable> > fwd_list;
        auto fwd_visit = [&](Ref<Variable> v) {
            assert(vars[v].useful());
            if (!fwd_reached.contains(v)) {
                fwd_reached.insert(v);
                fwd_list.push_back(v);
            }
        };
        fwd_visit(entry);
        while (!fwd_list.empty()) {
            Ref<Variable> src = fwd_list.front();
            for (Ref<Variable> tgt : epsilons.pri()[src]) {
                fwd_visit(tgt);
            }
            for (const auto& fld_p : opens.pri()) {
                for (Ref<Variable> tgt : fld_p.second[src]) {
                    fwd_visit(tgt);
                }
            }
            for (const auto& fld_p : closes.pri()) {
                for (Ref<Variable> tgt : fld_p.second[src]) {
                    fwd_visit(tgt);
                }
            }
            fwd_list.pop_front();
        }
        // Perform backward reachability
        RefSet<Variable> bck_reached(vars);
        std::deque<Ref<Variable> > bck_list;
        auto bck_visit = [&](Ref<Variable> v) {
            assert(vars[v].useful());
            if (!fwd_reached.contains(v)) {
                return;
            }
            if (!bck_reached.contains(v)) {
                bck_reached.insert(v);
                bck_list.push_back(v);
            }
        };
        for (Ref<Variable> v : exits) {
            bck_visit(v);
        }
        while (!bck_list.empty()) {
            Ref<Variable> tgt = bck_list.front();
            for (Ref<Variable> src : epsilons.sec<0>()[tgt]) {
                bck_visit(src);
            }
            for (const auto& fld_p : opens.sec<0>()) {
                for (Ref<Variable> src : fld_p.second[tgt]) {
                    bck_visit(src);
                }
            }
            for (const auto& fld_p : closes.sec<0>()) {
                for (Ref<Variable> src : fld_p.second[tgt]) {
                    bck_visit(src);
                }
            }
            bck_list.pop_front();
        }
        // Prune states based on the above information.
        using std::swap;
        if (!bck_reached.contains(entry)) {
            // Special case: empty FSM
            clear();
            entry = vars.mktemp().ref;
            return;
        }
        for (Variable& v : vars) {
            if (!bck_reached.contains(v.ref)) {
                assert(v.ref != entry);
                v.mark_useless();
            }
        }
        std::set<Ref<Variable> > new_exits;
        for (Ref<Variable> v : exits) {
            if (vars[v].useful()) {
                new_exits.insert(v);
            }
        }
        assert(!new_exits.empty());
        swap(exits, new_exits);
        decltype(epsilons) new_epsilons;
        for (const auto& src_p : epsilons.pri()) {
            if (!vars[src_p.first].useful()) {
                continue;
            }
            for (Ref<Variable> tgt : src_p.second) {
                if (!vars[tgt].useful()) {
                    continue;
                }
                new_epsilons.insert(src_p.first, tgt);
            }
        }
        swap(epsilons, new_epsilons);
        decltype(opens) new_opens;
        for (const auto& fld_p : opens.pri()) {
            for (const auto& src_p : fld_p.second) {
                if (!vars[src_p.first].useful()) {
                    continue;
                }
                for (Ref<Variable> tgt : src_p.second) {
                    if (!vars[tgt].useful()) {
                        continue;
                    }
                    new_opens.insert(fld_p.first, src_p.first, tgt);
                }
            }
        }
        swap(opens, new_opens);
        decltype(closes) new_closes;
        for (const auto& fld_p : closes.pri()) {
            for (const auto& src_p : fld_p.second) {
                if (!vars[src_p.first].useful()) {
                    continue;
                }
                for (Ref<Variable> tgt : src_p.second) {
                    if (!vars[tgt].useful()) {
                        continue;
                    }
                    new_closes.insert(fld_p.first, src_p.first, tgt);
                }
            }
        }
        swap(closes, new_closes);
    }
    Signature to_sig() const {
        assert(entry.valid());
        timer.start("Building NFA for function");
        // Collect all used delimiters, to form the FSM's alphabet.
        std::vector<Delimiter> delims;
        for (Ref<Field> fld : opens.sec<2>()) {
            delims.emplace_back(true, fld);
        }
        for (Ref<Field> fld : closes.sec<1>()) {
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
        unsigned n_orig = nfa.num_states();
        unsigned e_orig = nfa.num_trans();
        timer.log("Size: ", n_orig, " states, ", e_orig, " transitions");
        timer.done();
        // Determinize and minimize the FSM.
        timer.start("Merging epsilons");
        nfa.merge_epsilons();
        unsigned n_merge = nfa.num_states();
        unsigned e_merge = nfa.num_trans();
        timer.log("Size: ", n_merge, " states, ", e_merge, " transitions");
        typename Timer::TimeDiff dt_merge = timer.done();
        timer.start("Determinizing NFA");
        Signature dfa = nfa.determinize();
        unsigned n_det = dfa.num_states();
        unsigned e_det = dfa.num_trans();
        timer.log("Size: ", n_det, " states, ", e_det, " transitions");
        typename Timer::TimeDiff dt_det = timer.done();
        timer.start("Minimizing DFA");
        Signature min = dfa.minimize();
        unsigned n_min = min.num_states();
        unsigned e_min = min.num_trans();
        timer.log("Size: ", n_min, " states, ", e_min, " transitions");
        typename Timer::TimeDiff dt_min = timer.done();
        timer.log("FSMSizes\t",   n_orig,  "\t", e_orig,  "\t",
                  dt_merge, "\t", n_merge, "\t", e_merge, "\t",
                  dt_det,   "\t", n_det,   "\t", e_det,   "\t",
                  dt_min,   "\t", n_min,   "\t", e_min);
        return min;
    }
    void to_tgf(std::ostream& os, const Registry<Field>& fld_reg) const {
        for (const Variable& v : vars) {
            if (!v.useful()) {
                continue;
            }
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
        unsigned count = 0;
        for (const Variable& v : vars) {
            if (v.useful()) {
                count++;
            }
        }
        return count;
    }
    unsigned num_ops() const {
        return epsilons.size() + opens.size() + closes.size();
    }
    unsigned num_fields() const {
        return opens.sec<2>().size() + closes.sec<1>().size();
    }
};

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
    unsigned size() const {
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
    unsigned num_vars() const {
        return code_.vars.size();
    }
    unsigned num_ops() const {
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
        unsigned f_num = 0;
        for (Ref<Function> f_ref : scc.entries()) {
            Function& f = funs[f_ref];
            timer.start("Emitting sig for entry", f_num, " of ",
                        scc.entries().size(), ": ", f.name);

            timer.start("Copying SCC code");
            CodeGraph f_code = scc.make_fun_code(f);
            timer.done();
            timer.log("Size: ", f_code.num_vars(), " vars, ",
                      f_code.num_ops(), " ops");

            timer.start("Pruning function code");
            f_code.prune_useless_states();
            timer.done();
            timer.log("Size: ", f_code.num_vars(), " vars, ",
                      f_code.num_ops(), " ops");

            timer.start("PN-extending function code");
            f_code.pn_extend();
            timer.done();
            timer.log("Size: ", f_code.num_vars(), " vars, ",
                      f_code.num_ops(), " ops");

            timer.start("Pruning function code");
            f_code.prune_useless_states();
            timer.done();
            timer.log("Size: ", f_code.num_vars(), " vars, ",
                      f_code.num_ops(), " ops");

            timer.start("Emitting function signature");
            f.set_sig(f_code.to_sig());
            timer.done();
            timer.log("Size: ", f.sig().num_states(), " states, ",
                      f.sig().num_trans(), " transitions");
            timer.log("Fields: from ", f_code.num_fields(), " to ",
                      f.sig().num_letters());

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

    // Print stats
    timer.log("Time breakdown:");
    timer.print_stats();
}
