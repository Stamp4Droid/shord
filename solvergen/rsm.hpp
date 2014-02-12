#ifndef RSM_HPP
#define RSM_HPP

#include <boost/program_options.hpp>
#include <boost/regex.hpp>

#include "util.hpp"

namespace po = boost::program_options;
namespace fs = boost::filesystem;

// ALPHABET ===================================================================

// TODO: separate class for parametric and non-parametric symbols
class Symbol {
    friend Registry<Symbol>;
private:
    static const boost::regex NAME_REGEX;
public:
    const std::string name;
    const Ref<Symbol> ref;
    const bool parametric;
private:
    explicit Symbol(const std::string& name, Ref<Symbol> ref, bool parametric)
	: name(name), ref(ref), parametric(parametric) {
	assert(boost::regex_match(name, NAME_REGEX));
    }
public:
    bool merge(bool parametric) const {
	assert(parametric == this->parametric);
	return false;
    }
};

enum class Direction {FWD, REV};

class Label {
public:
    const Ref<Symbol> symbol;
    const Direction dir;
    // The specific index character on a label doesn't matter.
    const bool tagged;
public:
    explicit Label(Ref<Symbol> symbol, Direction dir, bool tagged)
	: symbol(symbol), dir(dir), tagged(tagged) {}
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg) const;
    bool operator<(const Label& rhs) const {
	return (std::tie(    symbol,     dir,     tagged) <
		std::tie(rhs.symbol, rhs.dir, rhs.tagged));
    }
};

// RSM ========================================================================

// States carry very little actual information; e.g. transitions are stored on
// a separate table.
class State {
    friend Registry<State>;
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
	assert(initial == this->initial && final == this->final);
	return false;
    }
    void print(std::ostream& os) const;
};

class Component;

class Box {
    friend Registry<Box>;
public:
    const std::string name;
    const Ref<Box> ref;
    const Ref<Component> comp;
private:
    explicit Box(const std::string& name, Ref<Box> ref, Ref<Component> comp)
	: name(name), ref(ref), comp(comp) {}
public:
    bool merge(Ref<Component> comp) const {
	assert(comp == this->comp);
	return false;
    }
    void print(std::ostream& os, const Registry<Component>& comp_reg) const;
};

// No tags allowed on state-to-state transitions; parametric symbols must be
// tagged with '[*]'.
class Transition {
public:
    const Ref<State> src;
    const Ref<State> dst;
    const Label label;
public:
    explicit Transition(Ref<State> src, Ref<State> dst, Label label)
	: src(src), dst(dst), label(label) {
	assert(!label.tagged);
    }
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg) const;
    bool operator<(const Transition& rhs) const {
	return (std::tie(    src,     dst,     label) <
		std::tie(rhs.src, rhs.dst, rhs.label));
    }
};

// TODO:
// - Enforce that either all entries and exits to the same box are tagged,
//   or none is.
// - Can't move from a box directly to another box.
class Entry {
public:
    const Ref<State> src;
    const Ref<Box> dst;
    const Label label;
public:
    explicit Entry(Ref<State> src, Ref<Box> dst, Label label)
	: src(src), dst(dst), label(label) {}
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg) const;
    bool operator<(const Entry& rhs) const {
	return (std::tie(    src,     dst,     label) <
		std::tie(rhs.src, rhs.dst, rhs.label));
    }
};

class Exit {
public:
    const Ref<Box> src;
    const Ref<State> dst;
    const Label label;
public:
    explicit Exit(Ref<Box> src, Ref<State> dst, Label label)
	: src(src), dst(dst), label(label) {}
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg) const;
    bool operator<(const Exit& rhs) const {
	return (std::tie(    src,     dst,     label) <
		std::tie(rhs.src, rhs.dst, rhs.label));
    }
};

class Component {
    friend Registry<Component>;
private:
    static const boost::regex NAME_REGEX;
public:
    const std::string name;
    const Ref<Component> ref;
    Registry<Box> boxes;
    Index<Table<Transition>,Ref<State>,&Transition::src> transitions;
    Index<Table<Entry>,Ref<Box>,&Entry::dst> entries;
    Index<Table<Exit>,Ref<Box>,&Exit::src> exits;
private:
    Registry<State> states;
    Ref<State> initial = Ref<State>::none();
    std::set<Ref<State>> final;
private:
    explicit Component(const std::string& name, Ref<Component> ref)
	: name(name), ref(ref) {
	assert(boost::regex_match(name, NAME_REGEX));
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
};

class RSM {
public:
    static const std::string FILE_EXTENSION;
public:
    Registry<Symbol> symbols;
    Registry<Component> components;
private:
    Label parse_label(const std::string& str);
public:
    void parse_dir(const std::string& dirname);
    void parse_file(const fs::path& fpath);
    void print(std::ostream& os) const;
};

// GRAPH ======================================================================

class Node {
    friend Registry<Node>;
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
    const Ref<Tag> tag;
public:
    explicit Edge(Ref<Node> src, Ref<Node> dst, Ref<Symbol> symbol,
		  Ref<Tag> tag)
	: src(src), dst(dst), symbol(symbol), tag(tag) {}
    bool operator<(const Edge& rhs) const {
	return (std::tie(    src,     dst,     symbol,     tag) <
		std::tie(rhs.src, rhs.dst, rhs.symbol, rhs.tag));
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

class Graph {
public:
    static const std::string FILE_EXTENSION;
private:
    Registry<Node> nodes;
    Registry<Tag> tags;
    Index<Table<Edge>,Ref<Symbol>,&Edge::symbol> edges;
    Table<Summary> summaries;
private:
    void parse_file(const Symbol& symbol, const fs::path& fpath);
public:
    explicit Graph(const Registry<Symbol>& symbols, const std::string& dir);
    void print_stats(std::ostream& os, const Registry<Symbol>& symbols) const;
};

#endif
