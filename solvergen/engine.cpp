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
LightMap<K,V>::LightMap() : list_size(0) {}

template<typename K, typename V>
V& LightMap<K,V>::const_get(K k) {
    auto pos = find(k);
    if (pos == list.end()) {
	return dummy;
    } else {
	return (*pos).second;
    }
}

template<typename K, typename V>
V& LightMap<K,V>::operator[](K k) {
    auto pos = find(k);
    if (pos == list.end()) {
	list.emplace_front(k, V());
	list_size++;
	return list.front().second;
    } else {
	return (*pos).second;
    }
}

template<typename K, typename V>
V& LightMap<K,V>::at(K k) {
    auto pos = find(k);
    if (pos == list.end()) {
	throw std::out_of_range("");
    } else {
	return (*pos).second;
    }
}

template<typename K, typename V>
typename LightMap<K,V>::Size LightMap<K,V>::size() const {
    return list_size;
}

template<typename K, typename V>
typename LightMap<K,V>::Iterator LightMap<K,V>::begin() {
    return list.begin();
}

template<typename K, typename V>
typename LightMap<K,V>::Iterator LightMap<K,V>::end() {
    return list.end();
}

template<typename K, typename V>
void LightMap<K,V>::swap(LightMap& other) {
    std::swap(list_size, other.list_size);
    std::swap(list, other.list);
}

/* NODES HANDLING ========================================================== */

NodeNameMap::NodeNameMap() : is_final(false) {}

void NodeNameMap::add(const char *name_cstr) {
    assert(!is_final);
    std::string name(name_cstr);
    if (map.insert(std::make_pair(name, vector.size())).second) {
	vector.push_back(name);
	// Make sure we haven't overflown NODE_REF.
	assert(VALID_NODE_REF(vector.size()));
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
// TODO: Don't need to perform the field checks; Edges are uniqued (should
// still use this function instead of a naked pointer check).
bool edge_equals(Edge *a, Edge *b) {
    return a == b || ((a->from == b->from) && (a->to == b->to) &&
		      (a->kind == b->kind) && (a->index == b->index));
}

bool same_arc(Edge *a, Edge *b) {
    // TODO: The equality check is added to gracefully handle NULL pointers.
    return a == b || ((a->from == b->from) && (a->to == b->to) &&
		      (a->kind == b->kind));
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

/* DERIVATION HANDLING ===================================================== */

EdgeGroup::EdgeGroup(Edge *top_edge) {
    edges[top_edge];
}

EdgeGroup::EdgeGroup(Edge *e, Edge *parent) {
    edges[e].insert(parent);
}

EdgeGroup::EdgeGroup(Edge *e, EdgeGroup& prods_src) {
    for (const auto& edgeAndProds : prods_src) {
	const std::set<Edge*>& prods = edgeAndProds.second;
	edges[e].insert(prods.cbegin(), prods.cend());
    }
}

void EdgeGroup::swap(EdgeGroup& other) {
    std::swap(edges, other.edges);
}

EdgeGroup::EdgeGroup(EdgeGroup&& other) {
    other.swap(*this);
}

bool EdgeGroup::add(Edge *e, Edge *parent) {
    // No need to record the characteristics of the group's endpoints, we can
    // just check against an arbitrary edge from the group (since it's always
    // non-empty).
    if (same_arc(e, first())) {
	edges[e].insert(parent);
	return true;
    }
    return false;
}

const std::set<Edge*>& EdgeGroup::operator[](Edge *e) {
    return edges.at(e);
}

EdgeGroup::Iterator EdgeGroup::begin() {
    return edges.begin();
}

EdgeGroup::Iterator EdgeGroup::end() {
    return edges.end();
}

Edge *EdgeGroup::first() {
    return begin()->first;
}

Derivation::Shared::Shared(Edge* common_edge, bool left_reverse,
			   bool right_reverse)
    : common_edge(common_edge), left_reverse(left_reverse),
      right_reverse(right_reverse) {}


bool Derivation::Shared::is_null() const {
    return common_edge == NULL;
}

bool Derivation::Shared::operator==(const Shared& other) const {
    // TODO: Using pointer equality for Edge*.
    return (common_edge == other.common_edge &&
	    left_reverse == other.left_reverse &&
	    right_reverse == other.left_reverse);
}

Derivation::Group::Group(Position group_by) : group_by(group_by) {}

void Derivation::Group::add(const Derivation& deriv, Edge *parent) {
    std::pair<Shared,Edge*> parts = deriv.split(group_by);
    // Group the single and empty Derivations under a special Shared part.
    // Derivation::split() will always return NULL in the Shared part for
    // those, regardless of requested direction.
    std::forward_list<EdgeGroup>& completions = groups[parts.first];
    Edge *e = parts.second;

    for (EdgeGroup& g : completions) {
	if (g.add(e, parent)) {
	    return;
	}
    }
    completions.emplace_front(e, parent);
}

// Not accurate: will count all non-double derivations as one group, but that's
// acceptable, because this is only used for comparing groupings, and both
// possible directions treat those Derivations in the same way.
Derivation::Group::Size Derivation::Group::size() const {
    return groups.size();
}

Derivation::Group::Iterator Derivation::Group::begin() {
    return groups.begin();
}

Derivation::Group::Iterator Derivation::Group::end() {
    return groups.end();
}

Derivation::Derivation()
    : left_edge(NULL),     right_edge(NULL),
      left_reverse(false), right_reverse(false) {}

Derivation::Derivation(Edge *e, bool reverse)
    : left_edge(e),          right_edge(NULL),
      left_reverse(reverse), right_reverse(false) {}

Derivation::Derivation(Edge *left_edge,  bool left_reverse,
		       Edge *right_edge, bool right_reverse)
    : left_edge(left_edge),       right_edge(right_edge),
      left_reverse(left_reverse), right_reverse(right_reverse) {}

std::pair<Derivation::Shared,Edge*>
Derivation::split(Position group_by) const {
    if (group_by == Position::RIGHT || right_edge == NULL) {
	return std::make_pair(Shared(right_edge, left_reverse, right_reverse),
			      left_edge);
    } else {
	return std::make_pair(Shared(left_edge, left_reverse, right_reverse),
			      right_edge);
    }
}

/* STEP HANDLING =========================================================== */

void StepTree::Step::check_invariants() {
    assert(!is_terminal() || !expanded);
    assert(expanded || choices.empty());
    // TODO: Could check the parent pointer on all the sub-steps.
    // TODO: Could check that all sub-steps of a closed Step are also closed.
}

StepTree::Step::Step(Step *parent, Step *next_sibling, EdgeGroup&& edges,
		     bool reverse)
    : parent(parent), next_sibling(next_sibling), edges(std::move(edges)),
      reverse(reverse), expanded(false), closed(false) {
    check_invariants();
}

void StepTree::Step::add_choices(Derivation::Group& grouping) {
    for (auto& subgroup : grouping) {
	const Derivation::Shared& common = subgroup.first;
	std::forward_list<EdgeGroup>& completions = subgroup.second;

	if (common.is_null()) {
	    // Special case for empty and single productions
	    for (EdgeGroup& g : completions) {
		if (g.first() == NULL) {
		    // Don't create a Step for empty productions.
		    choices.push_back(NULL);
		} else {
		    choices.push_back(new Step(this, NULL, std::move(g),
					       common.left_reverse));
		}
	    }
	    return;
	}

	bool common_l = grouping.group_by == Position::LEFT;
	for (EdgeGroup& g : completions) {
	    assert(g.first() != NULL);
	    // The shared Edge can produce in any of the parent Edges that the
	    // completion Edges produce.
	    // BUG: The two chains are now independent, so we might accept a
	    // derivation chaing that wouldn't work if we kept them linked.
	    // Will probably not cause an infinite loop, just a slightly
	    // inconsistent result.
	    EdgeGroup common_group = EdgeGroup(common.common_edge, g);
	    Step *second =
		new Step(this, NULL,
			 (common_l) ? std::move(g) : std::move(common_group),
			 common.right_reverse);
	    Step *first =
		new Step(this, second,
			 (common_l) ? std::move(common_group) : std::move(g),
			 common.left_reverse);
	    choices.push_back(first);
	}
    }
}

bool StepTree::Step::valid_derivation(const Derivation& deriv, Edge *e) {
    // TODO: Verify that e is actually an Edge of the current EdgeGroup.
    Edge *left_edge = deriv.left_edge;
    Edge *right_edge = deriv.right_edge;
    // Nothing to check for an empy Derivation.
    if (left_edge == NULL) {
	assert(right_edge == NULL);
	return true;
    }
    // Verify that the producing Edges don't conflict with our current Edge.
    if (edge_equals(e, left_edge) ||
	(right_edge != NULL && edge_equals(e, right_edge))) {
	return false;
    }
    // Nothing more to check if we're at the top Edge of the StepTree.
    if (parent == NULL) {
	assert(edges[e].empty());
	return true;
    }
    // Verify that we haven't recursed on one of the Edges in the Derivation.
    // We only need to find a single consistent production chain for the
    // current Edge.
    for (Edge *product : edges[e]) {
	if (parent->valid_derivation(deriv, product)) {
	    return true;
	}
    }
    // No production chain without repetition could be found.
    return false;
}

StepTree::Step::Step(Edge *top_edge)
    : Step(NULL, NULL, EdgeGroup(top_edge), false) {}

bool StepTree::Step::is_terminal() {
    return ::is_terminal(edges.first()->kind);
}

bool StepTree::Step::is_expanded() const {
    return expanded;
}

bool StepTree::Step::is_closed() const {
    return closed;
}

bool StepTree::Step::valid_choice(CHOICE_REF c) const {
    assert(expanded);
    return c < choices.size();
}

CHOICE_REF StepTree::Step::num_choices() const {
    assert(expanded);
    return choices.size();
}

void StepTree::Step::expand() {
    // Edges corresponding to terminal symbols are elementary steps, which
    // cannot be expanded any further.
    assert(!is_terminal());
    if (expanded) {
	return;
    }
    expanded = true;

    // We only support Edge grouping on one of the two sub-steps, so we need to
    // decide which grouping strategy would be more efficient. For this reason,
    // we build two alternative groupings from Derivations, then pick the best.
    // TODO: Allow grouping on both positions. But we need to allow independent
    // expansion of the two sub-steps, so that would only trigger iff we're
    // processing the cartesian product of two index sets. Alternatively, we
    // could track the relationship between the two.
    // TODO: Could allow both kinds of grouping simultaneously (some part of
    // the choices are grouped one way, and the rest the other way).
    Derivation::Group left_grouping(Position::LEFT);
    Derivation::Group right_grouping(Position::RIGHT);

    // TODO: Make sure we don't get more choices than we can store.
    for (const auto& edgeAndProds : edges) {
	Edge *parent = edgeAndProds.first;
	// For each Edge in the current EdgeGroup, calculate all derivations
	// that could have produced it.
	// TODO: Could build the maps directly inside all_derivations, instead
	// of returning a list (but would have to filter for validity on the
	// maps).
	for (const Derivation& d : all_derivations(parent)) {
	    // Some of these might be invalid for this Step, so we filter them.
	    // TODO: Prune the branch if we find no valid derivations.
	    if (valid_derivation(d, parent)) {
		left_grouping.add(d, parent);
		right_grouping.add(d, parent);
	    }
	}
    }

    // Pick the best grouping: currently just taking the one with the fewest
    // common groups.
    // TODO: Should also count the #choices each shared group would produce.
    // TODO: Could implement a better decision heuristic.
    if (left_grouping.size() < right_grouping.size()) {
	add_choices(left_grouping);
    } else {
	add_choices(right_grouping);
    }

    check_invariants();
}

void StepTree::Step::close() {
    if (closed) {
	// We assume that all sub-steps of closed steps have also been marked
	// as closed.
	return;
    }
    closed = true;
    for (Step *step : choices) {
	for (; step != NULL; step = step->next_sibling) {
	    step->close();
	}
    }

    check_invariants();
}

StepTree::Step *StepTree::Step::follow(CHOICE_REF choice) const {
    assert(expanded);
    return choices.at(choice);
}

PATH_LENGTH StepTree::Step::estimate_sequence_length() {
    PATH_LENGTH min_length = 0;
    for (Step *step = this; step != NULL; step = step->next_sibling) {
	min_length += static_min_length(step->edges.first()->kind);
    }
    return min_length;
}

/* EXPANDER HANDLING ======================================================= */

void StepTree::Expander::check_invariants() const {
    if (curr_step == NULL) {
	assert(choice == CHOICE_NONE);
    } else {
	assert(!curr_step->is_terminal());
	if (choice != CHOICE_NONE) {
	    assert(curr_step->valid_choice(choice));
	}
    }
}

StepTree::Expander::Expander(Step *curr_step, CHOICE_REF choice,
			     PATH_LENGTH min_length, Path *path)
    : curr_step(curr_step), choice(choice), min_length(min_length),
      path(path) {
    check_invariants();
}

StepTree::Expander::Expander(Step *top_step)
    : Expander(top_step, CHOICE_NONE,
	       static_min_length(top_step->edges.first()->kind), NULL) {}

bool StepTree::Expander::at_closed_step() const {
    return curr_step != NULL && curr_step->is_closed();
}

bool StepTree::Expander::at_end() const {
    return curr_step == NULL;
}

bool StepTree::Expander::at_choice() const {
    return curr_step != NULL && choice == CHOICE_NONE;
}

void StepTree::Expander::move_to_next_choice() {
    assert(curr_step == NULL || !curr_step->is_closed());
    // We only traverse terminals and empty productions, whose length has
    // already been included in the running estimate, so we don't need to
    // update min_length. Also, we don't need to make any non-deterministic
    // choices, so we don't need to update our path.

    if (choice == CHOICE_NONE || curr_step == NULL) {
	// Special case, where we start right on a choice, or at the end.
	return;
    }

    Step *parent = curr_step;
    Step *curr = parent->follow(choice);
    choice = CHOICE_NONE;

    while (curr == NULL) {
	// We've hit an empty production, so we try the next sibling of the
	// parent step. We may have to skip multiple levels to reach one that
	// hasn't been fully traversed yet.
	if (parent == NULL) {
	    // We've reached the top of the step tree.
	    curr_step = NULL;
	    check_invariants();
	    return;
	}
	// We're the first Expander to pop back to a non-deterministic choice,
	// so we mark it as closed (any other Expander that reaches this point
	// will be longer, and thus not worth keeping).
	assert(!parent->is_closed());
	parent->close();
	curr = parent->next_sibling;
	parent = parent->parent;
    }

    // We are at a concrete Step, and need to stop at the first non-terminal
    // that we find.
    while (curr->is_terminal()) {
	assert(!curr->is_closed());
	// Move to the next Step on the same level, repeat if it's a terminal
	// Step.
	while (curr->next_sibling == NULL) {
	    // If we're at the end of the current level, move up to the first
	    // level that is not yet fully traversed, and continue from there.
	    curr = curr->parent;
	    if (curr == NULL) {
		// We've reached the top of the step tree.
		curr_step = NULL;
		check_invariants();
		return;
	    }
	    // Another case of pop-back; close all choices.
	    assert(!curr->is_closed());
	    curr->close();
	}
	curr = curr->next_sibling;
    }

    assert(!curr->is_closed());
    curr_step = curr;
    check_invariants();
}

std::list<StepTree::Expander> StepTree::Expander::fork() const {
    assert(at_choice());
    // Lazily expand the tree at the current step.
    curr_step->expand();

    std::list<Expander> children;
    PATH_LENGTH curr_edge_estimate =
	static_min_length(curr_step->edges.first()->kind);

    for (CHOICE_REF c = 0; c < curr_step->num_choices(); c++) {
	Step *sub_steps = curr_step->follow(c);
	// We know how many terminals are added to the path by this production,
	// so we immediatelly include them in the lower bound for the path's
	// length.
	PATH_LENGTH child_min_len = (min_length - curr_edge_estimate +
				     sub_steps->estimate_sequence_length());
	Path *child_path = new Path(path, c);
	children.push_back(Expander(curr_step, c, child_min_len, child_path));
    }

    return children;
}

bool StepTree::Expander::operator<(const Expander& other) const {
    return min_length > other.min_length;
}

/* PATH CALCULATION ======================================================== */

StepTree::Path::Path(Path *prefix, CHOICE_REF last)
    : prefix(prefix), last(last) {}

std::stack<CHOICE_REF> StepTree::Path::unwind(const Path *path) {
    std::stack<CHOICE_REF> choices;
    for (const Path *p = path; p != NULL; p = p->prefix) {
	choices.push(p->last);
    };
    return choices;
}

StepTree::StepTree(Edge *top_edge) : root(Step(top_edge)) {
    assert(!is_terminal(top_edge->kind));
}

std::stack<CHOICE_REF> StepTree::expand() {
    // Priority queue of all live Expanders over the StepTree, sorted by lower
    // bound on length.
    std::priority_queue<Expander> queue;
    queue.push(Expander(&root));

    while (!queue.empty()) {

	Expander exp = queue.top();
	queue.pop();

	// Check if we should discard this Expander: If the Step we're
	// currently on or any of its ancestors has been closed, that must have
	// been caused by a previously processed Expander, which had a better
	// minimum length estimate, therefore we don't need to continue with
	// this one (we couldn't possibly do any better).
	// We only need to check if the Step we start from is closed; only one
	// of the Expanders forked at each non-deterministic choice survives,
	// and when it does, it recursively closes all the Steps under all
	// other choices.
	if (exp.at_closed_step()) {
	    continue;
	}
	exp.move_to_next_choice();
	if (exp.at_end()) {
	    // We have fully traversed the StepTree, while always working on
	    // the shortest possible alternative. Thus, we have found the
	    // shortest path.
	    return Path::unwind(exp.path);
	}
	// The Expander points to a non-terminal Step: split it according to
	// the expansion choices.
	for (Expander e : l) {
	    queue.push(e);
	}

	// Discard the parent Expander.
    }

    // No path found, return an empty choice stack (since we only allow path
    // calculations for non-terminals, this should be unambiguous).
    return std::stack<CHOICE_REF>();
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
void StepTree::Step::print(std::stack<CHOICE_REF>& path, FILE *f) {
    print_step_open(edges.first(), reverse, f);
    if (!is_terminal()) {
	assert(!path.empty());
	CHOICE_REF c = path.top();
	path.pop();
	for (Step *s = follow(c); s != NULL; s = s->next_sibling) {
	    s->print(path, f);
	}
    }
    print_step_close(edges.first(), f);
}

void StepTree::print_path(const std::stack<CHOICE_REF>& path, FILE *f) {
    fprintf(f, "<path>\n");
    std::stack<CHOICE_REF> p(path);
    root.print(p, f);
    // All choices should have been exhausted.
    assert(p.empty());
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
	    StepTree tree = StepTree(e);
	    std::stack<CHOICE_REF> path = tree.expand();
	    tree.print_path(path, f);
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
