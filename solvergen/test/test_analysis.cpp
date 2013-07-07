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
    Edge *other;
    switch (base->kind) {
    case 0: /* Flow */
	/* Flow + assign => Flow */
	other = get_out_edges(base->to, 1);
	for (; other != NULL; other = next_out_edge(other)) {
	    add_edge(base->from, other->to, 0, INDEX_NONE);
	}
	/* Flow + Temp1 => Flow */
	other = get_out_edges(base->to, 4);
	for (; other != NULL; other = next_out_edge(other)) {
	    add_edge(base->from, other->to, 0, INDEX_NONE);
	}
	/* Flow + ret[i] => Temp2[i] */
	other = get_out_edges(base->to, 3);
	for (; other != NULL; other = next_out_edge(other)) {
	    add_edge(base->from, other->to, 5, other->index);
	}
	break;
    case 1: /* assign */
	/* Flow + assign => Flow */
	other = get_in_edges(base->from, 0);
	for (; other != NULL; other = next_in_edge(other)) {
	    add_edge(other->from, base->to, 0, INDEX_NONE);
	}
	break;
    case 2: /* param */
	/* param[i] + Temp2[i] => Temp1 */
	other = get_out_edges(base->to, 5);
	for (; other != NULL; other = next_out_edge(other)) {
	    if (base->index == other->index) {
		add_edge(base->from, other->to, 4, INDEX_NONE);
	    }
	}
	break;
    case 3: /* ret */
	/* Flow + ret[i] => Temp2[i] */
	other = get_in_edges(base->from, 0);
	for (; other != NULL; other = next_in_edge(other)) {
	    add_edge(other->from, base->to, 5, base->index);
	}
	break;
    case 4: /* Temp1 */
	/* Flow + Temp1 => Flow */
	other = get_in_edges(base->from, 0);
	for (; other != NULL; other = next_in_edge(other)) {
	    add_edge(other->from, base->to, 0, INDEX_NONE);
	}
	break;
    case 5: /* Temp2 */
	/* param[i] + Temp2[i] => Temp1 */
	other = get_in_edges(base->from, 2);
	for (; other != NULL; other = next_in_edge(other)) {
	    if (base->index == other->index) {
		add_edge(other->from, base->to, 4, INDEX_NONE);
	    }
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
    Edge *l, *r;
    std::list<Derivation> derivs;
    switch (e->kind) {
    case 0: /* Flow */
	/* - => Flow */
	if (e->from == e->to) {
	    derivs.push_back(derivation_empty());
	}
	/* Flow + assign => Flow */
	l = get_out_edges(e->from, 0);
	for (; l != NULL; l = next_out_edge(l)) {
	    r = get_out_edges_to_target(l->to, e->to, 1);
	    for (; r != NULL; r = next_out_edge(r)) {
		derivs.push_back(derivation_double(l, false, r, false));
	    }
	}
	/* Flow + Temp1 => Flow */
	l = get_out_edges(e->from, 0);
	for (; l != NULL; l = next_out_edge(l)) {
	    r = get_out_edges_to_target(l->to, e->to, 4);
	    for (; r != NULL; r = next_out_edge(r)) {
		derivs.push_back(derivation_double(l, false, r, false));
	    }
	}
	break;
    case 4: /* Temp1 */
	/* param[i] + Temp2[i] => Temp1 */
	l = get_out_edges(e->from, 2);
	for (; l != NULL; l = next_out_edge(l)) {
	    r = get_out_edges_to_target(l->to, e->to, 5);
	    for (; r != NULL; r = next_out_edge(r)) {
		if (l->index == r->index) {
		    derivs.push_back(derivation_double(l, false, r, false));
		}
	    }
	}
	break;
    case 5: /* Temp2 */
	/* Flow + ret[i] => Temp2[i] */
	l = get_out_edges(e->from, 0);
	for (; l != NULL; l = next_out_edge(l)) {
	    r = get_out_edges_to_target(l->to, e->to, 3);
	    for (; r != NULL; r = next_out_edge(r)) {
		if (r->index == e->index) {
		    derivs.push_back(derivation_double(l, false, r, false));
		}
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
