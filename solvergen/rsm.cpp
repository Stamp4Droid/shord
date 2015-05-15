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
TUPLE_TAG(FUN);

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
public:
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Symbol> ref;
    const bool parametric;
public:
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
public:
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Tag> ref;
public:
    explicit Tag(const std::string* name, Ref<Tag> ref)
        : name(*name), ref(ref) {}
    bool merge() {
        return false;
    }
};

class Node {
public:
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Node> ref;
public:
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
public:
    typedef std::string Key;
public:
    const std::string name;
    const Ref<State> ref;
    const bool initial;
    const bool final;
public:
    explicit State(const std::string* name, Ref<State> ref, bool initial,
                   bool final)
        : name(*name), ref(ref), initial(initial), final(final) {}
    bool merge(bool initial, bool final) {
        EXPECT(initial == this->initial && final == this->final);
        return false;
    }
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
public:
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Box> ref;
    const Ref<Component> comp;
public:
    explicit Box(const std::string* name, Ref<Box> ref, Ref<Component> comp)
        : name(*name), ref(ref), comp(comp) {}
    bool merge(Ref<Component> comp) {
        EXPECT(comp == this->comp);
        return false;
    }
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
public:
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
    explicit Component(const std::string* name, Ref<Component> ref)
        : name(*name), ref(ref) {
        EXPECT(boost::regex_match(*name, boost::regex("[A-Z]\\w*")));
    }
    bool merge() {
        return false;
    }
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

// TRANSITIONING OVER SECONDARY RSM ===========================================

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

// FUNCTION BODY SYNTHESIS (CODE-RSM INTERSECTION) ============================

class SuperNode {
public:
    // PRI_CP is implicit (the one we're summarizing over).
    typedef mi::NamedTuple<NODE, Ref<Node>,
                mi::NamedTuple<PRI_ST, Ref<State>,
                    mi::NamedTuple<SEC_CP, Ref<Component>,
                        mi::NamedTuple<SEC_ST, Ref<State>,
                            mi::Nil > > > > Key;
public:
    const Key fields;
    const Ref<SuperNode> ref;
    bool fwd_reached = false;
    bool bck_reached = false;
public:
    SuperNode(const Key* fields, Ref<SuperNode> ref)
        // TODO: Possibly inefficient way to default-initialize a Key in case
        // of temporary SuperNode.
        : fields(fields != NULL ? *fields : Key()), ref(ref) {}
};

typedef mi::NamedTuple<PRI_CP, Ref<Component>, // = PRI_CP_FROM = PRI_CP_TO
            // PRI_ST_FROM = PRI_CP's initial state
            mi::NamedTuple<SRC, Ref<Node>,
                mi::NamedTuple<SEC_CP_FROM, Ref<Component>,
                    mi::NamedTuple<SEC_ST_FROM, Ref<State>,
                        // PRI_ST_TO = any of PRI_CP's final states
                        mi::NamedTuple<DST, Ref<Node>,
                            mi::NamedTuple<SEC_CP_TO, Ref<Component>,
                                mi::NamedTuple<SEC_ST_TO, Ref<State>,
                                    mi::Nil> > > > > > > Signature;
std::ostream& operator<<(std::ostream& os, const Signature& sig);

class Function {
public:
    typedef Signature Key;
public:
    const Signature sig;
    const Ref<Function> ref;
private:
    bool empty_body_ = false;
public:
    explicit Function(const Signature* sig, Ref<Function> ref)
        : sig(*sig), ref(ref) {}
    bool empty_body() const {
        return empty_body_;
    }
    void set_empty_body(bool empty) {
        if (!empty) {
            // Can only move from non-empty to empty.
            assert(!empty_body_);
        } else {
            empty_body_ = true;
        }
    }
};

Registry<Function> funs;

class Body {
private:
    const Component& pri_comp_;
    Registry<SuperNode> sup_nodes_;
    std::set<Ref<Function> > incl_funs_;
    mi::MultiIndex<
        mi::Index<FUN, Ref<Function>,
            mi::Table<SRC, Ref<SuperNode> > >,
        mi::Table<SRC, Ref<SuperNode> > > initials_;
    mi::MultiIndex<
        mi::Index<FUN, Ref<Function>,
            mi::Table<DST, Ref<SuperNode> > >,
        mi::Table<DST, Ref<SuperNode> > > finals_;
    mi::Index<DST, Ref<SuperNode>,
        mi::Table<SRC, Ref<SuperNode> > > eps_moves_;
    mi::Index<DST, Ref<SuperNode>,
        mi::Index<SRC, Ref<SuperNode>,
            mi::Bag<MOD, SecStackMod> > > stack_moves_;
    mi::Index<DST, Ref<SuperNode>,
        mi::Index<SRC, Ref<SuperNode>,
            mi::Table<FUN, Ref<Function> > > > call_moves_;
    std::deque<Ref<SuperNode> > fwd_list_;
    std::deque<Ref<SuperNode> > bck_list_;
private:
    const typename SuperNode::Key& curr() const {
        return sup_nodes_[curr_id()].fields;
    }
    Ref<SuperNode> curr_id() const {
        return fwd_list_.front();
    }
    Ref<SuperNode> add_node(Ref<Node> node, Ref<State> pri_state,
                            Ref<Component> sec_comp, Ref<State> sec_state) {
        return sup_nodes_.add(node, pri_state, sec_comp, sec_state).ref;
    }
    void fwd_visit(Ref<SuperNode> point) {
        bool& fwd_reached = sup_nodes_[point].fwd_reached;
        if (!fwd_reached) {
            fwd_reached = true;
            fwd_list_.push_back(point);
        }
    }
    void bck_visit(Ref<SuperNode> point) {
        bool& bck_reached = sup_nodes_[point].bck_reached;
        if (!bck_reached) {
            bck_reached = true;
            bck_list_.push_back(point);
        }
    }
    void cross_trans(const EdgeTail& e, const SecMoves& sec_moves) {
        std::list<Ref<State> > pri_st_to;
        const auto& trans =
            pri_comp_.transitions.sec<1>()[curr().get<PRI_ST>()][e.get<REV>()]
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
                Ref<SuperNode> next_id =
                    add_node(e.get<DST>(), s, move.get<SEC_CP_TO>(),
                             move.get<SEC_ST_TO>());
                fwd_visit(next_id);
                eps_moves_.insert(next_id, curr_id());
            }
            for (const auto& move : sec_moves.stack_moves()) {
                Ref<SuperNode> next_id =
                    add_node(e.get<DST>(), s, move.get<SEC_CP_TO>(),
                             move.get<SEC_ST_TO>());
                fwd_visit(next_id);
                stack_moves_.insert(next_id, curr_id(), move.get<MOD>());
            }
        }
    }
    void cross_entries(const EdgeTail& e, const SecMoves& sec_moves) {
        for (Ref<Box> b : pri_comp_.entries.sec<2>()[curr().get<PRI_ST>()]
                 [e.get<REV>()][e.get<SYMBOL>()]) {
            for (const auto& move : sec_moves.eps_moves()) {
                Ref<SuperNode> next_id = sup_nodes_.mktemp().ref;
                eps_moves_.insert(next_id, curr_id());
                cross_call(b, e, next_id,
                           move.get<SEC_CP_TO>(), move.get<SEC_ST_TO>());
            }
            for (const auto& move : sec_moves.stack_moves()) {
                Ref<SuperNode> next_id = sup_nodes_.mktemp().ref;
                stack_moves_.insert(next_id, curr_id(), move.get<MOD>());
                cross_call(b, e, next_id,
                           move.get<SEC_CP_TO>(), move.get<SEC_ST_TO>());
            }
        }
    }
    void cross_call(Ref<Box> box, const EdgeTail& e_in, Ref<SuperNode> in_id,
                    Ref<Component> sec_cp_in, Ref<State> sec_st_in) {
        Ref<Component> entered = pri_comp_.boxes[box].comp;
        FOR(s, funs.index()[entered][e_in.get<DST>()][sec_cp_in][sec_st_in]) {
            Function& callee = funs[s.get<REF>()];
            if (callee.empty_body()) {
                continue;
            }
            Ref<SuperNode> out_id = sup_nodes_.mktemp().ref;
            if (incl_funs_.count(callee.ref) > 0) {
                // Intra-SCC call: emit epsilon moves instead of calls.
                for (Ref<SuperNode> sn : initials_.pri()[callee.ref]) {
                    eps_moves_.insert(sn, in_id);
                }
                for (Ref<SuperNode> sn : finals_.pri()[callee.ref]) {
                    eps_moves_.insert(out_id, sn);
                }
            } else {
                call_moves_.insert(out_id, in_id, callee.ref);
            }
            cross_exits(box, e_in.get<TAG>(), callee, out_id);
        }
    }
    void cross_exits(Ref<Box> box, Ref<Tag> tag, const Function& callee,
                     Ref<SuperNode> out_id) {
        FOR(exit, pri_comp_.exits.sec<2>()[box]) {
            for (Ref<Node> n : edges->pri()[callee.sig.get<DST>()]
                     [exit.get<REV>()][exit.get<SYMBOL>()][tag]) {
                EdgeTail e_out(exit.get<REV>(), exit.get<SYMBOL>(), tag, n);
                SecMoves
                    moves_out(sec_rsm->components[callee.sig.get<SEC_CP_TO>()],
                              callee.sig.get<SEC_ST_TO>(), e_out);
                for (const auto& move : moves_out.eps_moves()) {
                    Ref<SuperNode> next_id =
                        add_node(n, exit.get<TO>(), move.get<SEC_CP_TO>(),
                                 move.get<SEC_ST_TO>());
                    fwd_visit(next_id);
                    eps_moves_.insert(next_id, out_id);
                }
                for (const auto& move : moves_out.stack_moves()) {
                    Ref<SuperNode> next_id =
                        add_node(n, exit.get<TO>(), move.get<SEC_CP_TO>(),
                                 move.get<SEC_ST_TO>());
                    fwd_visit(next_id);
                    stack_moves_.insert(next_id, out_id, move.get<MOD>());
                }
            }
        }
    }
public:
    explicit Body(Ref<Component> pri_comp)
        : pri_comp_(pri_rsm->components[pri_comp]) {}
    Body(const Body&) = delete;
    Body(Body&&) = default;
    Body& operator=(const Body&) = delete;
    void add_function(Ref<Function> f) {
        const Signature& sig = funs[f].sig;
        EXPECT(sig.get<PRI_CP>() == pri_comp_.ref);
        incl_funs_.insert(f);
        initials_.insert(f, add_node(sig.get<SRC>(), pri_comp_.initial,
                                     sig.get<SEC_CP_FROM>(),
                                     sig.get<SEC_ST_FROM>()));
        for (Ref<State> s : pri_comp_.final) {
            finals_.insert(f, add_node(sig.get<DST>(), s, sig.get<SEC_CP_TO>(),
                                       sig.get<SEC_ST_TO>()));
        }
    }
    void fill() {
        // Starting from the entries, traverse the code and emit SuperNodes.
        for (Ref<SuperNode> point : initials_.sec<0>()) {
            fwd_visit(point);
        }
        while (!fwd_list_.empty()) {
            const Component& sec_comp =
                sec_rsm->components[curr().get<SEC_CP>()];
            // Enumerate all edges originating from the current node.
            FOR(e, edges->pri()[curr().get<NODE>()]) {
                // Follow the edge on the secondary RSM, starting from the
                // current sec_comp + sec_state.
                SecMoves sec_moves(sec_comp, curr().get<SEC_ST>(), e);
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
            fwd_list_.pop_front();
        }
        // Starting from the final points, move backwards and mark useful
        // points (those that can reach an exit). This filtering step is
        // overapproximate, but sound (before any matching is performed, we can
        // only be certain that the FSM is empty if we can't reach any final
        // point from the initial).
        // TODO: Considering a SuperNode useful even if it's only on the path
        // from an entry point to an exit point that don't match.
        for (Ref<SuperNode> point : finals_.sec<0>()) {
            if (!sup_nodes_[point].fwd_reached) {
                continue;
            }
            bck_visit(point);
        }
        while (!bck_list_.empty()) {
            for (Ref<SuperNode> src : eps_moves_[bck_list_.front()]) {
                bck_visit(src);
            }
            for (const auto& src_p : stack_moves_[bck_list_.front()]) {
                bck_visit(src_p.first);
            }
            for (const auto& src_p : call_moves_[bck_list_.front()]) {
                bck_visit(src_p.first);
            }
            bck_list_.pop_front();
        }
        // TODO: Some initial points may be unreachable from the final points,
        // so we could re-run forward and backward propagation, until fixpoint.
    }
    std::set<Ref<Function> > callees() const {
        std::set<Ref<Function> > res;
        for (const auto& dst_p : call_moves_) {
            if (!sup_nodes_[dst_p.first].bck_reached) {
                continue;
            }
            for (const auto& src_p : dst_p.second) {
                if (!sup_nodes_[src_p.first].bck_reached) {
                    continue;
                }
                for (Ref<Function> f : src_p.second) {
                    res.insert(f);
                }
            }
        }
        return res;
    }
    bool empty() const {
        for (Ref<SuperNode> point : initials_.sec<0>()) {
            if (sup_nodes_[point].bck_reached) {
                return false;
            }
        }
        return true;
    }
    friend std::ostream& operator<<(std::ostream& os, const Body& body);
    void print_function_part(std::ostream& os, Ref<Function> f) const;
};

Body make_scc_body(const std::set<Ref<Function> >& scc) {
    Body body(funs[*(scc.begin())].sig.get<PRI_CP>());
    for (Ref<Function> f : scc) {
        body.add_function(f);
    }
    body.fill();
    for (Ref<Function> f : scc) {
        funs[f].set_empty_body(body.empty());
    }
    return body;
}

Body make_fun_body(Ref<Function> f) {
    std::set<Ref<Function> > singleton_scc;
    singleton_scc.insert(f);
    return make_scc_body(singleton_scc);
}

void intersect_all(const std::string& out_dir) {
    using std::swap;
    std::cout << "Creating dummy callgraph" << std::endl;
    std::map<Ref<Function>,std::set<Ref<Function> > > calls;
    for (Function& f : funs) {
        calls[f.ref];
    }
    SccGraph<Ref<Function> > cg(calls);

    unsigned prev_num_funs;
    unsigned num_passes = 0;
    do {
        num_passes++;
        std::cout << "Performing pass # " << num_passes << ": "
                  << calls.size() << " non-empty functions" << std::endl;
        prev_num_funs = calls.size();
        calls.clear();

        std::cout << "Processing functions in bottom-up order" << std::endl;
        for (unsigned i = 0; i < cg.num_sccs(); i++) {
            const auto& scc = cg.scc(i);
            std::cout << "Processing SCC " << i << " of " << cg.num_sccs()
                      << " (size " <<  scc.nodes.size() <<  ")" << std::endl;
            unsigned f_num = 0;
            for (Ref<Function> f : scc.nodes) {
                std::cout << "    Intersecting function # " << f_num << " of "
                          << scc.nodes.size() << ": " << funs[f].sig
                          << std::endl;
                Body body = make_fun_body(f);
                if (body.empty()) {
                    std::cout << "        Empty" << std::endl;
                } else {
                    std::cout << "        Non-empty" << std::endl;
                    calls.emplace(f, body.callees());
                }
                f_num++;
            }
        }

        std::cout << "Computing new callgraph" << std::endl;
        SccGraph<Ref<Function> > new_cg(calls);
        swap(cg, new_cg);
        std::cout << "    " << cg.num_sccs() << " SCCs" << std::endl;
        Histogram<unsigned> scc_size_freqs;
        for (const auto& scc : cg.sccs()) {
            scc_size_freqs.record(scc.nodes.size());
        }
        std::cout << "    SCC size\tFrequency" << std::endl;
        for (const auto& p : scc_size_freqs) {
            std::cout << "    " << p.first << "\t" << p.second << std::endl;
        }
    } while (calls.size() < prev_num_funs);

    std::cout << "Emitting SCC code bottom-up" << std::endl;
    for (unsigned i = 0; i < cg.num_sccs(); i++) {
        const auto& scc = cg.scc(i);
        std::cout << "Processing SCC " << i << " of " << cg.num_sccs()
                  << " (size " <<  scc.nodes.size() <<  ")" << std::endl;
        Body body = make_scc_body(scc.nodes);
        // TODO: We shouldn't find empty SCCs (or calls to non-existent SCCs).
        if (body.empty()) {
            std::cout << "    Empty" << std::endl;
            continue;
        }
        std::cout << "    Non-empty" << std::endl;
        std::cout << "    Printing SCC code" << std::endl;
        std::stringstream body_fname;
        body_fname << std::setfill('0') << std::setw(10) << i << ".fun.tgf";
        fs::path body_fpath = fs::path(out_dir)/body_fname.str();
        std::ofstream body_fout(body_fpath.string());
        EXPECT((bool) body_fout);
        body_fout << body;
        std::stringstream scc_dirname;
        scc_dirname << std::setfill('0') << std::setw(10) << i;
        fs::path scc_dir(fs::path(out_dir)/scc_dirname.str());
        fs::create_directory(scc_dir);
        for (Ref<Function> f : scc.nodes) {
            std::cout << "    Printing part for " << funs[f].sig << std::endl;
            std::stringstream fname;
            fname << funs[f].sig << ".fun.tgf";
            fs::path fpath = fs::path(scc_dir)/fname.str();
            std::ofstream fout(fpath.string());
            EXPECT((bool) fout);
            body.print_function_part(fout, f);
        }
    }
}

// FUNCTION PRINTING ==========================================================

void print(std::ostream& os, const SuperNode& sn, const Component& pri_comp) {
    // For invalid points, simply print their identifier.
    if (!sn.fields.get<NODE>().valid()) {
        os << sn.ref;
        return;
    }
    const Component& sec_comp = sec_rsm->components[sn.fields.get<SEC_CP>()];
    os << nodes[sn.fields.get<NODE>()].name << "_"
       << pri_comp.states[sn.fields.get<PRI_ST>()].name << "_"
       << sec_comp.name << "_"
       << sec_comp.states[sn.fields.get<SEC_ST>()].name;
}

// XXX: Not unique if there's underscores in the individual names.
std::ostream& operator<<(std::ostream& os, const Signature& sig) {
    const Component& sec_cp_from = sec_rsm->components[sig.get<SEC_CP_FROM>()];
    const Component& sec_cp_to = sec_rsm->components[sig.get<SEC_CP_TO>()];
    os << pri_rsm->components[sig.get<PRI_CP>()].name << "_"
       << nodes[sig.get<SRC>()].name << "_"
       << nodes[sig.get<DST>()].name << "_"
       << sec_cp_from.name << "_"
       << sec_cp_from.states[sig.get<SEC_ST_FROM>()].name << "_"
       << sec_cp_to.name << "_"
       << sec_cp_to.states[sig.get<SEC_ST_TO>()].name;
    return os;
}

std::ostream& operator<<(std::ostream& os, const Body& body) {
    // Only print useful points.
    // Ignore the 'initial' and 'final' flags on points (those are covered on
    // the function-specific portion).
    for (const SuperNode& sn : body.sup_nodes_) {
        if (!sn.bck_reached) {
            continue;
        }
        print(os, sn, body.pri_comp_);
        os << std::endl;
    }
    os << "#" << std::endl;
    // Only print arrows between useful points.
    for (const auto& dst_p : body.eps_moves_) {
        const SuperNode& dst = body.sup_nodes_[dst_p.first];
        if (!dst.bck_reached) {
            continue;
        }
        for (Ref<SuperNode> src_id : dst_p.second) {
            const SuperNode& src = body.sup_nodes_[src_id];
            if (!src.bck_reached) {
                continue;
            }
            print(os, src, body.pri_comp_);
            os << " ";
            print(os, dst, body.pri_comp_);
            os << std::endl;
        }
    }
    for (const auto& dst_p : body.stack_moves_) {
        const SuperNode& dst = body.sup_nodes_[dst_p.first];
        if (!dst.bck_reached) {
            continue;
        }
        for (const auto& src_p : dst_p.second) {
            const SuperNode& src = body.sup_nodes_[src_p.first];
            if (!src.bck_reached) {
                continue;
            }
            for (const SecStackMod& mod : src_p.second) {
                print(os, src, body.pri_comp_);
                os << " ";
                print(os, dst, body.pri_comp_);
                os << " " << mod << std::endl;
            }
        }
    }
    for (const auto& dst_p : body.call_moves_) {
        const SuperNode& dst = body.sup_nodes_[dst_p.first];
        if (!dst.bck_reached) {
            continue;
        }
        for (const auto& src_p : dst_p.second) {
            const SuperNode& src = body.sup_nodes_[src_p.first];
            if (!src.bck_reached) {
                continue;
            }
            for (Ref<Function> callee : src_p.second) {
                print(os, src, body.pri_comp_);
                os << " ";
                print(os, dst, body.pri_comp_);
                os << " " << funs[callee].sig << std::endl;
            }
        }
    }
    return os;
}

void Body::print_function_part(std::ostream& os, Ref<Function> f) const {
    assert(incl_funs_.count(f) > 0);
    for (Ref<SuperNode> point : initials_.pri()[f]) {
        print(os, sup_nodes_[point], pri_comp_);
        os << " in" << std::endl;
    }
    for (Ref<SuperNode> point : finals_.pri()[f]) {
        print(os, sup_nodes_[point], pri_comp_);
        os << " out" << std::endl;
    }
}

// FUNCTION ENUMERATION =======================================================

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
                        funs.add(callee,
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
    std::cout << funs.size() << " functions in total" << std::endl;
    std::cout << "Intersecting functions with secondary RSM" << std::endl;
    fs::create_directory(out_dir);
    intersect_all(out_dir);

    return EXIT_SUCCESS;
}
