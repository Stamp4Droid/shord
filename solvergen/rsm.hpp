#ifndef RSM_HPP
#define RSM_HPP

#include <boost/program_options.hpp>
#include <boost/regex.hpp>
#include <cstring>

#include "util.hpp"

namespace po = boost::program_options;
namespace fs = boost::filesystem;

// ALPHABET ===================================================================

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
	EXPECT(boost::regex_match(name, NAME_REGEX));
    }
public:
    bool merge(bool parametric) const {
	EXPECT(parametric == this->parametric);
	return false;
    }
};

template<bool Tagged> class Label {
public:
    const Ref<Symbol> symbol;
    const bool rev;
public:
    explicit Label(const Symbol& symbol, bool rev) : symbol(symbol.ref),
						     rev(rev) {
	EXPECT(!Tagged || symbol.parametric);
    }
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg) const;
    bool operator<(const Label& rhs) const {
	return (std::tie(    symbol,     rev) <
		std::tie(rhs.symbol, rhs.rev));
    }
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
	EXPECT(initial == this->initial && final == this->final);
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
	EXPECT(comp == this->comp);
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
    const Label<false> label;
public:
    explicit Transition(Ref<State> from, Ref<State> to, Label<false> label)
	: from(from), to(to), label(label) {}
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg) const;
    bool operator<(const Transition& rhs) const {
	return (std::tie(    from,     to,     label) <
		std::tie(rhs.from, rhs.to, rhs.label));
    }
};

// TODO:
// - Allow non-parametric and untagged parametric symbols. Should then also
//   enforce that either all entries and exits to the same box are tagged, or
//   none is.
// - Can't move from a box directly to another box.
class Entry {
public:
    const Ref<State> from;
    const Ref<Box> to;
    const Label<true> label;
public:
    explicit Entry(Ref<State> from, Ref<Box> to, Label<true> label)
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
    const Label<true> label;
public:
    explicit Exit(Ref<Box> from, Ref<State> to, Label<true> label)
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
	       Index<PtrTable<Entry>,Ref<State>,&Entry::from>> entries;
    Index<Table<Exit>,Ref<Box>,&Exit::from> exits;
private:
    Registry<State> states;
    Ref<State> initial = Ref<State>::none();
    std::set<Ref<State>> final;
private:
    explicit Component(const std::string& name, Ref<Component> ref)
	: name(name), ref(ref) {
	EXPECT(boost::regex_match(name, NAME_REGEX));
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
    template<bool Tagged> Label<Tagged> parse_label(const std::string& str);
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
    const bool rev;
    const Ref<Tag> tag;
public:
    explicit Edge(Ref<Node> src, Ref<Node> dst, const Symbol& symbol, bool rev,
		  Ref<Tag> tag)
	: src(src), dst(dst), symbol(symbol.ref), rev(rev), tag(tag) {
	EXPECT(!symbol.parametric ^ tag.valid());
    }
    bool operator<(const Edge& rhs) const {
	return (std::tie(    src,     dst,     symbol,     rev,     tag) <
		std::tie(rhs.src, rhs.dst, rhs.symbol, rhs.rev, rhs.tag));
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

// TODO: Disallow adding edges while an iterator is live.
class Graph {
    typedef Index<Table<Edge>,Ref<Tag>,&Edge::tag> SrcLabelSlice;
    typedef Index<PtrTable<Edge>,Ref<Tag>,&Edge::tag> LabelSlice;
public:
    static const std::string FILE_EXTENSION;
public:
    // TODO: Should make some of these private?
    Registry<Node> nodes;
    Registry<Tag> tags;
    Index<MultiIndex<
	      FlatIndex<FlatIndex<Index<Table<Edge>,
					Ref<Tag>,&Edge::tag>,
				  Symbol,&Edge::symbol>,
			Node,&Edge::src>,
	      Index<Index<PtrTable<Edge>,
			  Ref<Tag>,&Edge::tag>,
		    Ref<Symbol>,&Edge::symbol>>,
	  bool,&Edge::rev> edges;
    Index<Index<Table<Summary>,Ref<Node>,&Summary::src>,
	  Ref<Component>,&Summary::comp> summaries;
private:
    void parse_file(const Symbol& symbol, const fs::path& fpath);
public:
    explicit Graph(const Registry<Symbol>& symbols,
		   const std::string& dirname);
    std::map<Ref<Node>,std::set<Ref<Node>>>
	subpath_bounds(const Label<true>& hd_lab,
		       const Label<true>& tl_lab) const;
    template<bool Tagged>
    const LabelSlice& search(const Label<Tagged>& label) const {
	return edges[label.rev].template secondary<0>()[label.symbol];
    }
    template<bool Tagged>
    const SrcLabelSlice& search(Ref<Node> src,
				const Label<Tagged>& label) const {
	return edges[label.rev].primary()[src][label.symbol];
    }
    void print_stats(std::ostream& os) const;
    void print_summaries(const std::string& dirname,
			 const Registry<Component>& components) const;
};

// SOLVING ====================================================================

class Dependence;
class SummaryWorklist;

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
	EXPECT(!tgts.empty());
    }
public:
    explicit Worker(Ref<Node> start, const Component& comp)
	: start(start), ref(Ref<Worker>::none()), comp(comp) {}
    bool merge(const Component& comp, const std::set<Ref<Node>>& new_tgts);
    Result summarize(const Graph& graph, SummaryWorklist& worklist) const;
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

class TwoDimBitSet {
private:
    const unsigned int size;
    unsigned short* shorts;
public:
    TwoDimBitSet(unsigned int pri_size, unsigned int sec_size)
	: size(pri_size * sizeof(unsigned short)),
	  shorts(new unsigned short[pri_size]) {
	EXPECT(sizeof(unsigned short) * 8 >= sec_size);
	clear();
    }
    ~TwoDimBitSet() {
	delete[] shorts;
    }
    void set(unsigned int i, unsigned int j) {
	// no bounds check
	shorts[i] |= 1 << j;
    }
    bool test(unsigned int i, unsigned int j) {
	// no bounds check
	return shorts[i] & (1 << j);
    }
    void clear() {
	memset(shorts, 0, size);
    }
};

class SummaryWorklist {
private:
    TwoDimBitSet reached;
    std::deque<Position> queue;
public:
    explicit SummaryWorklist(unsigned int nodes, unsigned int states)
	: reached(nodes, states) {}
    bool empty() const {
	return queue.empty();
    }
    bool enqueue(const Position& pos) {
	if (!reached.test(pos.node.value, pos.state.value)) {
	    reached.set(pos.node.value, pos.state.value);
	    queue.push_back(pos);
	    return true;
	}
	return false;
    }
    Position dequeue() {
	Position res = queue.front();
	queue.pop_front();
	return res;
    }
    void clear() {
	reached.clear();
	queue.clear();
    }
};

#endif
