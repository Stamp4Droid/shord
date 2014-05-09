#ifndef RSM_HPP
#define RSM_HPP

#include <boost/program_options.hpp>
#include <boost/regex.hpp>
#include <cstring>

#include "util.hpp"

namespace po = boost::program_options;
namespace fs = boost::filesystem;

// TAGS =======================================================================

enum class ParsingMode {NODES, EDGES};

TUPLE_TAG(SYMBOL);
TUPLE_TAG(REV);
TUPLE_TAG(SRC);
TUPLE_TAG(DST);
TUPLE_TAG(TAG);
TUPLE_TAG(COMP);
TUPLE_TAG(STATE);
TUPLE_TAG(CP_FROM);
TUPLE_TAG(CP_TO);
TUPLE_TAG(ST_FROM);
TUPLE_TAG(ST_TO);
TUPLE_TAG(REQD);
TUPLE_TAG(PUSH);

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

class Label {
public:
    const Ref<Symbol> symbol;
    const bool rev;
    const Ref<Tag> tag; // An invalid tag here means "any".
public:
    explicit Label(const Symbol& symbol, bool rev, Ref<Tag> tag)
	: symbol(symbol.ref), rev(rev), tag(tag) {
	EXPECT(!tag.valid() || symbol.parametric);
    }
    static Label parse(const std::string& str, Registry<Symbol>& symbol_reg,
		       Registry<Tag>& tag_reg);
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg,
	       const Registry<Tag>& tag_reg) const;
    bool operator<(const Label& rhs) const {
	return (std::tie(    symbol,     rev,     tag) <
		std::tie(rhs.symbol, rhs.rev, rhs.tag));
    }
};

class MatchLabel {
public:
    const Ref<Symbol> symbol;
    const bool rev;
public:
    explicit MatchLabel(const Symbol& symbol, bool rev)
	: symbol(symbol.ref), rev(rev) {
	EXPECT(symbol.parametric);
    }
    static MatchLabel parse(const std::string& str,
			    Registry<Symbol>& symbol_reg);
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg) const;
    bool operator<(const MatchLabel& rhs) const {
	return (std::tie(    symbol,     rev) <
		std::tie(rhs.symbol, rhs.rev));
    }
};

// SM COMPONENTS ==============================================================

class Component;
class Graph;

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
    const Label label;
public:
    explicit Transition(Ref<State> from, Ref<State> to, const Label& label)
	: from(from), to(to), label(label) {}
    void print(std::ostream& os, const Registry<State>& state_reg,
	       const Registry<Symbol>& symbol_reg,
	       const Registry<Tag>& tag_reg) const;
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
    const MatchLabel label;
public:
    explicit Entry(Ref<State> from, Ref<Box> to, const MatchLabel& label)
	: from(from), to(to), label(label) {}
    void print(std::ostream& os, const Registry<State>& state_reg,
	       const Registry<Box>& box_reg,
	       const Registry<Symbol>& symbol_reg) const;
    bool operator<(const Entry& rhs) const {
	return (std::tie(    from,     to,     label) <
		std::tie(rhs.from, rhs.to, rhs.label));
    }
};

class Exit {
public:
    const Ref<Box> from;
    const Ref<State> to;
    const MatchLabel label;
public:
    explicit Exit(Ref<Box> from, Ref<State> to, const MatchLabel& label)
	: from(from), to(to), label(label) {}
    void print(std::ostream& os, const Registry<State>& state_reg,
	       const Registry<Box>& box_reg,
	       const Registry<Symbol>& symbol_reg) const;
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
    MultiIndex<Index<Table<Transition>,Ref<State>,&Transition::from>,
	       Index<PtrTable<Transition>,Label,&Transition::label>>
	transitions;
    MultiIndex<Index<Table<Entry>,Ref<Box>,&Entry::to>,
	       Index<PtrTable<Entry>,Ref<State>,&Entry::from>,
	       Index<PtrTable<Entry>,MatchLabel,&Entry::label>> entries;
    MultiIndex<Index<Table<Exit>,Ref<Box>,&Exit::from>,
	       Index<PtrTable<Exit>,MatchLabel,&Exit::label>> exits;
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
	       const Registry<Tag>& tag_reg,
	       const Registry<Component>& comp_reg) const;
};

// EFFECT HANDLING ============================================================

class RSM;

class Frame {
public:
    Ref<Component> comp;
    Ref<Box> box;
    Ref<Tag> tag;
public:
    friend int compare(const Frame& lhs, const Frame& rhs) {
	int fld_rel = compare(lhs.comp, rhs.comp);
	if (fld_rel != 0) {
	    return fld_rel;
	}
	fld_rel = compare(lhs.box, rhs.box);
	if (fld_rel != 0) {
	    return fld_rel;
	}
	return compare(lhs.tag, rhs.tag);
    }
    bool operator<(const Frame& rhs) const {
	return compare(*this, rhs) < 0;
    }
    bool operator==(const Frame& rhs) const {
	return compare(*this, rhs) == 0;
    }
    std::size_t hash_code() const {
	return hash(comp, box, tag);
    }
    friend void print(std::ostream& os, const Frame& frame,
		      const Registry<Component>& comp_reg,
		      const Registry<Tag>& tag_reg);
};

typedef FuzzyStack<Frame,2> EfftReqd;
typedef FuzzyStack<Frame,2> EfftPush;

typedef mi::Index<CP_TO, Ref<Component>,
	    mi::Index<ST_TO, Ref<State>,
		mi::Index<PUSH, EfftPush,
		    mi::Index<CP_FROM, Ref<Component>,
			mi::Index<ST_FROM, Ref<State>,
			    mi::Table<REQD, EfftReqd>>>>>> EffectRTL;

typedef mi::Index<CP_FROM, Ref<Component>,
	    mi::Index<ST_FROM, Ref<State>,
		mi::Index<REQD, EfftReqd,
		    mi::Index<CP_TO, Ref<Component>,
			mi::Index<ST_TO, Ref<State>,
			    mi::Table<PUSH, EfftPush>>>>>> EffectLTR;

template<class EfftT>
void print_effect(std::ostream& os, const std::string& prefix,
		  const EfftT& efft, const Registry<Component>& comp_reg,
		  const Registry<Tag>& tag_reg);

bool compose(const EffectRTL& l_efft, const EffectLTR& r_efft,
	     EffectRTL& res_efft, bool fwd_only);

bool copy_trans(const EffectRTL& src, EffectLTR& dst, bool accepting_only,
		const RSM& rsm);

// ANALYSIS SPEC ==============================================================

class RSM {
public:
    static const std::string FILE_EXTENSION;
public:
    Registry<Component> components;
private:
    // If the symbol is not parametric, there will only be one entry, for the
    // invalid tag. Otherwise, there will be a separate entry for each tag.
    // These entries get constructed on the fly.
    // TODO: Perhaps a copy for each separate tag causes blowup?
    // XXX: This gets initialized using the original capacity of the symbol
    // store, however the RSM might later introduce new symbols.
    mutable mi::FlatIndex<REV, bool,
		mi::FlatIndex<SYMBOL, Ref<Symbol>,
		    mi::Index<TAG, Ref<Tag>,
			EffectLTR>>> base_effts;
    EffectRTL id_efft_;
private:
    void parse_file(const fs::path& fpath, Registry<Symbol>& symbol_reg,
		    Registry<Tag>& tag_reg);
public:
    explicit RSM(const std::string& dirname, Registry<Symbol>& symbol_reg,
		 Registry<Tag>& tag_reg);
    const EffectLTR& effect_of(const Symbol& symbol, bool rev,
			       Ref<Tag> tag) const;
    const EffectRTL& id_efft() const {
	return id_efft_;
    }
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg,
	       const Registry<Tag>& tag_reg) const;
};

class Analysis {
public:
    Registry<Symbol> symbols;
    Registry<Tag> tags;
    RSM pri;
    RSM sec; // TODO: Exactly one secondary RSM for now
private:
    void summarize(Graph& graph, const Component& comp) const;
    void propagate(Graph& graph, const Component& comp) const;
public:
    explicit Analysis(const std::string& pri_dname,
		      const std::string& sec_dname)
	: pri(pri_dname, symbols, tags), sec(sec_dname, symbols, tags) {}
    void print(std::ostream& os) const;
    void close(Graph& graph) const;
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

// TODO:
// - Disallow adding edges while an iterator is live.
// - Additional indexing dimensions.
// - Should make additional fields private?
class Graph {
public:
    typedef mi::Index<TAG, Ref<Tag>,
		mi::Table<DST, Ref<Node>>>
	SrcLabelSlice;
    typedef mi::FlatIndex<REV, bool,
		mi::FlatIndex<SRC, Ref<Node>,
		    mi::LightIndex<SYMBOL, Ref<Symbol>,
			SrcLabelSlice>>>
	EdgesSrcLabelIndex;
    typedef mi::Index<TAG, Ref<Tag>,
		mi::Index<SRC, Ref<Node>,
		    mi::Table<DST, Ref<Node>>>>
	LabelSlice;
    typedef mi::FlatIndex<REV, bool,
		mi::FlatIndex<SYMBOL, Ref<Symbol>,
		    LabelSlice>>
	EdgesLabelIndex;
    typedef mi::Index<DST, Ref<Node>, EffectLTR> WorkerSummaries;
    typedef mi::Index<COMP, Ref<Component>,
		mi::Index<SRC, Ref<Node>,
		    WorkerSummaries>>
	SummaryStore;
public:
    static const std::string FILE_EXTENSION;
public:
    Registry<Node> nodes;
    // TODO:
    // - Separate indices increases the chances that we'll forget to apply all
    //   modifications on both.
    // - Need to know the number of nodes before we can instantiate the outer
    //   FlatIndex, so we can't store it by value.
    EdgesSrcLabelIndex* edges_1 = NULL;
    EdgesLabelIndex* edges_2 = NULL;
    SummaryStore summaries;
private:
    void parse_file(const Symbol& symbol, const fs::path& fpath,
		    ParsingMode mode, Registry<Tag>& tag_reg);
public:
    explicit Graph(const Registry<Symbol>& symbol_reg, Registry<Tag>& tag_reg,
		   const std::string& dirname);
    Graph(const Graph& rhs) = delete;
    Graph& operator=(const Graph& rhs) = delete;
    ~Graph() {
	delete edges_1;
	delete edges_2;
    }
    std::map<Ref<Node>,std::set<Ref<Node>>>
	subpath_bounds(const MatchLabel& hd_lab,
		       const MatchLabel& tl_lab) const;
    const LabelSlice& search(Ref<Symbol> symbol, bool rev) const {
	return (*edges_2)[rev][symbol];
    }
    const SrcLabelSlice& search(Ref<Symbol> symbol, bool rev,
				Ref<Node> src) const {
	return (*edges_1)[rev][src][symbol];
    }
    void print_stats(std::ostream& os) const;
    void print_summaries(const std::string& dirname,
			 const Registry<Component>& pri_comp_reg,
			 const Registry<Component>& sec_comp_reg,
			 const Registry<Tag>& tag_reg) const;
};

// SOLVING ====================================================================

// TODO: alternative name ("Summarizer"? "Propagator"?)
class Worker {
public:

    struct Result {
	typename Graph::WorkerSummaries summs;
	std::set<Ref<Node>> deps;
    };

private:
    friend Registry<Worker>;
    typedef Ref<Node> Key;
public:
    const Ref<Node> start; // used as primary key, to identify a Worker
    const Ref<Worker> ref;
    const Component& comp;
    const bool top_level; // TODO: Make this into a template parameter.
private:
    std::set<Ref<Node>> tgts;
private:
    explicit Worker(Ref<Node> start, Ref<Worker> ref, const Component& comp,
		    const std::set<Ref<Node>>& tgts)
	: start(start), ref(ref), comp(comp), top_level(false), tgts(tgts) {
	assert(!tgts.empty());
    }
    bool merge(const Component& comp, const std::set<Ref<Node>>& new_tgts);
public:
    explicit Worker(Ref<Node> start, const Component& comp)
	: start(start), comp(comp), top_level(true) {}
    Result handle(const Graph& graph, const Registry<Symbol>& symbol_reg,
		  const RSM& sec) const;
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

// A summary normally contains a single transition over the secondary RSM, but
// we track them in bulk during summarization (for efficiency). Thus, a
// Position object only covers the rest of the required fields.
// TODO: Only construct through a 'follow' method, not directly?
struct Position {
    Ref<Node> dst;
    Ref<State> state;
};

// A single Position can carry a set of RSM effects, so we prefer to schedule
// all available effects at once. However, a new set of effects can reach a
// node+state, so we have to reschedule at least the added effects. We're not
// currently tracking the difference set (we expect such updated to happen
// infrequently) (TODO: try this), and simply reschedule the entire set of
// effects. Therefore, we only need to store the node+state on the worklist,
// and retrieve the effects when dequeing. One problem is that we might
// duplicate work (TODO: guard against this).
class WorkerWorklist {
private:
    const bool fwd_only;
    mi::Index<DST, Ref<Node>,
	mi::Index<STATE, Ref<State>,
	    EffectRTL>> reached;
    std::deque<Position> queue;
public:
    WorkerWorklist(bool fwd_only) : fwd_only(fwd_only) {}
    bool empty() const {
	return queue.empty();
    }
    bool enqueue(Ref<Node> dst, Ref<State> state, const EffectRTL& efft) {
	// TODO: Check that the provided effect agrees with our directionality.
	if (reached.copy(efft, dst, state)) {
	    queue.push_back(Position{dst, state});
	    return true;
	}
	return false;
    }
    bool enqueue(Ref<Node> dst, Ref<State> state, const EffectRTL& l_efft,
		 const EffectLTR& r_efft) {
	if (compose(l_efft, r_efft, reached.follow(dst).follow(state),
		    fwd_only)) {
	    queue.push_back(Position{dst, state});
	    return true;
	}
	return false;
    }
    Position dequeue() {
	Position res = queue.front();
	queue.pop_front();
	return res;
    }
    const EffectRTL& effect_at(const Position& pos) const {
	return reached[pos.dst][pos.state];
    }
};

#endif
