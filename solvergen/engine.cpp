#include <assert.h>
#include <list>
#include <errno.h>
#include <locale.h>
#include <queue>
#include <signal.h>
#include <stack>
#include <stdbool.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <vector>

#include "solvergen.hpp"

/**
 * @file
 * Implementation of the CFL-Reachability Solver Engine.
 */

/* GLOBAL VARIABLES ======================================================== */

/**
 * An array containing all the Node%s of the input graph.
 */
Node *nodes;

/**
 * A mapping between Node%s and their names.
 */
NodeNameMap node_names;

/**
 * The Edge worklist used by the fixpoint calculation.
 */
EdgeWorklist worklist;

/**
 * A global counter for the number of iterations the solver loop has executed
 * so far.
 */
DECL_COUNTER(iteration, 0);

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

/**
 * Update the NODE_REF that @a ref_ptr points to, but only if its current value
 * is @e NODE_NONE.
 */
void node_ref_set_if_none(NODE_REF *ref_ptr, NODE_REF new_value) {
    if (*ref_ptr == NODE_NONE) {
	*ref_ptr = new_value;
    }
}

/* WORKLIST HANDLING ======================================================= */

/**
 * Initialize an empty ::worklist.
 */
void worklist_init() {
    worklist.head = NULL;
    worklist.tail = NULL;
}

/**
 * Append an Edge to the end of the ::worklist.
 */
void worklist_insert(Edge *e) {
    /* Check that the edge is not already in the worklist. */
    assert(e->worklist_next == NULL && worklist.tail != e);
    if (worklist.tail == NULL) {
	assert(worklist.head == NULL);
	worklist.head = e;
	worklist.tail = e;
    } else {
	assert(worklist.tail->worklist_next == NULL);
	worklist.tail->worklist_next = e;
	worklist.tail = e;
    }
}

/**
 * Remove the first Edge in the ::worklist. Assumes the ::worklist is not
 * empty.
 */
Edge *worklist_pop() {
    Edge *e = worklist.head;
    worklist.head = e->worklist_next;
    if (worklist.head == NULL) {
	worklist.tail = NULL;
    }
    e->worklist_next = NULL;
    return e;
}

/**
 * Check if the ::worklist is empty.
 */
bool worklist_is_empty() {
    bool is_empty = (worklist.head == NULL);
    assert(!is_empty || worklist.tail == NULL);
    return is_empty;
}

/* STRING QUEUE HANDLING =================================================== */

/**
 * Copy @a str into a freshly allocated string. Allocates just enough memory
 * for the copied string.
 */
char *string_copy(const char *str) {
    char *copy;
    STRICT_ALLOC(copy, strlen(str) + 1, char);
    strcpy(copy, str);
    return copy;
}

/**
 * Initialize an empty StringQueue.
 */
StringQueue *string_queue_new() {
    StringQueue *q;
    STRICT_ALLOC(q, 1, StringQueue);
    q->length = 0;
    q->head = NULL;
    q->tail = NULL;
    return q;
}

/**
 * Append a string to the end of the StringQueue. We don't store the input
 * string itself, but rather a fresh copy of it, so the client does not need to
 * worry about keeping that memory live.
 */
void string_queue_insert(StringQueue *q, const char *str) {
    StringCell *cell;
    STRICT_ALLOC(cell, 1, StringCell);
    cell->value = string_copy(str);
    cell->next = NULL;
    if (q->tail == NULL) {
	assert(q->head == NULL);
	q->head = cell;
	q->tail = cell;
    } else {
	assert(q->tail->next == NULL);
	q->tail->next = cell;
	q->tail = cell;
    }
    (q->length)++;
}

/**
 * Convert this StringQueue into an array of strings. The order of the strings
 * in the output array is the same as their original order in the queue. The
 * memory for the queue is deallocated, but the string contents are not
 * modified.
 */
const char **string_queue_to_array(StringQueue *q) {
    const char **str_arr;
    STRICT_ALLOC(str_arr, q->length, const char *);
    unsigned long next_idx = 0;
    StringCell *cell = q->head;
    while (cell != NULL) {
	str_arr[next_idx++] = cell->value;
	StringCell *temp = cell;
	cell = cell->next;
	free(temp);
    }
    free(q);
    return str_arr;
}

/* TRIE OPERATIONS ========================================================= */

/* TODO: The trie operations might be simpler if we include \0 as a
   character. */

/**
 * Allocate space for a new NodeNameTrieCell to hold the specified character.
 * The new cell does not contain a Node value, and is not connected to any
 * other cells.
 */
NodeNameTrieCell *node_name_trie_cell_alloc(char character) {
    NodeNameTrieCell *cell;
    STRICT_ALLOC(cell, 1, NodeNameTrieCell);
    cell->character = character;
    cell->node = NODE_NONE;
    cell->first_child = NULL;
    cell->next_sibling = NULL;
    return cell;
}

/**
 * Create an empty name-to-Node trie.
 */
NodeNameTrie *node_name_trie_new() {
    NodeNameTrie *trie;
    STRICT_ALLOC(trie, 1, NodeNameTrie);
    trie->empty_name_node = NODE_NONE;
    trie->first_child = NULL;
    return trie;
}

/**
 * Create a stick (unary trie) that maps @a node to the non-empty string
 * @e name.
 */
NodeNameTrieCell *node_name_trie_make_stick(const char *name, NODE_REF node) {
    assert(*name != '\0');
    assert(node != NODE_NONE);
    NodeNameTrieCell *stick = NULL;
    NodeNameTrieCell *parent = NULL;
    do {
	NodeNameTrieCell *cell = node_name_trie_cell_alloc(*name);
	if (stick == NULL) {
	    stick = cell;
	}
	if (parent != NULL) {
	    parent->first_child = cell;
	}
	parent = cell;
	name++;
    } while (*name != '\0');
    parent->node = node;
    return stick;
}

/**
 * Record @a new_node in @a trie under string @a name, if @a name is not
 * already associated with a value.
 *
 * @return The final value stored in @a trie under string @a name.
 */
NODE_REF node_name_trie_set_if_none(NodeNameTrie *trie, const char *name,
				    NODE_REF new_node) {
    /* Handle the empty string: no need to traverse the trie. */
    if (*name == '\0') {
	node_ref_set_if_none(&(trie->empty_name_node), new_node);
	return trie->empty_name_node;
    }

    /* Non-empty string: traverse the trie to find it. */
    NodeNameTrieCell **parent_ptr = &(trie->first_child);
    while (true) {
	NodeNameTrieCell *cell = *parent_ptr;
	NodeNameTrieCell *prev_sibling = NULL;
	/* Search all the trie cells on the first level for the one holding the
	   character at the beginning of the string. Cells on the same level of
	   the trie are sorted by character, so we stop if we encounter a
	   lexicographically larger character. */
	while (cell != NULL && cell->character < *name) {
	    prev_sibling = cell;
	    cell = cell->next_sibling;
	}
	if (cell != NULL && cell->character == *name) {
	    /* We stopped because we found the character we were looking for:
	       proceed to the next character in the string. If we've processed
	       the whole string, then we're done, otherwise we continue
	       recursively on the trie rooted under the current cell. */
	    name++;
	    if (*name == '\0') {
		node_ref_set_if_none(&(cell->node), new_node);
		return cell->node;
	    }
	    parent_ptr = &(cell->first_child);
	    continue;
	}
	/* Either we've reached the end of the level without finding our target
	   character (cell == NULL), or we've encountered a larger character on
	   the current level (cell->character > *name). Either way, the input
	   string is not in the trie, and we have to insert it at this
	   point. */
	/* Non-existing cells implicitly store NODE_NONE, so we don't have to
	   actually create a new cell to store the new Node if it's equal to
	   NODE_NONE. */
	if (new_node == NODE_NONE) {
	    return NODE_NONE;
	}
	/* Where we connect the new subtree depends on whether we're at the
	   head of this level (prev_sibling == NULL) or not (prev_sibling
	   != NULL). In the first case, we set it as the first child of the
	   previous level cell, otherwise we insert it right after the previous
	   cell on the same level. */
	NodeNameTrieCell *stick = node_name_trie_make_stick(name, new_node);
	stick->next_sibling = cell;
	if (prev_sibling == NULL) {
	    *parent_ptr = stick;
	} else {
	    prev_sibling->next_sibling = stick;
	}
	return new_node;
    }
}

/**
 * Add @a node to the @a trie, under string @a name.
 *
 * @return @e true if @a name is now associated with @a node (either because of
 * this operation, or because that was already the case), @e false if there was
 * already some different Node stored under that name.
 */
bool node_name_trie_add(NodeNameTrie *trie, const char *name, NODE_REF node) {
    assert(node != NODE_NONE);
    return node_name_trie_set_if_none(trie, name, node) == node;
}

/**
 * Return the Node stored in @a trie under string @a name, or @e NODE_NONE if
 * that name is not present in @a trie.
 */
NODE_REF node_name_trie_find(NodeNameTrie *trie, const char *name) {
    return node_name_trie_set_if_none(trie, name, NODE_NONE);
}

/* NODES HANDLING ========================================================== */

/**
 * Initialize the Node%s-names mapping.
 */
void nodes_init() {
    node_names.is_final = false;
    node_names.num_nodes = 0;
    node_names.names.queue = string_queue_new();
    node_names.trie = node_name_trie_new();
}

/**
 * Record a Node of the specified name. Assigns a unique identifier to each
 * distinct Node name.
 */
void add_node(const char *name) {
    assert(!node_names.is_final);
    if (node_name_trie_add(node_names.trie, name, node_names.num_nodes)) {
	node_names.num_nodes++;
	string_queue_insert(node_names.names.queue, name);
    }
}

/**
 * Signal the end of Node additions. Now that we know the final number of
 * Node%s, we can finalize the Node-to-name mapping and initialize the input
 * graph.
 */
void finalize_nodes() {
    assert(!node_names.is_final);
    node_names.is_final = true;
    /* Bake the names queue into an array. */
    node_names.names.array = string_queue_to_array(node_names.names.queue);
    /* Initialize the input graph with the number of nodes we've inserted so
       far, and the number of kinds as returned by the grammar-generated
       code. */
    STRICT_ALLOC(nodes, node_names.num_nodes, Node);
    for (NODE_REF n = 0; n < node_names.num_nodes; n++) {
	STRICT_ALLOC(nodes[n].in, num_kinds(), Edge *);
	STRICT_ALLOC(nodes[n].out, num_kinds(), OutEdgeSet *);
	for (EDGE_KIND k = 0; k < num_kinds(); k++) {
	    nodes[n].in[k] = NULL;
	    nodes[n].out[k] = NULL;
	}
    }
}

/**
 * Get the number of Node%s in the input graph. Can only be called after the
 * Node%s container has been finalized.
 */
NODE_REF num_nodes() {
    assert(node_names.is_final);
    return node_names.num_nodes;
}

/**
 * Get the name for the specified Node. Can only be called after the Node%s
 * container has been finalized.
 */
const char *node2name(NODE_REF node) {
    assert(node_names.is_final);
    assert(node < node_names.num_nodes);
    return node_names.names.array[node];
}

/**
 * Get the Node for the specified name. Can only be called after the Node%s
 * container has been finalized. Expects that the requested name had previously
 * been recorded.
 */
NODE_REF name2node(const char *name) {
    assert(node_names.is_final);
    NODE_REF node = node_name_trie_find(node_names.trie, name);
    assert(node != NODE_NONE);
    return node;
}

/* EDGE OPERATIONS ========================================================= */

/**
 * Create an Edge with the given attributes.
 *
 * The new Edge is not added to the graph or the ::worklist.
 */
Edge *edge_new(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index,
	       Edge *l_edge, bool l_rev, Edge *r_edge, bool r_rev) {
    assert((is_parametric(kind)) ^ (index == INDEX_NONE));
    Edge *e;
    STRICT_ALLOC(e, 1, Edge);
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
    e->in_next = NULL;
    e->out_next = NULL;
    e->worklist_next = NULL;
    return e;
}

/**
 * Check if two Edge objects represent the same graph edge.
 */
bool edge_equals(Edge *a, Edge *b) {
    return a == b || ((a->from == b->from) && (a->to == b->to) &&
		      (a->kind == b->kind) && (a->index == b->index));
}

/* EDGE LIST TABLE OPERATIONS ============================================== */

/**
 * Allocate a new array of @a size linked lists of Edge%s. All the linked lists
 * start out empty.
 */
Edge **out_edge_table_alloc(unsigned int size) {
    Edge **table;
    STRICT_ALLOC(table, size, Edge *);
    for (unsigned int i = 0; i < size; i++) {
	table[i] = NULL;
    }
    return table;
}

/**
 * Insert Edge @a e to the linked list at position @a index in @a table. We
 * don't check whether @a index is within bounds, or whether @a e is already
 * present in the list.
 */
void out_edge_table_add(Edge **table, unsigned int index, Edge *e) {
    /* TODO: Could keep the overflow lists sorted, to make searching faster. */
    e->out_next = table[index];
    table[index] = e;
}

/**
 * Check if the linked list at position @a index in @a table contains an Edge
 * @a e.
 */
bool out_edge_table_contains(Edge **table, unsigned int index, Edge *to_find) {
    for (Edge *e = table[index]; e != NULL; e = e->out_next) {
	if (edge_equals(e, to_find)) {
	    return true;
	}
    }
    return false;
}

/**
 * Calculate the bucket in a @a num_buckets-wide OutEdgeSet where we should
 * insert an Edge with target Node @a key.
 */
unsigned int out_edge_set_get_bucket(NODE_REF key, unsigned int num_buckets) {
    /* TODO: We don't use any smart hash function, instead we use the NODE_REF
       value itself (which is already an integer) to pick a bucket. */
    /* Equivalent to key % num_buckets
       The size of the hashtable is always a power of 2, so we can use bit
       masking instead of modulus operations. */
    return key & (num_buckets - 1);
}

/* EDGE SET ITERATOR OPERATIONS ============================================ */

/**
 * Check whether an OutEdgeSet currently has a live iterator over it.
 */
bool has_live_iterator(OutEdgeSet *set) {
    if (set->iter_bucket < set->num_buckets) {
	assert(set->iter_list_ptr != NULL);
	return true;
    }
    assert(set->iter_bucket == set->num_buckets);
    assert(set->iter_tgt_node == NODE_NONE);
    assert(set->iter_list_ptr == NULL);
    return false;
}

/**
 * Advance the internal iterator of @a set to the first Edge residing at or
 * after @a bucket. If no such Edge exists, advance the iterator past the end
 * of the set. Only applies for unconstrained iterators.
 *
 * @return The Edge at the iterator's new position, or @e NULL if there are no
 * such Edge%s.
 */
Edge *out_edge_set_iterator_move_to(OutEdgeSet *set, unsigned int bucket) {
    assert(bucket <= set->num_buckets);
    assert(set->iter_tgt_node == NODE_NONE);
    Edge *list_ptr = NULL;
    for (; bucket < set->num_buckets; bucket++) {
	list_ptr = set->table[bucket];
	if (list_ptr != NULL) {
	    break;
	}
    }
    set->iter_bucket = bucket;
    set->iter_list_ptr = list_ptr;
    return list_ptr;
}

/**
 * Initialize an unconstrained iterator over @a set. The iterator is set to
 * point to the first element of @a set.
 *
 * There can't already be a live iterator over @a set.
 *
 * @return The Edge at the iterator's new position, or @e NULL if @a set is
 * empty.
 */
Edge *out_edge_set_first(OutEdgeSet *set) {
    assert(!has_live_iterator(set));
    // Leave set->iter_tgt_node to NODE_NONE.
    return out_edge_set_iterator_move_to(set, 0);
}

/**
 * Initialize a constrained iterator over @a set. The iterator is set to
 * point to the first Edge in @a set that targets @a target.
 *
 * There can't already be a live iterator over @a set.
 *
 * @return The Edge at the iterator's new position, or @e NULL if there are no
 * such Edge%s.
 */
Edge *out_edge_set_first_to_target(OutEdgeSet *set, NODE_REF target) {
    assert(!has_live_iterator(set));
    const unsigned int bucket =
	out_edge_set_get_bucket(target, set->num_buckets);
    Edge *e = set->table[bucket];
    while (e != NULL && e->to != target) {
	e = e->out_next;
    }
    if (e != NULL) {
	set->iter_bucket = bucket;
	set->iter_tgt_node = target;
	set->iter_list_ptr = e;
    }
    // If there's no edge with the specific target, don't even initialize the
    // iterator.
    return e;
}

/**
 * Advance the internal iterator of @a set to the next Edge.
 *
 * @return The Edge at the iterator's new position, or @e NULL if there are no
 * more Edge%s.
 */
Edge *out_edge_set_next(OutEdgeSet *set) {
    assert(has_live_iterator(set));
    const bool constrained = (set->iter_tgt_node != NODE_NONE);
    Edge *e = set->iter_list_ptr;
    do {
	// If the iterator is constrained to a specific target node, iterate
	// until the end of the bucket, or the first instance of an Edge with
	// the desired target.
	e = e->out_next;
    } while (constrained && e != NULL && e->to != set->iter_tgt_node);
    if (e == NULL) {
	// We've reached the end of the current bucket. If we're constrained to
	// a specific target node, then we don't need to search any other
	// bucket (the current bucket is the only one that contains Edges to
	// that target), so we immediatelly invalidate the iterator.
	unsigned int next_bucket =
	    constrained ? set->num_buckets : set->iter_bucket + 1;
	// Also erase the constraint, if any.
	set->iter_tgt_node = NODE_NONE;
	return out_edge_set_iterator_move_to(set, next_bucket);
    }
    set->iter_list_ptr = e;
    return e;
}

/* EDGE SET OPERATIONS ===================================================== */

/**
 * Allocate an empty OutEdgeSet with @ref OUT_EDGE_SET_INIT_NUM_BUCKETS
 * buckets.
 */
OutEdgeSet *out_edge_set_new() {
    OutEdgeSet *set;
    STRICT_ALLOC(set, 1, OutEdgeSet);
    set->size = 0;
    set->num_buckets = OUT_EDGE_SET_INIT_NUM_BUCKETS;
    set->table = out_edge_table_alloc(set->num_buckets);
    set->iter_bucket = set->num_buckets;
    set->iter_tgt_node = NODE_NONE;
    set->iter_list_ptr = NULL;
    return set;
}

/**
 * Double the number of buckets in @a set and rehash all elements.
 *
 * Can't be called while there exists a live iterator over @a set.
 */
void out_edge_set_grow(OutEdgeSet *set) {
    assert(!has_live_iterator(set));
    /* Equivalent to new_num_buckets = set->num_buckets * 2
       The size of the hashtable is always a power of 2, so we can use left
       shift instead of multiplication. */
    unsigned int new_num_buckets = set->num_buckets << 1;
    Edge **new_table = out_edge_table_alloc(new_num_buckets);
    Edge *e = out_edge_set_first(set);
    do {
	/* UGLY: The pointer to the next element in the overflow list,
	   'out_next', is part of the Edge struct, so when we transfer the Edge
	   to the new table, we invalidate the structure of the old table. By
	   skipping over an element before we transfer it, we make sure the
	   iterator won't have trouble traversing the old table. */
	Edge *temp = e;
	e = out_edge_set_next(set);
	unsigned int new_bucket =
	    out_edge_set_get_bucket(temp->to, new_num_buckets);
	out_edge_table_add(new_table, new_bucket, temp);
    } while (e != NULL);
    free(set->table);
    set->num_buckets = new_num_buckets;
    set->table = new_table;
    set->iter_bucket = new_num_buckets;
}

/**
 * Add Edge @a e to @a set, expanding the table if its load factor exceeds
 * @ref OUT_EDGE_SET_MAX_LOAD_FACTOR.
 *
 * We don't check whether @a e is already present in @a set. This function
 * can't be called while there exists a live iterator over @a set.
 */
void out_edge_set_add(OutEdgeSet *set, Edge *e) {
    assert(!has_live_iterator(set));
    (set->size)++;
    float load_factor = (float) set->size / (float) set->num_buckets;
    if (load_factor > OUT_EDGE_SET_MAX_LOAD_FACTOR) {
	out_edge_set_grow(set);
    }
    unsigned int bucket = out_edge_set_get_bucket(e->to, set->num_buckets);
    out_edge_table_add(set->table, bucket, e);
}

/**
 * Check if an Edge is present in the @a set.
 */
bool out_edge_set_contains(OutEdgeSet *set, Edge *e) {
    unsigned int bucket = out_edge_set_get_bucket(e->to, set->num_buckets);
    return out_edge_table_contains(set->table, bucket, e);
}

/* GRAPH HANDLING ========================================================== */

/**
 * Check if an Edge is present in the graph.
 */
bool graph_contains(Edge *e) {
    /* We search on the set of outgoing edges of the source node. */
    OutEdgeSet *set = nodes[e->from].out[e->kind];
    if (set == NULL) {
	return false;
    }
    return out_edge_set_contains(set, e);
}

/**
 * Add an Edge to the graph.
 *
 * Assumes that an equivalent Edge is not already present in the graph.
 */
void graph_add(Edge *e) {
    e->in_next = nodes[e->to].in[e->kind];
    nodes[e->to].in[e->kind] = e;
    OutEdgeSet *set = nodes[e->from].out[e->kind];
    if (set == NULL) {
	set = out_edge_set_new();
	nodes[e->from].out[e->kind] = set;
    }
    out_edge_set_add(set, e);
}

/**
 * Add a new Edge to the graph, unless it's already present.
 */
void add_edge(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index,
	      Edge *l_edge, bool l_rev, Edge *r_edge, bool r_rev) {
    Edge *e = edge_new(from, to, kind, index, l_edge, l_rev, r_edge, r_rev);
    if (graph_contains(e)) {
	free(e);
    } else {
	graph_add(e);
	worklist_insert(e);
    }
}

/**
 * Return an iterator over the entire set of outgoing Edge%s of @Kind @a kind,
 * for source Node @a from. Use next_out_edge() to advance the iterator.
 * Returns @e NULL in the case of the empty set.
 */
Edge *get_out_edges(NODE_REF from, EDGE_KIND kind) {
    OutEdgeSet *set = nodes[from].out[kind];
    if (set == NULL) {
	return NULL;
    }
    return out_edge_set_first(set);
}

/**
 * Return an iterator over the set of Edge%s of @Kind @a kind originating from
 * Node @a from and targeting Node @a target. Use next_out_edge() to advance
 * the iterator. Returns @e NULL if there are no such Edge%s.
 */
Edge *get_out_edges_to_target(NODE_REF from, NODE_REF to, EDGE_KIND kind) {
    OutEdgeSet *set = nodes[from].out[kind];
    if (set == NULL) {
	return NULL;
    }
    return out_edge_set_first_to_target(set, to);
}

/**
 * Advance an iterator returned by get_out_edges(). Returns @e NULL when we
 * reach the end of the iteration range.
 */
Edge *next_out_edge(Edge *e) {
    OutEdgeSet *set = nodes[e->from].out[e->kind];
    assert(set != NULL);
    return out_edge_set_next(set);
}

/**
 * Return an iterator over the set of incoming Edge%s of @Kind @a kind, for
 * target Node @a to. Use next_in_edge() to advance the iterator. Returns
 * @e NULL in the case of the empty set.
 */
Edge *get_in_edges(NODE_REF to, EDGE_KIND kind) {
    /* The incoming Edge%s are stored in linked lists, so we simply return
       the head of the list. */
    return nodes[to].in[kind];
}

/**
 * Advance an iterator returned by get_in_edges(). Returns @e NULL when we reach
 * the end of the set.
 */
Edge *next_in_edge(Edge *e) {
    /* The incoming Edge%s are stored in linked lists, so we simply move
       forward in the list. */
    return e->in_next;
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
 * They must only contain lines of the form `source sink index`, where
 * @e source and @e sink are arbitrary string names for the nodes, and @e index
 * is either a single number, or `*`, which means "any index". Each line
 * corresponds to a separate Edge.
 */
#define EDGE_SET_FORMAT "dat"

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
typedef enum {
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
} PARSING_MODE;

/**
 * Parse in the input graph.
 *
 * For each terminal @Symbol in the input grammar, find the corresponding
 * @ref EDGE_SET_FORMAT file in subdir @ref INPUT_DIR and process it according
 * to @a mode.
 */
void parse_input_files(PARSING_MODE mode) {
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
	    ERROR("Can't open input file: %s\n", fname);
	    exit(1);
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
	    if (errno != 0 || (num_scanned != exp_num_scanned
			       && num_scanned != EOF)) {
		fclose(f);
		ERROR("Error while parsing file: %s\n", fname);
		exit(1);
	    }
	    if (num_scanned == EOF) {
		fclose(f);
		break;
	    }
	    NODE_REF src_node, tgt_node;
	    switch (mode) {
	    case RECORD_NODES:
		add_node(src_buf);
		add_node(tgt_buf);
		break;
	    case RECORD_EDGES:
		src_node = name2node(src_buf);
		tgt_node = name2node(tgt_buf);
		if (parametric) {
		    INDEX index = index_parse(idx_buf);
		    if (index == INDEX_NONE) {
			ERROR("Invalid index: '%s'\n", idx_buf);
			fclose(f);
			exit(1);
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
	if (is_terminal(k)) {
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
	    ERROR("Can't open output file: %s\n", fname);
	    exit(1);
	}
	for (NODE_REF n = 0; n < num_nodes(); n++) {
	    const char *src_name = node2name(n);
	    /* TODO: Iterate over in_edges, so we avoid paying the overhead of
	       traversing the out_edges hashtable. */
	    Edge *e = get_out_edges(n, k);
	    for (; e != NULL; e = next_out_edge(e)) {
		const char *tgt_name = node2name(e->to);
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
	ERROR("Can't open output file: %s\n", fname);
	exit(1);
    }
    for (NODE_REF n = 0; n < num_nodes(); n++) {
	fprintf(f, "%s\n", node2name(n));
    }
    fclose(f);
}

/* CHOICE SEQUENCE HANDLING ================================================ */

constexpr ChoiceSequence *choice_sequence_empty() {
    return NULL;
}

ChoiceSequence *choice_sequence_extend(ChoiceSequence *prev, CHOICE last) {
    ChoiceSequence *choice;
    STRICT_ALLOC(choice, 1, ChoiceSequence);
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
 * follow the first @Production, i.e. `A :: B`, which leads us to consider the
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
    Step *step;
    STRICT_ALLOC(step, 1, Step);
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
    STRICT_ALLOC(step->sub_step_seqs, derivs.size(), Step *);
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
    PartialPath *path;
    STRICT_ALLOC(path, 1, PartialPath);
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
    fprintf(f, "<step reverse='%s' symbol='%s' from='%s' to='%s'",
	    reverse ? "true" : "false", kind2symbol(edge->kind),
	    node2name(edge->from), node2name(edge->to));
    if (is_parametric(edge->kind)) {
	char idx_buf[32];
	// TODO: Check that printing is successful.
	index_print(edge->index, idx_buf, sizeof(idx_buf));
	fprintf(f, " index='%s'", idx_buf);
    }
    fprintf(f, ">\n");
}

void print_step_close(FILE *f) {
    fprintf(f, "</step>\n");
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
    print_step_close(f);
}

void print_path(PartialPath *path, Step *top_step, FILE *f) {
    assert(partial_path_is_complete(path));
    assert(top_step->next_sibling == NULL);
    fprintf(f, "<path length='%u'>\n", path->min_length);
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
    print_step_close(f);
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
	ERROR("Can't open output file: %s\n", fname);
	exit(1);
    }
    fprintf(f, "<paths>\n");

    for (NODE_REF n = 0; n < num_nodes(); n++) {
	const char *tgt_name = node2name(n);
	// HACK: The reason we're using incoming iterators is because we'll be
	// iterating on the OutEdgeSet inside the loop body, and the current
	// implementation of OutEdgeSet iterators allows only a single
	// live iterator per node and kind.
	Edge *e = get_in_edges(n, k);
	for (; e != NULL; e = next_in_edge(e)) {
	    const char *src_name = node2name(e->from);
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
    setlocale(LC_NUMERIC, "");
}

/**
 * Print out statistics about the Edge%s in the graph.
 *
 * @param [in] terminal Whether to print statistics about the Edges
 * representing terminal @Symbol%s or non-terminals.
 */
void print_edge_counts(bool terminal) {
    DECL_COUNTER(total_edge_count, 0);
    DECL_COUNTER(total_index_count, 0);
    for (EDGE_KIND k = 0; k < num_kinds(); k++) {
	if (is_terminal(k) ^ terminal) {
	    continue;
	}
	bool parametric = is_parametric(k);
	DECL_COUNTER(edge_count, 0);
	DECL_COUNTER(index_count, 0);
	for (NODE_REF n = 0; n < num_nodes(); n++) {
	    Edge *e = get_in_edges(n, k);
	    for (; e != NULL; e = next_in_edge(e)) {
		edge_count++;
		total_edge_count++;
		if (parametric) {
		    index_count++;
		    total_index_count++;
		}
	    }
	}
	if (parametric) {
	    LOG("%15s: %'12lu edges, %'12lu indices\n", kind2symbol(k),
		edge_count, index_count);
	} else {
	    LOG("%15s: %'12lu edges\n", kind2symbol(k), edge_count);
	}
    }
    LOG("%'lu edges in total\n", total_edge_count);
    LOG("%'lu indices in total\n", total_index_count);
}

/**
 * Underlying implementation of @ref PRINT_STATS.
 */
void print_stats() {
    LOG("Terminals:\n");
    print_edge_counts(true);
    LOG("Non-terminals:\n");
    print_edge_counts(false);
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
    LOG("Starting profiler\n");
    char pid_arg[10];
    /* TODO: PID arg buffer may be too small. */
    snprintf(pid_arg, sizeof(pid_arg), "%u", getpid());
    profiler_pid = fork();
    if (profiler_pid == 0) { /* In the child process. */
	/* TODO: The solver will run unprofiled for a little time before the
	   profiler has had enough time to finish initialization. */
	execlp("perf", "perf", "stat", "-p", pid_arg, (char *) NULL);
	/* Will only reach here if exec fails. */
	ERROR("Profiler spawn failed\n");
    }
}

/**
 * Underlying implementation of @ref STOP_PROFILER.
 */
void stop_profiler() {
    LOG("Stopping profiler\n");
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
    LOGGING_INIT();

    DECL_COUNTER(loading_start, CURRENT_TIME());
    nodes_init();
    parse_input_files(RECORD_NODES);
    finalize_nodes();
    LOG("Nodes: %'u\n", num_nodes());
    worklist_init();
    parse_input_files(RECORD_EDGES);
    LOG("Loading completed in %'lu ms\n", CURRENT_TIME() - loading_start);

    START_PROFILER();
    DECL_COUNTER(solving_start, CURRENT_TIME());
    /* Process empty productions. */
    for (EDGE_KIND k = 0; k < num_kinds(); k++) {
	if (has_empty_prod(k)) {
	    for (NODE_REF n = 0; n < num_nodes(); n++) {
		add_edge(n, n, k, INDEX_NONE, NULL, false, NULL, false);
	    }
	}
    }
    /* Main loop. */
    while (!worklist_is_empty()) {
	INC_COUNTER(iteration, 1);
	Edge *base = worklist_pop();
	main_loop(base);
    }
    LOG("Fixpoint reached after %'lu iterations\n", iteration);
    LOG("Solving completed in %'lu ms\n", CURRENT_TIME() - solving_start);
    STOP_PROFILER();

    DECL_COUNTER(printing_start, CURRENT_TIME());
    PRINT_STATS();
    print_results();
    print_paths();
    LOG("Printing completed in %'lu ms\n", CURRENT_TIME() - printing_start);
}