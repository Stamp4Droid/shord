#ifndef UTIL_HPP
#define UTIL_HPP

#include <boost/filesystem.hpp>
#include <cassert>
#include <functional>
#include <iostream>
#include <iterator>
#include <limits>
#include <map>
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

// ITERATOR HANDLING ==========================================================

// Very simple iterator wrapper, cannot write through it (constant iterator).
// The wrapper code must return a reference to an object. It can do that by
// either creating and managing a temporary object (hard to do with the current
// design), or derive a reference to some component of the element at the
// current position of the underlying iterator (which should remain alive
// until the underlying iterator's next move).
// TODO: Missing operators:
// iter++, default constructor, copy assignment, destructor, swap, ->
template<typename Iter, typename Out,
	 const Out& F(const typename std::iterator_traits<Iter>::value_type&)>
class IterWrapper : public std::iterator<std::input_iterator_tag,Out> {
private:
    Iter iter;
public:
    IterWrapper(Iter iter) : iter(iter) {}
    IterWrapper(const IterWrapper& other) : iter(other.iter) {}
    IterWrapper& operator=(const IterWrapper& other) = delete;
    const Out& operator*() const {
	return F(*iter);
    }
    IterWrapper& operator++() {
	++iter;
	return *this;
    }
    bool operator==(const IterWrapper& other) const {
	return iter == other.iter;
    }
    bool operator!=(const IterWrapper& other) const {
	return !(*this == other);
    }
};

// GENERIC DATA STRUCTURES ====================================================

// Properties:
// - Guarantees no duplicate entries in the queue.
// - FIFO ordering.
// - Enqueues and dequeues are done by copying.
//   (need to be careful with large composites)
// - Stored classes need to be comparable.
// TODO:
// - copy/iterator constructor
// - Boolean flag could be made into a template parameter.
template<typename T> class Worklist {
private:
    const bool can_reprocess;
    std::set<T> reached;
    std::queue<T> queue;
public:
    explicit Worklist(bool can_reprocess) : can_reprocess(can_reprocess) {}
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
	if (can_reprocess) {
	    reached.erase(val);
	}
	return val;
    }
};

// HELPER CODE ================================================================

namespace detail {

inline const boost::filesystem::path&
get_path(const boost::filesystem::directory_entry& entry) {
    return entry.path();
}

template<typename T>
const T& unwrap_ref(const std::reference_wrapper<T>& r) {
    return r.get();
}

template<typename T>
const T& deref(const T* const& ptr) {
    return *ptr;
}

template<typename T, typename V, int I>
struct TupleInserter {
    static void insert(T& idxs, V val) {
	TupleInserter<T,V,I-1>::insert(idxs, val);
	std::get<I-1>(idxs).insert(val);
    }
};

template<typename T, typename V>
struct TupleInserter<T,V,0> {
    static void insert(T&, V) {}
};

template<typename T, typename V>
void insert_all(T& idxs, V val) {
    TupleInserter<T,V,std::tuple_size<T>::value>::insert(idxs, val);
}

} // namespace detail

// OS SERVICES ================================================================

class Directory {
public:
    typedef IterWrapper<boost::filesystem::directory_iterator,
			boost::filesystem::path,detail::get_path> Iterator;
private:
    const boost::filesystem::path path;
public:
    explicit Directory(const std::string& name) : path(name) {
	assert(boost::filesystem::is_directory(path));
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

// Alternative: store pointers to objects:
//   std::vector<T*> array;
//   std::map<std::string,T*> map;
// Managed classes must define a 'make' friend function template, used for
// initialization (normally just calls 'new'):
// template<typename T, typename... ArgTs>
// T* make(const std::string& name, Ref<T> ref, ArgTs&&... args) {
//     return new T(name, ref, std::forward<ArgTs>(args)...);
// }

template<typename T> class Ref;

template<typename T>
std::ostream& operator<<(std::ostream& os, const Ref<T>& ref) {
    assert(ref.valid());
    os << ref.value;
    return os;
}

template<typename T> class Registry;

// TODO: Could make this class const-correct, i.e. get a const& when indexing a
// Registry using a const Ref.
template<typename T> class Ref {
    friend Registry<T>;
    friend std::ostream& operator<< <>(std::ostream& os, const Ref<T>& ref);
private:
    unsigned int value;
private:
    explicit Ref(unsigned int value) : value(value) {}
    static Ref<T> for_value(unsigned int value) {
	Ref<T> ref(value);
	assert(ref.valid());
	return ref;
    }
public:
    bool valid() const {
	return value < std::numeric_limits<unsigned int>::max();
    }
    static Ref<T> none() {
	return Ref<T>(std::numeric_limits<unsigned int>::max());
    }
    bool operator==(const Ref& other) const {
	return value == other.value;
    }
    bool operator!=(const Ref& other) const {
	return !(*this == other);
    }
    bool operator<(const Ref& other) const {
	return value < other.value;
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
    T& operator[](const Ref<T> ref) {
	assert(ref.valid());
	return array.at(ref.value).get();
    }
    const T& operator[](const Ref<T> ref) const {
	assert(ref.valid());
	return array.at(ref.value).get();
    }
    template<typename... ArgTs> T& make(const Key& key, ArgTs&&... args) {
	Ref<T> next_ref = Ref<T>::for_value(array.size());
	auto res = map.emplace(key, T(key, next_ref,
				      std::forward<ArgTs>(args)...));
	assert(res.second);
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
// - implement proper iterators on the secondary index path
// - variadic template of all indexing fields
//   - shouldn't have to redefine Index twice, for the two cases of >3 and 3
//     should instead be able to say that Index<T> = T
//   - bug: would then be able to kill the matching of nested secondary indices
//     by calling primary_select on an intermediate step
//   but can't have non-types as variadic template parameters
// - exploit functional dependencies
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
// - Relation<T>:
//   - typedef Tuple = T
//   - typedef Iterator: iterator with traits:
//     - constant iterator
//     - value_type = Tuple
//     - iterator_category = input_iterator_tag (at least)
//   - std::pair<const Tuple*,bool> insert(const Tuple& tuple)
//   - Iterator begin() const
//   - Iterator end() const
//   - unsigned int size() const
// - SecIndex<T> : Relation<T>
//   except:
//   - void insert(const Tuple* ptr)

// Implementations:
// - Table<T> : Relation<T>
// - Index<S,K,MemPtr> : Relation<T>
//   where:
//   - S : Relation<T>
//   - MemPtr is a pointer to a member of T, of type K
//   additional:
//   - typedef Wrapped = S
//   - typedef Key = K
//   - const Wrapped& operator[](const Key& key) const
// - PtrTable<T> : SecIndex<T>
// - PtrIndex<S,K,MemPtr> : SecIndex<T>
//   where:
//   - S : SecIndex<T>
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
public:

    // TODO: Missing operators: iter++, destructor
    // XXX: The sub-iterators are reset to their default values when the map
    // iterator moves to the next element. Such default-value iterators will be
    // used in comparisons, so we require that all default-constructed
    // iterators of type Wrapped::Iterator compare equal.
    class Iterator : public std::iterator<std::input_iterator_tag,Tuple> {
    private:
	typename Map::const_iterator map_curr;
	typename Map::const_iterator map_end;
	typename Wrapped::Iterator sub_curr;
	typename Wrapped::Iterator sub_end;
    private:
	void skip_empty_entries() {
	    while (sub_curr == sub_end) {
		++map_curr;
		if (map_curr == map_end) {
		    sub_curr = typename Wrapped::Iterator();
		    sub_end = typename Wrapped::Iterator();
		    return;
		}
		sub_curr = map_curr->second.begin();
		sub_end = map_curr->second.end();
	    }
	}
    public:
	Iterator() {}
	Iterator(const Map& map, bool at_end)
	    : map_curr(at_end ? map.cend() : map.cbegin()),
	      map_end(map.cend()) {
	    if (map_curr != map_end) {
		sub_curr = map_curr->second.begin();
		sub_end = map_curr->second.end();
		skip_empty_entries();
	    }
	}
	Iterator(const Iterator& other)
	    : map_curr(other.map_curr), map_end(other.map_end),
	      sub_curr(other.sub_curr), sub_end(other.sub_end) {}
	Iterator& operator=(const Iterator& other) {
	    map_curr = other.map_curr;
	    map_end = other.map_end;
	    sub_curr = other.sub_curr;
	    sub_end = other.sub_end;
	    return *this;
	}
	const Tuple& operator*() const {
	    return *sub_curr;
	}
	const Tuple* operator->() const {
	    return &(*sub_curr);
	}
	Iterator& operator++() {
	    ++sub_curr;
	    skip_empty_entries();
	    return *this;
	}
	bool operator==(const Iterator& other) const {
	    return (map_curr == other.map_curr && map_end == other.map_end &&
		    sub_curr == other.sub_curr && sub_end == other.sub_end);
	}
	bool operator!=(const Iterator& other) const {
	    return !(*this == other);
	}
    };

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
	return Iterator(idx, false);
    }
    Iterator end() const {
	return Iterator(idx, true);
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

template<typename T> class PtrTable {
public:
    typedef T Tuple;
    typedef typename std::deque<const Tuple*>::const_iterator PtrIterator;
    typedef IterWrapper<PtrIterator,Tuple,detail::deref<Tuple>> Iterator;
private:
    std::deque<const Tuple*> store;
public:
    void insert(const Tuple* ptr) {
	// Assuming this is only called via a fork point, it will never be
	// called for duplicate entries, so we don't need to check.
	store.push_back(ptr);
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

template<typename S, typename K, const K S::Tuple::* MemPtr> class PtrIndex {
public:
    typedef S Wrapped;
    typedef typename Wrapped::Tuple Tuple;
    typedef K Key;
private:
    static const Wrapped dummy;
private:
    std::map<Key,Wrapped> idx;
public:
    void insert(const Tuple* ptr) {
	idx[ptr->*MemPtr].insert(ptr);
    }
    const Wrapped& operator[](const Key& key) const {
	try {
	    return idx.at(key);
	} catch (const std::out_of_range& exc) {
	    return dummy;
	}
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
const S PtrIndex<S,K,MemPtr>::dummy;

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
	    detail::insert_all(sec_idxs, res.first);
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

// TODO:
// - Could alternatively produce the output by:
//   - constructing a (constant) iterable view
//   - sending to an output iterator
//   - filling in a provided container
// - This is a very specific case of a proper lazy iterator framework.
template <typename C, typename S>
Table<S> filter_map(const C& table,
		    std::function<bool(const typename C::Tuple&)> pred,
		    std::function<S(const typename C::Tuple&)> mod) {
    Table<S> res;
    for (const typename C::Tuple& t : table) {
	if (pred(t)) {
	    // TODO: Extraneous copying could occur here.
	    res.insert(mod(t));
	}
    }
    return res;
}

#endif
