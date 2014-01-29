#ifndef RSM_HPP
#define RSM_HPP

#include <boost/regex.hpp>

#include "util.hpp"

// ALPHABET ===================================================================

// TODO:
// - separate class for parametric and non-parametric (base) symbols
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
    bool consistent_with(bool parametric) const {
	return parametric == this->parametric;
    }
};

enum class Direction {FWD, REV};

class Label {
public:
    const Ref<Symbol> symbol;
    const Direction dir;
    const bool tagged;
public:
    explicit Label(Ref<Symbol> symbol, Direction dir, bool tagged)
	: symbol(symbol), dir(dir), tagged(tagged) {}
    void print(std::ostream& os, const Registry<Symbol>& symbol_reg) const;
    bool operator<(const Label& other) const {
	return (std::tie(      symbol,       dir,       tagged) <
		std::tie(other.symbol, other.dir, other.tagged));
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
    bool consistent_with(bool initial, bool final) const {
	return initial == this->initial && final == this->final;
    }
    void print(std::ostream& os) const;
};

class Component;

class Box {
    friend Registry<Box>;
public:
    const std::string name;
    const Ref<Box> ref;
    const Ref<Component> component;
private:
    explicit Box(const std::string& name, Ref<Box> ref,
		 Ref<Component> component)
	: name(name), ref(ref), component(component) {}
public:
    bool consistent_with(Ref<Component> component) const {
	return component == this->component;
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
    bool operator<(const Transition& other) const {
	return (std::tie(      src,       dst,       label) <
		std::tie(other.src, other.dst, other.label));
    }
};

// The specific index on an entry or exit arrow doesn't matter.
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
    bool operator<(const Entry& other) const {
	return (std::tie(      src,       dst,       label) <
		std::tie(other.src, other.dst, other.label));
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
    bool operator<(const Exit& other) const {
	return (std::tie(      src,       dst,       label) <
		std::tie(other.src, other.dst, other.label));
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
    bool consistent_with() const {
	return true;
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
    void parse_file(const boost::filesystem::path& fpath);
    void print(std::ostream& os) const;
};

#endif
