#ifndef UTIL_HPP
#define UTIL_HPP

#include <boost/filesystem.hpp>
#include <algorithm>
#include <cassert>
#include <cstdlib>
#include <deque>
#include <forward_list>
#include <functional>
#include <iostream>
#include <iterator>
#include <limits>
#include <map>
#include <memory>
#include <queue>
#include <set>
#include <string>
#include <tuple>
#include <vector>

// TYPE TRAITS ================================================================

template<int I, typename... Types> struct pack_elem {
public:
    typedef typename std::tuple_element<I,std::tuple<Types...>>::type type;
};

// ERROR HANDLING =============================================================

void expect(const char* file, unsigned int line, bool cond,
	    std::string err_msg = "(no details)") {
    if (cond) {
	return;
    }
    std::cout.flush();
    std::cerr << file << ", line " << line << ":" << std::endl;
    std::cerr << "Fatal Error: " << err_msg << std::endl;
    std::exit(EXIT_FAILURE);
}

#define EXPECT(...) do {expect(__FILE__, __LINE__, __VA_ARGS__);} while(0)

// HELPER CODE ================================================================

namespace detail {

const boost::filesystem::path&
get_path(const boost::filesystem::directory_entry& entry) {
    return entry.path();
}

template<typename T>
const T& unwrap_ref(const std::reference_wrapper<T>& r) {
    return r.get();
}

template<typename PtrT>
const typename std::pointer_traits<PtrT>::element_type&
deref(const PtrT& ptr) {
    return *ptr;
}

template<typename T, typename V, int I>
struct TupleInserter {
    static void insert(T& idxs, V& val) {
	TupleInserter<T,V,I-1>::insert(idxs, val);
	std::get<I-1>(idxs).insert(val);
    }
};

template<typename T, typename V>
struct TupleInserter<T,V,0> {
    static void insert(T&, V&) {}
};

template<typename T, typename V>
void insert_all(T& idxs, V& val) {
    TupleInserter<T,V,std::tuple_size<T>::value>::insert(idxs, val);
}

template<typename T, typename S>
const S& get_second(const std::pair<T,S>& p) {
    return p.second;
}

} // namespace detail

// ITERATOR HANDLING ==========================================================

// Very simple iterator wrapper, cannot write through it (constant iterator).
// The wrapper code must return a reference to an object. It can do that by
// either creating and managing a temporary object (hard to do with the current
// design), or derive a reference to some component of the element at the
// current position of the underlying iterator (which should remain alive
// until the underlying iterator's next move).
// TODO: Missing operators:
// iter++, default constructor, copy assignment, destructor, swap
template<typename Iter, typename Out,
	 const Out& F(const typename std::iterator_traits<Iter>::value_type&)>
class IterWrapper : public std::iterator<std::input_iterator_tag,Out> {
private:
    Iter iter;
public:
    explicit IterWrapper() {};
    explicit IterWrapper(Iter iter) : iter(iter) {}
    IterWrapper(const IterWrapper& rhs) : iter(rhs.iter) {}
    IterWrapper& operator=(const IterWrapper& rhs) {
	iter = rhs.iter;
	return *this;
    }
    const Out& operator*() const {
	return F(*iter);
    }
    const Out* operator->() const {
	return &(operator*());
    }
    IterWrapper& operator++() {
	++iter;
	return *this;
    }
    bool operator==(const IterWrapper& rhs) const {
	return iter == rhs.iter;
    }
    bool operator!=(const IterWrapper& rhs) const {
	return !(*this == rhs);
    }
};

template<typename Map>
using MappedIter = IterWrapper<typename Map::const_iterator,
			       typename Map::mapped_type,detail::get_second>;

template<typename PtrIter> using DerefIter =
    IterWrapper<PtrIter,
		typename std::pointer_traits<
		    typename std::iterator_traits<PtrIter>::value_type>
		::element_type,
		detail::deref<
		    typename std::iterator_traits<PtrIter>::value_type>>;

// Takes a constant iterator over const-iterable objects, and returns a
// constant iterator over the underlying objects.
// Requirements:
// - The objects covered by OutIter must defined begin() and end(), and those
//   must both return an InIter.
// - InIter must be default-constructible.
// - All default-constructed iterators of type InIter must compare equal. That
//   is because sub-iterators are reset to their default values when the outer
//   iterator moves to the next element, and those default-value iterators will
//   be used in comparisons.
//   XXX: This is NOT guaranteed by the standard STL containers.
// TODO:
// - Missing operators: iter++, destructor, swap
// - Could retrieve all the types through OutIter alone, but would need a way
//   to extract the iterator type from the pointed collection type.
template<typename OutIter, typename InIter> class Flattener
    : public std::iterator<std::input_iterator_tag,
			   typename std::iterator_traits<InIter>::value_type> {
private:
    OutIter out_curr;
    OutIter out_end;
    InIter sub_curr;
    InIter sub_end;
private:
    void skip_empty_entries() {
	while (sub_curr == sub_end) {
	    ++out_curr;
	    if (out_curr == out_end) {
		sub_curr = InIter();
		sub_end = InIter();
		return;
	    }
	    sub_curr = out_curr->begin();
	    sub_end = out_curr->end();
	}
    }
public:
    explicit Flattener() {}
    explicit Flattener(OutIter out_curr, OutIter out_end)
	: out_curr(out_curr), out_end(out_end) {
	if (out_curr != out_end) {
	    sub_curr = out_curr->begin();
	    sub_end = out_curr->end();
	    skip_empty_entries();
	}
    }
    Flattener(const Flattener& rhs)
	: out_curr(rhs.out_curr), out_end(rhs.out_end),
	  sub_curr(rhs.sub_curr), sub_end(rhs.sub_end) {}
    Flattener& operator=(const Flattener& rhs) {
	out_curr = rhs.out_curr;
	out_end = rhs.out_end;
	sub_curr = rhs.sub_curr;
	sub_end = rhs.sub_end;
	return *this;
    }
    const typename Flattener::value_type& operator*() const {
	return *sub_curr;
    }
    const typename Flattener::value_type* operator->() const {
	return &(*sub_curr);
    }
    Flattener& operator++() {
	++sub_curr;
	skip_empty_entries();
	return *this;
    }
    bool operator==(const Flattener& rhs) const {
	return (out_curr == rhs.out_curr && out_end == rhs.out_end &&
		sub_curr == rhs.sub_curr && sub_end == rhs.sub_end);
    }
    bool operator!=(const Flattener& rhs) const {
	return !(*this == rhs);
    }
};

// GENERIC DATA STRUCTURES ====================================================

// Properties:
// - Guarantees no duplicate entries in the queue.
// - FIFO ordering.
// - Stored classes need to be comparable.
// TODO:
// - copy/iterator constructor
template<typename T, bool CanReprocess,
	 typename Set = std::set<T>> class Worklist;

template<typename T, typename Set> class Worklist<T,false,Set> {
private:
    Set reached;
    std::queue<const T*> queue;
public:
    bool empty() const {
	return queue.empty();
    }
    bool enqueue(T val) {
	auto res = reached.insert(std::move(val));
	if (res.second) {
	    queue.push(&(*(res.first)));
	    return true;
	}
	return false;
    }
    const T& dequeue() {
	const T& ref = *(queue.front());
	queue.pop();
	return ref;
    }
};

// TODO: Enqueues and dequeues done by copying (careful with large structs).
template<typename T, typename Set> class Worklist<T,true,Set> {
private:
    Set reached;
    std::queue<T> queue;
public:
    bool empty() const {
	return queue.empty();
    }
    bool enqueue(T val) {
	if (reached.insert(val).second) {
	    queue.push(val);
	    return true;
	}
	return false;
    }
    T dequeue() {
	T val = queue.front();
	queue.pop();
	reached.erase(val);
	return val;
    }
};

template<typename T> class Histogram {
private:
    std::map<T,unsigned int> freqs;
public:
    void record(const T& val) {
	unsigned int prev = 0;
	try {
	    prev = freqs.at(val);
	} catch(std::out_of_range& exc) {}
	freqs[val] = prev + 1;
    }
    friend std::ostream& operator<<(std::ostream& os, const Histogram& ref) {
	for (const auto& p : ref.freqs) {
	    os << p.first << "\t" << p.second << std::endl;
	}
	return os;
    }
};

// OS SERVICES ================================================================

class Directory {
public:
    typedef IterWrapper<boost::filesystem::directory_iterator,
			boost::filesystem::path,detail::get_path> Iterator;
private:
    const boost::filesystem::path path;
public:
    explicit Directory(const std::string& name) : path(name) {
	EXPECT(boost::filesystem::is_directory(path));
    }
    Iterator begin() {
	return Iterator(boost::filesystem::directory_iterator(path));
    }
    Iterator end() {
	return Iterator(boost::filesystem::directory_iterator());
    }
};

// NAMED OBJECT MANAGEMENT ====================================================

// Features:
// - Refs can't be dangling; valid ones can only be created through a
//   Registry, to point to a specific object in that Registry.
// - Useful as the domain for various relations.
// - Each managed object must have a unique key, of type ObjClass::Key. The
//   Registry enforces this at runtime: if a new object is fed to it, which
//   agrees with an existing one on the key, then the new object is "merged"
//   with the old one. ObjClass must provide a callback 'merge(...)' that
//   performs the merging, and any sanity checks.
// - A unique key, of type Ref<ObjClass>, is assigned to objects.
// - Can have multiple instances of a domain (with separate references), for
//   different collections.

// Extensions:
// - Define the concept of Managed Object, and have instances inherit some of
//   the boilerplate?
// - Don't have two copies of the key:
//   - use some sort of container that uses the field directly
//     e.g. LightMap
//   - use the pair as the value directly
//   - keep the keys in a set, and pass references to them
// - Destructor that cleans the managed objects
// - No way to check if a Ref actually corresponds to some Registry instance
//   before dereferencing it
// - Configurable max number of Refs
// - Properly handle class hierarchies
//   Currently simply emplacing the objects => will all be of the same concrete
//   type. Could use a trait, or function template like make<T> below.
// - Behaves as a relational table; implement as special case of struct field
//   indexing infrastructure; requires some extensions:
//   - singleton leaf nodes
//   - mutable non-key attributes
//   - merging support
//   - multi-field indexing,
//   - random-access index/auto-increment attributes
// - Index domain members according to feature
//   (but these might change => re-indexing needed)
// - If the key is composite, need to group the attributes in a struct
// - "Void" key specialization: don't even make a map
// - Define the types of the extra parameters in the managed class
// - Common namespace for 2+ types
//   enforcing no conflicts
//   each addition is checked against both; one must reject, one must accept
//   'merge' should report failure in a less intrusive way
//   instances: State & Box, IdxSymbol & UnIdxSymbol

// Alternative: store pointers to objects:
//   std::vector<T*> array;
//   std::map<std::string,T*> map;
// Managed classes must define a 'make' friend function template, used for
// initialization (normally just calls 'new'):
// template<typename T, typename... ArgTs>
// T* make(const std::string& name, Ref<T> ref, ArgTs&&... args) {
//     return new T(name, ref, std::forward<ArgTs>(args)...);
// }

namespace mi {
    template<typename T> class KeyTraits;
}
template<typename T> class Ref;
template<typename T> class Registry;
template<typename S, typename C, const Ref<C> S::Tuple::* MemPtr>
class FlatIndex;

// TODO: Could make this class const-correct, i.e. get a const& when indexing a
// Registry using a const Ref.
template<typename T> class Ref {
    friend Registry<T>;
    friend mi::KeyTraits<Ref<T>>;
    // TODO: This should be with C==T, but partial specialization is not
    // allowed on friend class declarations.
    template<typename S, typename C, const Ref<C> S::Tuple::* MemPtr>
    friend class FlatIndex;
public:
    unsigned int value;
private:
    explicit Ref(unsigned int value) : value(value) {
	EXPECT(valid());
    }
public:
    explicit Ref() : value(std::numeric_limits<unsigned int>::max()) {}
    Ref(const Ref& rhs) : value(rhs.value) {}
    Ref& operator=(const Ref& rhs) {
	value = rhs.value;
	return *this;
    }
    bool valid() const {
	return value < std::numeric_limits<unsigned int>::max();
    }
    bool operator==(const Ref& rhs) const {
	return value == rhs.value;
    }
    bool operator!=(const Ref& rhs) const {
	return !(*this == rhs);
    }
    bool operator<(const Ref& rhs) const {
	return value < rhs.value;
    }
    friend std::ostream& operator<<(std::ostream& os, const Ref& ref) {
	EXPECT(ref.valid());
	os << ref.value;
	return os;
    }
};

template<typename T> class Registry {
public:
    typedef typename T::Key Key;
    typedef std::vector<std::reference_wrapper<T>> RefArray;
    typedef IterWrapper<typename RefArray::const_iterator, T,
			detail::unwrap_ref<T>> Iterator;
private:
    RefArray array;
    std::map<Key,T> map;
public:
    explicit Registry() {}
    Registry(const Registry& rhs) = delete;
    // XXX: This is dangerous/non-portable: The 'array' member variable stores
    // references, which would only be valid for the 'map' on the original
    // object. However, the move constructor on std::map (as normally
    // implemented in the standard library) doesn't move the map nodes on the
    // heap, and thus the references are still valid.
    Registry(Registry&& rhs)
	: array(std::move(rhs.array)), map(std::move(rhs.map)) {}
    Registry& operator=(const Registry& rhs) = delete;
    T& operator[](const Ref<T> ref) {
	EXPECT(ref.valid());
	return array.at(ref.value).get();
    }
    const T& operator[](const Ref<T> ref) const {
	EXPECT(ref.valid());
	return array.at(ref.value).get();
    }
    template<typename... ArgTs> T& make(const Key& key, ArgTs&&... args) {
	Ref<T> next_ref(array.size());
	auto res = map.emplace(key, T(key, next_ref,
				      std::forward<ArgTs>(args)...));
	EXPECT(res.second);
	T& obj = res.first->second;
	array.push_back(std::ref(obj));
	return obj;
    }
    template<typename... ArgTs> T& add(const Key& key, ArgTs&&... args) {
	try {
	    T& obj = map.at(key);
	    obj.merge(std::forward<ArgTs>(args)...);
	    return obj;
	} catch (const std::out_of_range& exc) {
	    return make(key, std::forward<ArgTs>(args)...);
	}
    }
    T& find(const Key& key) {
	return map.at(key);
    }
    const T& find(const Key& key) const {
	return map.at(key);
    }
    bool contains(const Key& key) const {
	return map.count(key) > 0;
    }
    unsigned int size() const {
	return array.size();
    }
    const T& first() const {
	return array.front().get();
    }
    const T& last() const {
	return array.back().get();
    }
    Iterator begin() const {
	return Iterator(array.cbegin());
    }
    Iterator end() const {
	return Iterator(array.cend());
    }
};

// STRUCT FIELD INDEXING ======================================================

// Features:
// - nested mapping
//   use a trait/typedef to bubble-up the tuple type
// - simultaneous index on 2 different fields
//   (can't emplace anymore)
//   careful if you try to store pointers; careful to avoid comparison
//   store on the primary index first
//   only store on the secondaries if it doesn't already exist
// - generic handling of multiple indices on the same level
//   use variadic templates
//   need to identify secondary indices by their number
//   type of field is not sufficient: might be the same for two fields
// - tuples are explicitly materialized in memory
//   the containers are stable, so the tuples can be handled via pointers
// - can iterate over the entire table, as well as intermediate index levels
// - selecting on a non-existent value doesn't create spurious entries

// Comparison with RelC:
// - leaf nodes contain unindexed sets, not single tuples
// - functional dependencies are ignored
// - delete/update not supported
// - tuples explicitly materialized (fully present in memory)
//   feasible to refer to tuples via pointers
// - implemented purely in C++
// - connection to the class structure (attributes are fields)
// - no relational operations, no query planning
//   user has to access the indices manually
//   => always has to provide all the required keys
//      (no need to verify that's the case)
// - single root (like RelC) and single leaf
//   single path through the graph is "primary"
//   "secondary" indices are separate arcs on the graph
//   those always start from the root node and end at the leaf node
// - re-indexing not supported => index fields must be const
//   underlying data structures implicitly disallow modifying the tuples at all
//   => all fields must be const
//   (partially enforced by having constant iterators)
// - always possible to recover all fields of the tuples
//   conditions 1 and 3 of query validity automatically satisfied:
//   - queries produce all of the columns requested as output
//   - tuples from each side of a fork can be accurately matched
// - primary and secondary indices are completely separate
//   not easy to support features like sharing the leaf representation
//   (but will need in order to get pre-leaf merges)

// Extensions:
// - multi-field keys
// - don't materialize tuples fully in memory
//   instead store only the missing fields
//   but instantiate a real tuple for every selection
//   (which is not actually stored)
//   this will require more verification
//   [how do we refer to tuples in this case?]
// - can avoid instantiating a separate set-of-refs for each secondary index?
// - 'find' function?
// - macro wrapping Index<...> sequence
// - concrete size type
// - give client choice of container to use
// - store on deque rather than set at the bottom?
// - handle derived classes correctly
// - secondary arcs starting from arbitrary points and ending at the leaf
//   probably make those branch points explicit using MultiIndex's
// - secondary arcs ending at arbitrary points (harder)
// - sharing among secondary arcs
// - relational operations & high-level query plans
//   possibly via pattern matching
//   (need specialized pattern language, with wildcards, constraints etc.)
//   could do this through template specialization:
//   - each level knows what attribute it controls
//     when "asked", combines that with answer from children
//   - to query, pass a pattern down the index tree
//     then each level:
//     - uses the correct map
//     - removes the matched field from the pattern
//     - passes the new pattern down
//     at fork points, can decide based on what the children report
//   maybe wrap in a macro, to offer a nice syntax
// - tuple deletion
//   could reuse RelC's cut-based approach?
//   need to handle single insertion but multiple mapping, at merge points
//   could clean up empty map nodes
// - tuple updates (allow reindexing to occur?)
// - use intrusive containers to weave multiple indices efficiently
//   i.e., wrap the data tuple over an element node for an intrusive container
//   (add the pointer fields)
//   useful for simple deletion of nodes at merge points
//   features:
//   - single object can belong to multiple indices
//   - objects don't need to be copy constructible
//   - derived classes can be handled
//   - manual memory management required
//   - manual re-indexing required
// - variadic template of all indexing fields
//   - shouldn't have to redefine Index twice, for the two cases of >3 and 3
//     should instead be able to say that Index<T> = T
//   - bug: would then be able to kill the matching of nested secondary indices
//     by calling primary_select on an intermediate step
//   but can't have non-types as variadic template parameters
// - exploit functional dependencies
//   defined e.g. on the tuple type, as attribute subsets that are keys
//   - singleton ("unit") nodes
//     only if we've crossed all functional dependencies
//     specialized API:
//     - iterator has size [either 0 or] 1
//       can make special wrapper class for this
//     - special function to retrieve single tuple
//     - insert checks whenever a new value is added
//       check that all remaining values are the same
//       always return false
//     - alternatively, if there is such a value, merge its non-const fields
//       (need a callback for that)
//   - modifiable non-key attributes
//   - tuples no longer fully immutable => can make iterators semi-constant
//     can still not insert through them
//     but can get non-const refs to objects, to edit non-key fields
// - union operator / bulk insertion
// - random-access index/auto-increment attribute
// - implement on top of boost::multi_index
// - named indices (use classes as index tags)

// Concepts:
// - Relation<T>, SecIndex<T>:
//   - typedef Tuple = T
//   - typedef Iterator: iterator with traits:
//     - constant iterator
//     - value_type = Tuple
//     - iterator_category = input_iterator_tag (at least)
//   - std::pair<const Tuple*,bool> insert(const Tuple& tuple)
//   - Iterator begin() const
//   - Iterator end() const
//   - unsigned int size() const
// Implementations:
// - Table<T> : Relation<T>
// - PtrTable<T> : SecIndex<T>
// - Index<S,K,MemPtr> : Relation<T> / SecIndex<T>
//   where:
//   - S : Relation<T> / SecIndex<T>
//   - MemPtr is a pointer to a member of T, of type K
//   additional:
//   - typedef Wrapped = S
//   - typedef Key = K
//   - const Wrapped& operator[](const Key& key) const
// - MultiIndex<PriIdxT,S[1],S[2],...> : Relation<T>:
//   where:
//   - PriIdxT : Relation<T>
//   - each S[i] : SecIndex<T>
//   additional:
//   - const PriIdxT& primary() const
//   - template<int I> const S[I] secondary() const

// Constraints:
// - correctness requirement:
//   for each key field k: for any two tuples t1,t2: t1.k != t2.k => t1 != t2
// - all levels must implement the same concept
// - can't call insert on the wrapped type, only on the top
// - the order of selects and that of template arguments is reverse
// - indexed fields can't change value (must be 'const')
//   (re-indexing not supported)
//   rest of fields should probably not change either
//   (unless they don't affect the ordering)

template<typename T> class Table {
public:
    typedef T Tuple;
    typedef typename std::set<Tuple>::const_iterator Iterator;
private:
    std::set<Tuple> store;
public:
    std::pair<const Tuple*,bool> insert(const Tuple& tuple) {
	auto res = store.insert(tuple);
	return std::make_pair(&(*(res.first)), res.second);
    }
    Iterator begin() const {
	return store.cbegin();
    }
    Iterator end() const {
	return store.cend();
    }
    unsigned int size() const {
	return store.size();
    }
};

template<typename S, typename K, const K S::Tuple::* MemPtr> class Index {
public:
    typedef S Wrapped;
    typedef typename Wrapped::Tuple Tuple;
    typedef K Key;
    typedef std::map<Key,Wrapped> Map;
    typedef Flattener<MappedIter<Map>,typename Wrapped::Iterator> Iterator;
private:
    static const Wrapped dummy;
private:
    Map idx;
public:
    std::pair<const Tuple*,bool> insert(const Tuple& tuple) {
	return idx[tuple.*MemPtr].insert(tuple);
    }
    const Wrapped& operator[](const Key& key) const {
	try {
	    return idx.at(key);
	} catch (const std::out_of_range& exc) {
	    return dummy;
	}
    }
    Iterator begin() const {
	return Iterator(MappedIter<Map>(idx.begin()),
			MappedIter<Map>(idx.end()));
    }
    Iterator end() const {
	return Iterator(MappedIter<Map>(idx.end()),
			MappedIter<Map>(idx.end()));
    }
    unsigned int size() const {
	unsigned int sz = 0;
	for (const auto& entry : idx) {
	    sz += entry.second.size();
	}
	return sz;
    }
};

template<typename S, typename K, const K S::Tuple::* MemPtr>
const S Index<S,K,MemPtr>::dummy;

// Partial specialization, for types small enough that it makes sense to
// preallocate all slots.
// TODO: Code duplication with main Index class.
template<typename S, const bool S::Tuple::* MemPtr>
class Index<S,bool,MemPtr> {
public:
    typedef S Wrapped;
    typedef typename Wrapped::Tuple Tuple;
    typedef Flattener<Wrapped*,typename Wrapped::Iterator> Iterator;
private:
    Wrapped array[2];
public:
    std::pair<const Tuple*,bool> insert(const Tuple& tuple) {
	return array[tuple.*MemPtr].insert(tuple);
    }
    const Wrapped& operator[](bool key) const {
	return array[key];
    }
    Iterator begin() const {
	return Iterator(&(array[0]));
    }
    Iterator end() const {
	return Iterator(NULL);
    }
    unsigned int size() const {
	return array[false].size() + array[true].size();
    }
};

// TODO:
// - Code duplication with Index class.
// - Could allocate the Wrapped class on a specialized container.
// - Could emplace the wrapped class directly on the vector, but would need
//   to implement move semantics on it, so that iterators and pointers to the
//   underlying tuples remain valid when the vector gets reallocated.
template<typename S, typename C, const Ref<C> S::Tuple::* MemPtr>
class FlatIndex {
public:
    typedef S Wrapped;
    typedef typename Wrapped::Tuple Tuple;
    typedef std::vector<std::unique_ptr<Wrapped>> PtrArray;
    typedef DerefIter<typename PtrArray::const_iterator> Iterator;
private:
    static const Wrapped dummy;
private:
    PtrArray array;
public:
    std::pair<const Tuple*,bool> insert(const Tuple& tuple) {
	return (*this)[tuple.*MemPtr].insert(tuple);
    }
    Wrapped& operator[](const Ref<C>& key) {
	while (key.value >= array.size()) {
	    array.push_back(std::unique_ptr<Wrapped>(new Wrapped()));
	}
	return *(array[key.value]);
    }
    const Wrapped& operator[](const Ref<C>& key) const {
	try {
	    return *(array.at(key.value));
	} catch (const std::out_of_range& exc) {
	    return dummy;
	}
    }
    Iterator begin() const {
	return Iterator(array.cbegin());
    }
    Iterator end() const {
	return Iterator(array.cend());
    }
    unsigned int size() const {
	unsigned int sz = 0;
	for (const std::unique_ptr<Wrapped>& elem : array) {
	    sz += elem->size();
	}
	return sz;
    }
};

template<typename S, typename C, const Ref<C> S::Tuple::* MemPtr>
const S FlatIndex<S,C,MemPtr>::dummy;

template<typename T> class PtrTable {
public:
    typedef T Tuple;
    typedef typename std::deque<const Tuple*>::const_iterator PtrIterator;
    typedef DerefIter<PtrIterator> Iterator;
private:
    std::deque<const Tuple*> store;
public:
    std::pair<const Tuple*,bool> insert(const Tuple& tuple) {
	// Assuming this is only called via a fork point, it will never be
	// called for duplicate entries, so we don't need to check.
	store.push_back(&tuple);
	return std::make_pair(&tuple, true);
    }
    Iterator begin() const {
	return Iterator(store.cbegin());
    }
    Iterator end() const {
	return Iterator(store.cend());
    }
    unsigned int size() const {
	return store.size();
    }
};

template<typename PriIdxT, typename... SecIdxTs> class MultiIndex {
public:
    typedef typename PriIdxT::Tuple Tuple;
    typedef typename PriIdxT::Iterator Iterator;
private:
    PriIdxT pri_idx;
    std::tuple<SecIdxTs...> sec_idxs;
public:
    std::pair<const Tuple*,bool> insert(const Tuple& tuple) {
	auto res = pri_idx.insert(tuple);
	// Only insert on secondary indices if tuple wasn't already present.
	if (res.second) {
	    detail::insert_all(sec_idxs, *(res.first));
	}
	return res;
    }
    const PriIdxT& primary() const {
	return pri_idx;
    }
    // TODO: Define a public array of indices instead?
    template<int I>
    const typename pack_elem<I,SecIdxTs...>::type& secondary() const {
	return std::get<I>(sec_idxs);
    }
    Iterator begin() const {
	return pri_idx.begin();
    }
    Iterator end() const {
	return pri_idx.end();
    }
    unsigned int size() const {
	return pri_idx.size();
    }
};

// OPTIMIZED MULTI-LEVEL INDEXING =============================================

// Features:
// - Iterators fill in a provided record, passed in as a set of references
// - Result tuples need to be default-constructible
//   the contents are NOT valid when passed to the iterator constructor
//   need the first call to next() to return true before there's anything valid
// - next() tries to move the iterator, and returns true if it succeeds
// - Iterators cache their position, and wrap sub iterators
//   each iterator knows if it's reached the end
// - Each iterator has the address where it must update its field on each move
//   only update it when we move on the current level, not just on sub-levels
//   (assuming the result tuples doesn't get modified by the client code)

// Primary extensions:
// - implement 'contains' (or 'find')
// - replace all uses of old framework
//   (need to suitably rewrite the data structures as PODs)
//   fix unit tests, remove relevant code
// - safe pre-allocation:
//   - pick a safe max value when writing the program
//   - enforce a static limit on the relevant Registry
//     will fail if it's asked to create more than that
//     would also help to minimize its size
//     (pick the smallest representation possible)
//     then can assume that in any Index over that
//     (will never require higher values)
//   - allow the limit to be picked dynamically (when we're done adding refs)
//     but "freeze" that set, and assert it's not frozen whenever it's modified
//     (perhaps only during debugging)
// - implement multi-indexing properly, to connect all dimensions
// - implement clear()
//   special case enabled if the underlying type is simple: memset to 0
//   should then probably use an array instead of a vector
//   this should allow us to reuse this scheme for worker Worklist
//   (and share with all)
// - more robust index use: each level knows what attribute it controls
//   instantiated with a member variable pointer
//   use that pointer to decide what to modify on the result tuple
//   harder to do on the bottom container:
//   variadic templates don't allow non-type parameters
//   - use a unique empty tag type for each member
//   - implement through macros
//   - use typelists
// - use an actual struct for the results
//   (even though no such struct is stored)
//   pass in a default-constructed such struct
//   some restrictions are imposed on this struct
//   (e.g. its fields can't be const)
//   all iterators accept pointer to full struct
// - special Table version for one or more small-width/ref types: BitSet
//   max width is provided through a hint parameter (for Refs: the Registry)
//   can then implement TwoDimBitSet as FlatIndex<...BitSet<...>>
//   (as long as we also implement clear() efficiently)
// - set the key parameters on iteration
//   mostly for completeness: the whole result tuple is now valid
//   if parameters haven't been exhausted yet, set that field directly
// - combine the two dimensions of lookup and query
//   while parameters haven't been exhausted, pick that key and follow it
//   if now parameters remaining, start iteration
//   also need to explicitly specify which dimension we pick
// - hack to avoid iterating on bottom fields, if not requested
//   create an iterator to a dummy singleton container

// Secondary extensions:
// - abstract-out size type
// - auto-increment attributes
// - more index implementations:
//   - hashtable
//   - dynamically growing flat table of pointers
//   - sorted k-ptr to allocated wrapped
// - named indices (use classes as index tags)
// - handle derived classes correctly
// - FlatIndex for small enums
// - keep the kvlist of LightIndex sorted
// - additional FD-mandated fields
//   check dynamically -- merge function?
//   no space overhead! will be second part of tuple
//   but can't index on those levels, because they're mutable
//   can edit through containers
// - reduce code duplication among index classes
// - having trouble using std::forward and &&
//   for now, doing all additions through const refs => copying occurs
// - use boost::multi_index or boost::intrusive
// - can simply use the first choice when dereferencing a MultiIndex?
//   will work if there's no type mix
// - indexing on multiple attributes concurrently (e.g. dir + symbol)
// - pattern matching-like support:
//   - ignores
//     especially convenient if they concern fields at the bottom of the tree
//     don't iterate over the rest of the indices, since we don't require them
//   - result fields
//   - provided fields
//   - constraints (richer filtering)
//     passed to the iterators as a predicate
//   to query, pass a pattern down the index tree
//   then each level:
//   - uses the correct map
//   - removes the matched field from the pattern
//   - passes the new pattern down
//   each pattern defines a "view"
//   i.e. a data structure optimized for retrieving only part of the answer
// - each level "reports" its full set of handled attributes
//   combines its own field with those of its children
//   this way, at fork points we can decide based on what the children report
//   need a way to produce a normal form for "set of handled fields"
//   => need member pointer ordering and equality
// - just use tuples
//   associate member variables with unique, empty type 'tags'
//   could provide a wrapper that presents it as a struct
// - special iteration syntax:
//   - re-use range-for loop syntax:
//     - operator* returns a reference to the result struct
//     - begin() calls iter()
//     - end() returns a constant, maybe even not of the same type
//     - operator== only works for the end() special value, and triggers next()
//     - operator++ does nothing
//     this is highly irregular semantically
//   - define custom syntax through macros
//     maybe even combine it with typelist macros:
//     e.g. QUERY(e, edges, false, src) ==>
//     for (typename decltype(edges)::Tuple e,
//          typename decltype(edges)::Iterator it = edges.iter(e, false);
// 	 it.next();)
// - currently can only have FlatIndex on the top
//   generalize hint passing to allow this deeper in the hierarchy
//   would need to cache the widths on parent levels
//   because we'd need to construct new sub-indices dynamically
// - support resizing of Flatindex?
//   would need to support move semantics on indices
// - outer iterator instantiates the result struct
//   will need to allocate on the heap, and manage its lifetime
//   call separate functions on the wrapped iters, that don't allocate a struct
//   return a const ref to the caller => we're certain they haven't modified it
// - ensure that all fields are covered
//   - recursively construct the full tuple covered by all indices
//     need to sort and unique the fields
//     compare with a typedef of the expected struct
//   - build the tuple we're expecting to get
//     then compare its size with the result struct
//     but might be different order
//     can't rely on this behavior anyway
//   - include the type of the whole struct at the bottom container
//     also at the top?
//   - check that all forks give the same resulting tuple
//   but might actually not want that
// - verify that the final struct is a POD, and has default constructor
// - modifiers on iterators (e.g. filter, map)
// - MultiIndex: two choices
//   - sets on all dimensions
//     skip inserting on the remaining dimensions if didn't succeed on one
//   - set only on primary dimension
//     insertion on that must be done first
//     not sound if a dimension is missing fields
// - selection/search patterns could also use typelists
//   for now, just to ensure that we're selecting on the expected field
//   actual instance of the type pair would include:
//   - the value for fields
//   - dimension to follow at fork points
//   would unwrap it to get the rest of the arguments
// - allowing type selectors in any order (and picking dimension automatically)
//   needs a method to extract the relevant part of the typelist
// - typelist functionality:
//   - sorting
//   - sentinel value at the end (nil)

namespace mi {

template<class... Cols> class Table {
public:
    class Iterator;
    friend Iterator;
private:
    typedef std::tuple<Cols...> Tuple;
private:
    std::set<Tuple> store;
public:
    bool insert(const Cols&... flds) {
	return store.emplace(flds...).second;
    }
    Iterator iter(Cols&... tgt_flds) const {
	Iterator it(tgt_flds...);
	it.migrate(*this);
	return it;
    }
    unsigned int size() const {
	return store.size();
    }
public:

    class Iterator {
    private:
	typename std::set<Tuple>::const_iterator curr;
	typename std::set<Tuple>::const_iterator end;
	std::tuple<Cols&...> tgt_flds;
	bool before_start = true;
    public:
	explicit Iterator(Cols&... tgt_flds)
	    : tgt_flds(std::tie(tgt_flds...)) {}
	void migrate(const Table& table) {
	    curr = table.store.cbegin();
	    end = table.store.cend();
	    before_start = true;
	}
	bool next() {
	    if (before_start) {
		before_start = false;
	    } else {
		++curr;
	    }
	    if (curr == end) {
		return false;
	    }
	    tgt_flds = *curr;
	    return true;
	}
    };
};

template<class Key, class Sub> class Index {
public:
    class Iterator;
    friend Iterator;
private:
    static const Sub dummy;
private:
    std::map<Key,Sub> map;
public:
    const Sub& operator[](const Key& key) const {
	// TODO: Should just create new entries?
	try {
	    return map.at(key);
	} catch (const std::out_of_range& exc) {
	    return dummy;
	}
    }
    template<class... Cols>
    bool insert(const Key& key, Cols&&... flds) {
	return map[key].insert(std::forward<Cols>(flds)...);
    }
    template<class... Cols>
    Iterator iter(Key& tgt_key, Cols&... tgt_flds) const {
	Iterator it(tgt_key, tgt_flds...);
	it.migrate(*this);
	return it;
    }
    unsigned int size() const {
	unsigned int sz = 0;
	for (const auto& entry : map) {
	    sz += entry.second.size();
	}
	return sz;
    }
public:

    class Iterator {
    private:
	typename std::map<Key,Sub>::const_iterator map_curr;
	typename std::map<Key,Sub>::const_iterator map_end;
	Key& tgt_key;
	typename Sub::Iterator sub_iter;
	bool before_start = true;
    public:
	template<class... Cols>
	explicit Iterator(Key& tgt_key, Cols&... tgt_flds)
	    : tgt_key(tgt_key), sub_iter(tgt_flds...) {}
	void migrate(const Index& idx) {
	    map_curr = idx.map.cbegin();
	    map_end = idx.map.cend();
	    before_start = true;
	}
	bool next() {
	    if (before_start) {
		before_start = false;
		if (map_curr == map_end) {
		    return false;
		}
		tgt_key = map_curr->first;
		sub_iter.migrate(map_curr->second);
	    }
	    while (!sub_iter.next()) {
		++map_curr;
		if (map_curr == map_end) {
		    return false;
		}
		tgt_key = map_curr->first;
		sub_iter.migrate(map_curr->second);
	    }
	    return true;
	}
    };
};

template<class Key, class Sub>
const Sub Index<Key,Sub>::dummy;

template<class T> struct KeyTraits;

template<> struct KeyTraits<bool> {
    typedef std::nullptr_t SizeHint;
    static unsigned int extract_size(const SizeHint&) {
	return 2;
    }
    static unsigned int extract_idx(bool val) {
	return val;
    }
    static bool from_idx(unsigned int idx) {
	return idx;
    }
};

template<class T>
struct KeyTraits<Ref<T>> {
    typedef Registry<T> SizeHint;
    static unsigned int extract_size(const SizeHint& reg) {
	return reg.size();
    }
    static unsigned int extract_idx(Ref<T> ref) {
	return ref.value;
    }
    static Ref<T> from_idx(unsigned int idx) {
	return Ref<T>(idx);
    }
};

template<class Key, class Sub> class FlatIndex {
public:
    class Iterator;
    friend Iterator;
private:
    std::vector<Sub> array;
public:
    template<class... Rest>
    explicit FlatIndex(const typename KeyTraits<Key>::SizeHint& hint,
		       const Rest&... rest)
	: array(KeyTraits<Key>::extract_size(hint), Sub(rest...)) {}
    const Sub& operator[](const Key& key) const {
	unsigned int i = KeyTraits<Key>::extract_idx(key);
#ifdef NDEBUG
	return array[i];
#else
	return array.at(i);
#endif
    }
    template<class... Cols>
    bool insert(const Key& key, Cols&&... flds) {
	unsigned int i = KeyTraits<Key>::extract_idx(key);
#ifdef NDEBUG
	return array[i].insert(std::forward<Cols>(flds)...);
#else
	return array.at(i).insert(std::forward<Cols>(flds)...);
#endif
    }
    template<class... Cols>
    Iterator iter(Key& tgt_key, Cols&... tgt_flds) const {
	Iterator it(tgt_key, tgt_flds...);
	it.migrate(*this);
	return it;
    }
    unsigned int size() const {
	unsigned int sz = 0;
	for (const Sub& entry : array) {
	    sz += entry.size();
	}
	return sz;
    }
public:

    class Iterator {
    private:
	unsigned int arr_idx;
	typename std::vector<Sub>::const_iterator arr_curr;
	typename std::vector<Sub>::const_iterator arr_end;
	Key& tgt_key;
	typename Sub::Iterator sub_iter;
	bool before_start = true;
    public:
	template<class... Cols>
	explicit Iterator(Key& tgt_key, Cols&... tgt_flds)
	    : tgt_key(tgt_key), sub_iter(tgt_flds...) {}
	void migrate(const FlatIndex& idx) {
	    arr_idx = 0;
	    arr_curr = idx.array.cbegin();
	    arr_end = idx.array.cend();
	    before_start = true;
	}
	bool next() {
	    if (before_start) {
		before_start = false;
		if (arr_curr == arr_end) {
		    return false;
		}
		tgt_key = KeyTraits<Key>::from_idx(arr_idx);
		sub_iter.migrate(*arr_curr);
	    }
	    while (!sub_iter.next()) {
		++arr_idx;
		++arr_curr;
		if (arr_curr == arr_end) {
		    return false;
		}
		tgt_key = KeyTraits<Key>::from_idx(arr_idx);
		sub_iter.migrate(*arr_curr);
	    }
	    return true;
	}
    };
};

// Unsorted list of key-value pairs.
template<class Key, class Sub> class LightIndex {
public:
    class Iterator;
    friend Iterator;
private:
    typedef std::forward_list<std::pair<Key,Sub>> List;
private:
    static const Sub dummy;
private:
    List list;
public:
    const Sub& operator[](const Key& key) const {
	auto key_matches = [&](const std::pair<Key,Sub>& elem) {
	    return key == elem.first;
	};
	auto pos = std::find_if(list.cbegin(), list.cend(), key_matches);
	if (pos == list.cend()) {
	    return dummy;
	}
	return pos->second;
    }
    template<class... Cols>
    bool insert(const Key& key, Cols&&... flds) {
	auto key_matches = [&](const std::pair<Key,Sub>& elem) {
	    return key == elem.first;
	};
	auto pos = std::find_if(list.begin(), list.end(), key_matches);
	if (pos == list.end()) {
	    list.emplace_front(key, Sub());
	    pos = list.begin();
	}
	return pos->second.insert(std::forward<Cols>(flds)...);
    }
    template<class... Cols>
    Iterator iter(Key& tgt_key, Cols&... tgt_flds) const {
	Iterator it(tgt_key, tgt_flds...);
	it.migrate(*this);
	return it;
    }
    unsigned int size() const {
	unsigned int sz = 0;
	for (const auto& entry : list) {
	    sz += entry.second.size();
	}
	return sz;
    }
public:

    class Iterator {
    private:
	typename List::const_iterator list_curr;
	typename List::const_iterator list_end;
	Key& tgt_key;
	typename Sub::Iterator sub_iter;
	bool before_start = true;
    public:
	template<class... Cols>
	explicit Iterator(Key& tgt_key, Cols&... tgt_flds)
	    : tgt_key(tgt_key), sub_iter(tgt_flds...) {}
	void migrate(const LightIndex& idx) {
	    list_curr = idx.list.cbegin();
	    list_end = idx.list.cend();
	    before_start = true;
	}
	bool next() {
	    if (before_start) {
		before_start = false;
		if (list_curr == list_end) {
		    return false;
		}
		tgt_key = list_curr->first;
		sub_iter.migrate(list_curr->second);
	    }
	    while (!sub_iter.next()) {
		++list_curr;
		if (list_curr == list_end) {
		    return false;
		}
		tgt_key = list_curr->first;
		sub_iter.migrate(list_curr->second);
	    }
	    return true;
	}
    };
};

template<class Key, class Sub>
const Sub LightIndex<Key,Sub>::dummy;

// TODO: Extend this to perform filtering
template<class T> T& ignore() {
    static T dummy;
    return dummy;
}

} // namespace mi

#endif
