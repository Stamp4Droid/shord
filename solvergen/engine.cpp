#include <algorithm>
#include <assert.h>
#include <deque>
#include <errno.h>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <list>
#include <locale>
#include <queue>
#include <set>
#include <signal.h>
#include <sstream>
#include <stack>
#include <stdexcept>
#include <stdlib.h>
#include <stdio.h>
#include <string>
#include <string.h>
#include <sys/resource.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <utility>
#include <vector>

#include "solvergen.hpp"

/**
 * @file
 * Implementation of the CFL-Reachability Solver Engine.
 */

/* GLOBAL VARIABLES ======================================================== */

/** An array containing all the Node%s of the input graph. */
Node *nodes;

/** A mapping between Node%s and their names. */
NodeNameMap node_names;

/** The Edge worklist used by the fixpoint calculation. */
std::deque<Edge*> worklist;

/**
 * All the indices on the @Relation%s used by the @Grammar, indexed by
 * @Relation reference number and target column.
 */
RelationIndex **rel_indices;

/**
 * A global counter for the number of iterations the solver loop has executed
 * so far.
 */
DECL_COUNTER(iteration, 0);

/* MEMORY & ERROR MANAGEMENT =============================================== */

/**
 * Underlying implementation of logging and error-reporting macros (base case).
 */
void report_impl(Severity lvl, int error_no) {
    switch (lvl) {
    case Severity::ERROR:
    case Severity::WARNING:
	std::cerr << std::endl;
	if (error_no != 0) {
	    std::cerr << "Reason: " << strerror(error_no) << std::endl;
	}
	if (lvl == Severity::ERROR) {
	    exit(EXIT_FAILURE);
	}
	break;
    case Severity::INFO:
	std::cout << std::endl;
	break;
    }
}

/**
 * Underlying implementation of logging and error-reporting macros (recursive
 * case).
 */
template<typename First, typename... Rest>
void report_impl(Severity lvl, int error_no, First first, Rest... rest) {
    switch (lvl) {
    case Severity::ERROR:
    case Severity::WARNING:
	std::cerr << first;
	break;
    case Severity::INFO:
	std::cout << first;
	break;
    }
    report(lvl, error_no, rest...);
}

/**
 * Underlying implementation of logging and error-reporting macros.
 */
template<typename... ArgTypes>
void report(Severity lvl, bool is_system, ArgTypes... args) {
    int saved_errno = errno;
    switch (lvl) {
    case Severity::ERROR:
    case Severity::WARNING:
	std::cout.flush();
	report_impl(lvl, is_system ? saved_errno : 0, args...);
	break;
    case Severity::INFO:
	report_impl(lvl, 0, args...);
	break;
    }
}

/**
 * Initialize the memory manager. This function should be called prior to any
 * dynamic memory allocation.
 */
void mem_manager_init() {
    struct rlimit rlp;
    rlp.rlim_cur = MAX_MEMORY;
    rlp.rlim_max = MAX_MEMORY;
    if (setrlimit(RLIMIT_AS, &rlp) != 0) {
	SYS_ERR("Unable to set memory limit");
    }
}

/**
 * Underlying implementation of @ref STRICT_ALLOC.
 */
void *strict_alloc(size_t num_bytes) {
    void *ptr;
    if ((ptr = malloc(num_bytes)) == NULL) {
	SYS_ERR("Out of memory");
    }
    return ptr;
}

/**
 * Return the maximum amount of memory this process has ever held at any one
 * time (in kbytes).
 */
long allocated_memory() {
    struct rusage usage;
    getrusage(RUSAGE_SELF, &usage);
    return usage.ru_maxrss;
}

/* BASE TYPE OPERATIONS ==================================================== */

/**
 * Print the value of @a index to @a out_buf. If @a buf_size is set to the size
 * of @a out_buf, then this function is guaranteed to never overflow it.
 *
 * @return @e true if @a index was printed successfully, @e false if @a out_buf
 * was too short for the full length of @e index and had to be truncated, or
 * some other error occured.
 */
bool index_print(INDEX index, char *out_buf, size_t buf_size) {
    int chars_written = snprintf(out_buf, buf_size, "%u", index);
    return (chars_written >= 0 && (unsigned int) chars_written < buf_size);
}

/**
 * Parse an @Index value from @a index_str.
 *
 * @return The parsed @Index if parsing was successful and the entire input
 * string was consumed, @ref INDEX_NONE otherwise.
 */
INDEX index_parse(const char *index_str) {
    char *first_invalid_char;
    INDEX index = strtoul(index_str, &first_invalid_char, 10);
    return (*first_invalid_char == '\0') ? index : INDEX_NONE;
}

/* TODO: index_equals */

/* GENERIC DATA STRUCTURES ================================================= */

template<typename K, typename V>
V LightMap<K,V>::dummy;

template<typename K, typename V>
typename LightMap<K,V>::List::iterator LightMap<K,V>::find(K k) {
    auto key_is_k = [k](const std::pair<K,V>& elem) {
	return k == elem.first;
    };
    return std::find_if(list.begin(), list.end(), key_is_k);
}

template<typename K, typename V>
LightMap<K,V>::LightMap() {}

template<typename K, typename V>
V& LightMap<K,V>::const_get(K k) {
    typename List::iterator pos = find(k);
    if (pos == list.end()) {
	return dummy;
    } else {
	return (*pos).second;
    }
}

template<typename K, typename V>
V& LightMap<K,V>::operator[](K k) {
    typename List::iterator pos = find(k);
    if (pos == list.end()) {
	list.emplace_front(k, V());
	return list.front().second;
    } else {
	return (*pos).second;
    }
}

/* NODES HANDLING ========================================================== */

NodeNameMap::NodeNameMap() : is_final(false) {}

void NodeNameMap::add(const char *name_cstr) {
    assert(!is_final);
    std::string name(name_cstr);
    if (map.insert(std::make_pair(name, vector.size())).second) {
	vector.push_back(name);
	// Make sure we haven't overflown NODE_REF.
	assert(vector.size() < NODE_NONE);
    }
}

void NodeNameMap::finalize() {
    assert(!is_final);
    is_final = true;
}

NODE_REF NodeNameMap::size() const {
    assert(is_final);
    return vector.size();
}

const std::string& NodeNameMap::name_of(NODE_REF node) const {
    assert(is_final);
    return vector.at(node);
}

NODE_REF NodeNameMap::node_for(const char *name) const {
    assert(is_final);
    return map.at(name);
}

/**
 * Get the number of Node%s in the input graph. Can only be called after the
 * Node%s container has been finalized.
 */
inline NODE_REF num_nodes() {
    return node_names.size();
}

/* RELATION HANDLING ======================================================= */

const std::set<INDEX> RelationIndex::EMPTY_INDEX_SET;

void RelationIndex::add(const Selection &sel, INDEX val) {
    map[sel].insert(val);
}

const std::set<INDEX>& RelationIndex::get(const Selection &sel) {
    try {
	return map.at(sel);
    } catch (const std::out_of_range& oor) {
	return EMPTY_INDEX_SET;
    }
}

/**
 * Initialize all possible indices on the declared @Relation%s. The indices
 * start out empty.
 */
void rels_init() {
    rel_indices = new RelationIndex *[num_rels()];
    for (int i = 1; i < num_rels(); i++) {
	// HACK: Ignoring special relation #0.
	// TODO: We only handle relations of arity 3.
	assert(rel_arity(i) == 3);
	rel_indices[i] = new RelationIndex[3];
    }
}

/**
 * Add a tuple to the @Relation identified by reference number @a ref. The
 * values of the tuple's fields must be specified in column order.
 */
// TODO: Duplicate entries are silently ignored.
// TODO: We don't necessarily need all 3 indices; can get this information
// from the grammar.
void rel_record(RELATION_REF ref, INDEX val_0, INDEX val_1, INDEX val_2) {
    rel_indices[ref][0].add(std::make_pair(val_1, val_2), val_0);
    rel_indices[ref][1].add(std::make_pair(val_0, val_2), val_1);
    rel_indices[ref][2].add(std::make_pair(val_0, val_1), val_2);
}

const std::set<INDEX>& rel_select(RELATION_REF ref, ARITY proj_col,
				  INDEX val_a, INDEX val_b) {
    return rel_indices[ref][proj_col].get(std::make_pair(val_a, val_b));
}

/* EDGE OPERATIONS ========================================================= */

/**
 * Create an Edge with the given attributes.
 *
 * The new Edge is not added to the graph or the ::worklist, and there is no
 * way to do so from client code. Thus, this function should only be used to
 * construct temporary Edge%s; otherwise use ::add_edge().
 */
Edge *edge_new(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index,
	       Edge *l_edge, bool l_rev, Edge *r_edge, bool r_rev) {
    assert((is_parametric(kind)) ^ (index == INDEX_NONE));
    Edge *e = STRICT_ALLOC(1, Edge);
    e->from = from;
    e->to = to;
    e->kind = kind;
    e->index = index;
#ifdef PATH_RECORDING
    assert(l_edge != NULL || r_edge == NULL);
    e->l_edge = l_edge;
    e->l_rev = l_rev;
    e->r_edge = r_edge;
    e->r_rev = r_rev;
#endif
    return e;
}

/**
 * Check if two Edge objects represent the same graph edge.
 */
bool edge_equals(Edge *a, Edge *b) {
    return a == b || ((a->from == b->from) && (a->to == b->to) &&
		      (a->kind == b->kind) && (a->index == b->index));
}

/* OUTGOING EDGE SET OPERATIONS ============================================ */

using OutIter = OutEdgeSet::Iterator;
void OutIter::advance_iter() {
    for (; iter != past_last; ++iter) {
	if (index == INDEX_NONE || (*iter).second->index == index) {
	    break;
	}
    }
}

OutIter::Iterator(OutEdgeSet *set, INDEX index, TableIterator iter,
		  TableIterator past_last)
    : set(set), index(index), iter(iter), past_last(past_last) {
    set->live_iters++;
    advance_iter();
}

OutIter::Iterator(OutIter&& other)
    : OutIter(other.set, other.index, other.iter, other.past_last) {}

OutIter::~Iterator() {
    set->live_iters--;
}

Edge *OutIter::operator*() const {
    return (*iter).second;
}

OutIter& OutIter::operator++() {
    ++iter;
    advance_iter();
    return *this;
}

bool OutIter::operator==(const OutIter& other) const {
    return (set == other.set && index == other.index && iter == other.iter &&
	    past_last == other.past_last);
}

bool OutIter::operator!=(const OutIter& other) const {
    return !(*this == other);
}

OutEdgeSet::View::View(OutEdgeSet *set, NODE_REF to, INDEX index)
    : set(set), index(index),
      first(to == NODE_NONE ? set->table.cbegin() :
	    set->table.equal_range(to).first),
      past_last(to == NODE_NONE ? set->table.cend() :
		set->table.equal_range(to).second) {}

OutIter OutEdgeSet::View::begin() const {
    return OutIter(set, index, first, past_last);
}

OutIter OutEdgeSet::View::end() const {
    return OutIter(set, index, past_last, past_last);
}

OutEdgeSet::OutEdgeSet() : live_iters(0) {}

void OutEdgeSet::add(Edge *e) {
    assert(live_iters == 0);
    table.insert(std::make_pair(e->to, e));
}

OutEdgeSet::View OutEdgeSet::view(NODE_REF to, INDEX index) {
    return View(this, to, index);
}

/* INCOMING EDGE SET OPERATIONS ============================================ */

using InIter = InEdgeSet::Iterator;

void InIter::advance_iter() {
    for (; iter != set->edges.cend(); ++iter) {
	if (index == INDEX_NONE || (*iter)->index == index) {
	    break;
	}
    }
}

InIter::Iterator(InEdgeSet *set, INDEX index, bool at_end)
    : set(set), index(index),
      iter(at_end ? set->edges.cend() : set->edges.cbegin()) {
    set->live_iters++;
    advance_iter();
}

InIter::Iterator(InIter&& other) : InIter(other.set, other.index, true) {
    std::swap(iter, other.iter);
}

InIter::~Iterator() {
    set->live_iters--;
}

Edge *InIter::operator*() const {
    return *iter;
}

InIter& InIter::operator++() {
    ++iter;
    advance_iter();
    return *this;
}

bool InIter::operator==(const InIter& other) const {
    return set == other.set && index == other.index && iter == other.iter;
}

bool InIter::operator!=(const InIter& other) const {
    return !(*this == other);
}

InEdgeSet::View::View(InEdgeSet *set, INDEX index) : set(set), index(index) {}

InIter InEdgeSet::View::begin() const {
    return InIter(set, index, false);
}

InIter InEdgeSet::View::end() const {
    return InIter(set, index, true);
}

InEdgeSet::InEdgeSet() : live_iters(0) {}

void InEdgeSet::add(Edge *e) {
    assert(live_iters == 0);
    edges.push_back(e);
}

InEdgeSet::View InEdgeSet::view(INDEX index) {
    return View(this, index);
}

/* GRAPH HANDLING ========================================================== */

/**
 * Locate the appropriate InEdgeSet, if such a set has been allocated.
 * Otherwise, return a reference to a dummy empty InEdgeSet.
 */
InEdgeSet& get_in_set(NODE_REF to, EDGE_KIND kind) {
    return nodes[to].in.const_get(kind);
}

/**
 * Locate the appropriate OutEdgeSet, if such a set has been allocated.
 * Otherwise, return a reference to a dummy empty OutEdgeSet.
 */
OutEdgeSet& get_out_set(NODE_REF from, EDGE_KIND kind) {
    return nodes[from].out.const_get(kind);
}

InEdgeSet::View edges_to(NODE_REF to, EDGE_KIND kind, INDEX index) {
    assert(!is_predicate(kind));
    assert(index == INDEX_NONE || is_parametric(kind));
    return get_in_set(to, kind).view(index);
}

OutEdgeSet::View edges_from(NODE_REF from, EDGE_KIND kind, INDEX index) {
    assert(!is_predicate(kind));
    assert(index == INDEX_NONE || is_parametric(kind));
    return get_out_set(from, kind).view(NODE_NONE, index);
}

OutEdgeSet::View edges_between(NODE_REF from, NODE_REF to, EDGE_KIND kind) {
    assert(!is_predicate(kind));
    return get_out_set(from, kind).view(to, INDEX_NONE);
}

Edge *find_edge(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index) {
    assert(!is_predicate(kind));
    assert((index == INDEX_NONE) ^ is_parametric(kind));
    // We search on the set of outgoing edges of the source node, because it's
    // indexed on more dimensions.
    for (Edge *e : get_out_set(from, kind).view(to, index)) {
	// We stop at the first compatible Edge; the view can only contain one
	// such element anyway.
	// TODO: Could verify this, but we'd be performing useless work.
	return e;
    }
    return NULL;
}

void add_edge(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index,
	      Edge *l_edge, bool l_rev, Edge *r_edge, bool r_rev) {
    if (find_edge(from, to, kind, index) != NULL) {
	return;
    }
    Edge *e = edge_new(from, to, kind, index, l_edge, l_rev, r_edge, r_rev);
    nodes[e->to].in[e->kind].add(e);
    nodes[e->from].out[e->kind].add(e);
    worklist.push_back(e);
}

/* INPUT & OUTPUT ========================================================== */

/**
 * The directory where we expect to find the initial set of terminal-@Symbol
 * Edge%s.
 */
#define INPUT_DIR "input"

/**
 * The directory where we dump the Node listing and final set of
 * non-terminal-@Symbol Edge%s.
 */
#define OUTPUT_DIR "output"

/**
 * The file extension for both input and output Edge set files.
 *
 * The basename for these files must be the same as the @Symbol they represent.
 * They must only contain lines of the form `source sink`, where @e source and
 * @e sink are arbitrary string names for the nodes. If the @Symbol is indexed,
 * the line format instead becomes `source sink index`, where @e index is a
 * number. Each line corresponds to a separate Edge.
 */
#define EDGE_SET_FORMAT "dat"

/**
 * The file extension for @Relation files.
 *
 * The basename for these files must be the same as the @Relation they
 * represent. Each line must contain exactly one tuple of @Indices from the
 * @Relation, represented as a whitespace-separated list of numbers.
 */
#define RELATION_FORMAT "rel"

/**
 * The file extension for path listing files.
 *
 * The basename for these files is the same as the @Symbol whose paths they
 * represent.
 */
#define PATHS_FORMAT "paths.xml"

/**
 * The file inside @ref OUTPUT_DIR where we dump the names of all Node%s in the
 * input graph.
 *
 * The `k`-th line of this file will contain the name of Node number `k-1`.
 */
#define NODE_LISTING "Nodes.map"

/** The mode of operation for parse_input_files(). */
enum class ParsingMode {
    /** Record all encountered unique Node names. */
    RECORD_NODES,
    /**
     * Assume that all Node%s have been recorded in a previous pass, and use
     * the pre-existing Node-name mapping to record the input Edge%s.
     *
     * Assumes that the contents of the input files do not change between
     * passes.
     */
    RECORD_EDGES
};

/**
 * Parse in the input graph.
 *
 * For each terminal @Symbol in the input grammar, find the corresponding
 * @ref EDGE_SET_FORMAT file in subdir @ref INPUT_DIR and process it according
 * to @a mode.
 */
void parse_input_files(ParsingMode mode) {
    for (EDGE_KIND k = 0; k < num_kinds(); k++) {
	if (!is_terminal(k)) {
	    continue;
	}
	bool parametric = is_parametric(k);
	int exp_num_scanned = (parametric) ? 3 : 2;
	/* TODO: Filename buffer may be too small. */
	char fname[256];
	snprintf(fname, sizeof(fname), "%s/%s.%s", INPUT_DIR, kind2symbol(k),
		 EDGE_SET_FORMAT);
	FILE *f = fopen(fname, "r");
	if (f == NULL) {
	    SYS_ERR("Can't open input file: ", fname);
	}
	while (true) {
	    char src_buf[256], tgt_buf[256], idx_buf[32];
	    errno = 0;
	    /* XXX: The following might overflow the buffers. */
	    int num_scanned;
	    if (parametric) {
		num_scanned = fscanf(f, "%s %s %s", src_buf, tgt_buf, idx_buf);
	    } else {
		num_scanned = fscanf(f, "%s %s", src_buf, tgt_buf);
	    }
	    if (errno != 0) {
		fclose(f);
		SYS_ERR("Error while parsing file: ", fname);
	    }
	    if (num_scanned != exp_num_scanned && num_scanned != EOF) {
		fclose(f);
		APP_ERR("Error while parsing file: ", fname);
	    }
	    if (num_scanned == EOF) {
		fclose(f);
		break;
	    }
	    NODE_REF src_node, tgt_node;
	    switch (mode) {
	    case ParsingMode::RECORD_NODES:
		node_names.add(src_buf);
		node_names.add(tgt_buf);
		break;
	    case ParsingMode::RECORD_EDGES:
		src_node = node_names.node_for(src_buf);
		tgt_node = node_names.node_for(tgt_buf);
		if (parametric) {
		    INDEX index = index_parse(idx_buf);
		    if (index == INDEX_NONE) {
			fclose(f);
			APP_ERR("Invalid index: ", idx_buf);
		    }
		    add_edge(src_node, tgt_node, k, index,
			     NULL, false, NULL, false);
		} else {
		    add_edge(src_node, tgt_node, k, INDEX_NONE,
			     NULL, false, NULL, false);
		}
		break;
	    }
	}
    }
}

/**
 * Parse in the @Relation%s used in the @Grammar.
 *
 * For each @Relation declared in the input grammar, find the corresponding
 * @ref RELATION_FORMAT file in subdir @ref INPUT_DIR and read in its tuples.
 */
void parse_rels() {
    // TODO: Could share some of this code with input facts parsing.
    for (RELATION_REF r = 1; r < num_rels(); r++) {
	// HACK: Ignoring special relation #0.
	assert(rel_arity(r) == 3);
	std::ostringstream ss;
	ss << INPUT_DIR << "/" << ref2rel(r) << "." << RELATION_FORMAT;
	std::string fname = ss.str();
	std::ifstream fin(fname);
	if (!fin) {
	    SYS_ERR("Can't open input file: ", fname);
	}
	std::string line;
	while (std::getline(fin, line)) {
	    std::istringstream iss(line);
	    INDEX val_0, val_1, val_2;
	    if (!(iss >> val_0 >> val_1 >> val_2)) {
		// TODO: This won't fail as expected in all cases, e.g. if one
		// of the numbers is negative.
		APP_ERR("Tuple too short or parsing error: ", fname);
	    }
	    if (!iss.eof()) {
		APP_ERR("Tuple too long or trailing whitespace: ", fname);
	    }
	    rel_record(r, val_0, val_1, val_2);
	}
	if (!fin.eof()) {
	    SYS_ERR("Error while parsing file: ", fname);
	}
    }
}

/**
 * Print out the final results of the analysis.
 *
 * For each non-terminal @Symbol in the input grammar, print out all Edge%s
 * generated by the solver in a @ref EDGE_SET_FORMAT file under
 * @ref OUTPUT_DIR. Also print out a listing of the names of all Node%s present
 * in the graph, in a file named @ref NODE_LISTING under @ref OUTPUT_DIR.
 */
void print_results() {
    /* Print out non-terminal edges. */
    for (EDGE_KIND k = 0; k < num_kinds(); k++) {
	if (is_predicate(k) || is_terminal(k)) {
	    continue;
	}
	bool parametric = is_parametric(k);
	/* TODO: Filename buffer may be too small. */
	char fname[256];
	/* TODO: Create output dir if missing. */
	snprintf(fname, sizeof(fname), "%s/%s.%s", OUTPUT_DIR, kind2symbol(k),
		 EDGE_SET_FORMAT);
	FILE *f = fopen(fname, "w");
	if (f == NULL) {
	    SYS_ERR("Can't open output file: ", fname);
	}
	for (NODE_REF n = 0; n < num_nodes(); n++) {
	    const char *src_name = node_names.name_of(n).c_str();
	    /* TODO: Iterate over in_edges, so we avoid paying the overhead of
	       traversing the out_edges hashtable. */
	    for (Edge *e : edges_from(n, k)) {
		const char *tgt_name = node_names.name_of(e->to).c_str();
		if (parametric) {
		    char idx_buf[32];
		    /* TODO: Check that printing is successful. */
		    index_print(e->index, idx_buf, sizeof(idx_buf));
		    fprintf(f, "%s %s %s\n", src_name, tgt_name, idx_buf);
		} else {
		    fprintf(f, "%s %s\n", src_name, tgt_name);
		}
	    }
	}
	fclose(f);
    }

    /* Print out a listing of node names. */
    /* TODO: Filename buffer may be too small. */
    char fname[256];
    snprintf(fname, sizeof(fname), "%s/%s", OUTPUT_DIR, NODE_LISTING);
    FILE *f = fopen(fname, "w");
    if (f == NULL) {
	SYS_ERR("Can't open output file: ", fname);
    }
    for (NODE_REF n = 0; n < num_nodes(); n++) {
	fprintf(f, "%s\n", node_names.name_of(n).c_str());
    }
    fclose(f);
}

/* CHOICE SEQUENCE HANDLING ================================================ */

constexpr ChoiceSequence *choice_sequence_empty() {
    return NULL;
}

ChoiceSequence *choice_sequence_extend(ChoiceSequence *prev, CHOICE last) {
    ChoiceSequence *choice = STRICT_ALLOC(1, ChoiceSequence);
    choice->last = last;
    choice->prev = prev;
    return choice;
}

std::stack<CHOICE> choice_sequence_unwind(const ChoiceSequence *seq_ptr) {
    std::stack<CHOICE> choices;
    for (; seq_ptr != NULL; seq_ptr = seq_ptr->prev) {
	choices.push(seq_ptr->last);
    }
    return choices;
}

/* DERIVATION HANDLING ===================================================== */

Derivation derivation_empty() {
    return Derivation({NULL, NULL, false, false});
}

Derivation derivation_single(Edge *e, bool reverse) {
    return Derivation({e, NULL, reverse, false});
}

Derivation derivation_double(Edge *left_edge, bool left_reverse,
			     Edge *right_edge, bool right_reverse) {
    return Derivation({left_edge, right_edge, left_reverse, right_reverse});
}

/**
 * Check if @a deriv could have produced the Edge for @a step.
 *
 * We assume that @a deriv is a valid Derivation for @a step<!-- -->'s Edge,
 * but that is not enough to guarantee that @a deriv could have been used in
 * the construction of the entire path we are following. For example, consider
 * the following grammar:
 *
 *     A :: B | t
 *     B :: A
 *
 * And the following set of initial Edge%s:
 *
 *     n0 --t--> n1
 *
 * By applying the grammar on the above set of Edge%s, the following
 * non-terminal Edge%s are produced:
 *
 *     n0 --A--> n1
 *     n0 --B--> n1
 *
 * When trying to recover the full `A`-path from `n0` to `n1`, we might try to
 * follow the first production, i.e. `A :: B`, which leads us to consider the
 * `B`-path from `n0` to `n1`. There is a valid Derivation for the
 * corresponding Edge (namely, that it was generated from the Edge
 * `n0 --A--> n1`), but we can't use it in the construction of the path, since
 * that Edge is itself the one we're trying to break down.
 */
bool derivation_is_valid(const Derivation &deriv, const Step *step) {
    Edge *left_edge = deriv.left_edge;
    Edge *right_edge = deriv.right_edge;
    if (left_edge == NULL) {
	assert(right_edge == NULL);
	return true;
    }
    // Verify that we haven't already recursed on one of the Edges in the
    // Derivation.
    for (; step != NULL; step = step->parent) {
	if (edge_equals(step->edge, left_edge) ||
	    (right_edge != NULL && edge_equals(step->edge, right_edge))) {
	    return false;
	}
    }
    return true;
}

/* STEP TREE HANDLING ====================================================== */

Step *step_alloc() {
    Step *step = STRICT_ALLOC(1, Step);
    step->is_reverse = false;
    step->is_expanded = false;
    step->num_choices = 0;
    step->edge = NULL;
    step->next_sibling = NULL;
    step->parent = NULL;
    step->sub_step_seqs = NULL;
    return step;
}

Step *step_init(Edge *edge) {
    Step *top = step_alloc();
    top->edge = edge;
    return top;
}

Step *derivation_to_step_sequence(const Derivation &deriv, Step *parent) {
    Step *first_step = NULL;
    if (deriv.left_edge == NULL) {
	assert(deriv.right_edge == NULL);
    } else {
	first_step = step_alloc();
	first_step->edge = deriv.left_edge;
	first_step->is_reverse = deriv.left_reverse;
	first_step->parent = parent;
	if (deriv.right_edge != NULL) {
	    Step *second_step = step_alloc();
	    second_step->edge = deriv.right_edge;
	    second_step->is_reverse = deriv.right_reverse;
	    second_step->parent = parent;
	    first_step->next_sibling = second_step;
	}
    }
    return first_step;
}

/**
 * Expand a path by considering all Derivation%s that could have been used to
 * produce @a step.
 */
void step_expand(Step *step) {
    // Edges corresponding to terminal symbols are elementary steps, which
    // cannot be expanded any further.
    assert(!is_terminal(step->edge->kind));
    if (step->is_expanded) {
	// This Step has already been expanded.
	return;
    }
    step->is_expanded = true;

    // Calculate all derivations that could have produced the parent step.
    std::list<Derivation> derivs = all_derivations(step->edge);
    auto is_invalid = [=](const Derivation &d){
	return !derivation_is_valid(d, step);
    };
    derivs.remove_if(is_invalid);
    if (derivs.size() == 0) {
	// There are no valid derivations: we have failed to find a path using
	// this branch.
	// TODO: Prune the branch at this point.
	return;
    }

    // Record all valid derivations as possible sub-steps of the parent step.
    step->num_choices = derivs.size();
    step->sub_step_seqs = STRICT_ALLOC(derivs.size(), Step *);
    unsigned int i = 0;
    for (const Derivation &d : derivs) {
	step->sub_step_seqs[i] = derivation_to_step_sequence(d, step);
	i++;
    }
}

Step *step_follow_choice(Step *parent, CHOICE choice) {
    assert(parent->is_expanded);
    assert(!is_terminal(parent->edge->kind));
    assert(choice < parent->num_choices);
    Step *sub_steps = parent->sub_step_seqs[choice];
    // TODO: Check the parent pointer on all the steps.
    // This may be NULL, in the case of empty productions.
    return sub_steps;
}

PATH_LENGTH step_sequence_estimate_length(Step *first_step) {
    PATH_LENGTH min_length = 0;
    for (Step *step = first_step; step != NULL; step = step->next_sibling) {
	min_length += static_min_length(step->edge->kind);
    }
    return min_length;
}

/**
 * Move forward in the Step tree according to an in-order traversal, up to the
 * next non-terminal Step. This operation is deterministic, because the tree
 * can only split at non-terminal Step%s.
 *
 * @param [in] parent The non-terminal Step to start from.
 * @param [in] choice The Derivation to follow on the @a parent Step.
 * @return The next non-terminal Step in the Step tree, if there exists one,
 * or @e NULL if we reach the end of the traversal without finding such a Step.
 */
Step *step_follow_choice_skip_terminals(Step *parent, CHOICE choice) {
    Step *curr = step_follow_choice(parent, choice);
    while (curr == NULL) {
	// We've hit an empty production, so we try the next sibling of the
	// parent step. We may have to skip multiple levels to reach one that
	// hasn't been fully traversed yet.
	if (parent == NULL) {
	    // We've reached the top of the step tree.
	    return NULL;
	}
	curr = parent->next_sibling;
	parent = parent->parent;
    }
    // We are at a concrete Step, and need to stop at the first non-terminal
    // that we find.
    while (is_terminal(curr->edge->kind)) {
	// Move to the next step on the same level, repeat if it's a
	// terminal step.
	assert(!curr->is_expanded);
	assert(curr->sub_step_seqs == NULL);
	while (curr->next_sibling == NULL) {
	    // If we're at the end of the current level, move up to the first
	    // level that is not yet fully traversed, and continue from there.
	    curr = curr->parent;
	    if (curr == NULL) {
		// We've reach the top of the step tree.
		return NULL;
	    }
	}
	curr = curr->next_sibling;
    }
    return curr;
}

// Edit steps in place to only keep the first choice we manage to fully expand.
// Will NOT produce the shortest path.
bool bake_single_path(Step *first) {
    // TODO: Discarded paths are not deallocated.
    for (Step *step = first; step != NULL; step = step->next_sibling) {
	if (is_terminal(step->edge->kind)) {
	    continue;
	}
	step_expand(step);
	if (step->num_choices == 0) {
	    return false;
	}
	bool recovered = false;
	for (CHOICE c = 0; c < step->num_choices; c++) {
	    if (bake_single_path(step->sub_step_seqs[c])) {
		recovered = true;
		step->num_choices = 1;
		step->sub_step_seqs[0] = step->sub_step_seqs[c];
	    }
	}
	if (!recovered) {
	    return false;
	}
    }
    return true;
}

/* PATH HANDLING =========================================================== */

PartialPath *partial_path_alloc() {
    PartialPath *path = STRICT_ALLOC(1, PartialPath);
    path->min_length = 0;
    path->choices = NULL;
    path->curr_step = NULL;
    return path;
}

PartialPath *partial_path_init(Step *top_step) {
    PartialPath *path = partial_path_alloc();
    path->min_length = static_min_length(top_step->edge->kind);
    path->choices = choice_sequence_empty();
    path->curr_step = top_step;
    return path;
}

/**
 * Check if the input PartialPath represents a complete path.
 *
 * For complete paths, PartialPath::min_length is the actual length of the
 * path, PartialPath::choices cover a full derivation, and
 * PartialPath::curr_step is @e NULL.
 */
constexpr bool partial_path_is_complete(const PartialPath *path) {
    // TODO: Also check that path->choices cover a full derivation.
    return path->curr_step == NULL;
    // NOTE: PartialPath::curr_step could also be NULL if we allowed
    // PartialPaths to point at empty productions, which is why we must take
    // special care before following a potentially empty derivation.
}

constexpr bool partial_path_is_at_terminal(const PartialPath *path) {
    return (path->curr_step != NULL &&
	    is_terminal(path->curr_step->edge->kind));
}

/**
 * Expand @a path once by considering all possible Derivation%s at the current
 * point. Assumes we are currently processing a non-terminal Step. Moves each
 * of the resulting PartialPath%s to the next non-terminal Step, if any.
 *
 * @param [in] path The path to split.
 * @return An unsorted list of extended PartialPath%s derived from @a path.
 * These will either be complete, or will point to non-terminal Step%s.
 */
std::list<PartialPath*> partial_path_split(const PartialPath *path) {
    std::list<PartialPath*> ext_paths;
    Step *parent = path->curr_step;
    PATH_LENGTH parent_min_length = static_min_length(parent->edge->kind);
    // Lazily expand the path at the current step.
    step_expand(parent);
    for (CHOICE c = 0; c < parent->num_choices; c++) {
	Step *sub_steps = step_follow_choice(parent, c);
	PartialPath *ext = partial_path_alloc();
	// We know how many terminals are added to the path by this production,
	// so we immediatelly include them in the lower bound for the path's
	// length.
	ext->min_length = path->min_length - parent_min_length
	    + step_sequence_estimate_length(sub_steps);
	ext->choices = choice_sequence_extend(path->choices, c);
	// Whenever we pick up this path again, we will continue from the next
	// non-terminal step.
	// We can safely skip any subsequent terminal steps at this point,
	// because any terminal steps we may take have already been counted in
	// PartialPath::min_length, and simply following the non-terminals does
	// not require any non-deterministic choices.
	ext->curr_step = step_follow_choice_skip_terminals(parent, c);
	ext_paths.push_back(ext);
    }
    return ext_paths;
}

/**
 * Construct the @a num_paths shortest paths that could have generated @a edge.
 * In case there's fewer than @a num_paths paths, return all of them.
 *
 * @return A list of up to @a num_paths PartialPath%s for @a edge, sorted in
 * shortest-first order. The objects returned are of type PartialPath, but they
 * represent complete paths, @see partial_path_is_complete().
 */
std::list<PartialPath*> recover_paths(Step *top_step,
				      const unsigned int num_paths) {
    if (num_paths == 1) {
	bake_single_path(top_step);
    }

    std::list<PartialPath*> full_paths;
    // std::priority_queue::pop() always returns the largest element, so we
    // have to use 'possibly longer path' as the 'less than' operator.
    auto maybe_longer = [](PartialPath *p1, PartialPath *p2) {
    	return p1->min_length >= p2->min_length;
    };
    // Priority queue of partial paths, sorted by lower bound on length. Paths
    // stored in the priority queue can either be complete or point to a
    // non-terminal edge.
    std::priority_queue<PartialPath*, std::vector<PartialPath*>,
    			decltype(maybe_longer)> queue(maybe_longer);
    queue.push(partial_path_init(top_step));
    unsigned int paths_found = 0;
    PATH_LENGTH min_length = 0;

    while (paths_found < num_paths && !queue.empty()) {
	PartialPath *p = queue.top();
	queue.pop();

	if (partial_path_is_complete(p)) {
	    // p is a complete path for the input edge, and it should be the
	    // shortest remaining one.
	    assert(min_length <= p->min_length);
	    min_length = p->min_length;
	    full_paths.push_back(p);
	    paths_found++;
	    continue;
	}
	assert(!partial_path_is_at_terminal(p));
	// The path points to a non-terminal edge: split it according to the
	// expansion choices.
	for (PartialPath *ext_p : partial_path_split(p)) {
	    // The terminals on the returned paths should have been skipped.
	    assert(!partial_path_is_at_terminal(ext_p));
	    queue.push(ext_p);
	}
	// The previous path is no longer needed, and can be free'd.
	free(p);
    }

    // We should find at least one path, the one which actually produced the
    // input edge.
    assert(paths_found > 0);
    // TODO: The remaining PartialPath's, as well as the Step and Choice trees,
    // are not deallocated. The latter two are necessary to allow the caller to
    // retrieve the paths from the returned ChoiceSequences.
    return full_paths;
}

/* PATH PRINTING =========================================================== */

void print_step_open(Edge *edge, bool reverse, FILE *f) {
    if (is_terminal(edge->kind)) {
	fprintf(f, "<%s", kind2symbol(edge->kind));
    } else {
	fprintf(f, "<%s symbol='%s'",
		is_temporary(edge->kind) ? "TempStep" : "NTStep",
		kind2symbol(edge->kind));
    }
    fprintf(f, " reverse='%s' from='%s' to='%s'", reverse ? "true" : "false",
	    node_names.name_of(edge->from).c_str(),
	    node_names.name_of(edge->to).c_str());
    if (is_parametric(edge->kind)) {
	char idx_buf[32];
	// TODO: Check that printing is successful.
	index_print(edge->index, idx_buf, sizeof(idx_buf));
	fprintf(f, " index='%s'", idx_buf);
    }
    if (is_terminal(edge->kind)) {
	// Terminal steps can have no nested sub-steps, so we can close the tag
	// at this point.
	fprintf(f, "/>\n");
    } else {
	fprintf(f, ">\n");
    }
}

void print_step_close(Edge *edge, FILE *f) {
    if (is_terminal(edge->kind)) {
	// The tag has already been closed, nothing to do at this point.
    } else if (is_temporary(edge->kind)) {
	fprintf(f, "</TempStep>\n");
    } else {
	fprintf(f, "</NTStep>\n");
    }
}

// Only takes complete paths (assumes trees are present).
// TODO: Reverse direction is not baked into the final representation. To
// support this, we'd need to:
// - add an 'in_reverse' boolean parameter, that defines the direction we're
//   currently visiting the edges
// - update 'in_reverse' appropriatelly for recursive calls
//   e.g. step->is_reverse ^ in_reverse
// - visit sub-steps in reverse order if the parent Step was visited in reverse
//   i.e. the last child first
// - reorder the choices appropriattely (they were recorded in unbaked order).
//   e.g. for D :: _C, C :: A B, A :: a0 | a1, B :: b0 | b1, and input "b1 a0",
//   the choices would be recorded as [0,1], but the real series of choices
//   (the one that respects the reverse directions) would be [1,0].
// - don't print out both endpoints for each step, only the "real target" node
//   (either the edge's target or source, depending on the direction of
//   traversal)
// For now, the client of this output will need to bake the results (or we
// could avoid printing immediatelly, and do this at a post-processing step).
// TODO: Generalize visitor pattern.
void print_step(Step *step, std::stack<CHOICE> &choices, FILE *f) {
    print_step_open(step->edge, step->is_reverse, f);
    if (!is_terminal(step->edge->kind)) {
	assert(!choices.empty());
	CHOICE c = choices.top();
	choices.pop();
	Step *sub_steps = step_follow_choice(step, c);
	for (Step *s = sub_steps; s != NULL; s = s->next_sibling) {
	    print_step(s, choices, f);
	}
    }
    print_step_close(step->edge, f);
}

void print_path(PartialPath *path, Step *top_step, FILE *f) {
    assert(partial_path_is_complete(path));
    assert(top_step->next_sibling == NULL);
    fprintf(f, "<path>\n");
    std::stack<CHOICE> choices = choice_sequence_unwind(path->choices);
    print_step(top_step, choices, f);
    // All choices should have been exhausted.
    assert(choices.empty());
    fprintf(f, "</path>\n");
}

#ifdef PATH_RECORDING
void print_prerecorded_step(Edge *e, bool reverse, FILE *f) {
    print_step_open(e, reverse, f);
    if (e->l_edge != NULL) {
	print_prerecorded_step(e->l_edge, e->l_rev, f);
	if (e->r_edge != NULL) {
	    print_prerecorded_step(e->r_edge, e->r_rev, f);
	}
    }
    print_step_close(e, f);
}

void print_prerecorded_path(Edge *e, FILE *f) {
    fprintf(f, "<path>\n");
    print_prerecorded_step(e, false, f);
    fprintf(f, "</path>\n");
}
#endif

void print_paths_for_kind(EDGE_KIND k, unsigned int num_paths) {
    assert(!is_terminal(k));
    // TODO: Filename buffer may be too small.
    char fname[256];
    snprintf(fname, sizeof(fname), "%s/%s.%s", OUTPUT_DIR, kind2symbol(k),
	     PATHS_FORMAT);
    FILE *f = fopen(fname, "w");
    if (f == NULL) {
	SYS_ERR("Can't open output file: ", fname);
    }
    fprintf(f, "<paths>\n");

    for (NODE_REF n = 0; n < num_nodes(); n++) {
	const char *tgt_name = node_names.name_of(n).c_str();
	for (Edge *e : edges_to(n, k)) {
	    const char *src_name = node_names.name_of(e->from).c_str();
	    // TODO: Quote input strings before printing to XML.
	    fprintf(f, "<edge from='%s' to='%s'>\n", src_name, tgt_name);
#ifdef PATH_RECORDING
	    print_prerecorded_path(e, f);
#else
	    Step *top_step = step_init(e);
	    for (PartialPath *p : recover_paths(top_step, num_paths)) {
		print_path(p, top_step, f);
	    }
#endif
	    fprintf(f, "</edge>\n");
	}
    }

    fprintf(f, "</paths>\n");
    fclose(f);
}

void print_paths() {
    for (EDGE_KIND k = 0; k < num_kinds(); k++) {
	if (is_predicate(k)) {
	    // TODO: Also check that no edges have been produced.
	    continue;
	}
	unsigned int num_paths = num_paths_to_print(k);
	if (num_paths > 0) {
	    print_paths_for_kind(k, num_paths);
	}
    }
}

/* LOGGING CODE ============================================================ */

/* Underlying implementation of logging macros. This code is also skipped if
   LOGGING is not set. These functions shouldn't be called directly; use the
   corresponding macros instead. */

#ifdef LOGGING

/**
 * Underlying implementation of @ref LOGGING_INIT.
 */
void logging_init() {
    // This should print large numbers according to the user's default locale.
    std::cout.imbue(std::locale(""));
}

/**
 * Print out statistics about the Edge%s in the graph.
 *
 * @param [in] terminal Whether to print statistics about the Edges
 * representing terminal @Symbol%s or non-terminals.
 */
void print_edge_counts(bool terminal) {
    // TODO: Could cache these counts.
    // TODO: use INC_COUNTER in print_stats
    DECL_COUNTER(total_edge_count, 0);
    DECL_COUNTER(total_index_count, 0);
    for (EDGE_KIND k = 0; k < num_kinds(); k++) {
	if (is_predicate(k) || is_terminal(k) ^ terminal) {
	    continue;
	}
	bool parametric = is_parametric(k);
	DECL_COUNTER(edge_count, 0);
	DECL_COUNTER(index_count, 0);
	for (NODE_REF n = 0; n < num_nodes(); n++) {
	    std::set<NODE_REF> sources;
	    for (Edge *e : edges_to(n, k)) {
		if (sources.insert(e->from).second) {
		    edge_count++;
		    total_edge_count++;
		}
		if (parametric) {
		    index_count++;
		    total_index_count++;
		}
	    }
	}
	if (parametric) {
	    LOG(std::setw(15), kind2symbol(k), ": ",
		std::setw(12), edge_count, " edges",
		std::setw(12), index_count, " indices");
	} else {
	    LOG(std::setw(15), kind2symbol(k), ": ",
		std::setw(12), edge_count, " edges");
	}
    }
    LOG("Total edges: ", total_edge_count);
    LOG("Total indices: ", total_index_count);
}

/**
 * Underlying implementation of @ref PRINT_STATS.
 */
void print_stats() {
    LOG("Terminals:");
    print_edge_counts(true);
    LOG("Non-terminals:");
    print_edge_counts(false);
    LOG("Memory usage: ", allocated_memory(), " kbytes");
}

/**
 * Underlying implementation of @ref CURRENT_TIME.
 */
COUNTER current_time() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (tv.tv_sec) * 1000 + (tv.tv_usec) / 1000;
}

#endif /* LOGGING */

/* PROFILING CODE ========================================================== */

#ifdef PROFILING

/**
 * The PID of the profiler attached to this process.
 */
pid_t profiler_pid;

/**
 * Underlying implementation of @ref START_PROFILER.
 */
void start_profiler() {
    LOG("Starting profiler");
    char pid_arg[10];
    /* TODO: PID arg buffer may be too small. */
    snprintf(pid_arg, sizeof(pid_arg), "%u", getpid());
    profiler_pid = fork();
    if (profiler_pid == 0) { /* In the child process. */
	/* TODO: The solver will run unprofiled for a little time before the
	   profiler has had enough time to finish initialization. */
	execlp("perf", "perf", "stat", "-p", pid_arg, (char *) NULL);
	/* Will only reach here if exec fails. */
	SYS_WARN("Profiler spawn failed");
    }
}

/**
 * Underlying implementation of @ref STOP_PROFILER.
 */
void stop_profiler() {
    LOG("Stopping profiler");
    kill(profiler_pid, SIGINT);
    waitpid(profiler_pid, NULL, 0);
}

/**
 * @defgroup Profiling Profiling Facilities
 * Functionality for profiling a solver run.
 *
 * Anything in this part of the API will be compiled away, and thus cause no
 * overhead, if @e PROFILING is not defined at compilation time.
 * @{
 */

/**
 * Start a profiler in an external process and attach it to the solver process.
 *
 * We are currently using @e perf as our profiler.
 */
#define START_PROFILER() start_profiler()

/**
 * Stop the running profiler. The profiler should print its measurements at
 * this point.
 */
#define STOP_PROFILER() stop_profiler()

/**
 * @}
 */

#else /* PROFILING */

#define START_PROFILER()
#define STOP_PROFILER()

#endif /* PROFILING */

/* MAIN FUNCTION =========================================================== */

/**
 * Read in the non-terminal Edge%s, run the analysis, then print out all
 * generated non-terminal Edge%s.
 */
int main() {
    mem_manager_init();
    LOGGING_INIT();

    DECL_COUNTER(loading_start, CURRENT_TIME());
    parse_input_files(ParsingMode::RECORD_NODES);
    node_names.finalize();
    nodes = new Node[num_nodes()];
    LOG("Nodes: ", num_nodes());
    parse_input_files(ParsingMode::RECORD_EDGES);
    rels_init();
    parse_rels();
    LOG("Loading completed in ", CURRENT_TIME() - loading_start, " ms");

    START_PROFILER();
    DECL_COUNTER(solving_start, CURRENT_TIME());
    /* Process empty productions. */
    for (EDGE_KIND k = 0; k < num_kinds(); k++) {
	if (has_empty_prod(k) && !is_predicate(k)) {
	    for (NODE_REF n = 0; n < num_nodes(); n++) {
		// We have disallowed predicates on empty productions.
		add_edge(n, n, k, INDEX_NONE, NULL, false, NULL, false);
	    }
	}
    }
    /* Main loop. */
    while (!worklist.empty()) {
	INC_COUNTER(iteration, 1);
	Edge *base = worklist.front();
	worklist.pop_front();
	main_loop(base);
    }
    LOG("Fixpoint reached after ", iteration, " iterations");
    LOG("Solving completed in ", CURRENT_TIME() - solving_start, " ms");
    STOP_PROFILER();

    DECL_COUNTER(printing_start, CURRENT_TIME());
    PRINT_STATS();
    print_results();
    print_paths();
    LOG("Printing completed in ", CURRENT_TIME() - printing_start, " ms");
}
