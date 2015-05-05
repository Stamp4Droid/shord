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
#include <stdio.h>
#include <string>
#include <string.h>
#include <sys/resource.h>
#include <sys/stat.h>
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

#ifdef PATH_RECORDING
/**
 * Edge comparator object: An Edge is ranked better than another iff the path
 * which constructed it was shorter.
 */
auto edge_lt = [](Edge* x, Edge* y){return x->length > y->length;};

/** The Edge worklist used by the fixpoint calculation. */
std::priority_queue<Edge*,std::vector<Edge*>,decltype(edge_lt)>
worklist(edge_lt);
#else
/** The Edge worklist used by the fixpoint calculation. */
std::queue<Edge*> worklist;
#endif

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

/* UTILITY FUNCTIONS ======================================================= */

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
 * Return the maximum amount of memory this process has ever held at any one
 * time (in kbytes).
 */
long allocated_memory() {
    struct rusage usage;
    getrusage(RUSAGE_SELF, &usage);
    return usage.ru_maxrss;
}

/**
 * Create a directory with the given name, unless it already exists.
 */
void create_directory(const char *path) {
    if (mkdir(path, S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH) != 0) {
	if (errno != EEXIST) {
	    SYS_ERR("Can't create directory ", path);
	}
    }
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

/* TRANSITION FUNCTION HANDLING ============================================ */

TRANS_FUN_REF compose(TRANS_FUN_REF f, TRANS_FUN_REF g) {
    assert(f < num_trans_funs() && g < num_trans_funs());
    return compose_tab[f * num_trans_funs() + g];
}

/* EDGE OPERATIONS ========================================================= */

Edge::Edge(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index,
	   TRANS_FUN_REF fwd_tfun, TRANS_FUN_REF bck_tfun,
	   Edge* l_edge, bool l_rev, Edge* r_edge, bool r_rev)
    : from(from), to(to), kind(kind)
#ifdef PATH_RECORDING
    , l_rev(l_rev), r_rev(r_rev)
    , length(is_terminal(kind) ? static_min_length(kind)
	     : (l_edge == NULL ? 0 : l_edge->length) +
	       (r_edge == NULL ? 0 : r_edge->length))
    , fwd_tfun(fwd_tfun), bck_tfun(bck_tfun)
#endif
    , index(index)
#ifdef PATH_RECORDING
    , l_edge(l_edge), r_edge(r_edge)
#endif
{
    assert((is_parametric(kind)) ^ (index == INDEX_NONE));
#ifdef PATH_RECORDING
    // TODO: Verify that at least one of the regular directions is valid.
    assert(l_edge != NULL || r_edge == NULL);
#endif
}

/**
 * Check if two Edge objects represent the same graph edge.
 */
// TODO: Don't need to perform the field checks; Edges are uniqued (should
// still use this function instead of a naked pointer check).
bool edge_equals(Edge *a, Edge *b) {
    return a == b || ((a->from == b->from) && (a->to == b->to)
		      && (a->kind == b->kind) && (a->index == b->index)
#ifdef PATH_RECORDING
		      && (a->fwd_tfun == b->fwd_tfun)
		      && (a->bck_tfun == b->bck_tfun)
#endif
		      );
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

Edge *find_edge(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index,
		TRANS_FUN_REF fwd_tfun, TRANS_FUN_REF bck_tfun) {
    assert(!is_predicate(kind));
    assert((index == INDEX_NONE) ^ is_parametric(kind));
    // We search on the set of outgoing edges of the source node, because it's
    // indexed on more dimensions.
    for (Edge *e : get_out_set(from, kind).view(to, index)) {
	// We stop at the first compatible Edge; the view can only contain one
	// such element anyway.
	// TODO: Could verify this, but we'd be performing useless work.
#ifdef PATH_RECORDING
	if (e->fwd_tfun == fwd_tfun && e->bck_tfun == bck_tfun) {
	    return e;
	}
#else
	return e;
#endif
    }
    return NULL;
}

// Return true iff the Edge wasn't already present in the graph.
bool graph_add(Edge* e) {
    if (find_edge(e->from, e->to, e->kind, e->index, e->fwd_tfun,
		  e->bck_tfun) != NULL) {
	return false;
    }
    nodes[e->to].in[e->kind].add(e);
    nodes[e->from].out[e->kind].add(e);
    return true;
}

void add_edge(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index,
	      Edge* l_edge, bool l_rev, Edge* r_edge, bool r_rev) {
#ifdef PATH_RECORDING
    TRANS_FUN_REF fwd_tfun, bck_tfun;
    if (l_edge == NULL) {
	assert(!l_rev && r_edge == NULL);
	if (is_terminal(kind)) {
	    fwd_tfun = trans_fun_for(kind, false);
	    bck_tfun = trans_fun_for(kind, true);
	} else {
	    // non-terminal with empty production
	    fwd_tfun = TRANS_FUN_ID;
	    bck_tfun = TRANS_FUN_ID;
	}
    } else if (r_edge == NULL) {
	fwd_tfun = l_rev ? l_edge->bck_tfun : l_edge->fwd_tfun;
	bck_tfun = l_rev ? l_edge->fwd_tfun : l_edge->bck_tfun;
    } else {
	fwd_tfun = compose(l_rev ? l_edge->bck_tfun : l_edge->fwd_tfun,
			   r_rev ? r_edge->bck_tfun : r_edge->fwd_tfun);
	bck_tfun = compose(r_rev ? r_edge->fwd_tfun : r_edge->bck_tfun,
			   l_rev ? l_edge->fwd_tfun : l_edge->bck_tfun);
    }
    if (fwd_tfun == TRANS_FUN_BOTTOM && bck_tfun == TRANS_FUN_BOTTOM) {
	return;
    }
#endif

    Edge* e = new Edge(from, to, kind, index, fwd_tfun, bck_tfun,
		       l_edge, l_rev, r_edge, r_rev);
#ifdef PATH_RECORDING
    if (is_terminal(kind)) {
#endif
	if (!graph_add(e)) {
	    delete e;
	    return;
	}
#ifdef PATH_RECORDING
    }
#endif
    worklist.push(e);
}

/* INPUT & OUTPUT ========================================================== */

/**
 * The directory where we expect to find the initial set of terminal-@Symbol
 * Edge%s.
 */
const char* INPUT_DIR;

/**
 * The directory where we dump the Node listing and final set of
 * non-terminal-@Symbol Edge%s.
 */
const char* OUTPUT_DIR;

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

bool Derivation::empty() const {
    if (left_edge == NULL) {
	assert(right_edge == NULL);
	return true;
    }
    return false;
}

/* PATH CALCULATION ======================================================== */

void DerivTable::Record::check_invariants() {
    // TODO: At least one parent should be present at some point.
    // TODO: Could check that this Edge is present on at least one Derivation
    // on each parent.
    // TODO: Could combine with characteristics of the Edge kind, e.g. if it's
    // a terminal or not.
    assert(!frozen || VALID_PATH_LENGTH(length));
    assert(!VALID_CHOICE_REF(best_choice) || (VALID_PATH_LENGTH(length) &&
					      best_choice < derivs.size()));
}

DerivTable::Record::Record(Edge* e)
    : derivs(e == NULL || is_terminal(e->kind)
	     ? std::vector<Derivation>() : all_derivations(e)),
      frozen(e == NULL || is_terminal(e->kind)),
      best_choice(CHOICE_NONE),
      length(e == NULL ? 0 : is_terminal(e->kind) ? 1 : LENGTH_INF) {
    // If this is a non-terminal Edge, it should have at least one Derivation.
    assert(e == NULL || is_terminal(e->kind) || !derivs.empty());
    check_invariants();
}

DerivTable::Record::Record(Record&& other)
    : derivs(std::move(other.derivs)), frozen(other.frozen),
      best_choice(other.best_choice), length(other.length),
      parents(std::move(other.parents)) {}

void DerivTable::Record::freeze() {
    if (!frozen) {
	frozen = true;
	check_invariants();
    }
}

bool DerivTable::Record::is_frozen() const {
    return frozen;
}

bool DerivTable::Record::is_reached() const {
    return VALID_PATH_LENGTH(length);
}

PATH_LENGTH DerivTable::Record::get_length() const {
    assert(VALID_PATH_LENGTH(length));
    return length;
}

const Derivation& DerivTable::Record::best_derivation() const {
    assert(VALID_CHOICE_REF(best_choice));
    return derivs.at(best_choice);
}

// TODO: Depends on the fact that Edges are uniqued.
std::set<Edge*> DerivTable::Record::get_children() const {
    std::set<Edge*> children;
    for (const Derivation& d : derivs) {
	// Will include NULL if this is an empty Derivation, but not if it's
	// single.
	children.insert(d.left_edge);
	if (d.left_edge != NULL && d.right_edge != NULL) {
	    children.insert(d.right_edge);
	}
    }
    return children;
}

std::list<std::pair<CHOICE_REF,Edge*>>
DerivTable::Record::completions(Edge* e) const {
    std::list<std::pair<CHOICE_REF,Edge*>> res;
    for (CHOICE_REF c = 0; c < derivs.size(); c++) {
	const Derivation& d = derivs[c];
	if (e == NULL) {
	    // This could only occur if the parent Edge contains an empty
	    // production, in which case that's the only production that the
	    // NULL Edge will match.
	    if (d.empty()) {
		res.push_back(std::make_pair(c, nullptr));
	    }
	} else if (d.left_edge != NULL && edge_equals(d.left_edge, e)) {
	    // This will add a NULL completion for single productions.
	    // If the Edge could be used for both parts of a Derivation, only
	    // the first will be included.
	    res.push_back(std::make_pair(c, d.right_edge));
	} else if (d.right_edge != NULL && edge_equals(d.right_edge, e)) {
	    res.push_back(std::make_pair(c, d.left_edge));
	}
    }
    return res;
}

// TODO: Assumes we've checked 'choice' for overflow.
bool DerivTable::Record::update(CHOICE_REF choice, PATH_LENGTH length) {
    if (!frozen && !VALID_CHOICE_REF(best_choice)) {
	best_choice = choice;
	this->length = length;
	check_invariants();
	return true;
    }
    assert(length >= this->length);
    return false;
}

bool DerivTable::Record::add_parent(Edge* p) {
    return parents.insert(p).second;
}

const std::set<Edge*>& DerivTable::Record::get_parents() const {
    return parents;
}

DerivTable::QueueItem::QueueItem(Edge* edge, CHOICE_REF choice,
				 PATH_LENGTH length)
    : edge(edge), choice(choice), length(length) {}

bool DerivTable::QueueItem::operator<(const QueueItem& other) const {
    return length > other.length;
}

void DerivTable::fill(Edge* e, Edge* parent, std::set<Edge*>& base_edges) {
    auto iterAndFlag = table.emplace(e, e);
    Record& rec = iterAndFlag.first->second;
    if (parent != NULL) {
	rec.add_parent(parent);
    }
    if (iterAndFlag.second) {
	for (Edge* child : rec.get_children()) {
	    fill(child, e, base_edges);
	}
    }
    // Add e to the base Edges for this round, if it's terminal or was
    // explored on a previous round.
    if (rec.is_frozen()) {
	base_edges.insert(e);
    }
}

void DerivTable::process(const QueueItem& item) {
    PATH_LENGTH length = item.length;
    Edge* e = item.edge;
    Record& rec = table.at(e);

    // Propagate frozen Edges, and newly reached non-terminals (which we've
    // just reached with the shortest possible path).
    if (rec.is_frozen() || rec.update(item.choice, length)) {
	for (Edge* p : rec.get_parents()) {
	    propagate(p, e, length);
	}
    }
}

void DerivTable::propagate(Edge* parent, Edge* child, PATH_LENGTH child_len) {
    Record& p_rec = table.at(parent);
    if (p_rec.is_reached()) {
	// No reason to propagate a derivation to a parent that's already been
	// reached, since the previous time we reached them, the corresponding
	// path would definitely have been no longer than the current one.
	// TODO: Could also check that we don't get a shorter path than the
	// stored one.
	return;
    }

    // First find the shortest path made possible by the newly closed child
    // (there may be multiple derivations that use the same Edge).
    CHOICE_REF best_choice = CHOICE_NONE;
    PATH_LENGTH min_length = LENGTH_INF;

    // TODO: Check that the completion set isn't empty.
    for (const auto& choiceAndEdge : p_rec.completions(child)) {
	CHOICE_REF choice = choiceAndEdge.first;
	Edge* completion = choiceAndEdge.second;
	PATH_LENGTH total_length = child_len;
	if (completion != NULL) {
	    const Record& completion_rec = table.at(completion);
	    // Could get the same Edge in the completion set, in cases like
	    // this: A :: B _B. This is OK, since the child Edge should have
	    // already been closed.
	    if (!completion_rec.is_reached()) {
		continue;
	    }
	    total_length += completion_rec.get_length();
	}
	if (!VALID_CHOICE_REF(best_choice) || total_length < min_length) {
	    best_choice = choice;
	    min_length = total_length;
	}
    }

    if (VALID_CHOICE_REF(best_choice)) {
	// The combining Edge might not have been reached yet.
	queue.emplace(parent, best_choice, min_length);
    }
}

void DerivTable::add(Edge* e) {
    std::set<Edge*> base_edges;
    fill(e, NULL, base_edges);
    for (Edge* b : base_edges) {
	// dummy choice value, won't be used anyway
	queue.emplace(b, CHOICE_NONE, table.at(b).get_length());
    }

    while (!queue.empty()) {
	QueueItem item = queue.top();
	queue.pop();
	process(item);
    }

    for (auto& edgeAndRec : table) {
	edgeAndRec.second.freeze();
    }
}

/* PATH PRINTING =========================================================== */

void print_step_open(Edge* edge, bool reverse, FILE* f) {
    EDGE_KIND kind = edge->kind;
    if (is_terminal(kind)) {
	fprintf(f, "<%s", kind2symbol(kind));
    } else {
	fprintf(f, "<%s symbol='%s'",
		is_temporary(kind) ? "TempStep" : "NTStep", kind2symbol(kind));
    }
    fprintf(f, " reverse='%s' from='%s' to='%s'", reverse ? "true" : "false",
	    node_names.name_of(edge->from).c_str(),
	    node_names.name_of(edge->to).c_str());
    if (is_parametric(kind)) {
	char idx_buf[32];
	// TODO: Check that printing is successful.
	index_print(edge->index, idx_buf, sizeof(idx_buf));
	fprintf(f, " index='%s'", idx_buf);
    }
    if (is_terminal(kind)) {
	// Terminal steps can have no nested sub-steps, so we can close the tag
	// at this point.
	fprintf(f, "/>\n");
    } else {
	fprintf(f, ">\n");
    }
}

void print_step_close(Edge* edge, FILE* f) {
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
// - reorder the choices appropriately (they were recorded in unbaked order).
//   e.g. for D :: _C, C :: A B, A :: a0 | a1, B :: b0 | b1, and input "b1 a0",
//   the choices would be recorded as [0,1], but the real series of choices
//   (the one that respects the reverse directions) would be [1,0].
// - don't print out both endpoints for each step, only the "real target" node
//   (either the edge's target or source, depending on the direction of
//   traversal)
// For now, the client of this output will need to bake the results (or we
// could avoid printing immediatelly, and do this at a post-processing step).
void DerivTable::print_edge(Edge* edge, bool reverse, FILE* f) {
    print_step_open(edge, reverse, f);
    if (!is_terminal(edge->kind)) {
	const Derivation& deriv = table.at(edge).best_derivation();
	if (deriv.left_edge != NULL) {
	    print_edge(deriv.left_edge, deriv.left_reverse, f);
	}
	if (deriv.right_edge != NULL) {
	    print_edge(deriv.right_edge, deriv.right_reverse, f);
	}
    }
    print_step_close(edge, f);
}

void DerivTable::print_path(Edge* top_edge, FILE* f) {
    fprintf(f, "<path>\n");
    print_edge(top_edge, false, f);
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

void print_paths_for_kind(EDGE_KIND k, unsigned int num_paths,
			  DerivTable& table) {
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
#ifdef PATH_RECORDING
	    if (!is_accepting(e->fwd_tfun)) {
		continue;
	    }
#endif
	    const char *src_name = node_names.name_of(e->from).c_str();
	    // TODO: Quote input strings before printing to XML.
	    fprintf(f, "<edge from='%s' to='%s'>\n", src_name, tgt_name);
#ifdef PATH_RECORDING
	    print_prerecorded_path(e, f);
#else
	    table.add(e);
	    table.print_path(e, f);
#endif
	    fprintf(f, "</edge>\n");
	}
    }

    fprintf(f, "</paths>\n");
    fclose(f);
}

void print_paths() {
    // Use a single table for all path printing.
    DerivTable table;
    for (EDGE_KIND k = 0; k < num_kinds(); k++) {
	if (is_predicate(k)) {
	    // TODO: Also check that no edges have been produced.
	    continue;
	}
	unsigned int num_paths = num_paths_to_print(k);
	if (num_paths > 0) {
	    print_paths_for_kind(k, num_paths, table);
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
	execlp("perf", "perf", "record", "-p", pid_arg, "-g", (char *) NULL);
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
int main(int argc, char* argv[]) {
    mem_manager_init();
    LOGGING_INIT();

    if (argc != 3) {
	APP_ERR("Usage: %s <in-dir> <out-dir>", argv[0]);
    }
    INPUT_DIR = argv[1];
    OUTPUT_DIR = argv[2];

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
#ifdef PATH_RECORDING
	Edge *base = worklist.top();
	worklist.pop();
	if (is_terminal(base->kind) || graph_add(base)) {
	    main_loop(base);
	} else {
	    delete base;
	}
#else
	Edge *base = worklist.front();
	worklist.pop();
	main_loop(base);
#endif
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
