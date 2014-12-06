#include <boost/algorithm/string.hpp>
#include <boost/filesystem.hpp>
#include <boost/program_options.hpp>
#include <boost/regex.hpp>
#include <cassert>
#include <cstdlib>
#include <deque>
#include <fstream>
#include <iostream>
#include <limits>
#include <vector>

#define LIBAMORE_LIBRARY_COMPILATION
#include "amore/dfa.h"
#include "amore/global.h"
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

class NFA {
private:
    NfaBackend* backend_;
    NfaWrapper* wrapper_;
private:
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
    NFA(const NFA&) = delete;
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

// Symbols must be comparable (the alphabet gets sorted at construction).
template<class Symbol> class DFA {
private:
    std::vector<Symbol> alphabet_;
    DfaBackend* backend_;
    DfaWrapper* wrapper_;
private:
    posint follow(posint src, posint letter) const {
	return backend_->delta[letter][src];
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
	std::swap(alphabet_, useful_syms);
	backend_->alphabet_size = alphabet_.size();
	dispose(backend_->delta);
	backend_->delta = new_delta;
	// This CAN break minimality, e.g. if there was a sink state just to
	// cover the useless letters.
	backend_->minimal = false;
    }
public:
    // An extra sink state is added: the final FSM will have N+1 states.
    template<typename C>
    explicit DFA(const C& alphabet, posint num_states)
	: alphabet_(alphabet.begin(), alphabet.end()) {
	// Sort the set of symbols, and remove duplicates.
	std::sort(alphabet_.begin(), alphabet_.end());
	std::unique(alphabet_.begin(), alphabet_.end());
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
    ~DFA() {
	delete wrapper_; // also deallocates backend_
    }
    DFA(const DFA&) = delete;
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
    // TODO: Only defined for minimal DFAs
    bool operator==(const DFA& other) const {
	// XXX: really want assert(alphabet_ == other.alphabet_);
	assert(num_letters() == other.num_letters());
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

void test_dfa_code() {
    std::vector<std::string> alph = {"ey","bee"};
    DFA<std::string> d1(alph, 3);
    d1.set_initial(0);
    d1.set_final(1);
    d1.add_symb_trans(0, "ey", 1);
    d1.add_symb_trans(1, "ey", 1);
    d1.add_symb_trans(1, "bee", 2);
    d1.add_symb_trans(2, "ey", 2);
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
    n.add_trans(0, 1, 1);
    n.add_trans(1, 1, 1);
    n.add_trans(1, 2, 2);
    n.add_trans(2, 1, 2);
    std::cout << "n:" << std::endl;
    std::cout << n;
    std::cout << n.to_regex() << std::endl;
    std::cout << std::endl;

    DFA<std::string> d2(alph, 2);
    d2.set_initial(1);
    d2.set_final(0);
    d2.add_symb_trans(1, "ey", 0);
    d2.add_symb_trans(0, "ey", 0);
    std::cout << "d2:" << std::endl;
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
