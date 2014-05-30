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
    static const boost::regex r("(_)?([a-z]\\w*)(?:\\[(\\w+|\\*)\\])?");
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

// EFFECT HANDLING ============================================================

void print(std::ostream& os, const Frame& frame,
	   const Registry<Component>& comp_reg, const Registry<Tag>& tag_reg) {
    const Component& c = comp_reg[frame.comp];
    os << c.name << ":" << c.boxes[frame.box].name
       << "{" << tag_reg[frame.tag].name << "}";
}

template<class EfftT>
void print_effect(std::ostream& os, const std::string& prefix,
		  const EfftT& efft, const Registry<Component>& comp_reg,
		  const Registry<Tag>& tag_reg) {
    FOR(trans, efft) {
	const Component& cp_from = comp_reg[trans.template get<CP_FROM>()];
	const State& st_from =
	    cp_from.get_states()[trans.template get<ST_FROM>()];
	const Component& cp_to = comp_reg[trans.template get<CP_TO>()];
	const State& st_to =
	    cp_to.get_states()[trans.template get<ST_TO>()];
	os << prefix
	   << cp_from.name << ":" << st_from.name << " "
	   <<   cp_to.name << ":" <<   st_to.name << " <";
	print(os, trans.template get<REQD>(), false, comp_reg, tag_reg);
	os << "|";
	print(os, trans.template get<PUSH>(), true, comp_reg, tag_reg);
	os << ">" << std::endl;
    }
}

std::list<std::pair<EfftReqd,EfftPush>> match(EfftPush l_push,
					      EfftReqd r_reqd) {
    std::list<std::pair<EfftReqd,EfftPush>> res;
    while (true) {
	if (l_push.empty()) {
	    if (l_push.exact()) {
		// α|- + γ|δ = αγ|δ
		res.emplace_back(r_reqd, l_push);
		break;
	    }
	    if (r_reqd.empty()) {
		// α|* + -|δ = α|*δ
		// α|* + *|δ = α|*δ, ...
		res.emplace_back(EfftReqd(), l_push);
		if (!r_reqd.exact()) {
		    // α|* + *|δ = ..., α*|δ
		    res.emplace_back(r_reqd, EfftPush());
		}
		break;
	    }
	    // α|* + xγ|δ = α|* + γ|δ
	    r_reqd.pop();
	    continue;
	}

	if (r_reqd.empty()) {
	    // α|βx + -|δ = α|βxδ
	    // α|βx + *|δ = α|βxδ, ...
	    res.emplace_back(EfftReqd(), l_push);
	    if (r_reqd.exact()) {
		break;
	    }
	    // α|βx + *|δ = ..., α|β + *|δ
	    l_push.pop();
	    continue;
	}

	if (l_push.top() == r_reqd.top()) {
	    // α|βx + xγ|δ = α|β + γ|δ
	    l_push.pop();
	    r_reqd.pop();
	    continue;
	}

	break;
    }
    return res;
}

// TODO: Wasteful to store forward-only effects in RTL.
bool compose(const EffectRTL& l_efft, const EffectLTR& r_efft,
	     EffectRTL& res_efft, bool fwd_only) {
    bool grew = false;
    auto add_product = [&](const typename EffectRTL::Sub::Sub& prefixes,
			   const typename EffectLTR::Sub::Sub& additions) {
	for (const auto& l_push_pair : prefixes) {
	    const EfftPush& l_push = l_push_pair.first;
	    for (const auto& r_reqd_pair : additions) {
		const EfftReqd& r_reqd = r_reqd_pair.first;
		std::list<std::pair<EfftReqd,EfftPush>> base =
		    match(l_push, r_reqd);
		if (base.empty()) {
		    continue;
		}
		FOR(l, l_push_pair.second) {
		    assert(!fwd_only || (l.get<REQD>().empty() &&
					 l.get<REQD>().exact()));
		    FOR(r, r_reqd_pair.second) {
			for (const auto& p : base) {
			    EfftReqd res_reqd = p.first;
			    res_reqd.append(l.get<REQD>());
			    if (fwd_only && (!res_reqd.empty() ||
					     !res_reqd.exact())) {
				continue;
			    }
			    EfftPush res_push = p.second;
			    res_push.append(r.get<PUSH>());
			    if (res_efft.insert(r.get<CP_TO>(),
						r.get<ST_TO>(),
						res_push,
						l.get<CP_FROM>(),
						l.get<ST_FROM>(),
						res_reqd)) {
				grew = true;
			    }
			}
		    }
		}
	    }
	}
    };
    join_zip<2>(l_efft, r_efft, add_product);
    return grew;
}

bool copy_trans(const EffectRTL& src, EffectLTR& dst, bool accepting_only,
		const RSM& rsm) {
    bool grew = false;
    if (accepting_only) {
	const Component& top_comp = rsm.components.last();
	Ref<State> init_st = top_comp.get_initial();
	for (const auto& st_to_pair : src[top_comp.ref]) {
	    Ref<State> st_to = st_to_pair.first;
	    if (top_comp.get_final().count(st_to) > 0
		&& st_to_pair.second.contains(EfftPush(), top_comp.ref,
					      init_st,    EfftReqd())
		&& dst.insert(top_comp.ref, init_st, EfftReqd(),
			      top_comp.ref, st_to,   EfftPush())) {
		grew = true;
	    }
	}
    } else {
	FOR(trans, src) {
	    if (dst.insert(trans.get<CP_FROM>(), trans.get<ST_FROM>(),
			   trans.get<REQD>(),    trans.get<CP_TO>(),
			   trans.get<ST_TO>(),   trans.get<PUSH>())) {
		grew = true;
	    }
	}
    }
    return grew;
}

const EffectLTR& RSM::effect_of(const Symbol& symbol, bool rev,
				Ref<Tag> tag) const {
    // Check that this is a valid Arc.
    assert(!symbol.parametric ^ tag.valid());

    // If not found in cache, compute all effects for this arc on the fly.
    // TODO: Somewhat wasteful, constructing temporary Label objects and
    // dereferencing the map using full labels. Would work better if we
    // indexed Transitions using mi library.
    // XXX: What if the label doesn't appear in the secondary RSM?
    EffectLTR& res = base_effts.of(rev).of(symbol.ref).of(tag);
    if (res.empty()) {
	for (const Component& comp : components) {

	    // Record effects due to tag-agnostic transitions.
	    for (const Transition& t :
		     comp.transitions.secondary<0>()[Label(symbol, rev,
							   Ref<Tag>())]) {
		res.insert(comp.ref, t.from, EfftReqd(),
			   comp.ref, t.to,   EfftPush());
	    }

	    // Record effects due to tag-specific transitions.
	    if (tag.valid()) {
		for (const Transition& t :
			 comp.transitions.secondary<0>()[Label(symbol, rev,
							       tag)]) {
		    res.insert(comp.ref, t.from, EfftReqd(),
			       comp.ref, t.to,   EfftPush());
		}
	    }

	    if (symbol.parametric) {
		// Record effects due to box entries.
		for (const Entry& e :
			 comp.entries.secondary<1>()[MatchLabel(symbol,
								rev)]) {
		    Ref<Component> cp_to = comp.boxes[e.to].comp;
		    Ref<State> st_to = components[cp_to].get_initial();
		    EfftPush push;
		    push.push(Frame{comp.ref, e.to, tag});
		    res.insert(comp.ref, e.from, EfftReqd(),
			       cp_to,    st_to,  push);
		}

		// Record effects due to box exits.
		for (const Exit& e :
			 comp.exits.secondary<0>()[MatchLabel(symbol, rev)]) {
		    Ref<Component> cp_from = comp.boxes[e.from].comp;
		    for (Ref<State> st_from :
			     components[cp_from].get_final()) {
			EfftReqd reqd;
			reqd.push(Frame{comp.ref, e.from, tag});
			res.insert(cp_from,  st_from, reqd,
				   comp.ref, e.to,    EfftPush());
		    }
		}
	    }
	}
    }

    return res;
}

// ANALYSIS SPEC ==============================================================

const std::string RSM::FILE_EXTENSION(".rsm.tgf");

RSM::RSM(const std::string& dirname, Registry<Symbol>& symbol_reg,
	 Registry<Tag>& tag_reg) : base_effts(boost::none, symbol_reg) {
    Directory dir(dirname);
    std::list<fs::path> paths(dir.begin(), dir.end());
    // TODO: Assuming the usage order of components is the same as their
    // alphabetic order.
    paths.sort();
    for (const fs::path& p : paths) {
	parse_file(p, symbol_reg, tag_reg);
    }
    // TODO: If this is a secondary RSM, verify that it doesn't introduce new
    // symbols, and it covers all symbols on the primary one.

    std::cout << "Calculating identity effect" << std::endl;
    for (const Component& comp : components) {
	for (const State& s : comp.get_states()) {
	    id_efft_.insert(comp.ref, s.ref, EfftPush(),
			    comp.ref, s.ref, EfftReqd());
	}
    }
}

void parse_component(const fs::path& fpath, Component& comp,
		     Registry<Symbol>& symbol_reg, Registry<Tag>& tag_reg,
		     const Registry<Component>& comp_reg) {
    std::ifstream fin(fpath.string());
    EXPECT((bool) fin);
    ParsingMode mode = ParsingMode::NODES;
    std::string line;

    while (std::getline(fin, line)) {
	boost::trim(line);
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
    std::cout << "Parsing " << fpath << std::endl;
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
    os << "Effects requested so far:" << std::endl;
    for (const auto& rev_pair : base_effts) {
	for (const auto& symb_pair : rev_pair.second) {
	    for (const auto& tag_pair : symb_pair.second) {
		Label(symbol_reg[symb_pair.first], rev_pair.first,
		      tag_pair.first).print(os, symbol_reg, tag_reg);
		os << ":" << std::endl;
		print_effect(os, "  ", tag_pair.second, components, tag_reg);
	    }
	}
    }
}

void Analysis::print(std::ostream& os) const {
    pri.print(os, symbols, tags);
    sec.print(os, symbols, tags);
}

// GRAPH ======================================================================

const std::string Graph::FILE_EXTENSION(".dat");

// TODO: Two passes over the files.
void Graph::parse_file(const Symbol& symbol, const fs::path& fpath,
		       ParsingMode mode, Registry<Tag>& tag_reg) {
    std::ifstream fin(fpath.string());
    EXPECT((bool) fin);
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
			    const Registry<Component>& pri_comp_reg,
			    const Registry<Component>& sec_comp_reg,
			    const Registry<Tag>& tag_reg) const {
    fs::path dirpath(dirname);
    for (const Component& pc : pri_comp_reg) {
	fs::path fpath(dirpath/(pc.name + FILE_EXTENSION));
	std::cout << "Printing " << fpath << std::endl;
	std::ofstream fout(fpath.string());
	EXPECT((bool) fout);
	for (const auto& src_pair : summaries[pc.ref]) {
	    for (const auto& dst_pair : src_pair.second) {
		std::string prefix = (nodes[src_pair.first].name + " " +
				      nodes[dst_pair.first].name + " ");
		print_effect(fout, prefix, dst_pair.second,
			     sec_comp_reg, tag_reg);
	    }
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
    FOR(hd, search(hd_lab.symbol, hd_lab.rev)) {
	FOR(tl, search(tl_lab.symbol, tl_lab.rev)[hd.get<TAG>()]) {
	    res[hd.get<DST>()].insert(tl.get<SRC>());
	}
    }
    return res;
}

void Analysis::close(Graph& graph, const fs::path& dump_dir) const {
    // Components are processed in order of addition, which is guaranteed to be
    // a valid bottom-up order.
    for (const Component& comp : pri.components) {
	const unsigned int t_summ = current_time();
	std::cout << "Summarizing " << comp.name << std::endl;
	summarize(graph, comp, dump_dir);
	std::cout << "Done in " << current_time() - t_summ << " ms"
		  << std::endl << std::endl;
    }

    // Final propagation step for the top component in the primary RSM. All
    // required summarization has been completed at this point, and we only
    // need to perform forward propagation.
    const Component& top_comp = pri.components.last();
    const unsigned int t_prop = current_time();
    std::cout << "Propagating over " << top_comp.name << std::endl;
    propagate(graph, top_comp, dump_dir);
    std::cout << "Done in " << current_time() - t_prop << " ms"
	      << std::endl << std::endl;
}

void Analysis::summarize(Graph& graph, const Component& comp,
			 const fs::path& dump_dir) const {
    Registry<Worker> workers;
    Index<Table<Dependence>,Ref<Node>,&Dependence::start> deps;
    Worklist<Ref<Worker>,true> worklist;
    Histogram<unsigned int> reschedule_freqs;

    // We need to search through the uses of each component to find all
    // compatible entry/exit node pairs on which we should summarize. This
    // process needs to happen on the full RSM level.
    // TODO: Use a better heuristic for the summarization order, to avoid
    // rescheduling (e.g. based on call graph, for call matching).
    for (const Component& user : pri.components) {
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
		for (const Exit& o : user.exits.primary()[b.ref]) {
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
	Worker::Result res = w.handle(graph, symbols, sec, dump_dir);

	// Dependencies must be recorded first, to ensure we re-process the
	// function in cases of self-recursion.
	// TODO: Could insert the dependencies as we find them, inside the
	// function-local propagation; no need to pass them in a separate set.
	for (Ref<Node> dep_start : res.deps) {
	    deps.insert(Dependence(dep_start, w.ref));
	}

	// Record summaries, and reschedule workers as necessary.
	if (graph.summaries.of(w.comp.ref).of(w.start).copy(res.summs)) {
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

void Analysis::propagate(Graph& graph, const Component& comp,
			 const fs::path& dump_dir) const {
    // We try starting from each node in the graph. The shape of the top
    // component (or any secondary dimensions) can enforce additional
    // constraints on acceptable sources.
    // TODO: Could do this in a query-driven manner?
    // We can do this one node at a time.
    for (const Node& start : graph.nodes) {
	unsigned int t_start = current_time();
	Worker::Result res =
	    Worker(start.ref, comp).handle(graph, symbols, sec, dump_dir);
	// Ignore any emitted dependencies, they should have been handled
	// during this component's summarization step.
	// TODO: Don't produce at all.
	if (!res.summs.empty()) {
	    std::cout << res.summs.size() << " summaries found in "
		      << current_time() - t_start << " ms" << std::endl;
	}
	// Reachability information is stored like regular summaries.
	// TODO: Should separate?
	graph.summaries.of(comp.ref).of(start.ref).copy(res.summs);
    }
}

bool Worker::merge(const Component& comp,
		   const std::set<Ref<Node>>& new_tgts) {
    assert(comp.ref == this->comp.ref);
    auto old_sz = tgts.size();
    tgts.insert(new_tgts.begin(), new_tgts.end());
    return tgts.size() > old_sz;
}

const RSM* glob_pri = NULL;
const Registry<Symbol>* glob_syms = NULL;
const Registry<Tag>* glob_tags = NULL;

void Worker::dump_node(std::ostream& out, const Node& n) const {
    std::string shape = (n.ref == start) ? "octagon" : "ellipse";
    std::string fillcolor = (n.ref == start) ? "lightcyan" : "white";
    unsigned int peripheries = (tgts.count(n.ref) > 0) ? 2 : 1;
    out << n.name
	<< " [id=\"" << n.name << "\""
	<< ",tooltip=\"" << n.name << "\""
	<< ",shape=\"" << shape << "\""
	<< ",fillcolor=\"" << fillcolor << "\""
	<< ",peripheries=\"" << peripheries << "\"];" << std::endl;
}

Worker::Result Worker::handle(const Graph& graph,
			      const Registry<Symbol>& symbol_reg,
			      const RSM& sec, const fs::path& dump_dir) const {
    Result res;
    WorkerWorklist worklist(top_level);
    if (top_level) {
	EffectRTL init_move;
	const Component& sec_top = sec.components.last();
	init_move.insert(sec_top.ref, sec_top.get_initial(), EfftPush(),
			 sec_top.ref, sec_top.get_initial(), EfftReqd());
	worklist.enqueue(start, comp.get_initial(), init_move);
    } else {
	worklist.enqueue(start, comp.get_initial(), sec.id_efft());
    }

#ifdef VIZ
    mi::Index<SRC, Ref<Node>,
	      mi::Index<DST, Ref<Node>,
			mi::Index<REV, bool,
				  mi::Index<SYMBOL, Ref<Symbol>,
					    mi::Table<TAG, Ref<Tag>>>>>>
	reached_edges;
    mi::Index<SRC, Ref<Node>,
	      mi::Index<DST, Ref<Node>,
			mi::Table<BOX, Ref<Box>>>>
	reached_summs;
    mi::Index<DST, Ref<Node>,
	      mi::Index<BOX, Ref<Box>,
			EffectRTL>>
	reached_borders;
#endif

    while (!worklist.empty()) {
	Position pos = worklist.dequeue();
	const EffectRTL& efft = worklist.effect_at(pos);

	// Report a summary edge if we've reached a final state.
	if (comp.get_final().count(pos.state) > 0) {
	    // During the summarization step, we only emit a summary at one
	    // of the "interesting" summary out-nodes.
	    if (top_level || tgts.count(pos.dst) > 0) {
		// During the final top-down reachability step, we only accept
		// initial-to-final FSM effects.
		copy_trans(efft, res.summs.of(pos.dst), top_level, sec);
	    }
	}

	// Cross edges according to the transitions out of the current state.
	for (const Transition& t : comp.transitions.primary()[pos.state]) {
	    const Symbol& e_symb = symbol_reg[t.label.symbol];
	    bool e_rev = t.label.rev;

	    auto cross = [&](Ref<Tag> e_tag,
			     const mi::Table<DST,Ref<Node>>& dsts){
		const EffectLTR& e_efft = sec.effect_of(e_symb, e_rev, e_tag);
		for (Ref<Node> e_dst : dsts) {
		    if (worklist.enqueue(e_dst, t.to, efft, e_efft)) {

#ifdef VIZ
			if (!top_level) {
			    reached_edges.insert(pos.dst, e_dst, e_rev,
						 e_symb.ref, e_tag);
			}
#endif

		    }
		}
	    };

	    const auto& slice = graph.search(e_symb.ref, e_rev, pos.dst);
	    if (t.label.tag.valid()) {
		cross(t.label.tag, slice[t.label.tag]);
	    } else {
		for (const auto& p : slice) {
		    cross(p.first, p.second);
		}
	    }
	}

	// Cross summary edges according to the boxes that the current state
	// enters into. The entry, box, and all exits are crossed in one step.
	for (const Entry& entry : comp.entries.secondary<0>()[pos.state]) {
	    Ref<Component> sub_comp = comp.boxes[entry.to].comp;
	    const Symbol& e_in_symb = symbol_reg[entry.label.symbol];
	    bool e_in_rev = entry.label.rev;

	    // Enter the box.
	    FOR(e_in, graph.search(e_in_symb.ref, e_in_rev, pos.dst)) {
		Ref<Node> in_node = e_in.get<DST>();
		Ref<Tag> call_tag = e_in.get<TAG>();

		// Calculate the effect up to the summary entry node.
		EffectRTL in_efft;
		const EffectLTR& e_in_efft =
		    sec.effect_of(e_in_symb, e_in_rev, call_tag);
		if (!compose(efft, e_in_efft, in_efft, top_level)) {
		    continue;
		}

#ifdef VIZ
		if (!top_level) {
		    reached_edges.insert(pos.dst, in_node, e_in_rev,
					 e_in_symb.ref, call_tag);
		    reached_borders.of(in_node).of(entry.to).copy(in_efft);
		}
#endif

		// If this is a self-reference, record our dependence on it
		// (otherwise it must refer to a component further down the
		// tree, which has already been fully summarized).
		if (sub_comp == comp.ref) {
		    res.deps.insert(in_node);
		}

		// Cross through any existing summary edges.
		const auto& compat_summs = graph.summaries[sub_comp][in_node];
		for (const auto& p : compat_summs) {
		    Ref<Node> out_node = p.first;

		    // Calculate the effect up to the summary exit node.
		    EffectRTL out_efft;
		    const EffectLTR& summ_efft = p.second;
		    if (!compose(in_efft, summ_efft, out_efft, top_level)) {
			continue;
		    }

#ifdef VIZ
		    if (!top_level) {
			reached_summs.insert(in_node, out_node, entry.to);
			reached_borders.of(out_node).of(entry.to)
			    .copy(out_efft);
		    }
#endif

		    // Cross through exit edges compatible with the previously
		    // entered entry edge.
		    for (const Exit& exit : comp.exits.primary()[entry.to]) {
			const Symbol& e_out_symb =
			    symbol_reg[exit.label.symbol];
			bool e_out_rev = exit.label.rev;

			// Calculate the effect up to the return node.
			EffectRTL full_efft;
			const EffectLTR& e_out_efft =
			    sec.effect_of(e_out_symb, e_out_rev, call_tag);
			if (!compose(out_efft, e_out_efft, full_efft,
				     top_level)) {
			    continue;
			}

			// Exit the box.
			FOR(e_out, graph.search(e_out_symb.ref, e_out_rev,
						out_node)[call_tag]) {
			    if (worklist.enqueue(e_out.get<DST>(), exit.to,
						 full_efft)) {

#ifdef VIZ
				if (!top_level) {
				    reached_edges.insert(out_node,
							 e_out.get<DST>(),
							 e_out_rev,
							 e_out_symb.ref,
							 call_tag);
				}
#endif

			    }
			}
		    }
		}
	    }
	}
    }

#ifdef VIZ
    if (!top_level) {
	fs::path out_path = dump_dir/(graph.nodes[start].name + ".dot");
	std::cout << "Dumping " << out_path << std::endl;
	std::ofstream fout(out_path.string());
	EXPECT((bool) fout);

	fout << "digraph __GRAPH {" << std::endl;
	fout << "id=\"__GRAPH\";" << std::endl;
	fout << "node [style=\"filled\"];" << std::endl;

	for (const auto& n_pair : worklist.reached()) {
	    if (n_pair.second.empty()) {
		continue;
	    }
	    dump_node(fout, graph.nodes[n_pair.first]);
	}

	for (const auto& n_pair : reached_borders) {
	    if (n_pair.second.empty()) {
		continue;
	    }
	    dump_node(fout, graph.nodes[n_pair.first]);
	}

	for (const auto& src_p : reached_edges) {
	    const std::string& src = graph.nodes[src_p.first].name;
	    for (const auto& dst_p : src_p.second) {
		const std::string& dst = graph.nodes[dst_p.first].name;
		fout << src << " -> " << dst
		     << " [id=\"" << src << "->" << dst << "\""
		     << ",tooltip=\"";
		FOR(tup, dst_p.second) {
		    Label label((*glob_syms)[tup.get<SYMBOL>()],
				tup.get<REV>(), tup.get<TAG>());
		    label.print(fout, *glob_syms, *glob_tags);
		    fout << " ";
		}
		fout << "\"];" << std::endl;
	    }
	}

	for (const auto& src_p : reached_summs) {
	    const std::string& src = graph.nodes[src_p.first].name;
	    for (const auto& dst_p : src_p.second) {
		const std::string& dst = graph.nodes[dst_p.first].name;
		fout << src << " -> " << dst
		     << " [id=\"" << src << "=>" << dst << "\""
		     << ",color=\"green\""
		     << ",tooltip=\"";
		FOR(tup, dst_p.second) {
		    Ref<Component> sub_comp = comp.boxes[tup.get<BOX>()].comp;
		    fout << glob_pri->components[sub_comp].name << " ";
		}
		fout << "\"];" << std::endl;
	    }
	}

	fout << "}" << std::endl;
    }

    if (!top_level) {
	fs::path out_path = dump_dir/(graph.nodes[start].name + ".summs");
	std::cout << "Dumping " << out_path << std::endl;
	std::ofstream fout(out_path.string());
	EXPECT((bool) fout);

	for (const auto& dst_pair : worklist.reached()) {
	    Ref<Node> dst = dst_pair.first;
	    for (const auto& pri_st_pair : dst_pair.second) {
		Ref<State> pri_st = pri_st_pair.first;
		std::string prefix = (graph.nodes[dst].name + " " +
				      comp.name + ":" +
				      comp.get_states()[pri_st].name + " ");
		print_effect(fout, prefix, pri_st_pair.second,
			     sec.components, *glob_tags);
	    }
	}

	for (const auto& dst_pair : reached_borders) {
	    Ref<Node> dst = dst_pair.first;
	    for (const auto& box_pair : dst_pair.second) {
		Ref<Box> box = box_pair.first;
		std::string prefix = (graph.nodes[dst].name + " " +
				      comp.name + ":" +
				      comp.boxes[box].name + " ");
		print_effect(fout, prefix, box_pair.second,
			     sec.components, *glob_tags);
	    }
	}

	FOR(tup, reached_summs) {
	    std::string prefix = (graph.nodes[tup.get<SRC>()].name + "=>" +
				  graph.nodes[tup.get<DST>()].name + " " +
				  comp.name + ":" +
				  comp.boxes[tup.get<BOX>()].name + " ");
	    print_effect(fout, prefix,
			 graph.summaries[comp.boxes[tup.get<BOX>()].comp]
					[tup.get<SRC>()][tup.get<DST>()],
			 sec.components, *glob_tags);
	}
    }
#endif

    return res;
}

// MAIN =======================================================================

int main(int argc, char* argv[]) {
    // Timekeeping
    const unsigned int t_input = current_time();

    // User-defined parameters
    std::string pri_dir;
    std::string sec_dir;
    std::string graph_dir;
    std::string summ_dir;

    // Parse options
    po::options_description desc("Options");
    desc.add_options()
	("help,h", "Print help message")
	("pri-dir", po::value<std::string>(&pri_dir)->required(),
	 "Directory of primary RSM components")
	("sec-dir", po::value<std::string>(&sec_dir)->required(),
	 "Directory of secondary RSM components")
	("graph-dir", po::value<std::string>(&graph_dir)->required(),
	 "Directory of edge files")
	("summ-dir", po::value<std::string>(&summ_dir)->required(),
	 "Directory to store the summaries");
    po::positional_options_description pos_desc;
    pos_desc.add("pri-dir", 1);
    pos_desc.add("sec-dir", 1);
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
    std::cout << "Parsing primary RSM from " << pri_dir
	      << ", secondary RSM from " << sec_dir << std::endl;
    Analysis spec(pri_dir, sec_dir);
    glob_pri = &(spec.pri);
    glob_syms = &(spec.symbols);
    glob_tags = &(spec.tags);
    std::cout << "Parsing graph from " << graph_dir << std::endl;
    Graph graph(spec.symbols, spec.tags, graph_dir);
    graph.print_stats(std::cout);

    // Timekeeping
    const unsigned int t_solving = current_time();
    std::cout << "Input parsing: " << t_solving - t_input << " ms"
	      << std::endl << std::endl;

    // Perform actual solving
    fs::path dump_dir = fs::path(summ_dir)/"dump";
    fs::create_directory(dump_dir);
    spec.close(graph, dump_dir);

    // Timekeeping
    const unsigned int t_output = current_time();

    // Print the output
    std::cout << "Printing summaries" << std::endl;
    graph.print_summaries(summ_dir, spec.pri.components, spec.sec.components,
			  spec.tags);

    // Timekeeping
    std::cout << "Output printing: " << current_time() - t_output << " ms"
	      << std::endl;

    return EXIT_SUCCESS;
}
