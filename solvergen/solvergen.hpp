#ifndef SOLVERGEN_HPP
#define SOLVERGEN_HPP

#include <limits.h>
#include <list>
#include <map>
#include <set>
#include <stdbool.h>
#include <stdlib.h>
#include <stdio.h>
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
#define edge_new(a, b, c, d, e, f, g, h) edge_new(a, b, c, d)
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

/** An integer wide enough to uniquely identify any node in the input graph. */
typedef unsigned int NODE_REF;
/** A NODE_REF value reserved for representing "none". */
#define NODE_NONE UINT_MAX

/** The length of some Edge<!-- -->'s witness path. */
typedef unsigned int PATH_LENGTH;

/**
 * An integer identifying a selection among multiple possible Derivation%s of
 * some Edge.
 */
typedef unsigned char CHOICE;

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
typedef struct Edge {
    /** This edge's source Node. */
    NODE_REF from;
    /** This edge's destination Node. */
    NODE_REF to;
    /**
     * The edge's @Kind, i.e.\ a reference to the grammar @Symbol it
     * represents.
     *
     * An Edge for @Symbol `S` represents an `S`-path from its source Node to
     * its destination Node.
     */
    EDGE_KIND kind;
#ifdef PATH_RECORDING
    /**
     * Whether the 1st of (up to) two Edge%s that produced this one was
     * traversed in reverse.
     */
    bool l_rev;
    /**
     * Whether the 2nd of (up to) two Edge%s that produced this one was
     * traversed in reverse.
     */
    bool r_rev;
#endif
    /**
     * The @Index associated with this Edge. An Edge for @Symbol `S` that
     * contains @Index `4` represents the @Literal `S[4]`.
     *
     * Is equal to @ref INDEX_NONE iff this Edge represents a non-parametric
     * @Symbol.
     */
    INDEX index;
#ifdef PATH_RECORDING
    /**
     * The 1st of (up to) two Edge%s that produced this one. Is @e NULL if
     * this Edge represents a terminal @Symbol, or it was generated by an empty
     * production.
     */
    struct Edge *l_edge;
    /**
     * The 2nd of (up to) two Edge%s that produced this one. Is @e NULL if
     * this Edge represents a terminal @Symbol, or it was generated by an empty
     * or single production.
     */
    struct Edge *r_edge;
#endif
    /**
     * The next Edge in the incoming Edge%s linked list that this Edge belongs
     * to.
     */
    struct Edge *in_next;
    /**
     * The next Edge in the outgoing Edge%s bucket that this Edge belongs to.
     */
    struct Edge *out_next;
    /**
     * A pointer to the next Edge in the ::worklist. Should not be used
     * directly by the client code.
     *
     * Is `NULL` in two cases: If this Edge is the last one in the ::worklist,
     * or if it's not in the ::worklist at all.
     */
    struct Edge *worklist_next;
} Edge;

/**
 * The initial number of buckets for new OutEdgeSet%s. Must be a power of `2`.
 */
#define OUT_EDGE_SET_INIT_NUM_BUCKETS 16

/**
 * The load factor that, when exceeded, will trigger a resize of an OutEdgeSet.
 */
#define OUT_EDGE_SET_MAX_LOAD_FACTOR 0.9

/**
 * A hashtable-backed set of Edge%s, indexed by target Node.
 */
typedef struct {
    /**
     * The number of Edge%s currenty present in the OutEdgeSet.
     */
    unsigned int size;
    /**
     * The number of buckets in this set's OutEdgeSet::table.
     */
    unsigned int num_buckets;
    /**
     * The number of OutEdgeIterator%s that are currently live on this
     * OutEdgeSet.
     *
     * An OutEdgeSet cannot be modified while there exists a live iterator over
     * it; the iterator must first be advanced to the end of the set.
     */
    unsigned int live_iters;
    /**
     * The actual collection of Edge%s in this OutEdgeSet. Implemented as a
     * hashtable, with unordered linked lists for collision handling.
     */
    Edge **table;
} OutEdgeSet;

/**
 * An iterator over the set of outgoing Edge%s of a Node, constructed on
 * demand.
 */
typedef struct {
    /**
     * The last Edge returned by this LazyOutEdgeIterator. Will get deallocated
     * on the next access.
     */
    Edge *last;
    /**
     * The Edge%s left to return.
     */
    std::list<Edge *> *edges;
} LazyOutEdgeIterator;

/**
 * An iterator over the set of outgoing Edge%s of a Node.
 */
typedef struct {
    /**
     * The OutEdgeSet that we are iterating on.
     */
    OutEdgeSet *set;
    /**
     * The bucket of OutEdgeIterator::set that this OutEdgeIterator is
     * currently on.
     */
    unsigned int bucket;
    /**
     * When set to a value different from @e NODE_NONE, the iterator will only
     * return Edge%s targeting that Node.
     */
    NODE_REF tgt_node;
    /**
     * The Edge in the overflow list of OutEdgeIterator::set that we are
     * currently on.
     */
    Edge *list_ptr;
} OutEdgeIterator;

/**
 * An iterator over the set of incoming Edge%s of a Node.
 *
 * Incoming Edge%s are stored in linked lists, so we only need to record our
 * position in the list.
 */
typedef struct {
    /**
     * The Edge that this InEdgeIterator is currenly on.
     */
    Edge *curr_edge;
} InEdgeIterator;

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
    // TODO: Not trivial to use unordered_map: no hash function is declared by
    // default for pairs/tuples.
    std::map<Selection, std::set<INDEX>> index;

public:
    /**
     * Record a tuple of the @Relation being indexed. The tuple is passed in
     * as a combination of a selection on all other columns, plus the value
     * that the remaining, targeted column takes.
     */
    void add(const Selection &sel, INDEX val) {
	index[sel].insert(val);
    }

    /**
     * Return all values that the targeted column takes for the selected values
     * on all other columns. In relational algebra terms, this corresponds to a
     * selection on all other columns, followed by a projection on the targeted
     * column.
     */
    const std::set<INDEX>& get(const Selection &sel) {
	return index[sel];
    }
};

/**
 * A node of the input graph.
 */
typedef struct {
    /**
     * All the incoming Edge%s of the Node, indexed by @Kind.
     *
     * Stored as an array of linked lists of Edge%s, with a separate list for
     * each @Kind.
     */
    Edge **in;
    /**
     * All the outgoing Edge%s of the Node, indexed by @Kind and target Node.
     *
     * Stored as an array of hashtable-backed sets of Edge%s, with a separate
     * OutEdgeSet for each @Kind. Edge%s on the same OutEdgeSet are indexed by
     * target Node.
     *
     * We initialize the sets lazily to conserve space.
     */
    OutEdgeSet **out;
} Node;

/**
 * A worklist of graph Edge%s, implemented as a linked list. The Edge%s in the
 * worklist are connected through the Edge::worklist_next pointer.
 */
typedef struct {
    Edge *head;
    Edge *tail;
} EdgeWorklist;

/**
 * Structure describing a possible way to produce an Edge.
 */
typedef struct {
    Edge *left_edge;
    Edge *right_edge;
    bool left_reverse;
    bool right_reverse;
} Derivation;

// Empty derivations have no Step associated with them: we must take special
// care of them.
// TODO: No support for freeing allocated memory. Should throw away subtrees
// when we're done with them (e.g. through ref-counting).
typedef struct Step {
    bool is_reverse; // relative to the original Edge
    bool is_expanded;
    CHOICE num_choices;
    Edge *edge;
    struct Step *next_sibling; // next in the step sequence (level on the tree)
    struct Step *parent;
    // array of possible sub-step sequences
    // - always NULL for terminals
    //   TODO: check this
    // - NULL or non-NULL for non-terminals, depending on whether they have any
    //   possible expansions
    struct Step **sub_step_seqs;
} Step;

// TODO: No memory deallocation support
typedef struct ChoiceSequence {
    CHOICE last;
    struct ChoiceSequence *prev;
} ChoiceSequence;

typedef struct {
    PATH_LENGTH min_length; // includes all expanded terminals
    ChoiceSequence *choices;
    // caches the node on the tree that we'll continue from
    // this is not necessary: we can derive it by following the choices
    // can never point to an empty derivation: we must skip over those
    Step *curr_step;
} PartialPath;

/**
 * An element in a linked list of strings.
 */
typedef struct StringCell {
    const char *value;
    struct StringCell *next;
} StringCell;

/**
 * A FIFO queue of strings, implemented as a linked list.
 */
typedef struct {
    unsigned long length;
    StringCell *head;
    StringCell *tail;
} StringQueue;

/**
 * An element of a trie mapping Node names (strings) to Node%s.
 */
typedef struct NodeNameTrieCell {
    /** The character at this point in the trie. */
    char character;
    /**
     * The Node corresponding to the name constructed by traversing the trie up
     * to this point. Is equal to @e NODE_NONE if there is no Node with that
     * name.
     */
    NODE_REF node;
    /**
     * The first cell on the next level of the trie (i.e., the level for the
     * next character of the string).
     */
    struct NodeNameTrieCell *first_child;
    /**
     * The next cell on the same level of the trie. Cells on the same level are
     * sorted lexicographically.
     */
    struct NodeNameTrieCell *next_sibling;
} NodeNameTrieCell;

/**
 * A trie mapping Node names (strings) to Node%s. This struct serves as the
 * root of the trie, which maps the empty string.
 *
 * Our trie implementation does not explicitly store the null-terminating byte
 * of strings.
 */
typedef struct {
    /**
     * The Node corresponding to the empty string. Is equal to @e NODE_NONE if
     * there is no Node with that name.
     */
    NODE_REF empty_name_node;
    /**
     * The first cell on the first level of the trie (i.e., the level for the
     * first character of the string).
     */
    NodeNameTrieCell *first_child;
} NodeNameTrie;

/**
 * A mapping between Node%s and their names. This consists of two mappings, one
 * from Node%s to names (strings), implemented as a string array indexed by
 * NODE_REF%s, and one from names to Node%s, implemented as a trie.
 *
 * We operate on this structure in two phases. In the insertion phase, Node
 * names are recorded and assigned NODE_REF%s sequentially (a single index is
 * assigned for each unique name). After finalization, no more insertions are
 * allowed, and the data structures are optimized for fast lookups.
  */
typedef struct {
    /** Whether this structure has been finalized yet. */
    bool is_final;
    /** The number of nodes added thus far. */
    NODE_REF num_nodes;
    union NODE_TO_NAME_MAPPING {
	/**
	 * A FIFO queue of Node names, used during the insertion phase, to
	 * allow easy addition of new names.
	 */
	StringQueue *queue;
	/**
	 * An array of Node names indexed by their NODE_REF%s. Constructed
	 * from NodeNameMap::NODE_TO_NAME_MAPPING::queue, after we are done
	 * adding Node%s.
	 */
	const char **array;
    } names; /**< The mapping from Node%s to their names. */
    /** The mapping from names back to Node%s. */
    NodeNameTrie *trie;
} NodeNameMap;

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

/** Report an app-specific error with a formatted message, then exit. */
#define APP_ERR(...) report_error(true, false, __VA_ARGS__)
/** Report an app-specific error with a formatted message. */
#define APP_WARN(...) report_error(false, false, __VA_ARGS__)
/** Report a system error with a formatted message, then exit. */
#define SYS_ERR(...) report_error(true, true, __VA_ARGS__)
/** Report a system error with a formatted message. */
#define SYS_WARN(...) report_error(false, true, __VA_ARGS__)

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
 * Log a message to @e stdout. The parameters of this macro behave like the
 * parameters of printf(). Values of @Counter%s are accessible inside
 * invocations of this macro.
 *
 * Anything inside this macro will be compiled away if logging is disabled, so
 * you shouldn't pass in any side-effecting expressions.
 */
#define LOG(...) printf(__VA_ARGS__)

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
 * Allocate and return a pointer to @a num objects of @a type. Terminate
 * execution if the allocation fails.
 */
#define STRICT_ALLOC(num, type) ((type*) strict_alloc((num) * sizeof(type)))

Edge *edge_new(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index,
	       Edge *l_edge, bool l_rev, Edge *r_edge, bool r_rev);
void add_edge(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index,
	      Edge *l_edge, bool l_rev, Edge *r_edge, bool r_rev);

OutEdgeIterator *get_out_edge_iterator(NODE_REF from, EDGE_KIND kind);
OutEdgeIterator *get_out_edge_iterator_to_target(NODE_REF from, NODE_REF to,
						 EDGE_KIND kind);
Edge *next_out_edge(OutEdgeIterator *iter);
LazyOutEdgeIterator *get_lazy_out_edge_iterator_to_target(NODE_REF from,
							  NODE_REF to,
							  EDGE_KIND kind);
Edge *next_lazy_out_edge(LazyOutEdgeIterator *iter);
InEdgeIterator *get_in_edge_iterator(NODE_REF to, EDGE_KIND kind);
Edge *next_in_edge(InEdgeIterator *iter);

const std::set<INDEX>& rel_select(RELATION_REF ref, ARITY proj_col,
				  INDEX val_a, INDEX val_b);

Derivation derivation_empty();
Derivation derivation_single(Edge *e, bool reverse);
Derivation derivation_double(Edge *left_edge, bool left_reverse,
			     Edge *right_edge, bool right_reverse);

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
void main_loop(Edge *base);

/**
 * Return the total number of Edge @Kind%s, i.e.\ the number of @Symbol%s in
 * the input grammar.
 */
EDGE_KIND num_kinds();

/** Get the Edge @Kind associated with a @Symbol of the input grammar. */
EDGE_KIND symbol2kind(const char *symbol);

/** Get the grammar @Symbol corresponding to some Edge @Kind. */
const char *kind2symbol(EDGE_KIND kind);

/** Return all possible ways that an Edge could have been produced. */
std::list<Derivation> all_derivations(Edge *edge);

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
 * Return whether Edge%s of a particular @Symbol have been specified as being
 * produced on demand.
 */
bool is_lazy(EDGE_KIND kind);

/**
 * Generate on-the-fly all Edge%s with the given features. Only appropriate for
 * Edge%s of lazily-generated @Symbol%s.
 */
std::list<Edge *> *all_lazy_edges(NODE_REF from, NODE_REF to, EDGE_KIND kind);

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
