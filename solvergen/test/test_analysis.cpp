#include <assert.h>
#include <list>
#include <stdbool.h>
#include <string.h>

#include "solvergen.hpp"

/**
 * @file
 * A sample analysis code file, to showcase the expected format of the
 * client-defined parts of the code. The following functions are normally
 * generated automatically, based on a Context-Free Grammar provided by the
 * user.
 *
 * In this example, we show what we expect the generated code to be like for
 * the following grammar, which encodes value flow analysis for a very simple
 * language with only primitive values, assignment and pass-by-value function
 * calls:
 *
 *     Flow :: -
 *           | Flow assign
 *           | Flow param[i] Flow ret[i]
 *
 * We assume that the above grammar gets normalized as:
 *
 *     Flow :: -
 *           | Flow assign
 *           | Flow Temp1
 *     Temp1 :: param[i] Temp2[i]
 *     Temp2[i] :: Flow ret[i]
 */

bool is_terminal(EDGE_KIND kind) {
    switch (kind) {
    case 1: /* assign */
    case 2: /* param */
    case 3: /* ret */
	return true;
    default:
	return false;
    }
}

bool is_parametric(EDGE_KIND kind) {
    switch (kind) {
    case 2: /* param */
    case 3: /* ret */
    case 5: /* Temp2 */
	return true;
    default:
	return false;
    }
}

bool has_empty_prod(EDGE_KIND kind) {
    return kind == 0; /* Only 'Flow' has an empty production. */
}

void main_loop(Edge *base) {
    /* To correctly process an edge, we have to check all the relevant
       productions (where its symbol appears in the RHS). */
    switch (base->kind) {
    case 0: /* Flow */
	/* Flow + assign => Flow */
	for (Edge *other : edges_from(base->to, 1)) {
	    add_edge(base->from, other->to, 0, INDEX_NONE,
		     base, false, other, false);
	}
	/* Flow + Temp1 => Flow */
	for (Edge *other : edges_from(base->to, 4)) {
	    add_edge(base->from, other->to, 0, INDEX_NONE,
		     base, false, other, false);
	}
	/* Flow + ret[i] => Temp2[i] */
	for (Edge *other : edges_from(base->to, 3)) {
	    add_edge(base->from, other->to, 5, other->index,
		     base, false, other, false);
	}
	break;
    case 1: /* assign */
	/* Flow + assign => Flow */
	for (Edge *other : edges_to(base->from, 0)) {
	    add_edge(other->from, base->to, 0, INDEX_NONE,
		     other, false, base, false);
	}
	break;
    case 2: /* param */
	/* param[i] + Temp2[i] => Temp1 */
	for (Edge *other : edges_from(base->to, 5, base->index)) {
	    add_edge(base->from, other->to, 4, INDEX_NONE,
		     base, false, other, false);
	}
	break;
    case 3: /* ret */
	/* Flow + ret[i] => Temp2[i] */
	for (Edge *other : edges_to(base->from, 0)) {
	    add_edge(other->from, base->to, 5, base->index,
		     other, false, base, false);
	}
	break;
    case 4: /* Temp1 */
	/* Flow + Temp1 => Flow */
	for (Edge *other : edges_to(base->from, 0)) {
	    add_edge(other->from, base->to, 0, INDEX_NONE,
		     other, false, base, false);
	}
	break;
    case 5: /* Temp2 */
	/* param[i] + Temp2[i] => Temp1 */
	for (Edge *other : edges_to(base->from, 2, base->index)) {
	    add_edge(other->from, base->to, 4, INDEX_NONE,
		     other, false, base, false);
	}
	break;
    }
}

EDGE_KIND num_kinds() {
    /* 6 symbols: 'Flow', 'assign', 'param', 'ret', 'Temp1' and 'Temp2'
       represented by non-negative integers ('kinds') 0..5 */
    return 6;
}

EDGE_KIND symbol2kind(const char *symbol) {
    if (strcmp(symbol, "Flow") == 0) {
	return 0;
    } else if (strcmp(symbol, "assign") == 0) {
	return 1;
    } else if (strcmp(symbol, "param") == 0) {
	return 2;
    } else if (strcmp(symbol, "ret") == 0) {
	return 3;
    } else if (strcmp(symbol, "Temp1") == 0) {
	return 4;
    } else if (strcmp(symbol, "Temp2") == 0) {
	return 5;
    } else {
	assert(false);
    }
}

const char *kind2symbol(EDGE_KIND kind) {
    switch (kind) {
    case 0:
	return "Flow";
    case 1:
	return "assign";
    case 2:
	return "param";
    case 3:
	return "ret";
    case 4:
	return "Temp1";
    case 5:
	return "Temp2";
    default:
	assert(false);
    }
}

std::list<Derivation> all_derivations(Edge *e) {
    Edge *r;
    std::list<Derivation> derivs;
    switch (e->kind) {
    case 0: /* Flow */
	/* - => Flow */
	if (e->from == e->to) {
	    derivs.push_back(derivation_empty());
	}
	/* Flow + assign => Flow */
	for (Edge *l : edges_from(e->from, 0)) {
	    for (Edge *r : edges_between(l->to, e->to, 1)) {
		derivs.push_back(derivation_double(l, false, r, false));
	    }
	}
	/* Flow + Temp1 => Flow */
	for (Edge *l : edges_from(e->from, 0)) {
	    for (Edge *r : edges_between(l->to, e->to, 4)) {
		derivs.push_back(derivation_double(l, false, r, false));
	    }
	}
	break;
    case 4: /* Temp1 */
	/* param[i] + Temp2[i] => Temp1 */
	for (Edge *l : edges_from(e->from, 2)) {
	    if ((r = find_edge(l->to, e->to, 5, l->index)) != NULL) {
		derivs.push_back(derivation_double(l, false, r, false));
	    }
	}
	break;
    case 5: /* Temp2 */
	/* Flow + ret[i] => Temp2[i] */
	for (Edge *l : edges_from(e->from, 0)) {
	    if ((r = find_edge(l->to, e->to, 3, l->index)) != NULL) {
		derivs.push_back(derivation_double(l, false, r, false));
	    }
	}
	break;
    default:
	assert(false);
    }
    return derivs;
}

unsigned int num_paths_to_print(EDGE_KIND kind) {
    switch (kind) {
    case 0: /* Flow */
	return 10;
    default:
	return 0;
    }
}

PATH_LENGTH static_min_length(EDGE_KIND kind) {
    switch (kind) {
    case 0: /* Flow */
	return 0;
    case 1: /* assign */
	return 1;
    case 2: /* param */
	return 1;
    case 3: /* ret */
	return 1;
    case 4: /* Temp1 */
	return 2;
    case 5: /* Temp2 */
	return 1;
    default:
	assert(false);
    }
}

bool is_predicate(EDGE_KIND kind) {
    return false; /* No predicates in this grammar. */
}

bool is_temporary(EDGE_KIND kind) {
    return kind == 4 || kind == 5;
}

bool reachable(NODE_REF from, NODE_REF to, EDGE_KIND kind, INDEX index) {
    switch (kind) {
    case 0: /* Flow */
	assert(false);
    case 1: /* assign */
	if (find_edge(from, to, 1) != NULL) {
	    return true;
	}
	break;
    case 2: /* param */
	if (find_edge(from, to, 2, index) != NULL) {
	    return true;
	}
	break;
    case 3: /* ret */
	if (find_edge(from, to, 3, index) != NULL) {
	    return true;
	}
	break;
    case 4: /* Temp1 */
	assert(false);
    case 5: /* Temp2 */
	assert(false);
    default:
	assert(false);
    }
    return false;
}

RELATION_REF num_rels() {
    return 0;
}

RELATION_REF rel2ref(const char *rel) {
    assert(false);
}

const char *ref2rel(RELATION_REF ref) {
    assert(false);
}

ARITY rel_arity(RELATION_REF ref) {
    assert(false);
}
