#include <boost/algorithm/string.hpp>
#include <fstream>
#include <iomanip>
#include <list>
#include <locale>
#include <sstream>

#include "rsm.hpp"

// ALPHABET ===================================================================

const boost::regex Symbol::NAME_REGEX("[a-z]\\w*");

void Label::print(std::ostream& os, const Registry<Symbol>& symbol_reg) const {
    if (dir == Direction::REV) {
	os << "_";
    }
    Symbol& s = symbol_reg[symbol];
    os << s.name;
    if (s.parametric) {
	os << "[" << (tagged ? "i" : "*") << "]";
    }
}

Ref<Node> Label::prev_node(const Edge& e) const {
    assert(symbol == e.symbol);
    switch (dir) {
    case Direction::FWD:
	return e.src;
    case Direction::REV:
	return e.dst;
    }
}

Ref<Node> Label::next_node(const Edge& e) const {
    assert(symbol == e.symbol);
    switch (dir) {
    case Direction::FWD:
	return e.dst;
    case Direction::REV:
	return e.src;
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
	assert(!this->initial.valid());
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

Label RSM::parse_label(const std::string& str) {
    static const boost::regex r("(_)?([a-z]\\w*)(?:\\[([a-z\\*])\\])?");
    boost::smatch m;
    bool matched = boost::regex_match(str, m, r);
    assert(matched);

    Direction dir = m[1].matched ? Direction::REV : Direction::FWD;
    std::string name(m[2].first, m[2].second);
    std::string tag(m[3].first, m[3].second);
    bool parametric = m[3].matched;
    bool tagged = parametric && (tag != "*");

    Ref<Symbol> symbol = symbols.add(name, parametric).ref;
    return Label(symbol, dir, tagged);
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
    assert(fin);
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
		assert(comp.get_initial().valid());
		assert(!comp.get_final().empty());
		mode = ParsingMode::EDGES;
		break;
	    }
	    bool can_be_state = true, initial = false, final = false;
	    bool can_be_box = true;
	    Ref<Component> box_rsm = Ref<Component>::none();
	    for (auto iter = toks.begin() + 1; iter != toks.end(); ++iter) {
		if (*iter == "in") {
		    assert(can_be_state);
		    can_be_box = false;
		    initial = true;
		} else if (*iter == "out") {
		    assert(can_be_state);
		    can_be_box = false;
		    final = true;
		} else {
		    assert(can_be_box);
		    can_be_state = false;
		    assert(!box_rsm.valid());
		    // The referenced component must be present in the RSM.
		    // TODO: This means we only allow self-recursion.
		    box_rsm = components.find(*iter).ref;
		}
	    }
	    if (can_be_state) {
		assert(!comp.boxes.contains(toks[0]));
		assert(!box_rsm.valid());
		comp.add_state(toks[0], initial, final);
	    } else {
		assert(!comp.get_states().contains(toks[0]));
		assert(can_be_box && box_rsm.valid());
		comp.boxes.make(toks[0], box_rsm);
	    }
	} break;

	case ParsingMode::EDGES: {
	    // We are certain that box names and transition names are disjoint.
	    assert(toks.size() >= 3);
	    std::list<Label> labels;
	    for (auto iter = toks.begin() + 2; iter != toks.end(); ++iter) {
		labels.push_back(parse_label(*iter));
	    }
	    bool src_is_state = comp.get_states().contains(toks[0]);
	    bool dst_is_state = comp.get_states().contains(toks[1]);
	    // Allowing an untagged entry to be paired with a tagged exit (and
	    // vica-versa).
	    if (src_is_state && dst_is_state) {
		Ref<State> src = comp.get_states().find(toks[0]).ref;
		Ref<State> dst = comp.get_states().find(toks[1]).ref;
		for (Label& lab : labels) {
		    comp.transitions.insert(Transition(src, dst, lab));
		}
	    } else if (src_is_state && !dst_is_state) {
		Ref<State> src = comp.get_states().find(toks[0]).ref;
		Ref<Box> dst = comp.boxes.find(toks[1]).ref;
		for (Label& lab : labels) {
		    comp.entries.insert(Entry(src, dst, lab));
		}
	    } else if (!src_is_state && dst_is_state) {
		Ref<Box> src = comp.boxes.find(toks[0]).ref;
		Ref<State> dst = comp.get_states().find(toks[1]).ref;
		for (Label& lab : labels) {
		    comp.exits.insert(Exit(src, dst, lab));
		}
	    } else {
		assert(false);
	    }
	} break;
	}
    }

    assert(fin.eof());
    assert(mode == ParsingMode::EDGES);
}

void RSM::print(std::ostream& os) const {
    for (const Component& comp : components) {
	os << comp.name << ":" << std::endl;
	comp.print(os, symbols, components);
	os << std::endl;
    }
}

// GRAPH ======================================================================

const std::string Graph::FILE_EXTENSION(".dat");

void Graph::parse_file(const Symbol& symbol, const fs::path& fpath) {
    std::ifstream fin(fpath.string());
    assert(fin);
    std::string line;
    while (std::getline(fin, line)) {
	if (line.empty()) {
	    continue; // Empty lines are ignored.
	}
	std::vector<std::string> toks;
	boost::split(toks, line, boost::is_any_of(" "),
		     boost::token_compress_on);
	assert(toks.size() >= 2);
	Ref<Node> src = nodes.add(toks[0]).ref;
	Ref<Node> dst = nodes.add(toks[1]).ref;
	if (symbol.parametric) {
	    assert(toks.size() == 3);
	    Ref<Tag> tag = tags.add(toks[2]).ref;
	    edges.insert(Edge(src, dst, symbol.ref, tag));
	} else {
	    assert(toks.size() == 2);
	    edges.insert(Edge(src, dst, symbol.ref, Ref<Tag>::none()));
	}
    }
    assert(fin.eof());
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
	   << std::setw(12) << edges[s.ref].size() << std::endl;
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
	assert(fout);
	for (const Summary& s : summaries[comp.ref]) {
	    fout << s.src.name << " " << s.dst.name << std::endl;
	}
    }
}

// SOLVING ====================================================================

bool Pattern::matches(const Edge& e) const {
    return ((!src.valid() || src == e.src) &&
	    (!dst.valid() || dst == e.dst) &&
	    symbol == e.symbol &&
	    (!match_tag || !tag.valid() || tag == e.tag));
}

// TODO:
// - Avoid creating a new container for the output.
// - May be able to just return a reference to an existing index. If we do have
//   to create a temporary table, we should return a managed pointer to it, so
//   it gets deallocated automatically. Probably need a class hierarchy to
//   support this properly. Currently, we're wasting space in a lot of cases.
// - Manual use of the indices; this could be done at a query planning stage.
// - Have to take cases explicitly on the code, because the different indices
//   don't have compatible base types.
Table<const Edge*> Graph::search(const Pattern& pat) const {
    std::function<bool(const Edge&)> matches =
	[&](const Edge& e){return pat.matches(e)};
    std::function<const Edge*(const Edge&)> get_addr =
	[](const Edge& e){return &e};
    const auto& s = edges[pat.symbol];

    if (pat.src.valid()) {
	return filter_map(s.primary()[pat.src], matches, get_addr);
    } else if (pat.dst.valid()) {
	return filter_map(s.secondary<0>()[pat.dst], matches, get_addr);
    } else if (pat.match_tag) {
	return filter_map(s.secondary<1>()[pat.tag], matches, get_addr);
    } else {
	return filter_map(s, matches, get_addr);
    }
}

// TODO: Don't produce the output by value.
std::map<Ref<Node>,std::set<Ref<Node>>>
Graph::subpath_bounds(const Label& head_lab, const Label& tail_lab) const {
    std::map<Ref<Node>,std::set<Ref<Node>>> res;

    Pattern head_pat(head_lab);
    for (const Edge* h : search(head_pat)) {
	Ref<Node> start = head_lab.next_node(*h);
	Pattern tail_pat(head_pat, *h, tail_lab);
	for (const Edge* t : search(tail_pat)) {
	    Ref<Node> tgt = tail_lab.prev_node(*t);
	    res[start].insert(tgt);
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
	Registry<Worker,Ref<Node>> workers;
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
			  const Registry<Worker,Ref<Node>>& workers) const {
    Index<Table<Dependence>,Ref<Node>,&Dependence::start>> deps;
    // TODO: Verify that all workers refer to this component.
    Worklist<Ref<Worker>> worklist(true);
    for (const Worker& w : workers) {
	worklist.enqueue(w.ref);
    }

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
	    if (graph.summaries.insert(s).second) {
		for (Ref<Worker> to_rerun : deps[s.src]) {
		    worklist.enqueue(to_rerun);
		}
	    }
	}
    }
}

void Component::propagate(Graph& graph) const {
    // We try starting from each node in the graph. The shape of the top
    // component (or any secondary dimensions) can enforce additional
    // constraints on acceptable sources.
    // TODO: Could do this in a query-driven manner?
    // We can do this one node at a time.
    for (const Node& start : graph.nodes) {
	Worker::Result res = Worker(start, *this).summarize(graph);
	// Ignore any emitted dependencies, they should have been handled
	// during this component's summarization step.
	// TODO: Don't produce at all.
	for (const Summary& s : res.summaries) {
	    assert(s.comp == ref && s.src == start);
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
    std::cout << "Starting from " << graph.nodes[start].name << std::endl;

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
	for (const Transition& t : comp.transitions[pos.state]) {
	    for (const Edge* e : graph.search(Pattern(t.label, pos.node))) {
		Ref<Node> next_node = t.label.next_node(*e);
		worklist.enqueue(Position(next_node, t.to));
	    }
	}

	// Cross summary edges according to the boxes that the current state
	// enters into. The entry, box, and all exits are crossed in one step.
	for (const Entry& entry : comp.entries.secondary<0>()[pos.state]) {
	    Ref<Component> sub_comp = comp.boxes[entry.to].comp;
	    Pattern in_pat(entry.label, pos.node);

	    for (const Edge* e_in : graph.search(in_pat)) {
		Ref<Node> sub_in = entry.label.next_node(*e_in);

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
			Pattern out_pat(in_pat, *e_in, exit.label, sub_out);
			for (const Edge* e_out : graph.search(out_pat)) {
			    Ref<Node> next_node = exit.label.next_node(*e_out);
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
    // Parse options
    po::options_description desc("Options");
    desc.add_options()
	("rsm-dir", po::value<std::string>()->required(),
	 "Directory of RSM components")
	("graph-dir", po::value<std::string>()->required(),
	 "Directory of edge files")
	("summ-dir", po::value<std::string>()->required(),
	 "Directory to store the summaries");
    po::positional_options_description pos_desc;
    pos_desc.add("rsm-dir", 1);
    pos_desc.add("graph-dir", 1);
    pos_desc.add("summ-dir", 1);
    po::variables_map vm;
    po::store(po::command_line_parser(argc, argv)
	      .options(desc).positional(pos_desc).run(), vm);
    po::notify(vm);
    std::string rsm_dir = vm.at("rsm-dir").as<std::string>();
    std::string graph_dir = vm.at("graph-dir").as<std::string>();
    std::string summ_dir = vm.at("summ-dir").as<std::string>();

    // Initialize logging system
    std::cout.imbue(std::locale(""));

    // Parse input
    RSM rsm;
    std::cout << "Parsing RSM components from " << rsm_dir << std::endl;
    rsm.parse_dir(rsm_dir);
    rsm.print(std::cout);
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

    return 0;
}
