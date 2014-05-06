#ifndef UTIL_HPP
#define UTIL_HPP

#include <boost/filesystem.hpp>
#include <boost/optional.hpp>
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
#include <sys/time.h>
#include <tuple>
#include <vector>

// TYPE TRAITS ================================================================

template<int I, typename... Types> struct pack_elem {
public:
    typedef typename std::tuple_element<I,std::tuple<Types...>>::type type;
};

template<class T, class S> struct cons {};

template<class H, class... Ts>
struct cons<H,std::tuple<Ts...>> {
    typedef std::tuple<H,Ts...> type;
};

template<unsigned int... Is> struct seq {};

template<unsigned int N, unsigned int... Is>
struct gen_seq : gen_seq<N-1,N-1,Is...> {};

template<unsigned int... Is>
struct gen_seq<0,Is...> : seq<Is...> {};

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

// CUSTOM ORDERING ============================================================

// TODO: Generic solution for comparing multiple dimensions, to implement
// simple struct comparison

template<class T>
typename std::enable_if<std::is_arithmetic<T>::value ||
			std::is_enum<T>::value, int>::type
compare(const T& lhs, const T& rhs) {
    if (lhs == rhs) {
	return 0;
    }
    if (lhs < rhs) {
	return -1;
    }
    return 1;
}

template<class T>
int compare(const std::set<T>& lhs, const std::set<T>& rhs) {
    auto l_curr = lhs.cbegin();
    auto r_curr = rhs.cbegin();
    const auto l_end = lhs.cend();
    const auto r_end = rhs.cend();

    while (true) {
	if (l_curr == l_end) {
	    if (r_curr == r_end) {
		return 0;
	    }
	    return -1;
	}
	if (r_curr == r_end) {
	    return 1;
	}
	int curr_rel = compare(*l_curr, *r_curr);
	if (curr_rel != 0) {
	    return curr_rel;
	}
	// *l_curr == *r_curr
	++l_curr;
	++r_curr;
    }
}

template<class T>
int map_compare(const T& lhs, const T& rhs) {
    auto l_curr = lhs.cbegin();
    auto r_curr = rhs.cbegin();
    const auto l_end = lhs.cend();
    const auto r_end = rhs.cend();

    while (true) {
	if (l_curr == l_end && r_curr == r_end) {
	    return 0;
	}
	// l_curr != l_end || r_curr != r_end
	if (l_curr == l_end // => r_curr != r_end
	    || (r_curr != r_end && r_curr->first < l_curr->first)) {
	    if (!r_curr->second.empty()) {
		return -1;
	    }
	    ++r_curr;
	    continue;
	}
	// l_curr != l_end
	if (r_curr == r_end || l_curr->first < r_curr->first) {
	    if (!l_curr->second.empty()) {
		return 1;
	    }
	    ++l_curr;
	    continue;
	}
	// l_curr != l_end && r_curr != r_end && *l_curr == *r_curr
	int sub_rel = compare(l_curr->second, r_curr->second);
	if (sub_rel != 0) {
	    return sub_rel;
	}
	++l_curr;
	++r_curr;
    }
}

template<class K, class V>
int compare(const std::map<K,V>& lhs, const std::map<K,V>& rhs) {
    return map_compare(lhs, rhs);
}

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
// iter++, copy assignment, destructor, swap
// TODO: Correctly set the iterator tag, according to the tag of the wrapped
// iterator.
template<typename Iter, typename Out,
	 const Out& F(const typename std::iterator_traits<Iter>::value_type&)>
class IterWrapper : public std::iterator<std::input_iterator_tag,Out> {
private:
    Iter iter;
public:
    explicit IterWrapper() {}
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

// GENERIC DATA STRUCTURES & ALGORITHMS =======================================

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

// TODO:
// - Also feed the common key to zip?
// - Should implement using iterators?
template<class LMap, class RMap, class ZipT>
void join_zip(const LMap& l, const RMap& r, const ZipT& zip) {
    auto l_curr = l.begin();
    auto r_curr = r.begin();
    const auto l_end = l.end();
    const auto r_end = r.end();
    while (l_curr != l_end && r_curr != r_end) {
	switch (compare(l_curr->first, r_curr->first)) {
	case -1:
	    ++l_curr;
	    break;
	case 1:
	    ++r_curr;
	    break;
	case 0:
	    zip(l_curr->second, r_curr->second);
	    ++l_curr;
	    ++r_curr;
	    break;
	default:
	    assert(false);
	}
    }
}

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

unsigned int current_time() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (tv.tv_sec) * 1000 + (tv.tv_usec) / 1000;
}

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
//   managed classes should contain a reference to their Registry?
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
private:
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
    friend int compare(const Ref& lhs, const Ref& rhs) {
	return compare(lhs.value, rhs.value);
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
    bool empty() const {
	return array.empty();
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
// - Each level handles a specific tag.
// - Regular iterators iterate through the keys
//   will attempt to retun only non-empty keys
// - Iterators fill in a provided record.
//   Special "FOR" macro provided to do this automatically.
// - Insertion can work with distinct fields, a tuple, or a series of fields
//   ending with a tuple.
// - Fields need to be default-constructible
//   the contents are NOT valid when passed to the iterator constructor
//   need the first call to next() to return true before there's anything valid
// - next() tries to move the iterator, and returns true if it succeeds
// - Iterators cache their position, and wrap sub iterators
//   each iterator knows if it's reached the end
// - Each iterator has the address where it must update its field on each move
//   only update it when we move on the current level, not just on sub-levels
//   (assuming the result tuples doesn't get modified by the client code)
// - Union operation
// - Join (static operation)
//   only works for relations of the form Index<T,Table<T>>
// - Identity (static operation)
//   only works for relations where all columns are of the same type
// - Instantiated nested sets are NOT guaranteed to be non-empty.

// Primary extensions:
// - tag uniqueness check
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
// - don't peel the insertion or iteration tuple
//   instead combine with index after which to read
//   alias top-level insert(tuple) as insert(0, tuple)
// - work with existing tuples
//   instantiate each level with a member variable pointer
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
// - set the key parameters on iteration
//   mostly for completeness: the whole result tuple is now valid
//   if parameters haven't been exhausted yet, set that field directly
// - combine the two dimensions of lookup and query
//   while parameters haven't been exhausted, pick that key and follow it
//   if now parameters remaining, start iteration
//   also need to explicitly specify which dimension we pick
// - hack to avoid iterating on bottom fields, if not requested
//   create an iterator to a dummy singleton container
// - primitive multi-dimension support:
//   - from top only (special wrapper to "close" the hierarchy?)
//   - primary index tuple order becomes the exported Tuple
//   - 'insert' automatically adds on both dimensions, does required reordering

// Secondary extensions:
// - abstract-out size type
// - auto-increment attributes
// - more index implementations:
//   - hashtable -- complicated by the need to implement ordering
//     (needed for uniquing using a std::set)
//   - dynamically growing flat table of pointers
//   - sorted k-ptr to allocated wrapped
// - more table implementations:
//   - Godel encoding
// - handle derived classes correctly
// - FlatIndex for small enums
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
// - provide a wrapper that presents a NamedTuple as a struct
//   how to combine multiple levels?
// - currently can only have FlatIndex on the top
//   generalize hint passing to allow this deeper in the hierarchy
//   would need to cache the widths on parent levels
//   because we'd need to construct new sub-indices dynamically
//   or store them on a special top-level wrapper
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
// - more flexible BitSet
//   - nesting under non-flat indices
//   - bitsets for multiple fields (currently have to wrap with FlatIndex)
// - implement clear()
//   special case enabled if the underlying type is simple: memset to 0
//   should then probably use an array instead of a vector
//   this should allow us to reuse this scheme for worker Worklist
//   (and share with all)
// - ability to iterate only partially to the end
// - some way to propagate the column type pack from the wrapped table.
// - also accept a series of fields ending with a tuple for iter()
// - implement some operations as algorithms instead
// - have a backdoor to get a non-const ref to the wrapped set
// - more efficient operator< implementation, by using a three-valued compare
//   primitive (=, <, >)
// - more operations on NamedTuple
//   especially field retrieval
// - support multi-field Table
//   will have extend NamedTuple to support full tuples
//   could provide the same functionality through a thin CombiningIndex wrapper
// - 'remove' and 'update' operations
// - combine multiple flat dimensions into a contiguous memory block
//   (currently only happens for a single FlatIndex over a BitSet)
//   parent would have to preallocate space => ask child for size
//   all sub-containers must have the same size
//   can this be done dynamically?
//   need variable-size class support?
// - TopIter on Table iterates over the values
// - use probabilistic data structures (as long as they're sound)
// - exploit sharing of bit patterns
// - disable operator[], only allow constraining the top iterator
//   then we get a full tuple (but more overhead when iterating?)
// - "full" observer on index

// Uniquing infrastructure:
// - works for any kind of Index class
// - objects of a uniq'd class wrap a reference to a const instance of the
//   underlying class
// - calls to constant member functions simply get forwarded
// - modifying operations are first looked up in the corresponding cache
//   if not found, the operation is performed on a new copy
//   the new copy is then added to the uniquing set

// Extensions:
// - automatically include (self,self)->self in union cache when constructing
// - when performing an insertion, also update the union cache with the
//   corresponding singleton union operation
// - can't merge into a nested Index of a uniq'd Index
// - can't mix fields and tuples when inserting
//   need the args in a tuple, to cache
// - constructor for uniq'd Indices can't take arguments
// - uniquing indices requires that they can be compared
//   have to implement operator< on all classes
// - lots of copying:
//   - the real instance of the Index gets copied when added to the Registry
//   - two instances of each underlying Index exist:
//     one on the Registry set, one on the const cell (as the Key)
//   - args are copied on a fresh tuple when insert is called with references
//     could save some copying if insert could work with rvalues
//   - argument tuples are copied on the insert cache
// - also cache the results of const operations
//   (store as fields on the corresponding backer cell object)
//   should have a generic Maybe<> template, which carries the value and a
//   validity flag
// - also support uniquing single tables
// - caches are static variables, this complicates concurrent operation
// - take advantage of uniquing in operations with special cases
//   e.g. implement empty() as comparison with the empty-index container
//   since they'll be uniqued

namespace mi {

// NAMED TUPLES ===============================================================

template<class Tag, class Hd, class Tl> class NamedTuple;

namespace detail {

template<class FldN, class NT> struct Getter;

template<class Tag, class Hd, class Tl>
struct Getter<Tag, NamedTuple<Tag,Hd,Tl>> {
    typedef Hd FldT;
    static FldT& get(NamedTuple<Tag,Hd,Tl>& ntup) {
	return ntup.hd;
    }
};

template<class FldN, class Tag, class Hd, class Tl>
struct Getter<FldN, NamedTuple<Tag,Hd,Tl>> {
    typedef typename Getter<FldN,Tl>::FldT FldT;
    static FldT& get(NamedTuple<Tag,Hd,Tl>& ntup) {
	return Getter<FldN,Tl>::get(ntup.tl);
    }
};

} // namespace detail

struct Nil {
    bool operator<(const Nil&) const {
	return false;
    }
    friend std::ostream& operator<<(std::ostream& os, const Nil&) {
	return os;
    }
};

template<class Tag, class Hd, class Tl>
class NamedTuple {
public:
    Hd hd;
    Tl tl;
public:
    explicit NamedTuple() {}
    template<class... Rest>
    explicit NamedTuple(const Hd& hd, const Rest&... rest)
	: hd(hd), tl(rest...) {}
    template<class FldN>
    typename detail::Getter<FldN,NamedTuple>::FldT get() {
	return detail::Getter<FldN,NamedTuple>::get(*this);
    }
    bool operator<(const NamedTuple& rhs) const {
	if (hd < rhs.hd) {
	    return true;
	}
	if (rhs.hd < hd) {
	    return false;
	}
	return tl < rhs.tl;
    }
    friend std::ostream& operator<<(std::ostream& os, const NamedTuple& tup) {
	os << Tag::name() << "=" << tup.hd << " " << tup.tl;
	return os;
    }
};

template<class T>
struct tuple_size;

template<>
struct tuple_size<Nil> {
    static const unsigned int value = 0;
};

template<class Tag, class Hd, class Tl>
struct tuple_size<NamedTuple<Tag,Hd,Tl>> {
    static const unsigned int value = tuple_size<Tl>::value + 1;
};

#define TUPLE_TAG(NAME) struct NAME {static const char* name() {return #NAME;}}

// HELPER CODE ================================================================

template<class T> struct KeyTraits;

template<> struct KeyTraits<bool> {
    typedef boost::none_t SizeHint;
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

template<class Idx> struct flat_dims {
    static const unsigned int value = 0;
};

template<class Tag, class K, class S> class FlatIndex;
template<class Tag, class K, class S> struct flat_dims<FlatIndex<Tag,K,S>> {
    static const unsigned int value = flat_dims<S>::value + 1;
};

template<class Tag, class T> class BitSet;
template<class Tag, class T> struct flat_dims<BitSet<Tag,T>> {
    static const unsigned int value = 1;
};

namespace detail {

template<class T, class S>
const T& get_first(const std::pair<const T,S>& p) {
    return p.first;
}

template<class T, unsigned int I>
static const T& id(const T& val) {
    return val;
}

} // namespace detail

// BASE CONTAINERS & OPERATIONS ===============================================

#define FOR_CNSTR(RES, EXPR, ...) \
    if (bool cond__ = true) \
	for (typename std::remove_reference<decltype(EXPR)>::type::Tuple RES; \
	     cond__; cond__ = false) \
	    for (auto it__ = (EXPR).iter(RES, __VA_ARGS__); it__.next();)

#define FOR(RES, EXPR) FOR_CNSTR(RES, EXPR, boost::none)

const boost::none_t any = boost::none;

template<class Idx> Idx join(const Idx& l, const Idx& r);

template<class Tag, class T> class Table {
public:
    class Iterator;
    friend Iterator;
    typedef NamedTuple<Tag,T,Nil> Tuple;
private:
    std::set<T> store;
public:
    bool insert(const T& val) {
	return store.insert(val).second;
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd);
    }
    bool copy(const Table& src) {
	unsigned int old_sz = size();
	store.insert(src.store.cbegin(), src.store.cend());
	return old_sz != size();
    }
    template<class... Rest>
    Iterator iter(Tuple& tgt, const Rest&... rest) const {
	Iterator it(tgt, rest...);
	it.migrate(*this);
	return it;
    }
    bool empty() const {
	return store.empty();
    }
    bool contains(const T& val) const {
	return store.count(val) > 0;
    }
    unsigned int size() const {
	return store.size();
    }
    friend int compare(const Table& lhs, const Table& rhs) {
	return compare(lhs.store, rhs.store);
    }
    bool operator<(const Table& rhs) const {
	return compare(*this, rhs) < 0;
    }
public:

    class Iterator {
    private:
	typename std::set<T>::const_iterator curr;
	typename std::set<T>::const_iterator end;
	T& tgt_fld;
	const boost::optional<T> cnstr;
	bool before_start = true;
    public:
	explicit Iterator(Tuple& tgt) : Iterator(tgt, boost::none) {}
	explicit Iterator(Tuple& tgt, const boost::optional<T>& cnstr)
	    : tgt_fld(tgt.hd), cnstr(cnstr) {}
	void migrate(const Table& table) {
	    if (cnstr) {
		std::tie(curr, end) = table.store.equal_range(*cnstr);
	    } else {
		curr = table.store.cbegin();
		end = table.store.cend();
	    }
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
	    tgt_fld = *curr;
	    return true;
	}
    };
};

template<class Tag, class K, class S> class Index {
    template<class A, class B, class C> friend class Index;
public:
    typedef K Key;
    typedef S Sub;
    class Iterator;
    friend Iterator;
    typedef typename std::map<Key,Sub>::const_iterator TopIter;
    typedef NamedTuple<Tag,Key,typename Sub::Tuple> Tuple;
private:
    static const Sub dummy;
private:
    std::map<Key,Sub> map;
public:
    Sub& follow(const Key& key) {
	return map[key];
    }
    const Sub& operator[](const Key& key) const {
	// TODO: Should just create new entries?
	try {
	    return map.at(key);
	} catch (const std::out_of_range& exc) {
	    return dummy;
	}
    }
    template<class... Rest>
    bool insert(const Key& key, const Rest&... rest) {
	return follow(key).insert(rest...);
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd, tuple.tl);
    }
    template<class C>
    bool copy(const Index<Tag,K,C>& src) {
	bool grew = false;
	for (const auto& p : src.map) {
	    if (follow(p.first).copy(p.second)) {
		grew = true;
	    }
	}
	return grew;
    }
    template<class C, class... Rest>
    bool copy(const C& src, const Key& key, const Rest&... rest) {
	return follow(key).copy(src, rest...);
    }
    template<class... Rest>
    Iterator iter(Tuple& tgt, const Rest&... rest) const {
	Iterator it(tgt, rest...);
	it.migrate(*this);
	return it;
    }
    TopIter begin() const {
	return map.cbegin();
    }
    TopIter end() const {
	return map.cend();
    }
    bool empty() const {
	for (const auto& entry : map) {
	    if (!entry.second.empty()) {
		return false;
	    }
	}
	return true;
    }
    bool contains(const Key& key) const {
	return map.count(key) > 0;
    }
    template<class... Rest>
    bool contains(const Key& key, const Rest&... rest) const {
	const Sub& sub = (*this)[key];
	return &sub != &dummy && sub.contains(rest...);
    }
    unsigned int size() const {
	unsigned int sz = 0;
	for (const auto& entry : map) {
	    sz += entry.second.size();
	}
	return sz;
    }
    friend int compare(const Index& lhs, const Index& rhs) {
	return compare(lhs.map, rhs.map);
    }
    bool operator<(const Index& rhs) const {
	return compare(*this, rhs) < 0;
    }
public:

    class Iterator {
    private:
	typename std::map<Key,Sub>::const_iterator map_curr;
	typename std::map<Key,Sub>::const_iterator map_end;
	Key& tgt_key;
	const boost::optional<Key> cnstr;
	typename Sub::Iterator sub_iter;
	bool before_start = true;
    public:
	explicit Iterator(Tuple& tgt) : Iterator(tgt, boost::none) {}
	template<class... Rest>
	explicit Iterator(Tuple& tgt, const boost::optional<Key>& cnstr,
			  const Rest&... rest)
	    : tgt_key(tgt.hd), cnstr(cnstr), sub_iter(tgt.tl, rest...) {}
	void migrate(const Index& idx) {
	    if (cnstr) {
		std::tie(map_curr, map_end) = idx.map.equal_range(*cnstr);
	    } else {
		map_curr = idx.map.cbegin();
		map_end = idx.map.cend();
	    }
	    before_start = true;
	}
	bool next() {
	    if (before_start) {
		before_start = false;
		if (map_curr == map_end) {
		    return false;
		}
		sub_iter.migrate(map_curr->second);
	    }
	    while (!sub_iter.next()) {
		++map_curr;
		if (map_curr == map_end) {
		    return false;
		}
		sub_iter.migrate(map_curr->second);
	    }
	    tgt_key = map_curr->first;
	    return true;
	}
    };
};

template<class Tag, class K, class S>
const S Index<Tag,K,S>::dummy;

// SPECIALIZED CONTAINERS =====================================================

template<class Tag, class K, class S> class FlatIndex {
    template<class A, class B, class C> friend class FlatIndex;
public:
    typedef K Key;
    typedef S Sub;
    class Iterator;
    friend Iterator;
    class TopIter;
    friend TopIter;
    typedef NamedTuple<Tag,Key,typename Sub::Tuple> Tuple;
private:
    std::vector<Sub> array;
public:
    template<class... Rest>
    explicit FlatIndex(const typename KeyTraits<Key>::SizeHint& hint,
		       const Rest&... rest)
	: array(KeyTraits<Key>::extract_size(hint), Sub(rest...)) {}
    Sub& follow(const Key& key) {
	return const_cast<Sub&>((*this)[key]);
    }
    const Sub& operator[](const Key& key) const {
	unsigned int i = KeyTraits<Key>::extract_idx(key);
#ifdef NDEBUG
	return array[i];
#else
	return array.at(i);
#endif
    }
    template<class... Rest>
    bool insert(const Key& key, const Rest&... rest) {
	return follow(key).insert(rest...);
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd, tuple.tl);
    }
    template<class C>
    bool copy(const FlatIndex<Tag,K,C>& src) {
	unsigned int lim = src.array.size();
	assert(array.size() == lim);
	bool grew = false;
	for (unsigned int i = 0; i < lim; i++) {
	    if (array[i].copy(src.array[i])) {
		grew = true;
	    }
	}
	return grew;
    }
    template<class C, class... Rest>
    bool copy(const C& src, const Key& key, const Rest&... rest) {
	return follow(key).copy(src, rest...);
    }
    template<class... Rest>
    Iterator iter(Tuple& tgt, const Rest&... rest) const {
	Iterator it(tgt, rest...);
	it.migrate(*this);
	return it;
    }
    TopIter begin() const {
	return TopIter(*this, false);
    }
    TopIter end() const {
	return TopIter(*this, true);
    }
    bool empty() const {
	for (const Sub& entry : array) {
	    if (!entry.empty()) {
		return false;
	    }
	}
	return true;
    }
    bool contains(const Key& key) const {
	unsigned int i = KeyTraits<Key>::extract_idx(key);
#ifdef NDEBUG
	return array[i].empty();
#else
	return array.at(i).empty();
#endif
    }
    template<class... Rest>
    bool contains(const Key& key, const Rest&... rest) const {
	return (*this)[key].contains(rest...);
    }
    unsigned int size() const {
	unsigned int sz = 0;
	for (const Sub& entry : array) {
	    sz += entry.size();
	}
	return sz;
    }
    friend int compare(const FlatIndex& lhs, const FlatIndex& rhs) {
	unsigned int lim = rhs.array.size();
	assert(lhs.array.size() == lim);
	for (unsigned int i = 0; i < lim; i++) {
	    int sub_rel = compare(lhs.array[i], rhs.array[i]);
	    if (sub_rel != 0) {
		return sub_rel;
	    }
	}
	return 0;
    }
    bool operator<(const FlatIndex& rhs) const {
	return compare(*this, rhs) < 0;
    }
public:

    class Iterator {
    private:
	unsigned int arr_idx;
	typename std::vector<Sub>::const_iterator arr_curr;
	typename std::vector<Sub>::const_iterator arr_end;
	Key& tgt_key;
	const boost::optional<unsigned int> cnstr;
	typename Sub::Iterator sub_iter;
	bool before_start = true;
    public:
	explicit Iterator(Tuple& tgt) : Iterator(tgt, boost::none) {}
	template<class... Rest>
	explicit Iterator(Tuple& tgt, const boost::optional<Key>& cnstr,
			  const Rest&... rest)
	    : tgt_key(tgt.hd),
	      cnstr(cnstr
		    ? boost::make_optional(KeyTraits<Key>::extract_idx(*cnstr))
		    : boost::optional<unsigned int>()),
	      sub_iter(tgt.tl, rest...) {}
	void migrate(const FlatIndex& idx) {
	    if (cnstr) {
		arr_idx = *cnstr;
		assert(arr_idx < idx.array.size());
		arr_curr = idx.array.cbegin() + arr_idx;
		arr_end = arr_curr + 1;
	    } else {
		arr_idx = 0;
		arr_curr = idx.array.cbegin();
		arr_end = idx.array.cend();
	    }
	    before_start = true;
	}
	bool next() {
	    if (before_start) {
		before_start = false;
		if (arr_curr == arr_end) {
		    return false;
		}
		sub_iter.migrate(*arr_curr);
	    }
	    while (!sub_iter.next()) {
		++arr_idx;
		++arr_curr;
		if (arr_curr == arr_end) {
		    return false;
		}
		sub_iter.migrate(*arr_curr);
	    }
	    tgt_key = KeyTraits<Key>::from_idx(arr_idx);
	    return true;
	}
    };

    class TopIter : public std::iterator<std::forward_iterator_tag,
					 std::pair<const Key,const Sub&>> {
    public:
	typedef std::pair<const Key,const Sub&> Value;
    private:
	unsigned int curr;
	const FlatIndex& parent;
    private:
	void skip_empty() {
	    while (curr < parent.array.size() && parent.array[curr].empty()) {
		++curr;
	    }
	}
    public:
	explicit TopIter(const FlatIndex& parent, bool at_end)
	    : curr(at_end ? parent.array.size() : 0), parent(parent) {
	    skip_empty();
	}
	TopIter(const TopIter& rhs) : curr(rhs.curr), parent(rhs.parent) {}
	TopIter& operator=(const TopIter& rhs) {
	    assert(&parent == &(rhs.parent));
	    curr = rhs.curr;
	    return *this;
	}
	Value operator*() const {
	    return Value(KeyTraits<Key>::from_idx(curr), parent.array[curr]);
	}
	TopIter& operator++() {
	    ++curr;
	    skip_empty();
	    return *this;
	}
	bool operator==(const TopIter& rhs) const {
	    assert(&parent == &(rhs.parent));
	    return curr == rhs.curr;
	}
	bool operator!=(const TopIter& rhs) const {
	    return !(*this == rhs);
	}
    };
};

template<class Tag, class T> class BitSet {
private:
    typedef unsigned short Store;
    static const Store top_bit = 1 << (sizeof(Store) * 8 - 1);
public:
    class Iterator;
    friend Iterator;
    typedef NamedTuple<Tag,T,Nil> Tuple;
private:
    Store bits = 0;
public:
    explicit BitSet(const typename KeyTraits<T>::SizeHint& hint) {
	EXPECT(sizeof(Store) * 8 >= KeyTraits<T>::extract_size(hint));
    }
    bool insert(const T& val) {
	unsigned int idx = KeyTraits<T>::extract_idx(val);
	assert(idx < sizeof(Store) * 8);
	Store prev_bits = bits;
	bits |= (top_bit >> idx);
	return prev_bits != bits;
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd);
    }
    bool copy(const BitSet& src) {
	Store prev_bits = bits;
	bits |= src.bits;
	return prev_bits != bits;
    }
    template<class... Rest>
    Iterator iter(Tuple& tgt, const Rest&... rest) const {
	Iterator it(tgt, rest...);
	it.migrate(*this);
	return it;
    }
    bool empty() const {
	return bits == 0;
    }
    bool contains(const T& val) const {
	unsigned int idx = KeyTraits<T>::extract_idx(val);
	return bits & (top_bit >> idx);
    }
    unsigned int size() const {
	Store v = bits;
	unsigned int count = 0;
	for (; v; v >>= 1) {
	    count += v & 1;
	}
	return count;
    }
    friend int compare(const BitSet& lhs, const BitSet& rhs) {
	return compare(lhs.bits, rhs.bits);
    }
    bool operator<(const BitSet& rhs) const {
	return bits < rhs.bits;
    }
public:

    class Iterator {
    private:
	unsigned int curr;
	const unsigned int lim;
	const bool constrained;
	T& tgt_fld;
	const typename BitSet::Store* bits;
	bool before_start = true;
    public:
	explicit Iterator(Tuple& tgt) : Iterator(tgt, boost::none) {}
	explicit Iterator(Tuple& tgt, const boost::optional<T>& cnstr)
	    : lim(cnstr ? KeyTraits<T>::extract_idx(*cnstr) + 1
		  : sizeof(typename BitSet::Store) * 8),
	      constrained(cnstr), tgt_fld(tgt.hd) {}
	void migrate(const BitSet& set) {
	    curr = constrained ? lim - 1 : 0;
	    bits = &(set.bits);
	    before_start = true;
	}
	bool next() {
	    if (before_start) {
		before_start = false;
	    } else {
		++curr;
	    }
	    while (curr < lim) {
		if (*bits & (top_bit >> curr)) {
		    tgt_fld = KeyTraits<T>::from_idx(curr);
		    return true;
		}
		++curr;
	    }
	    return false;
	}
    };
};

template<class Tag, class K, class S> class LightIndex {
    template<class A, class B, class C> friend class LightIndex;
    typedef std::forward_list<std::pair<const K,S>> List;
public:
    typedef K Key;
    typedef S Sub;
    class Iterator;
    friend Iterator;
    typedef typename List::const_iterator TopIter;
    typedef NamedTuple<Tag,Key,typename Sub::Tuple> Tuple;
private:
    static const Sub dummy;
private:
    List list;
private:
    bool find(const Key& key, typename List::const_iterator& pos) const {
	pos = list.cbegin();
	typename List::const_iterator prev = list.cbefore_begin();
	while (pos != list.cend() && pos->first < key) {
	    prev = pos;
	    ++pos;
	}
	if (pos != list.cend() && pos->first == key) {
	    return true;
	}
	pos = prev;
	return false;
    }
public:
    Sub& follow(const Key& key) {
	typename List::const_iterator cpos;
	typename List::iterator pos =
	    find(key, cpos)
	    // HACK: Convert a List::const_iterator to a List::iterator.
	    ? list.insert_after(cpos, list.cend(), list.cend())
	    : list.emplace_after(cpos, key, Sub());
	return pos->second;
    }
    const Sub& operator[](const Key& key) const {
	typename List::const_iterator pos;
	if (!find(key, pos)) {
	    return dummy;
	}
	return pos->second;
    }
    template<class... Rest>
    bool insert(const Key& key, const Rest&... rest) {
	return follow(key).insert(rest...);
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd, tuple.tl);
    }
    template<class C>
    bool copy(const LightIndex<Tag,K,C>& src) {
	bool grew = false;
	for (const auto& p : src.list) {
	    if (follow(p.first).copy(p.second)) {
		grew = true;
	    }
	}
	return grew;
    }
    template<class C, class... Rest>
    bool copy(const C& src, const Key& key, const Rest&... rest) {
	return follow(key).copy(src, rest...);
    }
    template<class... Rest>
    Iterator iter(Tuple& tgt, const Rest&... rest) const {
	Iterator it(tgt, rest...);
	it.migrate(*this);
	return it;
    }
    TopIter begin() const {
	return list.cbegin();
    }
    TopIter end() const {
	return list.cend();
    }
    bool empty() const {
	for (const auto& entry : list) {
	    if (!entry.second.empty()) {
		return false;
	    }
	}
	return true;
    }
    bool contains(const Key& key) const {
	typename List::const_iterator dummy;
	return find(key, dummy);
    }
    template<class... Rest>
    bool contains(const Key& key, const Rest&... rest) const {
	const Sub& sub = (*this)[key];
	return &sub != &dummy && sub.contains(rest...);
    }
    unsigned int size() const {
	unsigned int sz = 0;
	for (const auto& entry : list) {
	    sz += entry.second.size();
	}
	return sz;
    }
    friend int compare(const LightIndex& lhs, const LightIndex& rhs) {
	return map_compare(lhs.list, rhs.list);
    }
    bool operator<(const LightIndex& rhs) const {
	return compare(*this, rhs) < 0;
    }
public:

    class Iterator {
    private:
	typename List::const_iterator list_curr;
	typename List::const_iterator list_end;
	Key& tgt_key;
	const boost::optional<Key> cnstr;
	typename Sub::Iterator sub_iter;
	bool before_start = true;
    public:
	explicit Iterator(Tuple& tgt) : Iterator(tgt, boost::none) {}
	template<class... Rest>
	explicit Iterator(Tuple& tgt, const boost::optional<Key>& cnstr,
			  const Rest&... rest)
	    : tgt_key(tgt.hd), cnstr(cnstr), sub_iter(tgt.tl, rest...) {}
	void migrate(const LightIndex& idx) {
	    if (cnstr) {
		bool found = idx.find(*cnstr, list_curr);
		list_end = list_curr;
		if (found) {
		    ++list_end;
		}
	    } else {
		list_curr = idx.list.cbegin();
		list_end = idx.list.cend();
	    }
	    before_start = true;
	}
	bool next() {
	    if (before_start) {
		before_start = false;
		if (list_curr == list_end) {
		    return false;
		}
		sub_iter.migrate(list_curr->second);
	    }
	    while (!sub_iter.next()) {
		++list_curr;
		if (list_curr == list_end) {
		    return false;
		}
		sub_iter.migrate(list_curr->second);
	    }
	    tgt_key = list_curr->first;
	    return true;
	}
    };
};

template<class Tag, class K, class S>
const S LightIndex<Tag,K,S>::dummy;

template<class TagA, class TagB, class T> class BiRel;

template<class TagA, class TagB, class T>
BiRel<TagA,TagB,T> join(const BiRel<TagA,TagB,T>& l,
			const BiRel<TagA,TagB,T>& r) {
    BiRel<TagA,TagB,T> res;
    auto zip = [&](const std::set<T>& as, const std::set<T>& bs) {
	for (const T& a : as) {
	    for (const T& b : bs) {
		res.insert(a, b);
	    }
	}
    };
    join_zip(l.b2a, r.a2b, zip);
    return res;
}

// TODO: Not offering visibility into the nested container.
// Missing operations:
// - merging into nested table
// - operator[]
// - key iteration
// - key contains
// Could fix this by storing a2b and b2a as Index<Table>.
// (this is a special case of a multi-index)
template<class TagA, class TagB, class T> class BiRel {
    friend BiRel join<>(const BiRel& l, const BiRel& r);
public:
    typedef T Key;
    typedef boost::none_t Sub; // dummy declaration
    class Iterator;
    friend Iterator;
    typedef boost::none_t TopIter; // dummy declaration
    typedef NamedTuple<TagA,T,NamedTuple<TagB,T,Nil>> Tuple;
private:
    std::map<T,std::set<T>> a2b;
    std::map<T,std::set<T>> b2a;
public:
    bool insert(const T& a, const T& b) {
	if (a2b[a].insert(b).second) {
	    bool added = b2a[b].insert(a).second;
	    assert(added);
	    return true;
	}
	return false;
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd, tuple.tl.hd);
    }
    bool copy(const BiRel& src) {
	bool grew = false;
	for (const auto& p : src.a2b) {
	    const T& a = p.first;
	    for (const T& b : p.second) {
		if (insert(a, b)) {
		    grew = true;
		}
	    }
	}
	return grew;
    }
    template<class... Rest>
    Iterator iter(Tuple& tgt, const Rest&... rest) const {
	Iterator it(tgt, rest...);
	it.migrate(*this);
	return it;
    }
    bool empty() const {
	for (const auto& p : a2b) {
	    if (!p.second.empty()) {
		return false;
	    }
	}
	return true;
    }
    unsigned int size() const {
	unsigned int sz = 0;
	for (const auto& p : a2b) {
	    sz += p.second.size();
	}
	return sz;
    }
    friend int compare(const BiRel& lhs, const BiRel& rhs) {
	return compare(lhs.a2b, rhs.a2b);
    }
    bool operator<(const BiRel& rhs) const {
	return compare(*this, rhs) < 0;
    }
public:

    // TODO: Could use the b2a container instead, if b_cnstr && !a_cnstr
    class Iterator {
    private:
	typename std::map<T,std::set<T>>::const_iterator map_curr;
	typename std::map<T,std::set<T>>::const_iterator map_end;
	typename std::set<T>::const_iterator set_curr;
	typename std::set<T>::const_iterator set_end;
	T& a_tgt;
	T& b_tgt;
	const boost::optional<T> a_cnstr;
	const boost::optional<T> b_cnstr;
	bool before_start = true;
    private:
	bool set_sub_iter() {
	    if (map_curr == map_end) {
		return false;
	    }
	    a_tgt = map_curr->first;
	    if (b_cnstr) {
		std::tie(set_curr, set_end) =
		    map_curr->second.equal_range(*b_cnstr);
	    } else {
		set_curr = map_curr->second.cbegin();
		set_end  = map_curr->second.cend();
	    }
	    return true;
	}
    public:
	explicit Iterator(Tuple& tgt,
			  const boost::optional<T>& a_cnstr = boost::none,
			  const boost::optional<T>& b_cnstr = boost::none)
	    : a_tgt(tgt.hd), b_tgt(tgt.tl.hd),
	      a_cnstr(a_cnstr), b_cnstr(b_cnstr) {}
	void migrate(const BiRel& birel) {
	    if (a_cnstr) {
		std::tie(map_curr, map_end) = birel.a2b.equal_range(*a_cnstr);
	    } else {
		map_curr = birel.a2b.cbegin();
		map_end  = birel.a2b.cend();
	    }
	    before_start = true;
	}
	bool next() {
	    if (before_start) {
		before_start = false;
		if (!set_sub_iter()) {
		    return false;
		}
	    }
	    while (set_curr == set_end) {
		++map_curr;
		if (!set_sub_iter()) {
		    return false;
		}
	    }
	    b_tgt = *set_curr;
	    ++set_curr;
	    return true;
	}
    };
};

// UNIQUING INFRASTRUCTURE ====================================================

template<class Idx> class Immut;
template<class Idx> class Uniq;

template<class Idx>
Uniq<Idx> join(const Uniq<Idx>& l, const Uniq<Idx>& r) {
    static std::map<std::pair<Ref<Immut<Idx>>,Ref<Immut<Idx>>>,
		    Ref<Immut<Idx>>> cache;
    Ref<Immut<Idx>>& cached = cache[std::make_pair(l.cell_ref, r.cell_ref)];
    if (!cached.valid()) {
	Idx idx = join(l.real_idx(), r.real_idx());
	cached = Immut<Idx>::unique(idx);
    }
    return Uniq<Idx>(cached);
}

template<class Idx> class Immut {
    friend Registry<Immut>;
    typedef Idx Key;
private:
    // Can't simply declare this as a static field, because of initialization
    // order issues.
    static Registry<Immut>& store() {
	static Registry<Immut> store;
	return store;
    }
public:
    static Ref<Immut> unique(const Idx& idx) {
	return store().add(idx).ref;
    }
    static Immut& cell_for(Ref<Immut> ref) {
	return store()[ref];
    }
    static Ref<Immut> zero() {
	static Ref<Immut> cached;
	if (!cached.valid()) {
	    cached = unique(Idx());
	}
	return cached;
    }
private:
    std::map<typename Idx::Tuple,Ref<Immut>> insert_cache;
    std::map<Ref<Immut>,Ref<Immut>> copy_cache;
public:
    const Idx backer;
    const Ref<Immut> ref;
private:
    explicit Immut(const Idx& backer, Ref<Immut> ref)
	: backer(backer), ref(ref) {}
    bool merge() const {
	return false;
    }
public:
    Ref<Immut> insert(const typename Idx::Tuple& tuple) {
	Ref<Immut>& cached = insert_cache[tuple];
	if (!cached.valid()) {
	    Idx new_idx = backer;
	    new_idx.insert(tuple);
	    cached = unique(new_idx);
	}
	return cached;
    }
    Ref<Immut> copy(Ref<Immut> src) {
	Ref<Immut>& cached = copy_cache[src];
	if (!cached.valid()) {
	    cached = copy(cell_for(src).backer);
	}
	return cached;
    }
    Ref<Immut> copy(const Idx& src) {
	Idx new_idx = backer;
	new_idx.copy(src);
	return unique(new_idx);
    }
};

template<class Idx> class Uniq {
    friend Uniq join<>(const Uniq& r, const Uniq& s);
public:
    typedef typename Idx::Key Key;
    typedef typename Idx::Sub Sub;
    class Iterator;
    friend Iterator;
    typedef typename Idx::TopIter TopIter;
    typedef typename Idx::Tuple Tuple;
private:
    Ref<Immut<Idx>> cell_ref;
private:
    Immut<Idx>& immut_cell() {
	return Immut<Idx>::cell_for(cell_ref);
    }
    const Idx& real_idx() const {
	return Immut<Idx>::cell_for(cell_ref).backer;
    }
    explicit Uniq(Ref<Immut<Idx>> cell_ref) : cell_ref(cell_ref) {}
public:
    explicit Uniq() : Uniq(Immut<Idx>::zero()) {}
    const Sub& operator[](const Key& key) const {
	return real_idx()[key];
    }
    template<class... Rest>
    bool insert(const Key& key, const Rest&... rest) {
	return insert(Tuple(key, rest...));
    }
    bool insert(const Tuple& tuple) {
	Ref<Immut<Idx>> old_cell = cell_ref;
	cell_ref = immut_cell().insert(tuple);
	return old_cell != cell_ref;
    }
    bool copy(const Uniq& src) {
	Ref<Immut<Idx>> old_cell = cell_ref;
	cell_ref = immut_cell().copy(src.cell_ref);
	return old_cell != cell_ref;
    }
    bool copy(const Idx& src) {
	Ref<Immut<Idx>> old_cell = cell_ref;
	cell_ref = immut_cell().copy(src);
	return old_cell != cell_ref;
    }
    template<class... Rest>
    Iterator iter(Tuple& tgt, const Rest&... rest) const {
	Iterator it(tgt, rest...);
	it.migrate(*this);
	return it;
    }
    TopIter begin() const {
	return real_idx().begin();
    }
    TopIter end() const {
	return real_idx().end();
    }
    bool empty() const {
	return real_idx().empty();
    }
    template<class... Rest>
    bool contains(const Key& key, const Rest&... rest) const {
	return real_idx().contains(key, rest...);
    }
    unsigned int size() const {
	return real_idx().size();
    }
    friend int compare(const Uniq& lhs, const Uniq& rhs) {
	return compare(lhs.real_idx(), rhs.real_idx());
    }
    bool operator<(const Uniq& rhs) const {
	return real_idx() < rhs.real_idx();
    }
public:

    class Iterator {
    private:
	typename Idx::Iterator real_iter;
    public:
	template<class... Rest>
	explicit Iterator(Tuple& tgt, const Rest&... rest)
	    : real_iter(tgt, rest...) {}
	void migrate(const Uniq& uniqd) {
	    real_iter.migrate(uniqd.real_idx());
	}
	bool next() {
	    return real_iter.next();
	}
    };
};

} // namespace mi

#endif
