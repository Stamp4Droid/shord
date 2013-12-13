#ifndef SOLVERGEN_HPP
#define SOLVERGEN_HPP

#include <deque>
#include <forward_list>
#include <iterator>
#include <limits.h>
#include <list>
#include <map>
#include <queue>
#include <set>
#include <stack>
#include <stdlib.h>
#include <stdio.h>
#include <vector>
#include <unordered_map>
#include <utility>

/**
 * @mainpage
 * This is a framework for building customized CFL-Reachability Solvers. It
 * includes an optimized C implementation of a solver engine and a component
 * for generating custom solver loops for arbitrary Context-Free Grammars.
 *
 * The solver engine and the input/output interface is implemented in engine.c.
 * This file should be linked with a "client" file, which defines the
 * analysis-specific parts (documented in @ref Generated). These are normally
 * generated by the cfg_parser.py script.
 *
 * The steps to use this framework are:
 * - Write a Context-Free Grammar describing the analysis.
 * - Pass the grammar to cfg_parser.py, which will generate the missing
 *   functions (see that script's documentation for details).
 * - Compile and link the engine source and the analysis file.
 * - Prepare the input to the analysis (see parse_input_files() for more).
 * - Run the generated executable.
 * - Read the output (see print_results() for more).
 *
 * See test_analysis.c for an example of what the generated code might look
 * like.
 */

/**
 * @file
 * Header file to be included in any client analysis. Defines the custom types
 * and data structures used by the solver engine, and all the client-visible
 * functions.
 */

#ifndef PATH_RECORDING
// HACK: Evil use of the preprocessor, to discard the extra information when
// path recording is disabled. This covers both function declaration and use.
#define add_edge(a, b, c, d, e, f, g, h) add_edge(a, b, c, d)
#define Edge(a, b, c, d, e, f, g, h) Edge(a, b, c, d)
#endif

/* TYPES & SIZES =========================================================== */

/* TODO: The widths used for the following reference types are arbitrary. We
   should instead set them to the minimum width necessary to hold all the
   values, or at least fail if the default is not wide enough. */
/* Actually, the reference types should be wide enough to hold the count of all
   the corresponding values, i.e. one more than the maximum value. */

/** An integer identifying some @Symbol in the input @Grammar. */
typedef unsigned char EDGE_KIND;

/** An integer used to parameterize Edge%s of the same @Symbol. */
typedef unsigned int INDEX;
/** An INDEX value reserved for representing "none". */
#define INDEX_NONE UINT_MAX
/** Check if an INDEX is within bounds. */
#define VALID_INDEX(i) ((i) < INDEX_NONE)

/** An integer wide enough to uniquely identify any node in the input graph. */
typedef unsigned int NODE_REF;
/** A NODE_REF value reserved for representing "none". */
#define NODE_NONE UINT_MAX
/** Check if a NODE_REF is within bounds. */
#define VALID_NODE_REF(n) ((n) < NODE_NONE)

/** The length of some Edge<!-- -->'s witness path. */
typedef unsigned int PATH_LENGTH;
/** A PATH_LENGTH value reserved for representing "infinity". */
#define LENGTH_INF UINT_MAX
/** Check if a PATH_LENGTH is within bounds. */
#define VALID_PATH_LENGTH(l) ((l) < LENGTH_INF)

/**
 * An integer identifying a selection among multiple possible Derivation%s of
 * some Edge.
 */
typedef unsigned char CHOICE_REF;
/** A CHOICE_REF value reserved for representing "none". */
#define CHOICE_NONE UCHAR_MAX
/** Check if a CHOICE_REF is within bounds. */
#define VALID_CHOICE_REF(c) ((c) < CHOICE_NONE)

/** An integer identifying some @Relation in the input @Grammar. */
typedef unsigned char RELATION_REF;

/** The arity of some @Relation in the input @Grammar. */
typedef unsigned char ARITY;

/**
 * The maximum amount of memory that the solver will allocate on the heap, in
 * bytes. Attempting to allocate memory beyond this limit will result in a
 * program exit.
 */
#define MAX_MEMORY 10737418240 /* 10GB */

/* DATA STRUCTURES ========================================================= */

/**
 * An edge of the input graph.
 */
struct Edge {
    /** This edge's source Node. */
    const NODE_REF from;
    /** This edge's destination Node. */
    const NODE_REF to;
    /**
     * The edge's @Kind, i.e.\ a reference to the grammar @Symbol it
     * represents.
     *
     * An Edge for @Symbol `S` represents an `S`-path from its source Node to
     * its destination Node.
     */
    const EDGE_KIND kind;
#ifdef PATH_RECORDING
    /**
     * Whether the 1st of (up to) two Edge%s that produced this one was
     * traversed in reverse.
     */
    const bool l_rev;
    /**
     * Whether the 2nd of (up to) two Edge%s that produced this one was
     * traversed in reverse.
     */
    const bool r_rev;
    /** The length of the path used to construct this Edge. */
    const PATH_LENGTH length;
#endif
    /**
     * The @Index associated with this Edge. An Edge for @Symbol `S` that
     * contains @Index `4` represents the @Literal `S[4]`.
     *
     * Is equal to @ref INDEX_NONE iff this Edge represents a non-parametric
     * @Symbol.
     */
    const INDEX index;
#ifdef PATH_RECORDING
    /**
     * The 1st of (up to) two Edge%s that produced this one. Is @e NULL if
     * this Edge represents a terminal @Symbol, or it was generated by an empty
     * production.
     */
    Edge* const l_edge;
    /**
     * The 2nd of (up to) two Edge%s that produced this one. Is @e NULL if
     * this Edge represents a terminal @Symbol, or it was generated by an empty
     * or single production.
     */
    Edge* const r_edge;
#endif
public:
    explicit Edge(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index,
		  Edge* l_edge, bool l_rev, Edge* r_edge, bool r_rev);
};

/**
 * The set of all outgoing Edge%s to some Node, for a specific @Symbol.
 * Implemented using a hashtable indexed by target Node.
 */
class OutEdgeSet {
public:
    typedef std::unordered_multimap<NODE_REF,Edge*> Table;
    typedef Table::const_iterator TableIterator;
    class Iterator;
    class View;

    class Iterator : public std::iterator<std::forward_iterator_tag,Edge*> {
	friend View;
    private:
	/**
	 * The OutEdgeSet we are iterating on.
	 */
	OutEdgeSet* const set;
	/**
	 * The specific @Index that this Iterator is constrained on. Is equal
	 * to @e INDEX_NONE if the iterated Edge%s are not parametric, or this
	 * Iterator is unconstrained.
	 */
	const INDEX index;
	/**
	 * Our current position over the actual Edge container in the iterated
	 * set.
	 */
	TableIterator iter;
	/**
	 * An iterator marking the end of our allowed range over the Edge
	 * container of the iterated set.
	 */
	const TableIterator past_last;
    private:
	/**
	 * Advance the wrapped iterator to the next valid Edge. If the wrapped
	 * iterator is already on a valid Edge, it is not moved.
	 */
	void advance_iter();
	/**
	 * Initialize an Iterator over some View of an OutEdgeSet. This
	 * increments the number of live iterators over that set.
	 */
	explicit Iterator(OutEdgeSet *set, INDEX index, TableIterator iter,
			  TableIterator past_last);
    public:
	// TODO: Copy constructor and assignment disabled, to simplify
	// reference counting of live iterators.
	Iterator(const Iterator& other) = delete;
	Iterator& operator=(const Iterator& other) = delete;
	Iterator(Iterator&& other);
	/**
	 * Destroy this Iterator, decrementing the number of Iterator%s
	 * currently live over the iterated set.
	 */
	~Iterator();
	Edge *operator*() const;
	Iterator& operator++();
	bool operator==(const Iterator& other) const;
	bool operator!=(const Iterator& other) const;
    };

    /**
     * An iterable, unmodifiable view over an OutEdgeSet.
     */
    // TODO: A View objet holds live iterators over the underlying table, but
    // doesn't update the set's live_iters.
    class View {
	friend OutEdgeSet;
    private:
	/**
	 * The OutEdgeSet that this View is based on.
	 */
	OutEdgeSet* const set;
	/**
	 * The specific @Index that this View is constrained on. Is equal to
	 * @e INDEX_NONE if the iterated Edge%s are not parametric, or this
	 * View is unconstrained.
	 */
	const INDEX index;
	/**
	 * An iterator over the underlying container of the parent OutEdgeSet,
	 * pointing to the first element contained in this View.
	 */
	const TableIterator first;
	/**
	 * An iterator over the underlying container of the parent OutEdgeSet,
	 * pointing one element past the last one contained in this View.
	 */
	const TableIterator past_last;
    private:
	/**
	 * Create a View over OutEdgeSet @a set, according to the specified
	 * constraints.
	 *
	 * @param [in] to The destination Node that this View will be
	 * constrained to, ignored if set to @e NODE_NONE.
	 * @param [in] index The @Index that this View will be constrained to,
	 * ignored if set to @e INDEX_NONE.
	 */
	explicit View(OutEdgeSet *set, NODE_REF to, INDEX index);
    public:
	/**
	 * Return an Iterator positioned at the first element contained in this
	 * View.
	 */
	Iterator begin() const;
	/**
	 * Return an Iterator positioned after the last element contained in
	 * this View.
	 */
	Iterator end() const;
    };

private:
    friend Iterator;
private:
    /**
     * The actual container of outgoing Edge%s, a hashtable-backed multimap,
     * indexing Edge%s by destination Node.
     */
    Table table;
    /**
     * The number of Iterator%s that are currently live on this OutEdgeSet. The
     * set cannot be modified while there exists a live Iterator over it.
     */
    unsigned int live_iters;
public:
    /**
     * Create an empty OutEdgeSet, with an initial capacity of 0.
     */
    OutEdgeSet();
    /**
     * Add an Edge to this OutEdgeSet. Assumes the Edge is not already present
     * in the set, and that there are no live Iterator%s over the set.
     */
    void add(Edge *e);
    /**
     * Create a View over this OutEdgeSet, with the specified constraints.
     */
    View view(NODE_REF to, INDEX index);
};

/**
 * The set of all incoming Edge%s to some Node, for a specific @Symbol.
 * Implemented using an unsorted container.
 */
// TODO: Rewrite in a similar style to OutEdgeSet.
class InEdgeSet {
public:
    class Iterator;
    class View;

    class Iterator : public std::iterator<std::forward_iterator_tag,Edge*> {
	friend View;
    private:
	/**
	 * The InEdgeSet we are iterating on.
	 */
	InEdgeSet* const set;
	/**
	 * The specific @Index that this Iterator is constrained on. Is equal
	 * to @e INDEX_NONE if the iterated Edge%s are not parametric, or this
	 * Iterator is unconstrained.
	 */
	const INDEX index;
	/**
	 * Our current position over the actual Edge container in the iterated
	 * set.
	 */
	std::deque<Edge*>::const_iterator iter;
    private:
	/**
	 * Advance the wrapped iterator to the next valid Edge. If the wrapped
	 * iterator is already on a valid Edge, it is not moved.
	 */
	void advance_iter();
	/**
	 * Initialize an Iterator over some View of an InEdgeSet. This
	 * increments the number of live iterators over that set.
	 */
	explicit Iterator(InEdgeSet *set, INDEX index, bool at_end);
    public:
	// TODO: Copy constructor and assignment disabled, to simplify
	// reference counting of live iterators.
	Iterator(const Iterator& other) = delete;
	Iterator& operator=(const Iterator& other) = delete;
	Iterator(Iterator&& other);
	/**
	 * Destroy this Iterator, decrementing the number of Iterator%s
	 * currently live over the iterated set.
	 */
	~Iterator();
	Edge *operator*() const;
	Iterator& operator++();
	bool operator==(const Iterator& other) const;
	bool operator!=(const Iterator& other) const;
    };

    /**
     * An iterable, unmodifiable view of an InEdgeSet.
     */
    class View {
	friend InEdgeSet;
    private:
	/**
	 * The InEdgeSet that this View is based on.
	 */
	InEdgeSet* const set;
	/**
	 * The specific @Index that this View is constrained on. Is equal to
	 * @e INDEX_NONE if the iterated Edge%s are not parametric, or this
	 * View is unconstrained.
	 */
	const INDEX index;
    private:
	explicit View(InEdgeSet *set, INDEX index);
    public:
	/**
	 * Return an Iterator positioned at the first element contained in this
	 * View.
	 */
	Iterator begin() const;
	/**
	 * Return an Iterator positioned after the last element contained in
	 * this View.
	 */
	Iterator end() const;
    };

private:
    friend Iterator;
private:
    /**
     * The actual container of incoming Edge%s.
     */
    std::deque<Edge*> edges;
    /**
     * The number of Iterator%s that are currently live on this InEdgeSet. An
     * InEdgeSet cannot be modified while there exists a live iterator over it.
     */
    unsigned int live_iters;
public:
    /**
     * Create an empty InEdgeSet.
     */
    InEdgeSet();
    /**
     * Add an Edge to this InEdgeSet. Assumes the Edge is not already present
     * in the set, and that there are no live Iterator%s over the set.
     */
    void add(Edge *e);
    /**
     * Create a view over this InEdgeSet, with the specified constraints.
     */
    View view(INDEX index);
};

/**
 * An index over a @Relation. Provides efficient access to the subset of tuples
 * that have specific values on all but one columns. Each RelationIndex
 * corresponds to a specific choice of columns.
 *
 * Currently only supports @Relation%s of arity `3`.
 */
class RelationIndex {
public:
    /** A selection over all but one of a @Relation<!-- -->'s columns. */
    typedef std::pair<INDEX,INDEX> Selection;
private:
    /**
     * A dummy empty @Index set. Used in cases where a queried key is not
     * present in the RelationIndex, to provide an empty iterator without
     * polluting the map with useless default-initialized records.
     */
    static const std::set<INDEX> EMPTY_INDEX_SET;
    /** The actual index. */
    // TODO: Not trivial to use unordered_map: no hash function is declared by
    // default for pairs/tuples.
    std::map<Selection, std::set<INDEX>> map;
public:
    /**
     * Record a tuple of the @Relation being indexed. The tuple is passed in
     * as a combination of a selection on all other columns, plus the value
     * that the remaining, targeted column takes.
     */
    void add(const Selection &sel, INDEX val);
    /**
     * Return all values that the targeted column takes for the selected values
     * on all other columns. In relational algebra terms, this corresponds to a
     * selection on all other columns, followed by a projection on the targeted
     * column.
     */
    const std::set<INDEX>& get(const Selection &sel);
};

/**
 * A lightweight map data structure, implemented as an unsorted list of
 * key-value pairs.
 */
template<typename K, typename V> class LightMap {
private:
    typedef std::forward_list<std::pair<K,V>> List;
public:
    typedef typename List::size_type Size;
    // TODO: Should also provide constant iterator type.
    typedef typename List::iterator Iterator;
private:
    /**
     * A dummy default-constructed value element, returned by ::dummy_get(K k)
     * when the requested key is not present in the LightMap. This element will
     * be shared among all such requests, and should thus not be modified.
     *
     * We use this on a higher layer, to provide a zero-length Edge iterator
     * without breaking the View interface.
     */
    static V dummy;
    /** The size of the wrapped list. */
    Size list_size;
    /** The actual contents of the LightMap. */
    List list;
private:
    /**
     * Locate the first element with a given key. The returned iterator will be
     * at one-past-the-end if the key is not present in the LightMap.
     */
    typename List::iterator find(K k);
public:
    /** Create an empty LightMap. */
    LightMap();
    void swap(LightMap& other);
    /**
     * Retrieve the value mapped to key @a k. If @a k is not present in the
     * LightMap, a dummy default-constructed element is returned, which should
     * not be modified.
     */
    // TODO: Immutability requirement on returned reference is not enforced.
    V& const_get(K k);
    /**
     * Retrieve the value mapped to key @a k. If @a k is not present in the
     * LightMap, it get inserted under a newly default-constructed value.
     */
    V& operator[](K k);
    /** Retrieve the value mapped to key @a k. Fail if @a k is not present. */
    V& at(K k);
    Size size() const;
    // Iterator over K-V pairs.
    Iterator begin();
    Iterator end();
};

/**
 * A node of the input graph.
 */
struct Node {
    /**
     * All the incoming Edge%s of the Node, indexed by @Kind. Stored as a
     * collection of InEdgeSet%s, with a separate InEdgeSet for each @Kind.
     */
    LightMap<EDGE_KIND,InEdgeSet> in;
    /**
     * All the outgoing Edge%s of the Node, indexed by @Kind and target Node.
     * Stored as a collection of OutEdgeSet%s, with a separate OutEdgeSet for
     * each @Kind. Edge%s on the same OutEdgeSet are indexed by target Node.
     */
    LightMap<EDGE_KIND,OutEdgeSet> out;
};

/** Structure describing a possible way to produce an Edge. */
struct Derivation {
public:
    Edge* const left_edge;
    Edge* const right_edge;
    const bool left_reverse;
    const bool right_reverse;
public:
    /**
     * @addtogroup PublicAPI
     * @{
     */
    Derivation();
    Derivation(Edge *e, bool reverse);
    Derivation(Edge *left_edge, bool left_reverse,
	       Edge *right_edge, bool right_reverse);
    /**
     * @}
     */
    bool empty() const;
};

// TODO:
// - when closing a non-terminal edge, can close all unsuccessful derivations
//   recurse down all other derivations, remove self from parent list,
//   close/freeze any that have no parents, repeat
//   but then would be less efficient/harder to reuse for other top edges (?)
// - could have better child information on parents
//   so we don't have to iterate on their list
// - can add the no-recursion constraint
class DerivTable {
private:

    class Record {
    private:
	const std::vector<Derivation> derivs;
	/**
	 * Whether all derivation choices for this Edge have been explored.
	 * Terminal Edge%s and the special @e NULL Record start out frozen,
	 * non-terminals are frozen after a round of propagation completes.
	 */
	bool frozen;
	/**
	 * If this is a non-terminal Edge, the derivation that generates the
	 * shortest path for it. Equal to @e CHOICE_NONE for terminals, the
	 * special @e NULL Record, and for non-terminal Edge%s not yet reached.
	 */
	CHOICE_REF best_choice;
	/**
	 * The length of the shortest path found for this Edge. Equal to
	 * @e LENGTH_INF for non-terminal Edge%s not yet reached.
	 */
	PATH_LENGTH length;
	std::set<Edge*> parents;
    private:
	void check_invariants();
    public:
	Record(Edge* e);
	Record(const Record& other) = delete;
	Record& operator=(const Record& other) = delete;
	// TODO: Shouldn't have a move constructor if I'm ever going to keep
	// references to Records.
	Record(Record&& other);
	void freeze();
	bool is_frozen() const;
	bool is_reached() const;
	PATH_LENGTH get_length() const;
	const Derivation& best_derivation() const;
	// Will contain NULL if this Edge contains an empty Derivation.
	std::set<Edge*> get_children() const;
	/**
	 * Calculate the set of Edge%s that can be combined with @a e to
	 * complete a Derivation for this Edge. Can contain @e NULL, for single
	 * and empty Production%s.
	 */
	std::list<std::pair<CHOICE_REF,Edge*>> completions(Edge* e) const;
	/**
	 * Returns true iff this was the first (and best) arriving Derivation,
	 * and caused the Record to be updated.
	 */
	// We trust the information that gets passed in. Should never get two
	// updates for the same choice. The best choice should arrive first.
	bool update(CHOICE_REF choice, PATH_LENGTH length);
	/** Returns @e true iff @a parent was not already present. */
	bool add_parent(Edge* parent);
	const std::set<Edge*>& get_parents() const;
    };

    // TODO: We need to explicitly store the length under which an Edge is
    // added. If we simply stored Edge*'s, we would run the risk of elements in
    // the priority_queue changing in value.
    // TODO: Could perhaps get away with this, because the length of an Edge
    // can only decrease.
    // TODO: Fields should be const, but then need to write custom copy
    // assignment.
    struct QueueItem {
	Edge* edge;
	CHOICE_REF choice;
	PATH_LENGTH length;
	QueueItem(Edge* edge, CHOICE_REF choice, PATH_LENGTH length);
	// To achieve proper ordering, we consider an Edge with a shorter path
	// to be 'greater'.
	bool operator<(const QueueItem& other) const;
    };

private:
    std::map<Edge*,Record> table;
    std::priority_queue<QueueItem> queue;
private:
    void fill(Edge* e, Edge* parent, std::set<Edge*>& base_edges);
    void process(const QueueItem& item);
    void propagate(Edge* parent, Edge* child, PATH_LENGTH child_len);
    void print_edge(Edge* edge, bool reverse, FILE* f);
public:
    // Adds Edge and all induced Derivations.
    void add(Edge* e);
    void print_path(Edge* top_edge, FILE* f);
};

/**
 * A mapping between Node%s and their names. This consists of two mappings, one
 * from Node%s to names (strings), and one from names to Node%s.
 *
 * We operate on this structure in two phases. In the insertion phase, Node
 * names are recorded and assigned NODE_REF%s sequentially (a single index is
 * assigned for each unique name). After finalization, no more insertions are
 * allowed, and the client code is allowed to perform look-ups in any
 * direction.
 */
class NodeNameMap {
private:
    /** Whether this structure has been finalized yet. */
    bool is_final;
    /** A vector of Node names, indexed by their NODE_REF%s. */
    std::vector<std::string> vector;
    /** The mapping from names back to Node%s. */
    std::map<std::string, NODE_REF> map;
public:
    NodeNameMap();
    /**
     * Record a Node of the specified name. Assigns a unique identifier to each
     * distinct Node name.
     */
    void add(const char *name);
    /**
     * Signal the end of Node additions.
     */
    void finalize();
    /**
     * Return the total number of Node%s recorded. Can only be called after the
     * container has been finalized.
     */
    NODE_REF size() const;
    /**
     * Get the name of the specified Node. Can only be called after the
     * container has been finalized.
     */
    const std::string& name_of(NODE_REF node) const;
    /**
     * Get the Node for the specified name. Can only be called after the
     * container has been finalized. Expects that the requested name has
     * previously been recorded.
     */
    NODE_REF node_for(const char *name) const;
};

/* LOGGING FACILITIES ====================================================== */

/**
 * @defgroup Logging Logging Facilities
 * Components used for collecting information during a solver run. This
 * includes time keeping, @Counter variables, final stats printing, and custom
 * logging functionality.
 *
 * Anything in this part of the API (except error output) will be compiled
 * away, and thus cause no overhead, if @e LOGGING is not defined at
 * compilation time. Therefore, you shouldn't perform any important computation
 * within logging code, or refer to @Counter%s outside the logging API.
 * @{
 */

/** The severity level of a log message. */
enum class Severity {ERROR, WARNING, INFO};

/**
 * Report an app-specific error, then exit. Accepts a variable number of
 * string-convertible values.
 */
#define APP_ERR(...) report(Severity::ERROR, false, __VA_ARGS__)
/**
 * Report an app-specific error without exiting. Accepts a variable number of
 * string-convertible values.
 */
#define APP_WARN(...) report(Severity::WARNING, false, __VA_ARGS__)
/**
 * Report a system error, then exit. Accepts a variable number of
 * string-convertible values.
 */
#define SYS_ERR(...) report(Severity::ERROR, true, __VA_ARGS__)
/**
 * Report a system error without exiting. Accepts a variable number of
 * string-convertible values.
 */
#define SYS_WARN(...) report(Severity::WARNING, true, __VA_ARGS__)

/**
 * A numeric counter, used for keeping statistics during a solver run.
 */
typedef unsigned long COUNTER;

#ifdef LOGGING

/**
 * Initialize the logging system. This function should be called prior to any
 * use of logging functionality.
 */
#define LOGGING_INIT() logging_init()

/**
 * Log a message to standard output. Accepts a variable number of
 * string-convertible values. Values of @Counter%s are accessible inside
 * invocations of this macro.
 *
 * Anything inside this macro will be compiled away if logging is disabled, so
 * you shouldn't pass in any side-effecting expressions.
 */
#define LOG(...) report(Severity::INFO, false, __VA_ARGS__)

/**
 * Declare a local @Counter-type variable, @a NAME, in the current block and
 * initialize it to @a INIT_VAL.
 */
#define DECL_COUNTER(NAME, INIT_VAL) COUNTER NAME = INIT_VAL

/**
 * Increase the value of locally visible @Counter @a NAME by @a COUNT.
 */
#define INC_COUNTER(NAME, COUNT) do {(NAME) += (COUNT);} while(0)

/**
 * Return a @Counter-type value containing the current system time in
 * milliseconds.
 */
#define CURRENT_TIME() current_time()

/**
 * Print out aggregate statistics about the current state of the graph.
 */
#define PRINT_STATS() do {print_stats();} while(0)

#else /* LOGGING */

#define LOGGING_INIT()
#define LOG(...)
#define DECL_COUNTER(NAME, INIT_VAL)
#define INC_COUNTER(NAME, COUNT)
#define CURRENT_TIME() (0)
#define PRINT_STATS()

#endif /* LOGGING */

/**
 * @}
 */

/* PUBLIC SOLVER API ======================================================= */

/**
 * @defgroup PublicAPI Public CFL Solver API
 * Grammar-generated code should only use these functions.
 * @{
 */

/**
 * Add a new Edge to the ::worklist for future processing.
 */
void add_edge(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index,
	      Edge* l_edge, bool l_rev, Edge* r_edge, bool r_rev);

/**
 * Select from the set of incoming Edge%s to a target Node @a to, those of a
 * specific @Symbol and @Index.
 *
 * @param [in] index The index to look for; ignored if set to @e INDEX_NONE.
 */
InEdgeSet::View edges_to(NODE_REF to, EDGE_KIND kind,
			 INDEX index = INDEX_NONE);

/**
 * Select from the set of outgoing Edge%s from a source Node @a from, those of
 * a specific @Symbol and @Index.
 *
 * @param [in] index The index to look for; ignored if set to @e INDEX_NONE.
 */
OutEdgeSet::View edges_from(NODE_REF from, EDGE_KIND kind,
			    INDEX index = INDEX_NONE);

/**
 * Return the set of Edge%s of a specific @Symbol that lie between two
 * specified endpoints.
 */
OutEdgeSet::View edges_between(NODE_REF from, NODE_REF to, EDGE_KIND kind);

/**
 * Search the graph for an Edge.
 *
 * @return The single Edge identified by the provided features, if there exists
 * one, otherwise @e NULL.
 */
Edge *find_edge(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index);

/**
 * Peform a selection on all the columns of a @Relation except @a proj_col, and
 * return all values appearing in that column. The values used in the selection
 * must be specified in column order.
 */
const std::set<INDEX>& rel_select(RELATION_REF ref, ARITY proj_col,
				  INDEX val_a, INDEX val_b);

/**
 * @}
 */

/* GENERATED CODE ========================================================== */

/**
 * @defgroup Generated Grammar-Generated Code
 * This code is normally generated according to a Context-Free Grammar provided
 * by the user. See cfg_parser.py for more details.
 * @{
 */

/**
 * Return @e true iff @a kind represents a terminal @Symbol in the input
 * grammar.
 */
bool is_terminal(EDGE_KIND kind);

/**
 * Return @e true iff @a kind represents a parametric @Symbol in the input
 * grammar.
 */
bool is_parametric(EDGE_KIND kind);

/**
 * Return @e true only for those @Symbol%s that contain an empty production in
 * the input grammar.
 */
bool has_empty_prod(EDGE_KIND kind);

/**
 * The body of the main solver loop; specifies how each Edge in the ::worklist
 * should be processed.
 */
void main_loop(Edge* base);

/**
 * Return the total number of Edge @Kind%s, i.e.\ the number of @Symbol%s in
 * the input grammar.
 */
EDGE_KIND num_kinds();

/** Get the Edge @Kind associated with a @Symbol of the input grammar. */
EDGE_KIND symbol2kind(const char* symbol);

/** Get the grammar @Symbol corresponding to some Edge @Kind. */
const char* kind2symbol(EDGE_KIND kind);

/** Return all possible ways that an Edge could have been produced. */
std::vector<Derivation> all_derivations(Edge* edge);

/**
 * Return the number of paths to print for each Edge of @Kind @a kind in the
 * final graph.
 */
unsigned int num_paths_to_print(EDGE_KIND kind);

/**
 * Return an over-approximation for the minimum length of any witness path for
 * an Edge of @Kind @a kind.
 */
PATH_LENGTH static_min_length(EDGE_KIND kind);

/**
 * Return whether a @Symbol is used as a predicate on a @Production.
 */
bool is_predicate(EDGE_KIND kind);

/**
 * Check whether a particular @Symbol was introduced by the parser, and was not
 * present in the original grammar.
 */
bool is_temporary(EDGE_KIND kind);

/**
 * Check whether some Node is reachable from another through a path of a
 * specific @Symbol and @Index. Only appropriate for @Symbol%s used as
 * predicates.
 */
bool reachable(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index);

/** Return the total number of @Relation%s declared in the input grammar. */
RELATION_REF num_rels();

/** Get the reference number associated with a @Relation. */
RELATION_REF rel2ref(const char *rel);

/** Return the name of the @Relation with reference number @a ref. */
const char *ref2rel(RELATION_REF ref);

/** Return the arity of the @Relation with reference number @a ref. */
ARITY rel_arity(RELATION_REF ref);

/**
 * @}
 */

#endif
