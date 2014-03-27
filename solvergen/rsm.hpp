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
    bool merge(bool initial, bool final) const {
	EXPECT(initial == this->initial && final == this->final);
	return false;
    }
public:
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
    bool merge(Ref<Component> comp) const {
	EXPECT(comp == this->comp);
	return false;
    }
public:
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
    Ref<State> initial;
    std::set<Ref<State>> final;
private:
    explicit Component(const std::string& name, Ref<Component> ref)
	: name(name), ref(ref) {
	EXPECT(boost::regex_match(name, NAME_REGEX));
    }
    bool merge() const {
	return false;
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
    bool merge() const {
	return false;
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

enum class ParsingMode {NODES, EDGES};
TUPLE_TAG(SYMBOL);
TUPLE_TAG(REV);
TUPLE_TAG(SRC);
TUPLE_TAG(DST);
TUPLE_TAG(TAG);

// TODO:
// - Disallow adding edges while an iterator is live.
// - Additional indexing dimensions.
class Graph {
public:
    typedef mi::Index<TAG, Ref<Tag>,
		mi::Table<DST, Ref<Node>>
	    > SrcLabelSlice;
    typedef mi::FlatIndex<REV, bool,
		mi::FlatIndex<SRC, Ref<Node>,
		    mi::LightIndex<SYMBOL, Ref<Symbol>,
			SrcLabelSlice>>
	    > EdgesSrcLabelIndex;
    typedef mi::Index<TAG, Ref<Tag>,
		mi::Index<SRC, Ref<Node>,
		    mi::Table<DST, Ref<Node>>>
	    > LabelSlice;
    typedef mi::FlatIndex<REV, bool,
		mi::FlatIndex<SYMBOL, Ref<Symbol>,
		    LabelSlice>
	    > EdgesLabelIndex;
public:
    static const std::string FILE_EXTENSION;
public:
    // TODO: Should make some of these private?
    Registry<Node> nodes;
    Registry<Tag> tags;
    // TODO:
    // - Separate indices increases the chances that we'll forget to apply all
    //   modifications on both.
    // - Need to know the number of nodes before we can instantiate the outer
    //   FlatIndex, so we can't store it by value.
    EdgesSrcLabelIndex* edges_1 = NULL;
    EdgesLabelIndex* edges_2 = NULL;
    Index<Index<Table<Summary>,Ref<Node>,&Summary::src>,
	  Ref<Component>,&Summary::comp> summaries;
private:
    void parse_file(const Symbol& symbol, const fs::path& fpath,
		    ParsingMode mode);
public:
    explicit Graph(const Registry<Symbol>& symbols,
		   const std::string& dirname);
    Graph(const Graph& rhs) = delete;
    Graph& operator=(const Graph& rhs) = delete;
    ~Graph() {
	delete edges_1;
	delete edges_2;
    }
    std::map<Ref<Node>,std::set<Ref<Node>>>
	subpath_bounds(const Label<true>& hd_lab,
		       const Label<true>& tl_lab) const;
    template<bool Tagged>
    const LabelSlice& search(const Label<Tagged>& label) const {
	return (*edges_2)[label.rev][label.symbol];
    }
    template<bool Tagged>
    const SrcLabelSlice& search(Ref<Node> src,
				const Label<Tagged>& label) const {
	return (*edges_1)[label.rev][src][label.symbol];
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
    bool merge(const Component& comp, const std::set<Ref<Node>>& new_tgts);
public:
    explicit Worker(Ref<Node> start, const Component& comp)
	: start(start), comp(comp) {}
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

TUPLE_TAG(NODE);
TUPLE_TAG(STATE);

class SummaryWorklist {
private:
    mi::FlatIndex<NODE, Ref<Node>,
		  mi::BitSet<STATE, Ref<State>>> reached;
    std::deque<Position> queue;
public:
    explicit SummaryWorklist(const Registry<Node>& nodes,
			     const Registry<State>& states)
	: reached(nodes, states) {}
    bool empty() const {
	return queue.empty();
    }
    bool enqueue(const Position& pos) {
	if (reached.insert(pos.node, pos.state)) {
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
};

#endif
