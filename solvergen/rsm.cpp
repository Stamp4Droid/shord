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
	 "Directory of edge files");
    po::positional_options_description pos_desc;
    pos_desc.add("rsm-dir", 1);
    pos_desc.add("graph-dir", 1);
    po::variables_map vm;
    po::store(po::command_line_parser(argc, argv)
	      .options(desc).positional(pos_desc).run(), vm);
    po::notify(vm);
    std::string rsm_dir = vm.at("rsm-dir").as<std::string>();
    std::string graph_dir = vm.at("graph-dir").as<std::string>();
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

    return 0;
}
