#include <boost/algorithm/string.hpp>
#include <boost/none.hpp>
#include <boost/program_options.hpp>
#include <boost/regex.hpp>
#include <cstring>
#include <fstream>
#include <iomanip>
#include <list>
#include <locale>
#include <sstream>

#include "util.hpp"

namespace po = boost::program_options;
namespace fs = boost::filesystem;

// CONSTANTS ==================================================================

enum class ParsingMode {NODES, EDGES};
extern const boost::none_t NONE = boost::none;
const std::string PARAM_FILE_EXT = ".pdat";
const std::string NON_PARAM_FILE_EXT = ".ndat";
const std::string RSM_FILE_EXT = ".rsm.tgf";

TUPLE_TAG(SYMBOL);
TUPLE_TAG(REV);
TUPLE_TAG(TAG);
TUPLE_TAG(ID);
TUPLE_TAG(NODE);
TUPLE_TAG(SRC);
TUPLE_TAG(DST);
TUPLE_TAG(MOD);

TUPLE_TAG(COMP);
TUPLE_TAG(STATE);
TUPLE_TAG(BOX);
TUPLE_TAG(PRI_CP);
TUPLE_TAG(PRI_ST);
TUPLE_TAG(SEC_CP);
TUPLE_TAG(SEC_ST);

TUPLE_TAG(FROM);
TUPLE_TAG(TO);
TUPLE_TAG(SEC_CP_FROM);
TUPLE_TAG(SEC_ST_FROM);
TUPLE_TAG(SEC_CP_TO);
TUPLE_TAG(SEC_ST_TO);

// BASIC NAMED OBJECTS ========================================================

class Symbol {
    friend Registry<Symbol>;
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Symbol> ref;
    const bool parametric;
private:
    explicit Symbol(const std::string* name, Ref<Symbol> ref, bool parametric)
        : name(*name), ref(ref), parametric(parametric) {
        EXPECT(boost::regex_match(*name, boost::regex("[a-z]\\w*")));
    }
    bool merge(bool parametric) {
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
    explicit Tag(const std::string* name, Ref<Tag> ref)
        : name(*name), ref(ref) {}
    bool merge() {
        return false;
    }
};

class Node {
    friend Registry<Node>;
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Node> ref;
private:
    explicit Node(const std::string* name, Ref<Node> ref)
        : name(*name), ref(ref) {}
    bool merge() {
        return false;
    }
};

// GRAPH STORAGE ==============================================================

Registry<Symbol> symbols;
Registry<Tag> tags;
Registry<Node> nodes;
// XXX: At the point of declaration, neither 'symbols' nor 'nodes' have been
// filled in, and the required capacity of some of the nested containers
// depends on the sizes of those. If we construct 'edges' at this point, it
// will not reserve enough space for all symbols and nodes.
typedef mi::MultiIndex<
            mi::FlatIndex<SRC, Ref<Node>, nodes,
                mi::FlatIndex<REV, bool, NONE,
                    mi::LightIndex<SYMBOL, Ref<Symbol>,
                        mi::Index<TAG, Ref<Tag>,
                            mi::Table<DST, Ref<Node> > > > > >,
            mi::FlatIndex<REV, bool, NONE,
                mi::FlatIndex<SYMBOL, Ref<Symbol>, symbols,
                    mi::Index<TAG, Ref<Tag>,
                        mi::MultiIndex<
                            mi::Table<SRC, Ref<Node> >,
                            mi::Table<DST, Ref<Node> > > > > > > EdgeSet;
EdgeSet* edges = NULL;

typedef EdgeSet::Tuple Edge;
typedef mi::NamedTuple<REV, bool,
            mi::NamedTuple<SYMBOL, Ref<Symbol>,
                mi::NamedTuple<TAG, Ref<Tag>,
                    mi::NamedTuple<DST, Ref<Node>, mi::Nil> > > > EdgeTail;

// TODO: Performing two passes over the edge files.
void parse_edge_file(const fs::path& fpath, ParsingMode mode) {
    std::string fbase(fpath.filename().string());
    std::string name;
    bool parametric;
    if (boost::algorithm::ends_with(fbase, PARAM_FILE_EXT)) {
        size_t name_len = fbase.size() - PARAM_FILE_EXT.size();
        name = fbase.substr(0, name_len);
        parametric = true;
    } else if (boost::algorithm::ends_with(fbase, NON_PARAM_FILE_EXT)) {
        size_t name_len = fbase.size() - NON_PARAM_FILE_EXT.size();
        name = fbase.substr(0, name_len);
        parametric = false;
    } else {
        return;
    }
    const Symbol& symbol = symbols.add(name, parametric);

    std::cout << "Parsing " << fpath << std::endl;
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
        EXPECT((parametric && toks.size() == 3) ||
               (!parametric && toks.size() == 2));

        switch (mode) {
        case ParsingMode::NODES: {
            nodes.add(toks[0]);
            nodes.add(toks[1]);
        } break;
        case ParsingMode::EDGES: {
            Ref<Tag> tag;
            if (parametric) {
                tag = tags.add(toks[2]).ref;
            }
            Ref<Node> src = nodes.find(toks[0]).ref;
            Ref<Node> dst = nodes.find(toks[1]).ref;
            // Add the edge twice, once forwards and once backwards.
            // TODO: This simplifies the interface, but also increases space
            // requirements (but doesn't necessarily double them; if we didn't
            // do this we'd need to keep an index on the destination).
            edges->insert(src, false, symbol.ref, tag, dst);
            edges->insert(dst, true, symbol.ref, tag, src);
        } break;
        default:
            assert(false);
        }
    }

    EXPECT(fin.eof());
}

void parse_graph(const std::string& dirname) {
    Directory dir(dirname);
    std::cout << "Parsing nodes" << std::endl;
    for (const fs::path& fpath : dir) {
        parse_edge_file(fpath, ParsingMode::NODES);
    }
    // After all symbols & nodes have been collected, we can initialize the
    // edges container, and the contructor will pick the correct capacities
    // automatically.
    edges = new EdgeSet;
    std::cout << "Parsing edges" << std::endl;
    for (const fs::path& fpath : dir) {
        parse_edge_file(fpath, ParsingMode::EDGES);
    }
}

// ARROW LABELS ===============================================================

// A valid tag means "only follow that specific tag", an invalid tag means
// "follow any tag".
typedef mi::NamedTuple<REV, bool,
            mi::NamedTuple<SYMBOL, Ref<Symbol>,
                mi::NamedTuple<TAG, Ref<Tag>, mi::Nil> > > Label;

Label parse_label(const std::string& str) {
    static const boost::regex r("(_)?([a-z]\\w*)(?:\\[(\\w+|\\*)\\])?");
    boost::smatch m;
    bool matched = boost::regex_match(str, m, r);
    EXPECT(matched);
    bool rev = m[1].matched;
    std::string symbol_name(m[2].first, m[2].second);
    std::string tag_name(m[3].first, m[3].second);
    bool parametric = m[3].matched;
    const Symbol& symbol = symbols.find(symbol_name);
    EXPECT(symbol.parametric == parametric);
    Ref<Tag> tag;
    if (parametric && tag_name != "*") {
        tag = tags.add(tag_name).ref;
    }
    return Label(rev, symbol.ref, tag);
}

std::ostream& operator<<(std::ostream& os, const Label& label) {
    Ref<Tag> tag = label.get<TAG>();
    if (label.get<REV>()) {
        os << "_";
    }
    os << symbols[label.get<SYMBOL>()].name
       << "[" << (tag.valid() ? tags[tag].name : "*") << "]";
    return os;
}

typedef mi::NamedTuple<REV, bool,
            mi::NamedTuple<SYMBOL, Ref<Symbol>, mi::Nil> > MatchLabel;

MatchLabel parse_match_label(const std::string& str) {
    static const boost::regex r("(_)?([a-z]\\w*)\\[[a-z]\\]");
    boost::smatch m;
    bool matched = boost::regex_match(str, m, r);
    EXPECT(matched);
    bool rev = m[1].matched;
    std::string symbol_name(m[2].first, m[2].second);
    const Symbol& symbol = symbols.find(symbol_name);
    EXPECT(symbol.parametric);
    return MatchLabel(rev, symbol.ref);
}

std::ostream& operator<<(std::ostream& os, const MatchLabel& label) {
    if (label.get<REV>()) {
        os << "_";
    }
    os << symbols[label.get<SYMBOL>()].name << "[i]";
    return os;
}

// STATE MACHINE PARTS ========================================================

class Component;

class State {
    friend Registry<State>;
    typedef std::string Key;
public:
    const std::string name;
    const Ref<State> ref;
    const bool initial;
    const bool final;
private:
    explicit State(const std::string* name, Ref<State> ref, bool initial,
                   bool final)
        : name(*name), ref(ref), initial(initial), final(final) {}
    bool merge(bool initial, bool final) {
        EXPECT(initial == this->initial && final == this->final);
        return false;
    }
public:
    friend std::ostream& operator<<(std::ostream& os, const State& state) {
        os << state.name;
        if (state.initial) {
            os << " in";
        }
        if (state.final) {
            os << " out";
        }
        return os;
    }
};

class Box {
    friend Registry<Box>;
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Box> ref;
    const Ref<Component> comp;
private:
    explicit Box(const std::string* name, Ref<Box> ref, Ref<Component> comp)
        : name(*name), ref(ref), comp(comp) {}
    bool merge(Ref<Component> comp) {
        EXPECT(comp == this->comp);
        return false;
    }
public:
    // print function moved below Component
};

typedef mi::MultiIndex<
            mi::Index<FROM, Ref<State>,
                mi::Index<TO, Ref<State>,
                    mi::FlatIndex<REV, bool, NONE,
                        mi::Index<SYMBOL, Ref<Symbol>,
                            mi::Table<TAG, Ref<Tag> > > > > >,
            mi::FlatIndex<REV, bool, NONE,
                mi::FlatIndex<SYMBOL, Ref<Symbol>, symbols,
                    mi::MultiIndex<
                        mi::Table<FROM, Ref<State> >,
                        mi::Table<TO, Ref<State> > > > >,
            mi::Index<FROM, Ref<State>,
                mi::FlatIndex<REV, bool, NONE,
                    mi::Index<SYMBOL, Ref<Symbol>,
                        mi::Index<TAG, Ref<Tag>,
                            mi::Table<TO, Ref<State> > > > > > >
    TransitionSet;

typedef TransitionSet::Tuple Transition;

void print(std::ostream& os, const Transition& trans,
           const Registry<State>& state_reg) {
    os << state_reg[trans.get<FROM>()].name << " "
       << state_reg[trans.get<TO>()].name << " "
       << trans.tl.tl; // print the Label (last 3 fields)
}

typedef mi::MultiIndex<
            mi::Index<FROM, Ref<State>,
                mi::Index<TO, Ref<Box>,
                    mi::FlatIndex<REV, bool, NONE,
                        mi::Table<SYMBOL, Ref<Symbol> > > > >,
            mi::Index<TO, Ref<Box>,
                mi::FlatIndex<REV, bool, NONE,
                    mi::Table<SYMBOL, Ref<Symbol> > > >,
            mi::FlatIndex<REV, bool, NONE,
                mi::FlatIndex<SYMBOL, Ref<Symbol>, symbols,
                    mi::MultiIndex<
                        mi::Table<FROM, Ref<State> >,
                        mi::Table<TO, Ref<Box> > > > >,
            mi::Index<FROM, Ref<State>,
                mi::FlatIndex<REV, bool, NONE,
                    mi::Index<SYMBOL, Ref<Symbol>,
                        mi::Table<TO, Ref<Box> > > > > > EntrySet;

typedef EntrySet::Tuple Entry;

void print(std::ostream& os, const Entry& entry,
           const Registry<State>& state_reg, const Registry<Box>& box_reg) {
    os << state_reg[entry.get<FROM>()].name << " "
       << box_reg[entry.get<TO>()].name << " "
       << entry.tl.tl; // print the MatchLabel (last 2 fields)
}

typedef mi::MultiIndex<
            mi::Index<FROM, Ref<Box>,
                mi::Index<TO, Ref<State>,
                    mi::FlatIndex<REV, bool, NONE,
                        mi::Table<SYMBOL, Ref<Symbol> > > > >,
            mi::Index<FROM, Ref<Box>,
                mi::FlatIndex<REV, bool, NONE,
                    mi::Table<SYMBOL, Ref<Symbol> > > >,
            mi::FlatIndex<REV, bool, NONE,
                mi::FlatIndex<SYMBOL, Ref<Symbol>, symbols,
                    mi::MultiIndex<
                        mi::Table<FROM, Ref<Box> >,
                        mi::Table<TO, Ref<State> > > > >,
            mi::Index<FROM, Ref<Box>,
                mi::FlatIndex<REV, bool, NONE,
                    mi::FlatIndex<SYMBOL, Ref<Symbol>, symbols,
                        mi::Table<TO, Ref<State> > > > > > ExitSet;

typedef ExitSet::Tuple Exit;

void print(std::ostream& os, const Exit& exit,
           const Registry<State>& state_reg, const Registry<Box>& box_reg) {
    os << box_reg[exit.get<FROM>()].name << " "
       << state_reg[exit.get<TO>()].name << " "
       << exit.tl.tl; // print the MatchLabel (last 2 fields)
}

// RSM ========================================================================

class Component {
    friend Registry<Component>;
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Component> ref;
    Registry<State> states;
    Registry<Box> boxes;
    TransitionSet transitions;
    EntrySet entries;
    ExitSet exits;
    Ref<State> initial;
    std::set<Ref<State> > final;
private:
    explicit Component(const std::string* name, Ref<Component> ref)
        : name(*name), ref(ref) {
        EXPECT(boost::regex_match(*name, boost::regex("[A-Z]\\w*")));
    }
    bool merge() {
        return false;
    }
    const State& add_state(const std::string& name, bool initial, bool final) {
        const State& s = states.make(name, initial, final);
        if (initial) {
            EXPECT(!this->initial.valid());
            this->initial = s.ref;
        }
        if (final) {
            this->final.insert(s.ref);
        }
        return s;
    }
public:
    void parse(const fs::path& fpath, const Registry<Component>& comp_reg) {
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
                    // Verify that the component has a single initial state,
                    // and at least one final state.
                    EXPECT(initial.valid());
                    EXPECT(!final.empty());
                    mode = ParsingMode::EDGES;
                    break;
                }
                bool can_be_state = true, initial = false, final = false;
                bool can_be_box = true;
                Ref<Component> box_rsm;
                for (auto iter = toks.begin()+1; iter != toks.end(); ++iter) {
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
                    EXPECT(!boxes.contains(toks[0]));
                    EXPECT(!box_rsm.valid());
                    add_state(toks[0], initial, final);
                } else {
                    EXPECT(!states.contains(toks[0]));
                    EXPECT(can_be_box && box_rsm.valid());
                    boxes.make(toks[0], box_rsm);
                }
            } break;

            case ParsingMode::EDGES: {
                // We know that box names and transition names are disjoint.
                EXPECT(toks.size() >= 3);
                std::list<std::string>
                    label_toks(toks.begin() + 2, toks.end());
                bool src_is_state = states.contains(toks[0]);
                bool dst_is_state = states.contains(toks[1]);
                EXPECT(src_is_state || dst_is_state);
                if (src_is_state && dst_is_state) {
                    Ref<State> from = states.find(toks[0]).ref;
                    Ref<State> to = states.find(toks[1]).ref;
                    for (const std::string& t : label_toks) {
                        Label label = parse_label(t);
                        transitions.insert(from, to, label);
                    }
                } else if (src_is_state && !dst_is_state) {
                    Ref<State> from = states.find(toks[0]).ref;
                    Ref<Box> to = boxes.find(toks[1]).ref;
                    for (const std::string& t : label_toks) {
                        MatchLabel label = parse_match_label(t);
                        entries.insert(from, to, label);
                    }
                } else if (!src_is_state && dst_is_state) {
                    Ref<Box> from = boxes.find(toks[0]).ref;
                    Ref<State> to = states.find(toks[1]).ref;
                    for (const std::string& t : label_toks) {
                        MatchLabel label = parse_match_label(t);
                        exits.insert(from, to, label);
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
    friend void print(std::ostream& os, const Component& comp,
                      const Registry<Component>& comp_reg);
};

// Moved here because Registry<Component> needs Component to be fully defined.
void print(std::ostream& os, const Box& box,
           const Registry<Component>& comp_reg) {
    os << box.name << " " << comp_reg[box.comp].name;
}

void print(std::ostream& os, const Component& comp,
           const Registry<Component>& comp_reg) {
    for (const State& s : comp.states) {
        os << s << std::endl;
    }
    for (const Box& b : comp.boxes) {
        print(os, b, comp_reg);
        os << std::endl;
    }
    os << "#" << std::endl;
    FOR(t, comp.transitions) {
        print(os, t, comp.states);
        os << std::endl;
    }
    FOR(e, comp.entries) {
        print(os, e, comp.states, comp.boxes);
        os << std::endl;
    }
    FOR(e, comp.exits) {
        print(os, e, comp.states, comp.boxes);
        os << std::endl;
    }
}

class RSM {
public:
    Registry<Component> components;
private:
    void parse_file(const fs::path& fpath) {
        std::string fbase(fpath.filename().string());
        if (!boost::algorithm::ends_with(fbase, RSM_FILE_EXT)) {
            return;
        }
        std::cout << "Parsing " << fpath << std::endl;
        size_t name_len = fbase.size() - RSM_FILE_EXT.size();
        std::string name(fbase.substr(0, name_len));
        Component& comp = components.make(name);
        comp.parse(fpath, components);
    }
public:
    explicit RSM(const std::string& dirname) {
        Directory dir(dirname);
        std::list<fs::path> paths(dir.begin(), dir.end());
        // TODO: Assuming the usage order of components is the same as their
        // alphabetic order.
        paths.sort();
        for (const fs::path& p : paths) {
            parse_file(p);
        }
        // TODO: If this is a secondary RSM, verify that it doesn't introduce
        // new symbols, and it covers all symbols on the primary one.
    }
    friend std::ostream& operator<<(std::ostream& os, const RSM& rsm) {
        for (const Component& comp : rsm.components) {
            os << comp.name << ":" << std::endl;
            print(os, comp, rsm.components);
        }
        return os;
    }
};

RSM* pri_rsm = NULL;
RSM* sec_rsm = NULL; // TODO: Exactly one secondary RSM for now

// FUNCTION BOUNDS ============================================================

mi::Index<PRI_CP, Ref<Component>, // = PRI_CP_FROM = PRI_CP_TO
    // PRI_ST_FROM = PRI_CP's initial state
    mi::Index<SRC, Ref<Node>,
        mi::Index<SEC_CP_FROM, Ref<Component>,
            mi::Index<SEC_ST_FROM, Ref<State>,
                // PRI_ST_TO = any of PRI_CP's final states
                mi::Index<DST, Ref<Node>,
                    mi::Index<SEC_CP_TO, Ref<Component>,
                        mi::Table<SEC_ST_TO, Ref<State> > > > > > > > funs;

typedef decltype(funs)::Tuple Function;

// XXX: Not unique if there's underscores in the individual names.
std::ostream& operator<<(std::ostream& os, const Function& fun) {
    const Component& sec_cp_from = sec_rsm->components[fun.get<SEC_CP_FROM>()];
    const Component& sec_cp_to = sec_rsm->components[fun.get<SEC_CP_TO>()];
    os << pri_rsm->components[fun.get<PRI_CP>()].name << "_"
       << nodes[fun.get<SRC>()].name << "_"
       << nodes[fun.get<DST>()].name << "_"
       << sec_cp_from.name << "_"
       << sec_cp_from.states[fun.get<SEC_ST_FROM>()].name << "_"
       << sec_cp_to.name << "_"
       << sec_cp_to.states[fun.get<SEC_ST_TO>()].name;
    return os;
}

// Given a pair of entry/exit MatchLabels derived from the primary RSM, find
// compatible entry/exit nodes on the graph, and all secondary RSM states that
// we could ever occupy at those points. We ignore the specific tags on the
// secondary arrows during this search (this is sound but might cause us to
// include more secondary states than necessary).
void enum_funs(Ref<Component> callee, const MatchLabel& in_label,
               const MatchLabel& out_label) {
    // Collect all the states where a secondary RSM arrow compatible with
    // 'in_label' might lead.
    bool in_rev = in_label.get<REV>();
    Ref<Symbol> in_symb = in_label.get<SYMBOL>();
    std::list<std::pair<Ref<Component>, Ref<State> > > sec_from_set;
    for (const Component& sec_comp : sec_rsm->components) {
        // For any compatible transition, include the state where it arrives.
        for (Ref<State> s
                 : sec_comp.transitions.sec<0>()[in_rev][in_symb].sec<0>()) {
            sec_from_set.emplace_back(sec_comp.ref, s);
        }
        // For any compatible entry, include the initial state of the component
        // being entered.
        for (Ref<Box> b
                 : sec_comp.entries.sec<1>()[in_rev][in_symb].sec<0>()) {
            const Component& entered =
                sec_rsm->components[sec_comp.boxes[b].comp];
            sec_from_set.emplace_back(entered.ref, entered.initial);
        }
        // For any compatible exit, include the state where it arrives.
        for (Ref<State> s
                 : sec_comp.exits.sec<1>()[in_rev][in_symb].sec<0>()) {
            sec_from_set.emplace_back(sec_comp.ref, s);
        }
    }
    if (sec_from_set.empty()) {
        return;
    }

    // Collect all the states where a secondary RSM arrow compatible with
    // 'out_label' might originate.
    bool out_rev = out_label.get<REV>();
    Ref<Symbol> out_symb = out_label.get<SYMBOL>();
    std::list<std::pair<Ref<Component>, Ref<State> > > sec_to_set;
    for (const Component& sec_comp : sec_rsm->components) {
        // For any compatible transition, include the state where it starts.
        for (Ref<State> s
                 : sec_comp.transitions.sec<0>()[out_rev][out_symb].pri()) {
            sec_to_set.emplace_back(sec_comp.ref, s);
        }
        // For any compatible entry, include the state where it starts.
        for (Ref<State> s
                 : sec_comp.entries.sec<1>()[out_rev][out_symb].pri()) {
            sec_to_set.emplace_back(sec_comp.ref, s);
        }
        // For any compatible exit, include all final states of the component
        // being exited.
        for (Ref<Box> b
                 : sec_comp.exits.sec<1>()[out_rev][out_symb].pri()) {
            const Component& exited =
                sec_rsm->components[sec_comp.boxes[b].comp];
            for (Ref<State> s : exited.final) {
                sec_to_set.emplace_back(exited.ref, s);
            }
        }
    }
    if (sec_to_set.empty()) {
        return;
    }

    // Find pairs of edges that match the entry and exit MatchLabel
    // respectively. Then only keep those with matching tags.
    // The symbols on MatchLabels must be parametric, so we're certain that the
    // corresponding edges will have valid tags.
    typedef mi::MultiIndex<mi::Table<SRC, Ref<Node> >,
                           mi::Table<DST, Ref<Node> > > EndpStore;
    auto record_endps = [&](const EndpStore& in_matches,
                            const EndpStore& out_matches, Ref<Tag>) {
        for (Ref<Node> in : in_matches.sec<0>()) {
            for (Ref<Node> out : out_matches.pri()) {
                for (const auto& sec_from : sec_from_set) {
                    for (const auto& sec_to : sec_to_set) {
                        funs.insert(callee,
                                    in,  sec_from.first, sec_from.second,
                                    out, sec_to.first,   sec_to.second);
                    }
                }
            }
        }
    };
    join_zip<1>(edges->sec<0>()[in_rev][in_symb],
                edges->sec<0>()[out_rev][out_symb], record_endps);
}

// Take the primary RSM and extract the patterns for the edges that could cause
// us to enter/exit a box (recursive call to another component). Search the
// graph for pairs of edges that could function as such entries/exits, then
// output the corresponding entry/exit nodes as the "functions" to summarize
// over. Actually, for each entry/exit node pair, emit one function for each
// secondary RSM state we could enter/exit at.
void enum_funs() {
    typedef mi::FlatIndex<REV, bool, NONE,
                          mi::Table<SYMBOL, Ref<Symbol> > > MatchLabelSet;
    for (const Component& pri_comp : pri_rsm->components) {
        auto process = [&](const MatchLabelSet& in_labels,
                           const MatchLabelSet& out_labels, Ref<Box> box) {
            FOR(i, in_labels) {
                FOR(o, out_labels) {
                    enum_funs(pri_comp.boxes[box].comp, i, o);
                }
            }
        };
        // Process entry/exit pairs that correspond to the same box.
        join_zip<1>(pri_comp.entries.sec<0>(), pri_comp.exits.sec<0>(),
                    process);
    }
}

// FUNCTION-RSM INTERSECTION ==================================================

struct SecStackMod {
    bool is_entry;
    Ref<Component> sec_comp;
    Ref<Box> sec_box;
    Ref<Tag> tag;
    friend std::ostream& operator<<(std::ostream& os, const SecStackMod& mod) {
        const Component& sec_comp = sec_rsm->components[mod.sec_comp];
        os << (mod.is_entry ? "(" : ")") << "_"
           << sec_comp.name << "_"
           << sec_comp.boxes[mod.sec_box].name << "_"
           << tags[mod.tag].name;
        return os;
    }
};

class SecMoves {
public:
    typedef mi::NamedTuple<SEC_CP_FROM, Ref<Component>,
                mi::NamedTuple<SEC_ST_FROM, Ref<State>,
                    mi::NamedTuple<SEC_CP_TO, Ref<Component>,
                        mi::NamedTuple<SEC_ST_TO, Ref<State>, mi::Nil> > > >
        SecEndPts;
    typedef mi::NamedTuple<MOD, SecStackMod, SecEndPts> SecModEndPts;
private:
    std::deque<SecEndPts> eps_moves_;
    std::deque<SecModEndPts> stack_moves_;
public:
    explicit SecMoves(const Component& sec_comp, Ref<State> sec_state,
                      const EdgeTail& e) {
        bool rev = e.get<REV>();
        Ref<Symbol> symb = e.get<SYMBOL>();
        Ref<Tag> tag = e.get<TAG>();
        // Follow simple transitions out of the current state.
        const auto& trans =
            sec_comp.transitions.sec<1>()[sec_state][rev][symb];
        for (Ref<State> s : trans[Ref<Tag>()]) {
            // transitions with invalid tag, i.e. "*" moves for parametric
            // symbols, only kind of transitions for non-parametric symbols
            eps_moves_.emplace_back(sec_comp.ref, sec_state, sec_comp.ref, s);
        }
        if (symbols[symb].parametric) {
            // for parametric symbols, also check for specific-tag transitions
            assert(tag.valid());
            for (Ref<State> s : trans[tag]) {
                eps_moves_.emplace_back(sec_comp.ref, sec_state,
                                        sec_comp.ref, s);
            }
        }
        // Follow entries out of sec_state: move to the initial state of the
        // entered component, record the parameters of the stack change.
        for (Ref<Box> b : sec_comp.entries.sec<2>()[sec_state][rev][symb]) {
            Ref<Component> entered = sec_comp.boxes[b].comp;
            Ref<State> entered_init = sec_rsm->components[entered].initial;
            SecStackMod mod{true, sec_comp.ref, b, tag};
            stack_moves_.emplace_back(mod, sec_comp.ref, sec_state, entered,
                                      entered_init);
        }
        // Check if at a final state => can exit to any calling component
        // (assuming the crossed edge can function as a box exit).
        if (!sec_comp.final.count(sec_state)) {
            return;
        }
        for (const Component& parent : sec_rsm->components) {
            for (const Box& b : parent.boxes) {
                if (b.comp != sec_comp.ref) {
                    continue;
                }
                for (Ref<State> s : parent.exits.sec<2>()[b.ref][rev][symb]) {
                    SecStackMod mod{false, parent.ref, b.ref, tag};
                    stack_moves_.emplace_back(mod, sec_comp.ref, sec_state,
                                              parent.ref, s);
                }
            }
        }
    }
    const std::deque<SecEndPts>& eps_moves() const {
        return eps_moves_;
    }
    const std::deque<SecModEndPts>& stack_moves() const {
        return stack_moves_;
    }
    bool empty() const {
        return eps_moves_.empty() && stack_moves_.empty();
    }
};

// pri_comp is implicit (the one we're summarizing over)
struct SuperNode {
    Ref<Node> node;
    Ref<State> pri_state;
    Ref<Component> sec_comp;
    Ref<State> sec_state;
    // The default constructor creates an invalid SuperNode.
    explicit SuperNode() {}
    explicit SuperNode(Ref<Node> node, Ref<State> pri_state,
                       Ref<Component> sec_comp, Ref<State> sec_state)
        : node(node), pri_state(pri_state), sec_comp(sec_comp),
          sec_state(sec_state) {}
    bool valid() const {
        return node.valid();
    }
    friend void print(std::ostream& os, const SuperNode& sn,
                      const Component& pri_comp) {
        const Component& sec_comp = sec_rsm->components[sn.sec_comp];
        os << nodes[sn.node].name << "_"
           << pri_comp.states[sn.pri_state].name << "_"
           << sec_comp.name << "_"
           << sec_comp.states[sn.sec_state].name;
    }
};

typedef unsigned int PointID;

struct Move {
    PointID from;
    PointID to;
};

struct StackMove {
    PointID from;
    PointID to;
    SecStackMod mod;
};

struct CallMove {
    PointID from;
    PointID to;
    Function callee;
};

class Intersection {
private:
    const Function fun_;
    const Component& pri_comp_;
    mi::Index<NODE, Ref<Node>,
        mi::Index<PRI_ST, Ref<State>,
            mi::Index<SEC_CP, Ref<Component>,
                mi::Index<SEC_ST, Ref<State>,
                    mi::Cell<ID, PointID> > > > > sn2id_;
    std::vector<SuperNode> id2sn_;
    PointID initial_;
    std::set<PointID> finals_;
    std::deque<PointID> worklist_;
    std::deque<Move> eps_moves_;
    std::deque<StackMove> stack_moves_;
    std::deque<CallMove> call_moves_;
private:
    PointID add_node(const SuperNode& sn) {
        auto& id_cell =
            sn2id_.of(sn.node).of(sn.pri_state).of(sn.sec_comp)
            .of(sn.sec_state);
        if (id_cell.size() < 1) {
            id_cell.insert(id2sn_.size());
            worklist_.push_back(id2sn_.size());
            id2sn_.push_back(sn);
        }
        return id_cell.get();
    }
    PointID mktemp() {
        id2sn_.emplace_back(); // default-construct an invalid SuperNode
        return id2sn_.size() - 1;
    }
    const SuperNode& curr() const {
        return id2sn_[worklist_.front()];
    }
    PointID curr_id() const {
        return worklist_.front();
    }
    void cross_trans(const EdgeTail& e, const SecMoves& sec_moves) {
        std::list<Ref<State> > pri_st_to;
        const auto& trans =
            pri_comp_.transitions.sec<1>()[curr().pri_state][e.get<REV>()]
            [e.get<SYMBOL>()];
        for (Ref<State> s : trans[Ref<Tag>()]) {
            pri_st_to.push_back(s);
        }
        if (symbols[e.get<SYMBOL>()].parametric) {
            assert(e.get<TAG>().valid());
            for (Ref<State> s : trans[e.get<TAG>()]) {
                pri_st_to.push_back(s);
            }
        }
        for (Ref<State> s : pri_st_to) {
            for (const auto& move : sec_moves.eps_moves()) {
                SuperNode next(e.get<DST>(), s, move.get<SEC_CP_TO>(),
                               move.get<SEC_ST_TO>());
                PointID next_id = add_node(next);
                eps_moves_.push_back(Move{curr_id(), next_id});
            }
            for (const auto& move : sec_moves.stack_moves()) {
                SuperNode next(e.get<DST>(), s, move.get<SEC_CP_TO>(),
                               move.get<SEC_ST_TO>());
                PointID next_id = add_node(next);
                stack_moves_.push_back(StackMove{curr_id(), next_id,
                                                 move.get<MOD>()});
            }
        }
    }
    void cross_entries(const EdgeTail& e, const SecMoves& sec_moves) {
        for (Ref<Box> b : pri_comp_.entries.sec<2>()[curr().pri_state]
                 [e.get<REV>()][e.get<SYMBOL>()]) {
            for (const auto& move : sec_moves.eps_moves()) {
                PointID next_id = mktemp();
                eps_moves_.push_back(Move{curr_id(), next_id});
                cross_call(b, e, next_id,
                           move.get<SEC_CP_TO>(), move.get<SEC_ST_TO>());
            }
            for (const auto& move : sec_moves.stack_moves()) {
                PointID next_id = mktemp();
                stack_moves_.push_back(StackMove{curr_id(), next_id,
                                                 move.get<MOD>()});
                cross_call(b, e, next_id,
                           move.get<SEC_CP_TO>(), move.get<SEC_ST_TO>());
            }
        }
    }
    void cross_call(Ref<Box> box, const EdgeTail& e_in, PointID in_id,
                    Ref<Component> sec_cp_in, Ref<State> sec_st_in) {
        Ref<Component> entered = pri_comp_.boxes[box].comp;
        FOR(f, funs[entered][e_in.get<DST>()][sec_cp_in][sec_st_in]) {
            PointID out_id = mktemp();
            Function callee(entered, e_in.get<DST>(), sec_cp_in, sec_st_in,
                            f.get<DST>(), f.get<SEC_CP_TO>(),
                            f.get<SEC_ST_TO>());
            call_moves_.push_back(CallMove{in_id, out_id, callee});
            cross_exits(box, e_in.get<TAG>(), callee, out_id);
        }
    }
    void cross_exits(Ref<Box> box, Ref<Tag> tag, const Function& callee,
                     PointID out_id) {
        FOR(exit, pri_comp_.exits.sec<2>()[box]) {
            for (Ref<Node> n : edges->pri()[callee.get<DST>()][exit.get<REV>()]
                     [exit.get<SYMBOL>()][tag]) {
                EdgeTail e_out(exit.get<REV>(), exit.get<SYMBOL>(), tag, n);
                SecMoves
                    moves_out(sec_rsm->components[callee.get<SEC_CP_TO>()],
                              callee.get<SEC_ST_TO>(), e_out);
                for (const auto& move : moves_out.eps_moves()) {
                    SuperNode next(n, exit.get<TO>(), move.get<SEC_CP_TO>(),
                                   move.get<SEC_ST_TO>());
                    PointID next_id = add_node(next);
                    eps_moves_.push_back(Move{out_id, next_id});
                }
                for (const auto& move : moves_out.stack_moves()) {
                    SuperNode next(n, exit.get<TO>(), move.get<SEC_CP_TO>(),
                                   move.get<SEC_ST_TO>());
                    PointID next_id = add_node(next);
                    stack_moves_.push_back(StackMove{out_id, next_id,
                                                     move.get<MOD>()});
                }
            }
        }
    }
    void print_point(std::ostream& os, PointID id) const {
        const SuperNode& sn = id2sn_[id];
        // XXX: The two cases should be disjoint, since SuperNode's use
        // underscores to separate their components.
        if (sn.valid()) {
            print(os, sn, pri_comp_);
        } else {
            os << id;
        }
    }
public:
    explicit Intersection(const Function& fun)
        : fun_(fun), pri_comp_(pri_rsm->components[fun.get<PRI_CP>()]) {
        // Emit function endpoints as SuperNodes.
        initial_ = add_node(SuperNode(fun.get<SRC>(), pri_comp_.initial,
                                      fun.get<SEC_CP_FROM>(),
                                      fun.get<SEC_ST_FROM>()));
        for (Ref<State> s : pri_comp_.final) {
            finals_.insert(add_node(SuperNode(fun.get<DST>(), s,
                                              fun.get<SEC_CP_TO>(),
                                              fun.get<SEC_ST_TO>())));
        }
        // Traverse the function and emit SuperNodes.
        while (!worklist_.empty()) {
            const Component& sec_comp = sec_rsm->components[curr().sec_comp];
            // Enumerate all edges originating from the current node.
            FOR(e, edges->pri()[curr().node]) {
                // Follow the edge on the secondary RSM, starting from the
                // current sec_comp + sec_state.
                SecMoves sec_moves(sec_comp, curr().sec_state, e);
                if (sec_moves.empty()) {
                    continue;
                }
                // Follow primary RSM transitions out of pri_state.
                cross_trans(e, sec_moves);
                // Ignore primary RSM exits.
                // Traverse primary RSM entries by emitting edge triples (1 of
                // which is a summary edge).
                cross_entries(e, sec_moves);
            }
            worklist_.pop_front();
        }
    }
    friend std::ostream& operator<<(std::ostream& os,
                                    const Intersection& intxn) {
        for (PointID id = 0; id < intxn.id2sn_.size(); id++) {
            intxn.print_point(os, id);
            if (id == intxn.initial_) {
                os << " in";
            }
            if (intxn.finals_.count(id) > 0) {
                os << " out";
            }
            os << std::endl;
        }
        os << "#" << std::endl;
        for (const Move& m : intxn.eps_moves_) {
            intxn.print_point(os, m.from);
            os << " ";
            intxn.print_point(os, m.to);
            os << std::endl;
        }
        for (const StackMove& m : intxn.stack_moves_) {
            intxn.print_point(os, m.from);
            os << " ";
            intxn.print_point(os, m.to);
            os << " " << m.mod << std::endl;
        }
        for (const CallMove& m : intxn.call_moves_) {
            intxn.print_point(os, m.from);
            os << " ";
            intxn.print_point(os, m.to);
            os << " " << m.callee << std::endl;
        }
        return os;
    }
};

// MAIN =======================================================================

int main(int argc, char* argv[]) {
    // User-defined parameters
    std::string pri_dir;
    std::string sec_dir;
    std::string graph_dir;
    std::string out_dir;

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
        ("out-dir", po::value<std::string>(&out_dir)->required(),
         "Directory to output the blown-up functions");
    po::positional_options_description pos_desc;
    pos_desc.add("pri-dir", 1);
    pos_desc.add("sec-dir", 1);
    pos_desc.add("graph-dir", 1);
    pos_desc.add("out-dir", 1);
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
    // Need to parse the graph first, to have a full list of the used symbols,
    // because RSM construction needs to pre-allocate an amount of space
    // exactly equal to the number of symbols.
    std::cout << "Parsing graph from " << graph_dir << std::endl;
    parse_graph(graph_dir);
    std::cout << "Nodes: " << nodes.size() << std::endl;
    std::cout << "Edges: " << edges->size() / 2 << std::endl;
    std::cout << "Parsing primary RSM from " << pri_dir
              << ", secondary RSM from " << sec_dir << std::endl;
    pri_rsm = new RSM(pri_dir);
    sec_rsm = new RSM(sec_dir);

    // Perform intersection
    std::cout << "Enumerating functions, based on primary RSM" << std::endl;
    enum_funs();
    std::cout << "Intersecting functions with secondary RSM" << std::endl;
    fs::create_directory(out_dir);
    FOR(f, funs) {
        Intersection intxn(f);
        std::stringstream ss;
        ss << f << ".fun.tgf";
        fs::path fpath = fs::path(out_dir)/ss.str();
        std::ofstream fout(fpath.string());
        EXPECT((bool) fout);
        fout << intxn;
        std::cout << "Printed " << fpath << std::endl;
    }

    return EXIT_SUCCESS;
}
