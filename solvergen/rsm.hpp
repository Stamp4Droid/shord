#ifndef RSM_HPP
#define RSM_HPP

#include <boost/program_options.hpp>
#include <boost/regex.hpp>

#include "util.hpp"

namespace po = boost::program_options;
namespace fs = boost::filesystem;

// ALPHABET ===================================================================

class Node;
class Edge;

// TODO: separate class for parametric and non-parametric symbols
class Symbol {
    friend Registry<Symbol>;
    typedef std::string Key;
private:
    static const boost::regex NAME_REGEX;
public:
    const std::string name;
    const Ref<Symbol> ref;
    const bool parametric;
private:
    explicit Symbol(const std::string& name, Ref<Symbol> ref, bool parametric)
	: name(name), ref(ref), parametric(parametric) {
	assert(boost::regex_match(name, NAME_REGEX));
    }
public:
    bool merge(bool parametric) const {
	assert(parametric == this->parametric);
	return false;
    }
};

enum class Direction {FWD, REV};

class Label {
public:
    const Ref<Symbol> symbol;
    const Direction dir;
    // The specific index character on a label doesn't matter.
    const bool tagged;
public:
    explicit Label(Ref<Symbol> symbol, Direction dir, bool tagged)
	: symbol(symbol), dir(dir), tagged(tagged) {}
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg) const;
    bool operator<(const Label& rhs) const {
	return (std::tie(    symbol,     dir,     tagged) <
		std::tie(rhs.symbol, rhs.dir, rhs.tagged));
    }
    Ref<Node> prev_node(const Edge& e) const;
    Ref<Node> next_node(const Edge& e) const;
};

// RSM ========================================================================

class Component;
class Graph;
class Worker;

// States carry very little actual information; e.g. transitions are stored on
// a separate table.
class State {
    friend Registry<State>;
    typedef std::string Key;
public:
    const std::string name;
    const Ref<State> ref;
    const bool initial;
    const bool final;
private:
    explicit State(const std::string& name, Ref<State> ref, bool initial,
		   bool final)
	: name(name), ref(ref), initial(initial), final(final) {}
public:
    bool merge(bool initial, bool final) const {
	assert(initial == this->initial && final == this->final);
	return false;
    }
    void print(std::ostream& os) const;
};

class Box {
    friend Registry<Box>;
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Box> ref;
    const Ref<Component> comp;
private:
    explicit Box(const std::string& name, Ref<Box> ref, Ref<Component> comp)
	: name(name), ref(ref), comp(comp) {}
public:
    bool merge(Ref<Component> comp) const {
	assert(comp == this->comp);
	return false;
    }
    void print(std::ostream& os, const Registry<Component>& comp_reg) const;
};

// No tags allowed on state-to-state transitions; parametric symbols must be
// tagged with '[*]'.
class Transition {
public:
    const Ref<State> from;
    const Ref<State> to;
    const Label label;
public:
    explicit Transition(Ref<State> from, Ref<State> to, Label label)
	: from(from), to(to), label(label) {
	assert(!label.tagged);
    }
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg) const;
    bool operator<(const Transition& rhs) const {
	return (std::tie(    from,     to,     label) <
		std::tie(rhs.from, rhs.to, rhs.label));
    }
};

// TODO:
// - Enforce that either all entries and exits to the same box are tagged,
//   or none is.
// - Can't move from a box directly to another box.
class Entry {
public:
    const Ref<State> from;
    const Ref<Box> to;
    const Label label;
public:
    explicit Entry(Ref<State> from, Ref<Box> to, Label label)
	: from(from), to(to), label(label) {}
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg) const;
    bool operator<(const Entry& rhs) const {
	return (std::tie(    from,     to,     label) <
		std::tie(rhs.from, rhs.to, rhs.label));
    }
};

class Exit {
public:
    const Ref<Box> from;
    const Ref<State> to;
    const Label label;
public:
    explicit Exit(Ref<Box> from, Ref<State> to, Label label)
	: from(from), to(to), label(label) {}
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg) const;
    bool operator<(const Exit& rhs) const {
	return (std::tie(    from,     to,     label) <
		std::tie(rhs.from, rhs.to, rhs.label));
    }
};

// TODO: Index states on initial/final attribute, boxes on component.
class Component {
    friend Registry<Component>;
    typedef std::string Key;
private:
    static const boost::regex NAME_REGEX;
public:
    const std::string name;
    const Ref<Component> ref;
    Registry<Box> boxes;
    Index<Table<Transition>,Ref<State>,&Transition::from> transitions;
    MultiIndex<Index<Table<Entry>,Ref<Box>,&Entry::to>,
	       PtrIndex<PtrTable<Entry>,Ref<State>,&Entry::from>> entries;
    Index<Table<Exit>,Ref<Box>,&Exit::from> exits;
private:
    Registry<State> states;
    Ref<State> initial = Ref<State>::none();
    std::set<Ref<State>> final;
private:
    explicit Component(const std::string& name, Ref<Component> ref)
	: name(name), ref(ref) {
	assert(boost::regex_match(name, NAME_REGEX));
    }
public:
    const Registry<State>& get_states() const {
	return states;
    }
    const State& add_state(const std::string& name, bool initial, bool final);
    Ref<State> get_initial() const {
	return initial;
    }
    const std::set<Ref<State>>& get_final() const {
	return final;
    }
    bool merge() const {
	return false;
    }
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg,
	       const Registry<Component>& comp_reg) const;
    void summarize(Graph& graph, const Registry<Worker>& workers) const;
    void propagate(Graph& graph) const;
};

class RSM {
public:
    static const std::string FILE_EXTENSION;
public:
    Registry<Symbol> symbols;
    Registry<Component> components;
private:
    Label parse_label(const std::string& str);
public:
    void parse_dir(const std::string& dirname);
    void parse_file(const fs::path& fpath);
    void print(std::ostream& os) const;
    void propagate(Graph& graph) const;
};

// GRAPH ======================================================================

class Node {
    friend Registry<Node>;
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Node> ref;
private:
    explicit Node(const std::string& name, Ref<Node> ref)
	: name(name), ref(ref) {}
public:
    bool merge() const {
	return false;
    }
};

class Tag {
    friend Registry<Tag>;
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Tag> ref;
private:
    explicit Tag(const std::string& name, Ref<Tag> ref)
	: name(name), ref(ref) {}
public:
    bool merge() const {
	return false;
    }
};

// TODO: Separate class for parametric and non-parametric edges.
class Edge {
public:
    const Ref<Node> src;
    const Ref<Node> dst;
    const Ref<Symbol> symbol;
    const Ref<Tag> tag;
public:
    explicit Edge(Ref<Node> src, Ref<Node> dst, Ref<Symbol> symbol,
		  Ref<Tag> tag)
	: src(src), dst(dst), symbol(symbol), tag(tag) {}
    bool operator<(const Edge& rhs) const {
	return (std::tie(    src,     dst,     symbol,     tag) <
		std::tie(rhs.src, rhs.dst, rhs.symbol, rhs.tag));
    }
};

class Summary {
public:
    const Ref<Node> src;
    const Ref<Node> dst;
    const Ref<Component> comp;
public:
    explicit Summary(Ref<Node> src, Ref<Node> dst, Ref<Component> comp)
	: src(src), dst(dst), comp(comp) {}
    bool operator<(const Summary& rhs) const {
	return (std::tie(    src,     dst,     comp) <
		std::tie(rhs.src, rhs.dst, rhs.comp));
    }
};

// TODO: Define as nested class of Edge?
class Pattern {
public:
    const Ref<Node> src;
    const Ref<Node> dst;
    const Ref<Symbol> symbol;
    const bool match_tag;
    const Ref<Tag> tag;
public:
    explicit Pattern(const Label& l, Ref<Node> base = Ref<Node>::none())
	: src((l.dir == Direction::FWD) ? base : Ref<Node>::none()),
	  dst((l.dir == Direction::FWD) ? Ref<Node>::none() : base),
	  symbol(l.symbol), match_tag(l.tagged), tag(Ref<Tag>::none()) {}
    // Not continuing the matching from the previous edge.
    // TODO: Could decompose into:
    // - Match Pattern::apply(const Edge& e);
    // - Pattern Match::combine(const Label& l, Ref<Node> base);
    // TODO: Could do this in one step, when matching?
    explicit Pattern(const Pattern& prev, const Edge& e, const Label& l,
		     Ref<Node> base = Ref<Node>::none())
	: src((l.dir == Direction::FWD) ? base : Ref<Node>::none()),
	  dst((l.dir == Direction::FWD) ? Ref<Node>::none() : base),
	  symbol(l.symbol), match_tag(l.tagged),
	  tag(prev.match_tag ? e.tag : prev.tag) {
	assert(prev.matches(e));
    }
    bool matches(const Edge& e) const;
};

// TODO: Disallow adding edges while an iterator is live.
class Graph {
public:
    static const std::string FILE_EXTENSION;
public:
    // TODO: Should make some of these private?
    Registry<Node> nodes;
    Registry<Tag> tags;
    Index<MultiIndex<Index<Table<Edge>,Ref<Node>,&Edge::src>,
		     PtrIndex<PtrTable<Edge>,Ref<Node>,&Edge::dst>,
		     PtrIndex<PtrTable<Edge>,Ref<Tag>,&Edge::tag>>,
	  Ref<Symbol>,&Edge::symbol> edges;
    Index<Index<Table<Summary>,Ref<Node>,&Summary::src>,
	  Ref<Component>,&Summary::comp> summaries;
private:
    void parse_file(const Symbol& symbol, const fs::path& fpath);
public:
    explicit Graph(const Registry<Symbol>& symbols,
		   const std::string& dirname);
    Table<const Edge*> search(const Pattern& pat) const;
    std::map<Ref<Node>,std::set<Ref<Node>>>
	subpath_bounds(const Label& head_lab, const Label& tail_lab) const;
    void print_stats(std::ostream& os, const Registry<Symbol>& symbols) const;
    void print_summaries(const std::string& dirname,
			 const Registry<Component>& components) const;
};

// SOLVING ====================================================================

class Dependence;

// TODO: alternative name ("Summarizer"? "Propagator"?)
class Worker {
public:

    struct Result {
	std::set<Summary> summaries;
	std::set<Dependence> deps;
    };

private:
    friend Registry<Worker>;
    typedef Ref<Node> Key;
public:
    const Ref<Node> start; // used as primary key, to identify a Worker
    const Ref<Worker> ref;
    const Component& comp;
private:
    std::set<Ref<Node>> tgts; // if empty, any Node is acceptable
private:
    explicit Worker(Ref<Node> start, Ref<Worker> ref, const Component& comp,
		    const std::set<Ref<Node>>& tgts)
	: start(start), ref(ref), comp(comp), tgts(tgts) {
	assert(!tgts.empty());
    }
public:
    explicit Worker(Ref<Node> start, const Component& comp)
	: start(start), ref(Ref<Worker>::none()), comp(comp) {}
    bool merge(const Component& comp, const std::set<Ref<Node>>& new_tgts);
    Result summarize(const Graph& graph) const;
};

// TODO: Should include the component if we support multiple components in SCCs
class Dependence {
public:
    const Ref<Node> start;
    const Ref<Worker> worker;
public:
    explicit Dependence(Ref<Node> start, Ref<Worker> worker)
	: start(start), worker(worker) {}
    bool operator<(const Dependence& rhs) const {
	return (std::tie(    start,     worker) <
		std::tie(rhs.start, rhs.worker));
    }
};

// TODO: Only construct through a 'follow' method, not directly?
class Position {
public:
    const Ref<Node> node;
    const Ref<State> state;
public:
    explicit Position(Ref<Node> node, Ref<State> state)
	: node(node), state(state) {}
    bool operator<(const Position& rhs) const {
	return (std::tie(    node,     state) <
		std::tie(rhs.node, rhs.state));
    }
};

#endif
