#include <boost/algorithm/string.hpp>
#include <fstream>
#include <iomanip>
#include <list>
#include <locale>
#include <sstream>

#include "rsm.hpp"

// ALPHABET ===================================================================

const boost::regex Symbol::NAME_REGEX("[a-z]\\w*");

Label Label::parse(const std::string& str, Registry<Symbol>& symbol_reg,
		   Registry<Tag>& tag_reg) {
    static const boost::regex r("(_)?([a-z]\\w*)(?:\\[(\\w*|\\*)\\])?");
    boost::smatch m;
    bool matched = boost::regex_match(str, m, r);
    EXPECT(matched);

    bool rev = m[1].matched;
    std::string symbol_name(m[2].first, m[2].second);
    std::string tag_name(m[3].first, m[3].second);
    bool parametric = m[3].matched;
    const Symbol& symbol = symbol_reg.add(symbol_name, parametric);
    Ref<Tag> tag;
    if (parametric && tag_name != "*") {
	tag = tag_reg.add(tag_name).ref;
    }

    return Label(symbol, rev, tag);
}

void Label::print(std::ostream& os, const Registry<Symbol>& symbol_reg,
		  const Registry<Tag>& tag_reg) const {
    if (rev) {
	os << "_";
    }
    os << symbol_reg[symbol].name
       << "[" << (tag.valid() ? tag_reg[tag].name : "*") << "]";
}

MatchLabel MatchLabel::parse(const std::string& str,
			     Registry<Symbol>& symbol_reg) {
    static const boost::regex r("(_)?([a-z]\\w*)\\[[a-z]\\]");
    boost::smatch m;
    bool matched = boost::regex_match(str, m, r);
    EXPECT(matched);

    bool rev = m[1].matched;
    std::string symbol_name(m[2].first, m[2].second);
    const Symbol& symbol = symbol_reg.add(symbol_name, true);

    return MatchLabel(symbol, rev);
}

void MatchLabel::print(std::ostream& os,
		       const Registry<Symbol>& symbol_reg) const {
    if (rev) {
	os << "_";
    }
    os << symbol_reg[symbol].name << "[i]";
}

// SM COMPONENTS ==============================================================

void State::print(std::ostream& os) const {
    os << name;
    if (initial) {
	os << " in";
    }
    if (final) {
	os << " out";
    }
    os << std::endl;
}

void Box::print(std::ostream& os, const Registry<Component>& comp_reg) const {
    os << name << " " << comp_reg[comp].name << std::endl;
}

void Transition::print(std::ostream& os, const Registry<State>& state_reg,
		       const Registry<Symbol>& symbol_reg,
		       const Registry<Tag>& tag_reg) const {
    os << state_reg[from].name << " " << state_reg[to].name << " ";
    label.print(os, symbol_reg, tag_reg);
    os << std::endl;
}

void Entry::print(std::ostream& os, const Registry<State>& state_reg,
		  const Registry<Box>& box_reg,
		  const Registry<Symbol>& symbol_reg) const {
    os << state_reg[from].name << " " << box_reg[to].name << " ";
    label.print(os, symbol_reg);
    os << std::endl;
}

void Exit::print(std::ostream& os, const Registry<State>& state_reg,
		 const Registry<Box>& box_reg,
		 const Registry<Symbol>& symbol_reg) const {
    os << box_reg[from].name << " " << state_reg[to].name << " ";
    label.print(os, symbol_reg);
    os << std::endl;
}

const boost::regex Component::NAME_REGEX("[A-Z]\\w*");

const State& Component::add_state(const std::string& name, bool initial,
				  bool final) {
    State& s = states.make(name, initial, final);
    if (initial) {
	EXPECT(!this->initial.valid());
	this->initial = s.ref;
    }
    if (final) {
	this->final.insert(s.ref);
    }
    return s;
}

void Component::print(std::ostream& os, const Registry<Symbol>& symbol_reg,
		      const Registry<Tag>& tag_reg,
		      const Registry<Component>& comp_reg) const {
    for (const State& s : states) {
	s.print(os);
    }
    for (const Box& b : boxes) {
	b.print(os, comp_reg);
    }
    os << "#" << std::endl;
    for (const Transition& t : transitions) {
	t.print(os, states, symbol_reg, tag_reg);
    }
    for (const Entry& e : entries) {
	e.print(os, states, boxes, symbol_reg);
    }
    for (const Exit& e : exits) {
	e.print(os, states, boxes, symbol_reg);
    }
}

// ANALYSIS SPEC ==============================================================

const std::string RSM::FILE_EXTENSION(".rsm.tgf");

RSM::RSM(const std::string& dirname, Registry<Symbol>& symbol_reg,
	 Registry<Tag>& tag_reg) {
    Directory dir(dirname);
    std::list<fs::path> files(dir.begin(), dir.end());
    files.sort();
    for (const fs::path& fpath : files) {
	parse_file(fpath, symbol_reg, tag_reg);
    }
}

void parse_component(const fs::path& fpath, Component& comp,
		     Registry<Symbol>& symbol_reg, Registry<Tag>& tag_reg,
		     const Registry<Component>& comp_reg) {
    std::ifstream fin(fpath.string());
    EXPECT(fin);
    ParsingMode mode = ParsingMode::NODES;
    std::string line;

    while (std::getline(fin, line)) {
	if (line.empty()) {
	    continue; // Empty lines are ignored.
	}
	std::vector<std::string> toks;
	boost::split(toks, line, boost::is_any_of(" "),
		     boost::token_compress_on);

	switch (mode) {
	case ParsingMode::NODES: {
	    // Nodes serve as both states and boxes. Therefore, their names
	    // must not clash.
	    if (toks[0] == "#") {
		// Verify that the component has a single initial state, and at
		// least one final state.
		EXPECT(comp.get_initial().valid());
		EXPECT(!comp.get_final().empty());
		mode = ParsingMode::EDGES;
		break;
	    }
	    bool can_be_state = true, initial = false, final = false;
	    bool can_be_box = true;
	    Ref<Component> box_rsm;
	    for (auto iter = toks.begin() + 1; iter != toks.end(); ++iter) {
		if (*iter == "in") {
		    EXPECT(can_be_state);
		    can_be_box = false;
		    initial = true;
		} else if (*iter == "out") {
		    EXPECT(can_be_state);
		    can_be_box = false;
		    final = true;
		} else {
		    EXPECT(can_be_box);
		    can_be_state = false;
		    EXPECT(!box_rsm.valid());
		    // The referenced component must be present in the RSM.
		    // TODO: This means we only allow self-recursion.
		    box_rsm = comp_reg.find(*iter).ref;
		}
	    }
	    if (can_be_state) {
		EXPECT(!comp.boxes.contains(toks[0]));
		EXPECT(!box_rsm.valid());
		comp.add_state(toks[0], initial, final);
	    } else {
		EXPECT(!comp.get_states().contains(toks[0]));
		EXPECT(can_be_box && box_rsm.valid());
		comp.boxes.make(toks[0], box_rsm);
	    }
	} break;

	case ParsingMode::EDGES: {
	    // We are certain that box names and transition names are disjoint.
	    EXPECT(toks.size() >= 3);
	    std::list<std::string> label_toks(toks.begin() + 2, toks.end());
	    bool src_is_state = comp.get_states().contains(toks[0]);
	    bool dst_is_state = comp.get_states().contains(toks[1]);
	    EXPECT(src_is_state || dst_is_state);

	    if (src_is_state && dst_is_state) {
		Ref<State> src = comp.get_states().find(toks[0]).ref;
		Ref<State> dst = comp.get_states().find(toks[1]).ref;
		for (const std::string& t : label_toks) {
		    Label lab = Label::parse(t, symbol_reg, tag_reg);
		    comp.transitions.insert(Transition(src, dst, lab));
		}
	    } else if (src_is_state && !dst_is_state) {
		Ref<State> src = comp.get_states().find(toks[0]).ref;
		Ref<Box> dst = comp.boxes.find(toks[1]).ref;
		for (const std::string& t : label_toks) {
		    MatchLabel lab = MatchLabel::parse(t, symbol_reg);
		    comp.entries.insert(Entry(src, dst, lab));
		}
	    } else if (!src_is_state && dst_is_state) {
		Ref<Box> src = comp.boxes.find(toks[0]).ref;
		Ref<State> dst = comp.get_states().find(toks[1]).ref;
		for (const std::string& t : label_toks) {
		    MatchLabel lab = MatchLabel::parse(t, symbol_reg);
		    comp.exits.insert(Exit(src, dst, lab));
		}
	    }
	} break;

	default:
	    assert(false);
	}
    }

    EXPECT(fin.eof());
    EXPECT(mode == ParsingMode::EDGES);
}

void RSM::parse_file(const fs::path& fpath, Registry<Symbol>& symbol_reg,
		     Registry<Tag>& tag_reg) {
    std::string fbase(fpath.filename().string());
    if (!boost::algorithm::ends_with(fbase, FILE_EXTENSION)) {
	return;
    }
    std::cout << "Parsing " << fbase << std::endl;
    size_t name_len = fbase.size() - FILE_EXTENSION.size();
    std::string name(fbase.substr(0, name_len));
    Component& comp = components.make(name);
    parse_component(fpath, comp, symbol_reg, tag_reg, components);
}

void RSM::print(std::ostream& os, const Registry<Symbol>& symbol_reg,
		const Registry<Tag>& tag_reg) const {
    for (const Component& comp : components) {
	os << comp.name << ":" << std::endl;
	comp.print(os, symbol_reg, tag_reg, components);
    }
}

const std::string FSM::FILE_EXTENSION(".fsm.tgf");

FSM::FSM(const std::string& fname, Registry<Symbol>& symbol_reg,
	 Registry<Tag>& tag_reg)
    : base_effects(boost::none, symbol_reg) {
    fs::path fpath(fname);
    std::string fbase(fpath.filename().string());
    EXPECT(boost::algorithm::ends_with(fbase, FILE_EXTENSION));

    std::cout << "Parsing " << fbase << std::endl;
    unsigned int old_sz = symbol_reg.size();
    parse_component(fpath, comp, symbol_reg, tag_reg, Registry<Component>());
    // HACK: Don't allow new symbols on the secondary machine.
    // TODO: Also check that it covers all symbols from the primary one.
    EXPECT(old_sz == symbol_reg.size());
    // Disallow recursion.
    EXPECT(comp.simple());

    std::cout << "Calculating base effects" << std::endl;
    mi::FlatIndex<REV, bool,
	mi::FlatIndex<SYMBOL, Ref<Symbol>,
	    mi::Index<TAG, Ref<Tag>,
		FsmEffect>>> non_uniqd_effects(boost::none, symbol_reg);
    for (const Transition& t : comp.transitions) {
	non_uniqd_effects.insert(t.label.rev, t.label.symbol, t.label.tag,
				 t.from, t.to);
    }
    base_effects.copy(non_uniqd_effects);
}

bool FSM::is_accepting(const TransRel& trel) const {
    FOR(tup, trel[comp.get_initial()]) {
	if (comp.get_final().count(tup.get<F_TO>()) > 0) {
	    return true;
	}
    }
    return false;
}

void FSM::print_effects(std::ostream& os, const Symbol& s, bool rev,
			const Registry<Symbol>& symbol_reg,
			const Registry<Tag>& tag_reg) const {
    for (Ref<Tag> tag : base_effects[rev][s.ref]) {
	Label(s, rev, tag).print(os, symbol_reg, tag_reg);
	os << ":" << std::endl;
	FOR(trans, base_effects[rev][s.ref][tag]) {
	    os << "  " << comp.get_states()[trans.get<F_FROM>()].name << " "
	       << comp.get_states()[trans.get<F_TO>()].name << std::endl;
	}
    }
}

void FSM::print(std::ostream& os, const Registry<Symbol>& symbol_reg,
		const Registry<Tag>& tag_reg) const {
    os << "FSM:" << std::endl;
    comp.print(os, symbol_reg, tag_reg, Registry<Component>());
    os << "Base effects:" << std::endl;
    for (const Symbol& s : symbol_reg) {
	print_effects(os, s, false, symbol_reg, tag_reg);
    }
}

TransRel compose(const TransRel& trel1, const TransRel& trel2) {
    return mi::join(trel1, trel2);
}

void Analysis::print(std::ostream& os) const {
    rsm.print(os, symbols, tags);
    fsm.print(os, symbols, tags);
}

// GRAPH ======================================================================

const std::string Graph::FILE_EXTENSION(".dat");

// TODO: Two passes over the files.
void Graph::parse_file(const Symbol& symbol, const fs::path& fpath,
		       ParsingMode mode, Registry<Tag>& tag_reg) {
    std::ifstream fin(fpath.string());
    EXPECT(fin);
    std::string line;

    while (std::getline(fin, line)) {
	if (line.empty()) {
	    continue; // Empty lines are ignored.
	}
	std::vector<std::string> toks;
	boost::split(toks, line, boost::is_any_of(" "),
		     boost::token_compress_on);

	switch (mode) {
	case ParsingMode::NODES: {
	    EXPECT(toks.size() >= 2);
	    nodes.add(toks[0]);
	    nodes.add(toks[1]);
	} break;
	case ParsingMode::EDGES: {
	    Ref<Tag> tag;
	    if (symbol.parametric) {
		EXPECT(toks.size() == 3);
		tag = tag_reg.add(toks[2]).ref;
	    } else {
		EXPECT(toks.size() == 2);
	    }
	    Ref<Node> src = nodes.find(toks[0]).ref;
	    Ref<Node> dst = nodes.find(toks[1]).ref;
	    // Add the edge twice, once forwards and once backwards.
	    // TODO: This simplifies the interface, but also increases space
	    // requirements (but doesn't necessarily double them; if we didn't
	    // do this we'd need to keep an index on the destination).
	    edges_1->insert(false, src, symbol.ref, tag, dst);
	    edges_1->insert(true, dst, symbol.ref, tag, src);
	    edges_2->insert(false, symbol.ref, tag, src, dst);
	    edges_2->insert(true, symbol.ref, tag, dst, src);
	} break;
	default:
	    assert(false);
	}
    }

    EXPECT(fin.eof());
}

Graph::Graph(const Registry<Symbol>& symbol_reg, Registry<Tag>& tag_reg,
	     const std::string& dirname) {
    fs::path dirpath(dirname);
    std::cout << "Parsing nodes" << std::endl;
    for (const Symbol& s : symbol_reg) {
	std::string fname = s.name + FILE_EXTENSION;
	// Will fail if some symbol is missing its Edge file.
	parse_file(s, dirpath/fname, ParsingMode::NODES, tag_reg);
    }
    edges_1 = new EdgesSrcLabelIndex(boost::none, nodes);
    edges_2 = new EdgesLabelIndex(boost::none, symbol_reg);
    std::cout << "Parsing edges" << std::endl;
    for (const Symbol& s : symbol_reg) {
	std::string fname = s.name + FILE_EXTENSION;
	parse_file(s, dirpath/fname, ParsingMode::EDGES, tag_reg);
    }
}

void Graph::print_stats(std::ostream& os) const {
    os << "Nodes: " << nodes.size() << std::endl;
    os << "Edges: " << (*edges_1)[false].size() << std::endl;
}

void Graph::print_summaries(const std::string& dirname,
			    const Registry<Component>& comp_reg,
			    const FSM& fsm) const {
    fs::path dirpath(dirname);
    for (const Component& comp : comp_reg) {
	std::string fname = comp.name + FILE_EXTENSION;
	std::cout << "Printing " << fname << std::endl;
	std::ofstream fout((dirpath/fname).string());
	EXPECT(fout);
	FOR(s, summaries[comp.ref]) {
	    fout << nodes[s.get<SRC>()].name << " "
		 << nodes[s.get<DST>()].name << " "
		 << fsm.comp.get_states()[s.get<F_FROM>()].name << " "
		 << fsm.comp.get_states()[s.get<F_TO>()].name << std::endl;
	}
    }
}

// SOLVING ====================================================================

// TODO:
// - Don't produce the output by value.
std::map<Ref<Node>,std::set<Ref<Node>>>
Graph::subpath_bounds(const MatchLabel& hd_lab,
		      const MatchLabel& tl_lab) const {
    std::map<Ref<Node>,std::set<Ref<Node>>> res;
    FOR(hd, search(hd_lab)) {
	FOR(tl, search(tl_lab)[hd.get<TAG>()]) {
	    res[hd.get<DST>()].insert(tl.get<SRC>());
	}
    }
    return res;
}

void Analysis::close(Graph& graph) const {
    // Components are processed in order of addition, which is guaranteed to be
    // a valid bottom-up order.
    for (const Component& comp : rsm.components) {
	const unsigned int t_summ = current_time();
	std::cout << "Summarizing " << comp.name << std::endl;
	summarize(graph, comp);
	std::cout << "Done in " << current_time() - t_summ << " ms"
		  << std::endl << std::endl;
    }

    // Final propagation step for the top component in the RSM. All required
    // summarization has been completed at this point, and we only need to
    // perform forward propagation.
    const Component& top_comp = rsm.components.last();
    const unsigned int t_prop = current_time();
    std::cout << "Propagating over " << top_comp.name << std::endl;
    propagate(graph, top_comp);
    std::cout << "Done in " << current_time() - t_prop << " ms"
	      << std::endl << std::endl;
}

void Analysis::summarize(Graph& graph, const Component& comp) const {
    Registry<Worker> workers;
    Index<Table<Dependence>,Ref<Node>,&Dependence::start> deps;
    Worklist<Ref<Worker>,true> worklist;
    Histogram<unsigned int> reschedule_freqs;

    // We need to search through the uses of each component to find all
    // compatible entry/exit node pairs on which we should summarize. This
    // process needs to happen on the full RSM level.
    // TODO: Use a better heuristic for the summarization order, to avoid
    // rescheduling (e.g. based on call graph, for call matching).
    for (const Component& user : rsm.components) {
	for (const Box& b : user.boxes) {
	    // TODO: This is a simple selection filter, we could have used an
	    // index on boxes instead.
	    if (b.comp != comp.ref) {
		continue;
	    }
	    // Pick all entry/exit node pairs compatible with any combination
	    // of entry/exit transitions to the current box.
	    // TODO: We're performing a separate graph traversal for each
	    // member of the cartesian product. Could instead keep these
	    // grouped and pass them as sets to subpath_bounds.
	    for (const Entry& i : user.entries.primary()[b.ref]) {
		for (const Exit& o : user.exits[b.ref]) {
		    std::map<Ref<Node>,std::set<Ref<Node>>> bounds =
			graph.subpath_bounds(i.label, o.label);
		    for (const auto& p : bounds) {
			// TODO: Duplicate entry/exit node pairs are
			// filtered through Registry's 'merge' mechanism.
			workers.add(p.first, comp, p.second);
		    }
		}
	    }
	}
    }
    for (const Worker& w : workers) {
	worklist.enqueue(w.ref);
    }

    std::cout << "Starting with " << workers.size() << " workers" << std::endl;

    while (!worklist.empty()) {
	const Worker& w = workers[worklist.dequeue()];
	Worker::Result res = w.handle(graph, fsm);
	// Dependencies must be recorded first, to ensure we re-process the
	// function in cases of self-recursion.
	// TODO: Could insert the dependencies as we find them, inside the
	// function-local propagation; no need to pass them in a separate set.
	for (Ref<Node> dep_start : res.deps) {
	    deps.insert(Dependence(dep_start, w.ref));
	}
	if (graph.summaries.copy(res.summs, w.comp.ref, w.start)) {
	    unsigned int reschedules = 0;
	    for (const Dependence& d : deps[w.start]) {
		if (worklist.enqueue(d.worker)) {
		    reschedules++;
		}
	    }
	    reschedule_freqs.record(reschedules);
	}
    }

    std::cout << "Reschedules frequency:" << std::endl;
    std::cout << reschedule_freqs;
}

void Analysis::propagate(Graph& graph, const Component& comp) const {
    // We try starting from each node in the graph. The shape of the top
    // component (or any secondary dimensions) can enforce additional
    // constraints on acceptable sources.
    // TODO: Could do this in a query-driven manner?
    // We can do this one node at a time.
    for (const Node& start : graph.nodes) {
	unsigned int t_start = current_time();
	Worker::Result res = Worker(start.ref, comp).handle(graph, fsm);
	// Ignore any emitted dependencies, they should have been handled
	// during this component's summarization step.
	// TODO: Don't produce at all.
	if (!res.summs.empty()) {
	    std::cout << res.summs.size() << " summaries found in "
		      << current_time() - t_start << " ms" << std::endl;
	}
	// Reachability information is stored like regular summaries.
	// TODO: Should separate?
	graph.summaries.copy(res.summs, comp.ref, start.ref);
    }
}

bool Worker::merge(const Component& comp,
		   const std::set<Ref<Node>>& new_tgts) {
    assert(comp.ref == this->comp.ref);
    auto old_sz = tgts.size();
    tgts.insert(new_tgts.begin(), new_tgts.end());
    return tgts.size() > old_sz;
}

Worker::Result Worker::handle(const Graph& graph, const FSM& fsm) const {
    Result res;
    WorkerWorklist worklist;
    Ref<State> fsm_init = fsm.comp.get_initial();
    TransRel start_trel;
    if (top_level) {
	start_trel.insert(fsm_init, fsm_init);
    } else {
	start_trel = fsm.id_trel();
    }
    Position start_pos(start, comp.get_initial(), start_trel);
    worklist.enqueue(start_pos);

    while (!worklist.empty()) {
	Position pos = worklist.dequeue();

	// Report a summary edge if we've reached a final state.
	if (comp.get_final().count(pos.r_to) > 0) {
	    if (// During the final top-down reachability step, we only accept
		// initial-to-final FSM effects.
		(top_level && fsm.is_accepting(pos.trel)) ||
		// During the summarization step, we only emit a summary at one
		// of the "interesting" summary out-nodes.
		(!top_level && tgts.count(pos.dst) > 0)) {
		res.summs.copy(pos.trel, pos.dst);
	    }
	}

	// Cross edges according to the transitions out of the current state.
	for (const Transition& t : comp.transitions[pos.r_to]) {
	    boost::optional<Ref<Tag>> maybe_tag =
		boost::make_optional(t.label.tag.valid(), t.label.tag);
	    FOR(e, graph.search(pos.dst, t.label), maybe_tag) {
		TransRel new_trel =
		    compose(pos.trel, fsm.effect_of(t.label, e.get<TAG>()));
		if (new_trel.empty()) {
		    continue;
		}
		worklist.enqueue(Position(e.get<DST>(), t.to, new_trel));
	    }
	}

	// Cross summary edges according to the boxes that the current state
	// enters into. The entry, box, and all exits are crossed in one step.
	for (const Entry& entry : comp.entries.secondary<0>()[pos.r_to]) {
	    Ref<Component> sub_comp = comp.boxes[entry.to].comp;

	    // Enter the box.
	    FOR(e_in, graph.search(pos.dst, entry.label)) {
		Ref<Node> in_node = e_in.get<DST>();
		Ref<Tag> in_tag = e_in.get<TAG>();
		TransRel in_trel =
		    compose(pos.trel, fsm.effect_of(entry.label, in_tag));
		if (in_trel.empty()) {
		    continue;
		}

		// If this is a self-reference, record our dependence on it
		// (otherwise it must refer to a component further down the
		// tree, which has already been fully summarized).
		if (sub_comp == comp.ref) {
		    res.deps.insert(in_node);
		}

		// Cross through any existing summary edges.
		const auto& compat_summs = graph.summaries[sub_comp][in_node];
		for (Ref<Node> out_node : compat_summs) {
		    TransRel sub_trel = compat_summs[out_node];
		    TransRel out_trel = compose(in_trel, sub_trel);
		    if (out_trel.empty()) {
			continue;
		    }

		    // Cross through exit edges compatible with the previously
		    // entered entry edge.
		    for (const Exit& exit : comp.exits[entry.to]) {
			TransRel full_trel =
			    compose(out_trel,
				    fsm.effect_of(exit.label, in_tag));
			if (full_trel.empty()) {
			    continue;
			}
			const auto& slice =
			    graph.search(out_node, exit.label)[in_tag];
			FOR(e_out, slice) {
			    worklist.enqueue(Position(e_out.get<DST>(),
						      exit.to, full_trel));
			}
		    }
		}
	    }
	}
    }

    return res;
}

// MAIN =======================================================================

int main(int argc, char* argv[]) {
    // Timekeeping
    const unsigned int t_input = current_time();

    // User-defined parameters
    std::string rsm_dir;
    std::string fsm_file;
    std::string graph_dir;
    std::string summ_dir;

    // Parse options
    po::options_description desc("Options");
    desc.add_options()
	("help,h", "Print help message")
	("rsm-dir", po::value<std::string>(&rsm_dir)->required(),
	 "Directory of RSM components")
	("fsm-file", po::value<std::string>(&fsm_file)->required(),
	 "Intersected FSM file")
	("graph-dir", po::value<std::string>(&graph_dir)->required(),
	 "Directory of edge files")
	("summ-dir", po::value<std::string>(&summ_dir)->required(),
	 "Directory to store the summaries");
    po::positional_options_description pos_desc;
    pos_desc.add("rsm-dir", 1);
    pos_desc.add("fsm-file", 1);
    pos_desc.add("graph-dir", 1);
    pos_desc.add("summ-dir", 1);
    po::variables_map vm;
    try {
	po::store(po::command_line_parser(argc, argv)
		  .options(desc).positional(pos_desc).run(), vm);
	if (vm.count("help") > 0) {
	    // TODO: Also print usage
	    std::cerr << desc << std::endl;
	    return EXIT_FAILURE;
	}
	po::notify(vm);
    } catch (const po::error& e) {
        std::cerr << e.what() << std::endl;
	return EXIT_FAILURE;
    }

    // Initialize logging system
    std::cout.imbue(std::locale(""));

    // Parse input
    std::cout << "Parsing RSM from " << rsm_dir << ", FSM from " << fsm_file
	      << std::endl;
    Analysis spec(rsm_dir, fsm_file);
    std::cout << "Parsing graph from " << graph_dir << std::endl;
    Graph graph(spec.symbols, spec.tags, graph_dir);
    graph.print_stats(std::cout);

    // Timekeeping
    const unsigned int t_solving = current_time();
    std::cout << "Input parsing: " << t_solving - t_input << " ms"
	      << std::endl << std::endl;

    // Perform actual solving
    spec.close(graph);

    // Timekeeping
    const unsigned int t_output = current_time();

    // Print the output
    std::cout << "Printing summaries" << std::endl;
    graph.print_summaries(summ_dir, spec.rsm.components, spec.fsm);

    // Timekeeping
    std::cout << "Output printing: " << current_time() - t_output << " ms"
	      << std::endl;

    return EXIT_SUCCESS;
}
