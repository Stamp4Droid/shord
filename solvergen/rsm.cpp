#include <boost/algorithm/string.hpp>
#include <fstream>
#include <iomanip>
#include <list>
#include <locale>
#include <sstream>

#include "rsm.hpp"

// ALPHABET ===================================================================

const boost::regex Symbol::NAME_REGEX("[a-z]\\w*");

template<bool Tagged>
void Label<Tagged>::print(std::ostream& os,
			  const Registry<Symbol>& symbol_reg) const {
    if (rev) {
	os << "_";
    }
    const Symbol& s = symbol_reg[symbol];
    os << s.name;
    if (s.parametric) {
	os << "[" << (Tagged ? "i" : "*") << "]";
    }
}

// RSM ========================================================================

void State::print(std::ostream& os) const {
    os << "State" << ref;
    if (initial) {
	os << " in";
    }
    if (final) {
	os << " out";
    }
    os << std::endl;
}

void Box::print(std::ostream& os, const Registry<Component>& comp_reg) const {
    os << "Box" << ref << " " << comp_reg[comp].name << std::endl;
}

void Transition::print(std::ostream& os,
		       const Registry<Symbol>& symbol_reg) const {
    os << "State" << from << " State" << to << " ";
    label.print(os, symbol_reg);
    os << std::endl;
}

void Entry::print(std::ostream& os, const Registry<Symbol>& symbol_reg) const {
    os << "State" << from << " Box" << to << " ";
    label.print(os, symbol_reg);
    os << std::endl;
}

void Exit::print(std::ostream& os, const Registry<Symbol>& symbol_reg) const {
    os << "Box" << from << " State" << to << " ";
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
		      const Registry<Component>& comp_reg) const {
    for (const State& s : states) {
	s.print(os);
    }
    for (const Box& b : boxes) {
	b.print(os, comp_reg);
    }
    os << "#" << std::endl;
    for (const Transition& t : transitions) {
	t.print(os, symbol_reg);
    }
    for (const Entry& e : entries) {
	e.print(os, symbol_reg);
    }
    for (const Exit& e : exits) {
	e.print(os, symbol_reg);
    }
}

const std::string RSM::FILE_EXTENSION(".rsm.tgf");

template<bool Tagged>
Label<Tagged> RSM::parse_label(const std::string& str) {
    static const boost::regex r("(_)?([a-z]\\w*)(?:\\[([a-z\\*])\\])?");
    boost::smatch m;
    bool matched = boost::regex_match(str, m, r);
    EXPECT(matched);

    bool rev = m[1].matched;
    std::string name(m[2].first, m[2].second);
    std::string tag(m[3].first, m[3].second);
    bool parametric = m[3].matched;
    EXPECT((Tagged && parametric && tag != "*") ||
	   (!Tagged && (!parametric || tag == "*")));

    return Label<Tagged>(symbols.add(name, parametric), rev);
}

void RSM::parse_dir(const std::string& dirname) {
    Directory dir(dirname);
    std::list<fs::path> files(dir.begin(), dir.end());
    files.sort();
    for (const fs::path& fpath : files) {
	parse_file(fpath);
    }
}

enum class ParsingMode {NODES, EDGES};

void RSM::parse_file(const fs::path& fpath) {
    std::string fbase(fpath.filename().string());
    if (!boost::algorithm::ends_with(fbase, FILE_EXTENSION)) {
	return;
    }
    std::cout << "Parsing " << fbase << std::endl;
    size_t name_len = fbase.size() - FILE_EXTENSION.size();
    std::string name(fbase.substr(0, name_len));
    Component& comp = components.make(name);

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
	    Ref<Component> box_rsm = Ref<Component>::none();
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
		    box_rsm = components.find(*iter).ref;
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
		    comp.transitions.insert(Transition(src, dst,
						       parse_label<false>(t)));
		}
	    } else if (src_is_state && !dst_is_state) {
		Ref<State> src = comp.get_states().find(toks[0]).ref;
		Ref<Box> dst = comp.boxes.find(toks[1]).ref;
		for (const std::string& t : label_toks) {
		    comp.entries.insert(Entry(src, dst, parse_label<true>(t)));
		}
	    } else if (!src_is_state && dst_is_state) {
		Ref<Box> src = comp.boxes.find(toks[0]).ref;
		Ref<State> dst = comp.get_states().find(toks[1]).ref;
		for (const std::string& t : label_toks) {
		    comp.exits.insert(Exit(src, dst, parse_label<true>(t)));
		}
	    }
	} break;
	}
    }

    EXPECT(fin.eof());
    EXPECT(mode == ParsingMode::EDGES);
}

void RSM::print(std::ostream& os) const {
    for (const Component& comp : components) {
	os << comp.name << ":" << std::endl;
	comp.print(os, symbols, components);
    }
}

// GRAPH ======================================================================

const std::string Graph::FILE_EXTENSION(".dat");

void Graph::parse_file(const Symbol& symbol, const fs::path& fpath) {
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
	EXPECT(toks.size() >= 2);
	Ref<Node> src = nodes.add(toks[0]).ref;
	Ref<Node> dst = nodes.add(toks[1]).ref;
	Ref<Tag> tag = Ref<Tag>::none();
	if (symbol.parametric) {
	    EXPECT(toks.size() == 3);
	    tag = tags.add(toks[2]).ref;
	} else {
	    EXPECT(toks.size() == 2);
	}
	// Add the edge twice, once forwards and once backwards.
	// TODO: This simplifies the interface, but also increases space
	// requirements (but doesn't necessarily double them; if we didn't do
	// this we'd need to keep an index on the destination).
	edges.insert(Edge(src, dst, symbol, false, tag));
	edges.insert(Edge(dst, src, symbol, true, tag));
    }
    EXPECT(fin.eof());
}

Graph::Graph(const Registry<Symbol>& symbols, const std::string& dirname) {
    fs::path dirpath(dirname);
    for (const Symbol& s : symbols) {
	std::string fname = s.name + FILE_EXTENSION;
	std::cout << "Parsing " << fname << std::endl;
	// Will fail if some symbol is missing its Edge file.
	parse_file(s, dirpath/fname);
    }
}

void Graph::print_stats(std::ostream& os,
			const Registry<Symbol>& symbols) const {
    os << "Nodes: " << nodes.size() << std::endl;
    os << "Edges: " << std::endl;
    for (const Symbol& s : symbols) {
	os << std::setw(15) << s.name
	   << std::setw(12) << edges[false][s.ref].size() << std::endl;
    }
    // TODO: Also print out stats on summaries.
}

void Graph::print_summaries(const std::string& dirname,
			    const Registry<Component>& components) const {
    fs::path dirpath(dirname);
    for (const Component& comp : components) {
	std::string fname = comp.name + FILE_EXTENSION;
	std::cout << "Printing " << fname << std::endl;
	std::ofstream fout((dirpath/fname).string());
	EXPECT(fout);
	for (const Summary& s : summaries[comp.ref]) {
	    fout << nodes[s.src].name << " " << nodes[s.dst].name << std::endl;
	}
    }
}

// SOLVING ====================================================================

// TODO:
// - Don't produce the output by value.
// - Implicitly assumes all entry/exit labels are tagged.
std::map<Ref<Node>,std::set<Ref<Node>>>
Graph::subpath_bounds(const Label<true>& hd_lab,
		      const Label<true>& tl_lab) const {
    std::map<Ref<Node>,std::set<Ref<Node>>> res;
    for (const Edge& hd : search(hd_lab)) {
	for (const Edge& tl : search(tl_lab).secondary<0>()[hd.tag]) {
	    res[hd.dst].insert(tl.src);
	}
    }
    return res;
}

void RSM::propagate(Graph& graph) const {
    // Components are processed in order of addition, which is guaranteed to be
    // a valid bottom-up order.
    for (const Component& comp : components) {
	std::cout << "Summarizing " << comp.name << std::endl;

	// We need to search through the uses of each component to find all
	// compatible entry/exit node pairs on which we should summarize. This
	// process needs to happen on the full RSM level.
	// TODO: Use a better heuristic for the summarization order, to avoid
	// rescheduling (e.g. based on call graph, for call matching).
	Registry<Worker> workers;
	for (const Component& user : components) {
	    for (const Box& b : user.boxes) {
		// TODO: This is a simple selection filter, we could have used
		// an index on boxes instead.
		if (b.comp != comp.ref) {
		    continue;
		}
		// Pick all entry/exit node pairs compatible with any
		// combination of entry/exit transitions to the current box.
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

	comp.summarize(graph, workers);
    }

    // Final propagation step for the top component in the RSM. All required
    // summarization has been completed at this point, and we only need to
    // perform forward propagation.
    const Component& top_comp = components.last();
    std::cout << "Propagating over " << top_comp.name << std::endl;
    top_comp.propagate(graph);
}

void Component::summarize(Graph& graph,
			  const Registry<Worker>& workers) const {
    Index<Table<Dependence>,Ref<Node>,&Dependence::start> deps;
    // TODO: Verify that all workers refer to this component.
    Worklist<Ref<Worker>> worklist(true);
    for (const Worker& w : workers) {
	worklist.enqueue(w.ref);
    }

    Histogram<unsigned int> new_summ_freqs;
    Histogram<unsigned int> reschedule_freqs;

    while (!worklist.empty()) {
	Ref<Worker> w = worklist.dequeue();
	Worker::Result res = workers[w].summarize(graph);
	// Dependencies must be recorded first, to ensure we re-process the
	// function in cases of self-recursion.
	// TODO: Could insert the dependencies as we find them, inside the
	// function-local propagation; no need to pass them in a separate set.
	for (const Dependence& d : res.deps) {
	    assert(d.worker == w);
	    deps.insert(d);
	}
	for (const Summary& s : res.summaries) {
	    assert(s.comp == ref && s.src == workers[w].start);
	    unsigned int new_summs = 0;
	    unsigned int reschedules = 0;
	    if (graph.summaries.insert(s).second) {
		new_summs++;
		for (const Dependence& d : deps[s.src]) {
		    if (worklist.enqueue(d.worker)) {
			reschedules++;
		    }
		}
	    }
	    new_summ_freqs.record(new_summs);
	    reschedule_freqs.record(reschedules);
	}
    }

    std::cout << "Started with " << workers.size() << " workers" << std::endl;
    std::cout << "Summary addition frequency:" << std::endl;
    std::cout << new_summ_freqs;
    std::cout << "Reschedules frequency:" << std::endl;
    std::cout << reschedule_freqs << std::endl;
}

void Component::propagate(Graph& graph) const {
    // We try starting from each node in the graph. The shape of the top
    // component (or any secondary dimensions) can enforce additional
    // constraints on acceptable sources.
    // TODO: Could do this in a query-driven manner?
    // We can do this one node at a time.
    for (const Node& start : graph.nodes) {
	Worker::Result res = Worker(start.ref, *this).summarize(graph);
	// Ignore any emitted dependencies, they should have been handled
	// during this component's summarization step.
	// TODO: Don't produce at all.
	for (const Summary& s : res.summaries) {
	    assert(s.comp == ref && s.src == start.ref);
	    // Reachability information is stored like regular summaries.
	    // TODO: Should separate?
	    graph.summaries.insert(s);
	}
    }
}

bool Worker::merge(const Component& comp,
		   const std::set<Ref<Node>>& new_tgts) {
    assert(comp.ref == this->comp.ref);
    auto old_sz = tgts.size();
    tgts.insert(new_tgts.begin(), new_tgts.end());
    return tgts.size() > old_sz;
}

Worker::Result Worker::summarize(const Graph& graph) const {
    Worklist<Position> worklist(false);
    Result res;
    worklist.enqueue(Position(start, comp.get_initial()));

    while (!worklist.empty()) {
	Position pos = worklist.dequeue();

	// Report a summary edge if we've reached a final state, at one of the
	// "interesting" summary out-nodes (for the final top-down reachability
	// step, any node works as a final node).
	if ((tgts.empty() || tgts.count(pos.node) > 0) &&
	    comp.get_final().count(pos.state) > 0) {
	    res.summaries.emplace(start, pos.node, comp.ref);
	}

	// Cross edges according to the transitions out of the current state.
	// TODO: Implicitly assumes all transition labels are untagged.
	for (const Transition& t : comp.transitions[pos.state]) {
	    for (const Edge& e : graph.search(t.label).primary()[pos.node]) {
		worklist.enqueue(Position(e.dst, t.to));
	    }
	}

	// Cross summary edges according to the boxes that the current state
	// enters into. The entry, box, and all exits are crossed in one step.
	// TODO: Implicitly assumes all entry/exit labels are tagged.
	for (const Entry& entry : comp.entries.secondary<0>()[pos.state]) {
	    Ref<Component> sub_comp = comp.boxes[entry.to].comp;
	    for (const Edge& e_in :
		     graph.search(entry.label).primary()[pos.node]) {
		Ref<Node> sub_in = e_in.dst;

		// If this is a self-referring box (TODO: or any box in the
		// current RSM SCC, if we ever support non-singleton SCCs),
		// record our dependence on it.
		if (sub_comp == comp.ref) {
		    res.deps.insert(Dependence(sub_in, ref));
		}

		// Cross through any existing summary edges.
		for (const Summary& s : graph.summaries[sub_comp][sub_in]) {
		    Ref<Node> sub_out = s.dst;
		    // Cross through exit edges compatible with the previously
		    // entered entry edge.
		    for (const Exit& exit : comp.exits[entry.to]) {
			for (const Edge& e_out :
				 graph.search(exit.label).primary()
				 [sub_out][e_in.tag]) {
			    Ref<Node> next_node = e_out.dst;
			    worklist.enqueue(Position(next_node, exit.to));
			}
		    }
		}
	    }
	}
    }

    return res;
}

// MAIN =======================================================================

// TODO:
// - Better summary representation, for human consumption.

int main(int argc, char* argv[]) {
    // User-defined parameters
    std::string rsm_dir;
    std::string graph_dir;
    std::string summ_dir;

    // Parse options
    po::options_description desc("Options");
    desc.add_options()
	("help,h", "Print help message")
	("rsm-dir", po::value<std::string>(&rsm_dir)->required(),
	 "Directory of RSM components")
	("graph-dir", po::value<std::string>(&graph_dir)->required(),
	 "Directory of edge files")
	("summ-dir", po::value<std::string>(&summ_dir)->required(),
	 "Directory to store the summaries");
    po::positional_options_description pos_desc;
    pos_desc.add("rsm-dir", 1);
    pos_desc.add("graph-dir", 1);
    pos_desc.add("summ-dir", 1);
    po::variables_map vm;
    try {
	po::store(po::command_line_parser(argc, argv)
		  .options(desc).positional(pos_desc).run(), vm);
	if (vm.count("help") > 0) {
	    // TODO: Also print usage
	    std::cerr << desc << std::endl;
	    return 1;
	}
	po::notify(vm);
    } catch (const po::error& e) {
        std::cerr << e.what() << std::endl;
	return 1;
    }

    // Initialize logging system
    std::cout.imbue(std::locale(""));

    // Parse input
    RSM rsm;
    std::cout << "Parsing RSM components from " << rsm_dir << std::endl;
    rsm.parse_dir(rsm_dir);
    std::cout << std::endl;
    std::cout << "Parsing graph from " << graph_dir << std::endl;
    Graph graph(rsm.symbols, graph_dir);
    graph.print_stats(std::cout, rsm.symbols);
    std::cout << std::endl;

    // Perform actual solving
    std::cout << "Solving" << std::endl;
    rsm.propagate(graph);
    std::cout << std::endl;

    // Print the output
    std::cout << "Printing summaries" << std::endl;
    graph.print_summaries(summ_dir, rsm.components);

    return EXIT_SUCCESS;
}
