#ifndef UTIL_HPP
#define UTIL_HPP

#include <boost/filesystem.hpp>
#include <boost/none.hpp>
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

// CUSTOM PRINTING ============================================================

template<class T>
typename std::enable_if<std::is_arithmetic<T>::value ||
			std::is_enum<T>::value, void>::type
print(std::ostream& os, T val) {
    os << val;
}

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

// HASHING INFRASTRUCTURE =====================================================

namespace std {

// Any class declaring a member function 'std::size_t hash_code() const'
// automatically gets a custom overload of the default hasher.
// TODO: Will this work correctly with class hierarchies?
template<class T> struct hash {
    size_t operator()(const T& x) const {
	return x.hash_code();
    }
};

} // namespace std

namespace detail {

std::size_t hash_impl(std::size_t seed) {
    return seed;
}

template<class T, class... Rest>
std::size_t hash_impl(std::size_t seed, const T& v, const Rest&... rest) {
    seed ^= std::hash<T>()(v) + 0x9e3779b9 + (seed<<6) + (seed>>2);
    return hash_impl(seed, rest...);
}

} // namespace detail

template<class T, class... Rest>
std::size_t hash(const T& v, const Rest&... rest) {
    return detail::hash_impl(std::hash<T>()(v), rest...);
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
	auto bounds = freqs.equal_range(val);
	if (bounds.first == bounds.second) {
	    freqs.insert(bounds.first, std::make_pair(val, 1));
	} else {
	    bounds.first->second++;
	}
    }
    friend std::ostream& operator<<(std::ostream& os, const Histogram& ref) {
	for (const auto& p : ref.freqs) {
	    os << p.first << "\t" << p.second << std::endl;
	}
	return os;
    }
};

namespace mi {
template<class Tag, class T> class Table;
template<class Tag, class K, class S> class Index;
}

// CAUTION: Contents are destroyed lazily!
template<class T, unsigned int LIMIT> class FuzzyStack {
    // TODO: These should be with K == FuzzyStack<T,LIMIT>, but partial
    // specialization is not allowed on friend class declarations.
    template<class Tag, class K> friend class mi::Table;
    template<class Tag, class K, class S> friend class mi::Index;
private:
    T contents_[LIMIT];
    unsigned char size_ = 0;
    bool exact_ = true;
public:
    explicit FuzzyStack() {}
    FuzzyStack(const FuzzyStack& rhs) : size_(rhs.size_), exact_(rhs.exact_) {
	std::copy_backward(rhs.contents_ + LIMIT - size_,
			   rhs.contents_ + LIMIT,
			   contents_ + LIMIT);
    }
    FuzzyStack& operator=(const FuzzyStack& rhs) {
	size_  = rhs.size_;
	exact_ = rhs.exact_;
	std::copy_backward(rhs.contents_ + LIMIT - size_,
			   rhs.contents_ + LIMIT,
			   contents_ + LIMIT);
	return *this;
    }
    bool empty() const {
	return size_ == 0;
    }
    unsigned int size() const {
	return size_;
    }
    bool exact() const {
	return exact_;
    }
    const T& top() const {
	assert(!empty());
	return contents_[LIMIT - size_];
    }
    void push(const T& val) {
	if (size_ == LIMIT) {
	    pop_bottom();
	    exact_ = false;
	}
	size_++;
	contents_[LIMIT - size_] = val;
    }
    void pop() {
	assert(!empty());
	size_--;
    }
    void append(const FuzzyStack& rhs) {
	if (!rhs.exact_) {
	    *this = rhs;
	    return;
	}
	if (size_ + rhs.size_ > LIMIT) {
	    pop_bottom(size_ + rhs.size_ - LIMIT);
	    exact_ = false;
	}
	std::copy_backward(rhs.contents_ + LIMIT - rhs.size_,
			   rhs.contents_ + LIMIT,
			   contents_ + LIMIT - size_);
	size_ += rhs.size_;
    }
    void clear() {
	size_ = 0;
	exact_ = true;
    }
    const T* begin() const {
	return contents_ + LIMIT - size_;
    }
    const T* end() const {
	return contents_ + LIMIT;
    }
    void push_bottom(const T& val) {
	assert(size_ < LIMIT);
	std::move(contents_ + LIMIT - size_,
		  contents_ + LIMIT,
		  contents_ + LIMIT - size_ - 1);
	contents_[LIMIT - 1] = val;
	size_++;
    }
    void pop_bottom(unsigned int num = 1) {
	assert(size_ >= num);
	std::move_backward(contents_ + LIMIT - size_,
			   contents_ + LIMIT - num,
			   contents_ + LIMIT);
	size_ -= num;
    }
    template<class... ArgTs>
    friend void print(std::ostream& os, const FuzzyStack& s, bool reverse,
		      const ArgTs&... args) {
	if (!s.exact_ && reverse) {
	    os << " *";
	}
	std::list<T> stage;
	for (const T& slot : s) {
	    if (reverse) {
		stage.push_front(slot);
	    } else {
		stage.push_back(slot);
	    }
	}
	for (const T& val : stage) {
	    if (reverse) {
		os << " ";
	    }
	    print(os, val, args...);
	    if (!reverse) {
		os << " ";
	    }
	}
	if (!s.exact_ && !reverse) {
	    os << "* ";
	}
    }
    friend std::ostream& operator<<(std::ostream& os, const FuzzyStack& s) {
	for (const T& slot : s) {
	    os << slot << " ";
	}
	if (!s.exact_) {
	    os << "* ";
	}
	return os;
    }
};

namespace detail {

template<unsigned int DEPTH> struct JoinZipHelper {
    template<class LMap, class RMap, class ZipT>
    static void handle(LMap& l, RMap& r, const ZipT& zip) {
	auto l_curr = l.begin();
	auto r_curr = r.begin();
	const auto l_end = l.end();
	const auto r_end = r.end();
	while (l_curr != l_end && r_curr != r_end) {
	    int key_rel = compare(l_curr->first, r_curr->first);
	    if (key_rel == 0) {
		JoinZipHelper<DEPTH-1>::handle(l_curr->second,
					       r_curr->second, zip);
	    }
	    if (key_rel >= 0) {
		++r_curr;
	    }
	    if (key_rel <= 0) {
		++l_curr;
	    }
	}
    }
};

template<> struct JoinZipHelper<0> {
    template<class LMap, class RMap, class ZipT>
    static void handle(LMap& l, RMap& r, const ZipT& zip) {
	zip(l, r);
    }
};

} // namespace detail

// TODO:
// - Also feed the common key to zip?
// - Should implement using iterators?
// - Only works for sorted containers
template<unsigned int DEPTH, class LMap, class RMap, class ZipT>
void join_zip(LMap& l, RMap& r, const ZipT& zip) {
    detail::JoinZipHelper<DEPTH>::handle(l, r, zip);
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
template<typename T> struct KeyTraits;
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
    friend int compare(const Ref& lhs, const Ref& rhs) {
	return compare(lhs.value, rhs.value);
    }
    bool operator<(const Ref& rhs) const {
	return value < rhs.value;
    }
    bool operator==(const Ref& rhs) const {
	return value == rhs.value;
    }
    bool operator!=(const Ref& rhs) const {
	return !(*this == rhs);
    }
    std::size_t hash_code() const {
	return hash(value);
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
	auto it = map.find(key);
	if (it == map.end()) {
	    return make(key, std::forward<ArgTs>(args)...);
	}
	T& obj = it->second;
	obj.merge(std::forward<ArgTs>(args)...);
	return obj;
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
    Table() {}
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
    explicit Index() {}
    std::pair<const Tuple*,bool> insert(const Tuple& tuple) {
	return idx[tuple.*MemPtr].insert(tuple);
    }
    const Wrapped& operator[](const Key& key) const {
	auto it = idx.find(key);
	return (it == idx.cend()) ? dummy : it->second;
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
    explicit Index() {}
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
    explicit FlatIndex() {}
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
	return (key.value < array.size()) ? *(array[key.value]) : dummy;
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
    PtrTable() {}
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
    explicit MultiIndex() {}
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
// - Regular iterators iterate on the top level only
//   will attempt to retun only non-empty keys
// - Custom iterators fill in a provided record.
//   special "FOR" macro provided to do this automatically
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
// - use probabilistic data structures (as long as they're sound)
// - exploit sharing of bit patterns
// - disable operator[], only allow constraining the top iterator
//   then we get a full tuple (but more overhead when iterating?)
// - "full" observer on index
// - multi-level remove_if
// - compress() operation (clean up empty entries)
//   iterate, or on the current level only?
//   any useful return value?
//   this will invalidate iterators
// - "move" operation (like "copy")
// - remove operation
//   could accept partial tuple, and erase everything that matches
//   alternatively, pattern
// - follow-if-exists operation
//   that short-circuits at the first non-existent value

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

#define FOR(RES, EXPR) \
    if (bool cond__ = true) \
	for (typename std::remove_reference<decltype(EXPR)>::type::Tuple RES; \
	     cond__; cond__ = false) \
	    for (auto it__ = (EXPR).iter(RES); it__.next();)

template<class Tag, class T> class Table {
public:
    class Iterator;
    friend Iterator;
    typedef typename std::set<T>::const_iterator ConstTopIter;
    typedef NamedTuple<Tag,T,Nil> Tuple;
private:
    std::set<T> store;
public:
    explicit Table() {}
    bool insert(const T& val) {
	return store.insert(val).second;
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd);
    }
    void remove(const T& val) {
	store.erase(val);
    }
    void remove(const Tuple& tuple) {
	remove(tuple.hd);
    }
    bool copy(const Table& src) {
	unsigned int old_sz = size();
	// TODO: Is this optimized for sorted source collections?
	store.insert(src.store.cbegin(), src.store.cend());
	return old_sz != size();
    }
    ConstTopIter begin() const {
	return store.cbegin();
    }
    ConstTopIter end() const {
	return store.cend();
    }
    Iterator iter(Tuple& tgt) const {
	Iterator it(tgt);
	it.migrate(*this);
	return it;
    }
    bool empty() const {
	return store.empty();
    }
    bool contains(const T& val) const {
	return store.count(val) > 0;
    }
    bool contains(const Tuple& tuple) const {
	return contains(tuple.hd);
    }
    unsigned int size() const {
	return store.size();
    }
public:

    class Iterator {
    private:
	typename std::set<T>::const_iterator curr;
	typename std::set<T>::const_iterator end;
	T& tgt_fld;
	bool before_start = true;
    public:
	explicit Iterator(Tuple& tgt) : tgt_fld(tgt.hd) {}
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
	    tgt_fld = *curr;
	    return true;
	}
    };
};

template<class Tag, class K, class S> class Index {
public:
    typedef K Key;
    typedef S Sub;
    class Iterator;
    friend Iterator;
    typedef typename std::map<Key,Sub>::iterator TopIter;
    typedef typename std::map<Key,Sub>::const_iterator ConstTopIter;
    typedef NamedTuple<Tag,Key,typename Sub::Tuple> Tuple;
private:
    static const Sub dummy;
private:
    std::map<Key,Sub> map;
public:
    explicit Index() {}
    Sub& of(const Key& key) {
	return map[key];
    }
    const Sub& operator[](const Key& key) const {
	auto it = map.find(key);
	return (it == map.cend()) ? dummy : it->second;
    }
    template<class... Rest>
    bool insert(const Key& key, const Rest&... rest) {
	return of(key).insert(rest...);
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd, tuple.tl);
    }
    template<class... Rest>
    void remove(const Key& key, const Rest&... rest) {
	auto it = map.find(key);
	if (it != map.cend()) {
	    it->second.remove(rest...);
	}
    }
    void remove(const Tuple& tuple) {
	remove(tuple.hd, tuple.tl);
    }
    bool copy(const Index& src) {
	bool grew = false;
	for (const auto& p : src.map) {
	    if (of(p.first).copy(p.second)) {
		grew = true;
	    }
	}
	return grew;
    }
    Iterator iter(Tuple& tgt) const {
	Iterator it(tgt);
	it.migrate(*this);
	return it;
    }
    TopIter begin() {
	return map.begin();
    }
    TopIter end() {
	return map.end();
    }
    ConstTopIter begin() const {
	return map.cbegin();
    }
    ConstTopIter end() const {
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
    template<class... Rest>
    bool contains(const Key& key, const Rest&... rest) const {
	auto it = map.find(key);
	return (it == map.cend()) ? false : it->second.contains(rest...);
    }
    bool contains(const Tuple& tuple) const {
	return contains(tuple.hd, tuple.tl);
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
	explicit Iterator(Tuple& tgt) : tgt_key(tgt.hd), sub_iter(tgt.tl) {}
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

template<class Tag, class T, unsigned int LIMIT>
class Table<Tag,FuzzyStack<T,LIMIT> > {
public:
    typedef FuzzyStack<T,LIMIT> Key;
    class Iterator;
    friend Iterator;
    typedef NamedTuple<Tag,Key,Nil> Tuple;
private:
    bool at_star = false;
    bool at_here = false;
    std::map<T,Table> children;
public:
    explicit Table() {}
    bool insert(const Key& stack) {
	Table* node = this;
	if (node->at_star) {
	    return false;
	}
	for (const T& slot : stack) {
	    node = &(node->children[slot]);
	    if (node->at_star) {
		return false;
	    }
	}
	if (stack.exact()) {
	    if (node->at_here) {
		return false;
	    }
	    node->at_here = true;
	    return true;
	}
	node->at_star = true;
	node->at_here = false;
	node->children.clear();
	return true;
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd);
    }
    void remove(const Key& stack) {
	Table* node = this;
	for (const T& slot : stack) {
	    auto it = node->children.find(slot);
	    if (it == node->children.end()) {
		return;
	    }
	    node = &(it->second);
	}
	node->at_here = false;
	if (!stack.exact()) {
	    node->at_star = false;
	    node->children.clear();
	}
    }
    void remove(const Tuple& tuple) {
	remove(tuple.hd);
    }
    bool copy(const Table& src) {
	if (at_star) {
	    return false;
	}
	if (src.at_star) {
	    at_star = true;
	    at_here = false;
	    children.clear();
	    return true;
	}
	bool grew = false;
	if (!at_here && src.at_here) {
	    at_here = true;
	    grew = true;
	}
	for (const auto& p : src.children) {
	    if (children[p.first].copy(p.second)) {
		grew = true;
	    }
	}
	return grew;
    }
    Iterator iter(Tuple& tgt) const {
	Iterator it(tgt);
	it.migrate(*this);
	return it;
    }
    bool empty() const {
	if (at_star || at_here) {
	    return false;
	}
	for (const auto& p : children) {
	    if (!p.second.empty()) {
		return false;
	    }
	}
	return true;
    }
    bool contains(const Key& stack) const {
	const Table* node = this;
	if (node->at_star) {
	    return true;
	}
	for (const T& slot : stack) {
	    auto it = node->children.find(slot);
	    if (it == node->children.cend()) {
		return false;
	    }
	    node = &(it->second);
	    if (node->at_star) {
		return true;
	    }
	}
	return stack.exact() && node->at_here;
    }
    bool contains(const Tuple& tuple) const {
	return contains(tuple.hd);
    }
    unsigned int size() const {
	unsigned int sz = at_star + at_here;
	for (const auto& p : children) {
	    sz += p.second.size();
	}
	return sz;
    }
public:

    class Iterator {
	struct Config {
	    const Table& tab;
	    typename std::map<T,Table>::const_iterator ch_curr;
	    typename std::map<T,Table>::const_iterator ch_end;
	    Config(const Table& tab) : tab(tab),
				       ch_curr(tab.children.cbegin()),
				       ch_end(tab.children.cend()) {}
	};
	enum class Phase {STAR, HERE, NEXT_CHILD, FIRST_CHILD};
    private:
	std::deque<Config> pos_stack;
	Key& tgt_fld;
	Phase phase;
    public:
	explicit Iterator(Tuple& tgt) : tgt_fld(tgt.hd) {}
	void migrate(const Table& tab) {
	    pos_stack.clear();
	    pos_stack.emplace_front(tab);
	    tgt_fld.clear();
	    phase = Phase::STAR;
	}
	bool next() {
	    while (true) {
		Config& curr_pos = pos_stack.front();
		switch (phase) {
		case Phase::STAR:
		    tgt_fld.exact_ = false;
		    phase = Phase::HERE;
		    if (curr_pos.tab.at_star) {
			return true;
		    }
		    break;
		case Phase::HERE:
		    tgt_fld.exact_ = true;
		    phase = Phase::FIRST_CHILD;
		    if (curr_pos.tab.at_here) {
			return true;
		    }
		    break;
		case Phase::NEXT_CHILD:
		    ++(curr_pos.ch_curr);
		    // fall-through
		case Phase::FIRST_CHILD:
		    if (curr_pos.ch_curr == curr_pos.ch_end) {
			pos_stack.pop_front();
			if (pos_stack.empty()) {
			    return false;
			}
			tgt_fld.pop_bottom();
			phase = Phase::NEXT_CHILD;
		    } else {
			const auto& p = *(curr_pos.ch_curr);
			pos_stack.emplace_front(p.second);
			assert(tgt_fld.exact());
			tgt_fld.push_bottom(p.first);
			phase = Phase::STAR;
		    }
		    break;
		default:
		    assert(false);
		}
	    }
	}
    };
};

template<class Tag, class T, unsigned int LIMIT, class S>
class Index<Tag,FuzzyStack<T,LIMIT>,S> {
public:
    typedef FuzzyStack<T,LIMIT> Key;
    typedef S Sub;
    class Iterator;
    friend Iterator;
    class ConstTopIter;
    friend ConstTopIter;
    typedef NamedTuple<Tag,Key,typename Sub::Tuple> Tuple;
private:
    static const Sub dummy;
private:
    Sub at_star;
    Sub at_here;
    std::map<T,Index> children;
private:
    template<class... Rest>
    void clear(const Rest&... rest) {
	at_star.remove(rest...);
	at_here.remove(rest...);
	clear_children(rest...);
    }
    template<class... Rest>
    void clear_children(const Rest&... rest) {
	for (auto& p : children) {
	    p.second.clear(rest...);
	}
    }
public:
    explicit Index() {}
    template<class... Rest>
    bool insert(const Key& stack, const Rest&... rest) {
	Index* node = this;
	for (const T& slot : stack) {
	    if (node->at_star.contains(rest...)) {
		return false;
	    }
	    node = &(node->children[slot]);
	}
	if (!stack.exact()) {
	    if (node->at_star.insert(rest...)) {
		node->at_here.remove(rest...);
		node->clear_children(rest...);
		return true;
	    }
	    return false;
	}
	if (node->at_star.contains(rest...)) {
	    return false;
	}
	return node->at_here.insert(rest...);
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd, tuple.tl);
    }
    template<class... Rest>
    void remove(const Key& stack, const Rest&... rest) {
	Index* node = this;
	for (const T& slot : stack) {
	    auto it = node->children.find(slot);
	    if (it == node->children.end()) {
		return;
	    }
	    node = &(it->second);
	}
	if (stack.exact()) {
	    at_here.remove(rest...);
	} else {
	    node->clear(rest...);
	}
    }
    void remove(const Tuple& tuple) {
	remove(tuple.hd, tuple.tl);
    }
    bool copy(const Index& src) {
	bool grew = false;
	FOR(tup, src) {
	    if (insert(tup)) {
		grew = true;
	    }
	}
	return grew;
    }
    Iterator iter(Tuple& tgt) const {
	Iterator it(tgt);
	it.migrate(*this);
	return it;
    }
    ConstTopIter begin() const {
	return ConstTopIter(*this);
    }
    ConstTopIter end() const {
	return ConstTopIter();
    }
    bool empty() const {
	if (!at_star.empty() || !at_here.empty()) {
	    return false;
	}
	for (const auto& p : children) {
	    if (!p.second.empty()) {
		return false;
	    }
	}
	return true;
    }
    template<class... Rest>
    bool contains(const Key& stack, const Rest&... rest) const {
	const Index* node = this;
	if (node->at_star.contains(rest...)) {
	    return true;
	}
	for (const T& slot : stack) {
	    auto it = node->children.find(slot);
	    if (it == node->children.cend()) {
		return false;
	    }
	    node = &(it->second);
	    if (node->at_star.contains(rest...)) {
		return true;
	    }
	}
	return stack.exact() && node->at_here.contains(rest...);
    }
    bool contains(const Tuple& tuple) const {
	return contains(tuple.hd, tuple.tl);
    }
    unsigned int size() const {
	unsigned int sz = at_star.size() + at_here.size();
	for (const auto& p : children) {
	    sz += p.second.size();
	}
	return sz;
    }
public:

    class ConstTopIter
	: public std::iterator<std::forward_iterator_tag,
			       std::pair<const Key&,const Sub&>> {
	struct Config {
	    const Index& idx;
	    typename std::map<T,Index>::const_iterator ch_curr;
	    typename std::map<T,Index>::const_iterator ch_end;
	    Config(const Index& idx) : idx(idx),
				       ch_curr(idx.children.cbegin()),
				       ch_end(idx.children.cend()) {}
	};
	enum class Phase {BEFORE_STAR, AFTER_STAR, NEXT_CHILD, AFTER_HERE};
    public:
	typedef std::pair<const Key&,const Sub&> Value;
    private:
	std::deque<Config> pos_stack;
	Key cached_key;
	Phase phase;
    private:
	void move_to_next() {
	    while (true) {
		Config& curr_pos = pos_stack.front();
		switch (phase) {
		case Phase::BEFORE_STAR:
		    cached_key.exact_ = false;
		    phase = Phase::AFTER_STAR;
		    if (!curr_pos.idx.at_star.empty()) {
			return;
		    }
		    break;
		case Phase::AFTER_STAR:
		    cached_key.exact_ = true;
		    phase = Phase::AFTER_HERE;
		    if (!curr_pos.idx.at_here.empty()) {
			return;
		    }
		    break;
		case Phase::NEXT_CHILD:
		    ++(curr_pos.ch_curr);
		    // fall-through
		case Phase::AFTER_HERE:
		    if (curr_pos.ch_curr == curr_pos.ch_end) {
			pos_stack.pop_front();
			if (pos_stack.empty()) {
			    return;
			}
			cached_key.pop_bottom();
			phase = Phase::NEXT_CHILD;
		    } else {
			const auto& p = *(curr_pos.ch_curr);
			pos_stack.emplace_front(p.second);
			assert(cached_key.exact());
			cached_key.push_bottom(p.first);
			phase = Phase::BEFORE_STAR;
		    }
		    break;
		default:
		    assert(false);
		}
	    }
	}
    public:
	explicit ConstTopIter() {}
	explicit ConstTopIter(const Index& idx) : phase(Phase::BEFORE_STAR) {
	    pos_stack.emplace_front(idx);
	    move_to_next();
	}
	ConstTopIter(const ConstTopIter& rhs) : pos_stack(rhs.pos_stack),
						cached_key(rhs.cached_key),
						phase(rhs.phase) {}
	ConstTopIter& operator=(const ConstTopIter& rhs) {
	    pos_stack  = rhs.pos_stack;
	    cached_key = rhs.cached_key;
	    phase      = rhs.phase;
	    return *this;
	}
	Value operator*() const {
	    switch (phase) {
	    case Phase::AFTER_STAR:
		return Value(cached_key, pos_stack.front().idx.at_star);
	    case Phase::AFTER_HERE:
		return Value(cached_key, pos_stack.front().idx.at_here);
	    default:
		assert(false);
	    }
	}
	ConstTopIter& operator++() {
	    move_to_next();
	    return *this;
	}
	bool operator==(const ConstTopIter& rhs) const {
	    // XXX: We only support comparisons with past-the-end iterators.
	    assert(rhs.pos_stack.empty());
	    return pos_stack.empty();
	}
	bool operator!=(const ConstTopIter& rhs) const {
	    return !(*this == rhs);
	}
    };

    class Iterator {
	struct Config {
	    const Index& idx;
	    typename std::map<T,Index>::const_iterator ch_curr;
	    typename std::map<T,Index>::const_iterator ch_end;
	    Config(const Index& idx) : idx(idx),
				       ch_curr(idx.children.cbegin()),
				       ch_end(idx.children.cend()) {}
	};
	enum class Phase {BEFORE_STAR, STAR, HERE, NEXT_CHILD, FIRST_CHILD};
    private:
	std::deque<Config> pos_stack;
	Key& tgt_key;
	typename Sub::Iterator sub_iter;
	Phase phase;
    public:
	explicit Iterator(Tuple& tgt) : tgt_key(tgt.hd), sub_iter(tgt.tl) {}
	void migrate(const Index& idx) {
	    pos_stack.clear();
	    pos_stack.emplace_front(idx);
	    tgt_key.clear();
	    phase = Phase::BEFORE_STAR;
	}
	bool next() {
	    while (true) {
		Config& curr_pos = pos_stack.front();
		switch (phase) {
		case Phase::BEFORE_STAR:
		    tgt_key.exact_ = false;
		    sub_iter.migrate(curr_pos.idx.at_star);
		    phase = Phase::STAR;
		    break;
		case Phase::STAR:
		    if (sub_iter.next()) {
			return true;
		    }
		    tgt_key.exact_ = true;
		    sub_iter.migrate(curr_pos.idx.at_here);
		    phase = Phase::HERE;
		    break;
		case Phase::HERE:
		    if (sub_iter.next()) {
			return true;
		    }
		    phase = Phase::FIRST_CHILD;
		    break;
		case Phase::NEXT_CHILD:
		    ++(curr_pos.ch_curr);
		    // fall-through
		case Phase::FIRST_CHILD:
		    if (curr_pos.ch_curr == curr_pos.ch_end) {
			pos_stack.pop_front();
			if (pos_stack.empty()) {
			    return false;
			}
			tgt_key.pop_bottom();
			phase = Phase::NEXT_CHILD;
		    } else {
			const auto& p = *(curr_pos.ch_curr);
			pos_stack.emplace_front(p.second);
			assert(tgt_key.exact());
			tgt_key.push_bottom(p.first);
			phase = Phase::BEFORE_STAR;
		    }
		    break;
		default:
		    assert(false);
		}
	    }
	}
    };
};

template<class Tag, class T, unsigned int LIMIT, class S>
const S Index<Tag,FuzzyStack<T,LIMIT>,S>::dummy;

template<class Tag, class K, class S> class FlatIndex {
public:
    typedef K Key;
    typedef S Sub;
    class Iterator;
    friend Iterator;
    class TopIter;
    friend TopIter;
    class ConstTopIter;
    friend ConstTopIter;
    typedef NamedTuple<Tag,Key,typename Sub::Tuple> Tuple;
private:
    std::vector<Sub> array;
public:
    template<class... Rest>
    explicit FlatIndex(const typename KeyTraits<Key>::SizeHint& hint,
		       const Rest&... rest)
	: array(KeyTraits<Key>::extract_size(hint), Sub(rest...)) {}
    Sub& of(const Key& key) {
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
	return of(key).insert(rest...);
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd, tuple.tl);
    }
    template<class... Rest>
    void remove(const Key& key, const Rest&... rest) {
	of(key).remove(rest...);
    }
    void remove(const Tuple& tuple) {
	remove(tuple.hd, tuple.tl);
    }
    bool copy(const FlatIndex& src) {
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
    Iterator iter(Tuple& tgt) const {
	Iterator it(tgt);
	it.migrate(*this);
	return it;
    }
    TopIter begin() {
	return TopIter(*this, false);
    }
    TopIter end() {
	return TopIter(*this, true);
    }
    ConstTopIter begin() const {
	return ConstTopIter(*this, false);
    }
    ConstTopIter end() const {
	return ConstTopIter(*this, true);
    }
    bool empty() const {
	for (const Sub& entry : array) {
	    if (!entry.empty()) {
		return false;
	    }
	}
	return true;
    }
    template<class... Rest>
    bool contains(const Key& key, const Rest&... rest) const {
	return (*this)[key].contains(rest...);
    }
    bool contains(const Tuple& tuple) const {
	return contains(tuple.hd, tuple.tl);
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
	explicit Iterator(Tuple& tgt) : tgt_key(tgt.hd), sub_iter(tgt.tl) {}
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
					 std::pair<const Key,Sub&>> {
    public:
	typedef std::pair<const Key,Sub&> Value;
    private:
	ConstTopIter real_iter;
    public:
	explicit TopIter(FlatIndex& parent, bool at_end)
	    : real_iter(parent, at_end) {}
	TopIter(const TopIter& rhs) : real_iter(rhs.real_iter) {}
	TopIter& operator=(const TopIter& rhs) {
	    real_iter = rhs.real_iter;
	    return *this;
	}
	Value operator*() const {
	    typename ConstTopIter::Value cval = *real_iter;
	    return Value(cval.first, const_cast<Sub&>(cval.second));
	}
	TopIter& operator++() {
	    ++real_iter;
	    return *this;
	}
	bool operator==(const TopIter& rhs) const {
	    return real_iter == rhs.real_iter;
	}
	bool operator!=(const TopIter& rhs) const {
	    return !(*this == rhs);
	}
    };

    class ConstTopIter
	: public std::iterator<std::forward_iterator_tag,
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
	explicit ConstTopIter(const FlatIndex& parent, bool at_end)
	    : curr(at_end ? parent.array.size() : 0), parent(parent) {
	    skip_empty();
	}
	ConstTopIter(const ConstTopIter& rhs)
	    : curr(rhs.curr), parent(rhs.parent) {}
	ConstTopIter& operator=(const ConstTopIter& rhs) {
	    assert(&parent == &(rhs.parent));
	    curr = rhs.curr;
	    return *this;
	}
	Value operator*() const {
	    return Value(KeyTraits<Key>::from_idx(curr), parent.array[curr]);
	}
	ConstTopIter& operator++() {
	    ++curr;
	    skip_empty();
	    return *this;
	}
	bool operator==(const ConstTopIter& rhs) const {
	    assert(&parent == &(rhs.parent));
	    return curr == rhs.curr;
	}
	bool operator!=(const ConstTopIter& rhs) const {
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
    void remove(const T& val) {
	unsigned int idx = KeyTraits<T>::extract_idx(val);
	assert(idx < sizeof(Store) * 8);
	bits &= ~(top_bit >> idx);
    }
    void remove(const Tuple& tuple) {
	remove(tuple.hd);
    }
    bool copy(const BitSet& src) {
	Store prev_bits = bits;
	bits |= src.bits;
	return prev_bits != bits;
    }
    Iterator iter(Tuple& tgt) const {
	Iterator it(tgt);
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
    bool contains(const Tuple& tuple) const {
	return contains(tuple.hd);
    }
    unsigned int size() const {
	Store v = bits;
	unsigned int count = 0;
	for (; v; v >>= 1) {
	    count += v & 1;
	}
	return count;
    }
public:

    class Iterator {
    private:
	unsigned int curr;
	const unsigned int lim;
	T& tgt_fld;
	const typename BitSet::Store* bits;
	bool before_start = true;
    public:
	explicit Iterator(Tuple& tgt)
	    : lim(sizeof(typename BitSet::Store) * 8), tgt_fld(tgt.hd) {}
	void migrate(const BitSet& set) {
	    curr = 0;
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
    typedef std::forward_list<std::pair<const K,S>> List;
public:
    typedef K Key;
    typedef S Sub;
    class Iterator;
    friend Iterator;
    typedef typename List::iterator TopIter;
    typedef typename List::const_iterator ConstTopIter;
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
    explicit LightIndex() {}
    Sub& of(const Key& key) {
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
	return find(key, pos) ? pos->second : dummy;
    }
    template<class... Rest>
    bool insert(const Key& key, const Rest&... rest) {
	return of(key).insert(rest...);
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd, tuple.tl);
    }
    template<class... Rest>
    void remove(const Key& key, const Rest&... rest) {
	typename List::const_iterator pos;
	if (find(key, pos)) {
	    pos->second.remove(rest...);
	}
    }
    void remove(const Tuple& tuple) {
	remove(tuple.hd, tuple.tl);
    }
    bool copy(const LightIndex& src) {
	bool grew = false;
	for (const auto& p : src.list) {
	    if (of(p.first).copy(p.second)) {
		grew = true;
	    }
	}
	return grew;
    }
    Iterator iter(Tuple& tgt) const {
	Iterator it(tgt);
	it.migrate(*this);
	return it;
    }
    TopIter begin() {
	return list.begin();
    }
    TopIter end() {
	return list.end();
    }
    ConstTopIter begin() const {
	return list.cbegin();
    }
    ConstTopIter end() const {
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
    template<class... Rest>
    bool contains(const Key& key, const Rest&... rest) const {
	typename List::const_iterator pos;
	return find(key, pos) ? pos->second.contains(rest...) : false;
    }
    bool contains(const Tuple& tuple) const {
	return contains(tuple.hd, tuple.tl);
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
	explicit Iterator(Tuple& tgt) : tgt_key(tgt.hd), sub_iter(tgt.tl) {}
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

} // namespace mi

#endif
