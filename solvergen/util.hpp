#ifndef UTIL_HPP
#define UTIL_HPP

#include <boost/filesystem.hpp>
#include <boost/none.hpp>
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
#include <stack>
#include <string>
#include <sys/time.h>
#include <tuple>
#include <vector>

// TYPE TRAITS ================================================================

template<int I, typename... Types> struct pack_elem {
public:
    typedef typename std::tuple_element<I,std::tuple<Types...> >::type type;
};

template<class T, class S> struct cons {};

template<class H, class... Ts>
struct cons<H,std::tuple<Ts...> > {
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

template<class T, class S>
int set_compare(const T& lhs, const S& rhs) {
    auto l_curr = lhs.begin();
    auto r_curr = rhs.begin();
    const auto l_end = lhs.end();
    const auto r_end = rhs.end();

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
int compare(const std::set<T>& lhs, const std::set<T>& rhs) {
    return set_compare(lhs, rhs);
}

template<class T, class S>
int map_compare(const T& lhs, const S& rhs) {
    auto l_curr = lhs.begin();
    auto r_curr = rhs.begin();
    const auto l_end = lhs.end();
    const auto r_end = rhs.end();

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
const T& follow_ptr(T* const& r) {
    return *r;
}

template<typename PtrT>
const typename std::pointer_traits<PtrT>::element_type&
deref(const PtrT& ptr) {
    return *ptr;
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
		    typename std::iterator_traits<PtrIter>::value_type> >;

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
template<typename T, bool CanReprocess> class Worklist;

// TODO:
// - keep id-to-item mapping
// - use type-safe ids (Refs)
template<typename T> class Worklist<T,false> {
public:
    typedef typename std::map<T,unsigned>::const_iterator Iterator;
private:
    std::map<T,unsigned> reached_;
    std::deque<Iterator> queue_;
public:
    explicit Worklist() {}
    Worklist(const Worklist&) = delete;
    Worklist& operator=(const Worklist&) = delete;
    bool empty() const {
	return queue_.empty();
    }
    const std::map<T,unsigned>& reached() const {
        return reached_;
    }
    unsigned num_reached() const {
        return reached_.size();
    }
    Iterator enqueue(T&& val) {
        auto res = reached_.emplace(std::move(val), reached_.size());
        if (res.second) {
            queue_.push_back(res.first);
        }
        return res.first;
    }
    Iterator dequeue() {
	Iterator ret = queue_.front();
	queue_.pop_front();
	return ret;
    }
};

// TODO: Enqueues and dequeues done by copying (careful with large structs).
template<typename T> class Worklist<T,true> {
private:
    std::set<T> reached_;
    std::queue<T> queue_;
public:
    bool empty() const {
	return queue_.empty();
    }
    unsigned size() const {
        return queue_.size();
    }
    bool enqueue(T val) {
	if (reached_.insert(val).second) {
	    queue_.push(val);
	    return true;
	}
	return false;
    }
    T dequeue() {
	T val = queue_.front();
	queue_.pop();
	reached_.erase(val);
	return val;
    }
};

template<typename T> class Histogram {
private:
    std::map<T,unsigned int> freqs_;
public:
    void record(const T& val) {
	auto bounds = freqs_.equal_range(val);
	if (bounds.first == bounds.second) {
	    freqs_.insert(bounds.first, std::make_pair(val, 1));
	} else {
	    bounds.first->second++;
	}
    }
    typename std::map<T,unsigned int>::const_iterator begin() const {
        return freqs_.begin();
    }
    typename std::map<T,unsigned int>::const_iterator end() const {
        return freqs_.end();
    }
};

namespace detail {

template<unsigned int DEPTH> struct JoinZipHelper {
    template<class LMap, class RMap, class ZipT, class... KeyTs>
    static void handle(const LMap& l, const RMap& r, const ZipT& zip,
                       const KeyTs&... keys) {
	auto l_curr = l.begin();
	auto r_curr = r.begin();
	const auto l_end = l.end();
	const auto r_end = r.end();
	while (l_curr != l_end && r_curr != r_end) {
	    int key_rel = compare(l_curr->first, r_curr->first);
	    if (key_rel == 0) {
		JoinZipHelper<DEPTH-1>::handle(l_curr->second,
					       r_curr->second, zip,
                                               keys..., l_curr->first);
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
    template<class LMap, class RMap, class ZipT, class... KeyTs>
    static void handle(const LMap& l, const RMap& r, const ZipT& zip,
                       const KeyTs&... keys) {
	zip(l, r, keys...);
    }
};

} // namespace detail

// TODO:
// - Should implement using iterators?
// - Only works for sorted containers
template<unsigned int DEPTH, class LMap, class RMap, class ZipT>
void join_zip(const LMap& l, const RMap& r, const ZipT& zip) {
    detail::JoinZipHelper<DEPTH>::handle(l, r, zip);
}

// TODO:
// - Should implement using iterators?
// - Only works for sorted containers
template<class Map, class Set, class Fun, class... Keys>
static void filter(const Map& map, const Set& set, const Fun& fun,
                   const Keys&... keys) {
    auto map_curr = map.begin();
    auto set_curr = set.begin();
    const auto map_end = map.end();
    const auto set_end = set.end();
    while (map_curr != map_end && set_curr != set_end) {
        int key_rel = compare(map_curr->first, *set_curr);
        if (key_rel == 0) {
            fun(map_curr->second, keys..., map_curr->first);
        }
        if (key_rel >= 0) {
            ++set_curr;
        }
        if (key_rel <= 0) {
            ++map_curr;
        }
    }
}

template<class T, class S>
bool empty_intersection(const T& a, const S& b) {
    auto a_it = a.begin();
    auto b_it = b.begin();
    while (a_it != a.end() && b_it != b.end()) {
        if (*a_it == *b_it) {
            return false;
        } else if (*a_it < *b_it) {
            ++a_it;
        } else {
            ++b_it;
        }
    }
    return true;
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

// TODO: Could make this class const-correct, i.e. get a const& when indexing a
// Registry using a const Ref.
template<typename T> class Ref {
private:
    unsigned int value_;
public:
    explicit Ref() : value_(std::numeric_limits<unsigned int>::max()) {}
    explicit Ref(unsigned int value) : value_(value) {
	EXPECT(valid());
    }
    Ref(const Ref&) = default;
    Ref& operator=(const Ref&) = default;
    friend void swap(Ref& a, Ref& b) {
	using std::swap;
	swap(a.value_, b.value_);
    }
    bool valid() const {
	return value_ < std::numeric_limits<unsigned int>::max();
    }
    unsigned int value() const {
	return value_;
    }
    friend int compare(const Ref& lhs, const Ref& rhs) {
	return compare(lhs.value_, rhs.value_);
    }
    bool operator<(const Ref& rhs) const {
	return value_ < rhs.value_;
    }
    bool operator==(const Ref& rhs) const {
	return value_ == rhs.value_;
    }
    bool operator!=(const Ref& rhs) const {
	return !(*this == rhs);
    }
    std::size_t hash_code() const {
	return hash(value_);
    }
    friend std::ostream& operator<<(std::ostream& os, const Ref& ref) {
	EXPECT(ref.valid());
	os << ref.value_;
	return os;
    }
};

template<typename T, typename Key = typename T::Key> class Registry {
public:
    typedef IterWrapper<typename std::vector<T*>::const_iterator, T,
			detail::follow_ptr<T> > Iterator;
private:
    std::vector<T*> array;
    std::map<Key,T> map;
    std::deque<T> temps;
private:
    template<typename... ArgTs>
    T& insert(const Key* key_ptr, ArgTs&&... args) {
	Ref<T> next_ref(array.size());
	T* obj_ptr;
	if (key_ptr != NULL) {
	    auto res = map.emplace(*key_ptr, T(key_ptr, next_ref,
					       std::forward<ArgTs>(args)...));
	    EXPECT(res.second);
	    obj_ptr = &(res.first->second);
	} else {
	    temps.push_back(T(NULL, next_ref, std::forward<ArgTs>(args)...));
	    obj_ptr = &(temps.back());
	}
	array.push_back(obj_ptr);
	return *obj_ptr;
    }
public:
    explicit Registry() {}
    // TODO: Only works if T is copy-constructible.
    Registry(const Registry& rhs)
	: array(rhs.array.size(), NULL), map(rhs.map), temps(rhs.temps) {
	auto update_ptr = [&](T& obj) {
	    T*& cell = array.at(obj.ref.value());
	    EXPECT(cell == NULL);
	    cell = &obj;
	};
	for (auto& p : map) {
	    update_ptr(p.second);
	}
	for (T& obj : temps) {
	    update_ptr(obj);
	}
    }
    Registry(Registry&& rhs) {
	swap(*this, rhs);
    }
    Registry& operator=(const Registry& rhs) = delete;
    // XXX: This is dangerous/non-portable: The 'array' member variable stores
    // pointers to objects in the accompanying 'map' and 'temps', which would
    // normally become invalid if 'map' or 'temps' were moved in the heap.
    // However, the default implementations of swap on std::map and std::deque
    // don't move the actual heap storage, and thus the pointers remain valid.
    friend void swap(Registry& a, Registry& b) {
	using std::swap;
	swap(a.array, b.array);
	swap(a.map,   b.map);
	swap(a.temps, b.temps);
    }
    void clear() {
        Registry temp;
        swap(*this, temp);
    }
    T& operator[](Ref<T> ref) {
	EXPECT(ref.valid());
	return *(array.at(ref.value()));
    }
    const T& operator[](Ref<T> ref) const {
	EXPECT(ref.valid());
	return *(array.at(ref.value()));
    }
    template<typename... ArgTs> T& make(const Key& key, ArgTs&&... args) {
	return insert(&key, std::forward<ArgTs>(args)...);
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
    template<typename... ArgTs> T& mktemp(ArgTs&&... args) {
	return insert(NULL, std::forward<ArgTs>(args)...);
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
	return *(array.front());
    }
    const T& last() const {
	return *(array.back());
    }
    Iterator begin() const {
	return Iterator(array.cbegin());
    }
    Iterator end() const {
	return Iterator(array.cend());
    }
};

template<class K, class V> class RefMap {
private:
    std::vector<V> array_;
public:
    explicit RefMap(const Registry<K>& keys) : array_(keys.size()) {}
    RefMap(const RefMap& rhs) = delete;
    RefMap(RefMap&& rhs) = default;
    RefMap& operator=(const RefMap& rhs) = delete;
    V& operator[](Ref<K> ref) {
	return const_cast<V&>((*const_cast<const RefMap*>(this))[ref]);
    }
    const V& operator[](Ref<K> ref) const {
#ifdef NDEBUG
	return array_[ref.value()];
#else
	return array_.at(ref.value());
#endif
    }
};

// GRAPH ALGORITHMS ===========================================================

// TODO: Should make this a special case of a generic SccGraph<Node>, for
// Node == Ref<T>.
template<class T> class SccGraph {
public:
    typedef unsigned SccId;
    struct SCC {
        std::set<Ref<T> > nodes;
        std::set<SccId> parents;
        std::set<SccId> children;
        unsigned height = 0;
        unsigned cumm_size = 0;
        bool trivial = true;
    };
private:
    struct NodeInfo {
        int index = -1;
        int lowlink = -1;
        bool on_stack = false;
    };
private:
    RefMap<T,SccId> node2scc_;
    std::vector<SCC> comps_;
public:
    // Edges must be provided as a map from Ref<T>'s to sets of Ref<T>'s.
    template<class Edges>
    explicit SccGraph(const Registry<T>& nodes, const Edges& edges)
        : node2scc_(nodes) {
        RefMap<T,NodeInfo> info(nodes);
        int next_index = 0;
        std::stack<Ref<T> > stack;
        // Run Tarjan's algorithm to compute SCC's.
        // TODO: Unnecessary virtual function call.
        std::function<void(Ref<T>)> strong_connect = [&](Ref<T> src) {
            info[src].index = next_index;
            info[src].lowlink = next_index;
            next_index++;
            stack.push(src);
            info[src].on_stack = true;
            for (Ref<T> dst : edges[src]) {
                if (info[dst].index < 0) {
                    strong_connect(dst);
                    info[src].lowlink =
                        std::min(info[src].lowlink, info[dst].lowlink);
                } else if (info[dst].on_stack) {
                    info[src].lowlink =
                        std::min(info[src].lowlink, info[dst].index);
                }
            }
            if (info[src].lowlink == info[src].index) {
                SccId scc_id = comps_.size();
                comps_.emplace_back();
                SCC& scc = comps_.back();
                Ref<T> n;
                do {
                    n = stack.top();
                    stack.pop();
                    info[n].on_stack = false;
                    scc.nodes.insert(n);
                    node2scc_[n] = scc_id;
                } while (n != src);
            }
        };
        for (const T& n : nodes) {
            if (info[n.ref].index < 0) {
                strong_connect(n.ref);
            }
        }
        // Fill out the connections between SCCs.
        for (SccId src_id = 0; src_id < comps_.size(); src_id++) {
            SCC& src_scc = comps_[src_id];
            for (Ref<T> src_n : src_scc.nodes) {
                for (Ref<T> dst_n : edges[src_n]) {
                    SccId dst_id = node2scc_[dst_n];
                    assert(dst_id <= src_id);
                    if (dst_id == src_id) {
                        src_scc.trivial = false;
                        continue;
                    }
                    src_scc.children.insert(dst_id);
                    comps_[dst_id].parents.insert(src_id);
                }
            }
        }
        // Calculate statistics for each SCC.
        // TODO: Could do this externally.
        for (SccId id = 0; id < comps_.size(); id++) {
            SCC& scc = comps_[id];
            scc.cumm_size = scc.nodes.size();
            for (SccId c_id : scc.children) {
                const SCC& child = comps_[c_id];
                scc.height = std::max(child.height + 1, scc.height);
                scc.cumm_size += child.cumm_size;
            }
        }
    }
    SccGraph(const SccGraph& rhs) = delete;
    SccGraph(SccGraph&& rhs) = default;
    SccGraph& operator=(const SccGraph& rhs) = delete;
    SccId scc_of(Ref<T> node) const {
        return node2scc_[node];
    }
    SccId num_sccs() const {
        return comps_.size();
    }
    const SCC& scc(SccId id) const {
#ifdef NDEBUG
	return comps_[id];
#else
	return comps_.at(id);
#endif
    }
    // Returned in reverse topological order.
    const std::vector<SCC>& sccs() const {
        return comps_;
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
// - Primitive multi-dimension support:
//   - primary index tuple order becomes the exported Tuple
//   - 'insert' automatically adds on both dimensions, does required reordering
//   - only for base containers
//   - can't write-through (can't even get non-const references to sub-indices)
//   - can't refer to element directly; must pick sub

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
// - improve multi-index support:
//   - implement for specialized containers
//   - more than 2 index combination (or allow nesting)
//   - detect when secondary dimension doesn't cover all tags
//     or allow missing fields on secondary dimension
//   - more efficient insertion than building a full copy of the input tuple
//     could write a sorting algorithm with templates
//     but need to be able to name the fields, or reorder the argument pack
//   - contains check: can pick the most suitable dimension
//   - implement copy
// - removing: full query infrastructure

namespace mi {

// NAMED TUPLES ===============================================================

template<class Tag, class Hd, class Tl> class NamedTuple;

namespace detail {

template<class FldN, class NT> struct Getter;

template<class Tag, class Hd, class Tl>
struct Getter<Tag, NamedTuple<Tag,Hd,Tl> > {
    typedef Hd FldT;
    static FldT& get(NamedTuple<Tag,Hd,Tl>& ntup) {
	return ntup.hd;
    }
    static const FldT& get(const NamedTuple<Tag,Hd,Tl>& ntup) {
	return ntup.hd;
    }
};

template<class FldN, class Tag, class Hd, class Tl>
struct Getter<FldN, NamedTuple<Tag,Hd,Tl> > {
    typedef typename Getter<FldN,Tl>::FldT FldT;
    static FldT& get(NamedTuple<Tag,Hd,Tl>& ntup) {
	return Getter<FldN,Tl>::get(ntup.tl);
    }
    static const FldT& get(const NamedTuple<Tag,Hd,Tl>& ntup) {
	return Getter<FldN,Tl>::get(ntup.tl);
    }
};

} // namespace detail

struct Nil {
    explicit Nil() {}
    bool operator<(const Nil&) const {
	return false;
    }
    void print(std::ostream&) const {}
};

template<class Tag, class Hd, class Tl>
class NamedTuple {
public:
    typedef Hd Head;
    typedef Tl Tail;
public:
    Hd hd;
    Tl tl;
public:
    explicit NamedTuple() {}
    NamedTuple(const NamedTuple&) = default;
    NamedTuple& operator=(const NamedTuple&) = delete;
    template<class... Rest>
    explicit NamedTuple(const Hd& hd, const Rest&... rest)
	: hd(hd), tl(rest...) {}
    template<class FldN>
    typename detail::Getter<FldN,NamedTuple>::FldT& get() {
	return detail::Getter<FldN,NamedTuple>::get(*this);
    }
    template<class FldN>
    const typename detail::Getter<FldN,NamedTuple>::FldT& get() const {
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
    void print(std::ostream& os) const {
	os << Tag::name() << "=" << hd << " ";
	tl.print(os);
    }
};

template<class NT> struct TupleTraits;

template<>
struct TupleTraits<Nil> {
    static const unsigned int SIZE = 0;
};

template<class Tag, class Hd>
struct TupleTraits<NamedTuple<Tag,Hd,Nil> > {
    typedef Hd Daeh;
    typedef Nil Liat;
    static const unsigned int SIZE = 1;
};

template<class Tag, class Hd, class Tl>
struct TupleTraits<NamedTuple<Tag,Hd,Tl> > {
    typedef typename TupleTraits<Tl>::Daeh Daeh;
    typedef NamedTuple<Tag, Hd, typename TupleTraits<Tl>::Liat> Liat;
    static const unsigned int value = TupleTraits<Tl>::SIZE + 1;
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
struct KeyTraits<Ref<T> > {
    typedef Registry<T> SizeHint;
    static unsigned int extract_size(const SizeHint& reg) {
	return reg.size();
    }
    static unsigned int extract_idx(Ref<T> ref) {
	return ref.value();
    }
    static Ref<T> from_idx(unsigned int idx) {
	return Ref<T>(idx);
    }
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

template<typename T, typename V, int I>
struct TupleInserter {
    static void insert(T& idxs, const V& val) {
	TupleInserter<T,V,I-1>::insert(idxs, val);
	std::get<I-1>(idxs).sec_insert(val);
    }
};

template<typename T, typename V>
struct TupleInserter<T,V,0> {
    static void insert(T&, const V&) {}
};

template<typename T, typename V>
void insert_all(T& idxs, const V& val) {
    TupleInserter<T,V,std::tuple_size<T>::value>::insert(idxs, val);
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
    std::set<T> store_;
public:
    explicit Table() {}
    // TODO: Only works if T is copy-constructible.
    Table(const Table&) = default;
    Table(Table&&) = default;
    Table& operator=(const Table&) = delete;
    friend void swap(Table& a, Table& b) {
	using std::swap;
	swap(a.store_, b.store_);
    }
    void clear() {
        Table temp;
        swap(*this, temp);
    }
    Table& of() {
        return *this;
    }
    const Table& find() const {
        return *this;
    }
    bool insert(const T& val) {
	return store_.insert(val).second;
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd);
    }
    template<class Other>
    void sec_insert(const Other& other) {
	insert(other.template get<Tag>());
    }
    bool copy(const Table& src) {
	unsigned int old_sz = size();
	// TODO: Is this optimized for sorted source collections?
	store_.insert(src.store_.cbegin(), src.store_.cend());
	return old_sz != size();
    }
    ConstTopIter begin() const {
	return store_.cbegin();
    }
    ConstTopIter end() const {
	return store_.cend();
    }
    Iterator iter(Tuple& tgt) const {
	Iterator it(tgt);
	it.migrate(*this);
	return it;
    }
    bool empty() const {
	return store_.empty();
    }
    bool contains(const T& val) const {
	return store_.count(val) > 0;
    }
    bool contains(const Tuple& tuple) const {
	return contains(tuple.hd);
    }
    unsigned int size() const {
	return store_.size();
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
	    curr = table.store_.cbegin();
	    end = table.store_.cend();
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

template<class Tag, class T> class Cell {
public:
    class Iterator;
    friend Iterator;
    typedef NamedTuple<Tag,T,Nil> Tuple;
private:
    boost::optional<T> val_;
public:
    explicit Cell() {}
    Cell(const Cell&) = default;
    Cell(Cell&&) = default;
    Cell& operator=(const Cell&) = delete;
    friend void swap(Cell& a, Cell& b) {
	using std::swap;
	swap(a.val_, b.val_);
    }
    void clear() {
        Cell temp;
        swap(*this, temp);
    }
    Cell& of() {
        return *this;
    }
    const Cell& find() const {
        return *this;
    }
    bool insert(const T& val) {
	if ((bool) val_) {
	    // can't update the value once set
	    return false;
	}
	val_ = val;
	return true;
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd);
    }
    Iterator iter(Tuple& tgt) const {
	Iterator it(tgt);
	it.migrate(*this);
	return it;
    }
    bool empty() const {
	return !((bool) val_);
    }
    bool contains(const T& val) const {
	return val_ == val;
    }
    bool contains(const Tuple& tuple) const {
	return val_ == tuple.hd;
    }
    unsigned int size() const {
	return ((bool) val_) ? 1 : 0;
    }
    const T& get() const {
        EXPECT((bool) val_);
	return val_.get();
    }
    const boost::optional<T>& contents() const {
        return val_;
    }
public:

    class Iterator {
    private:
	const Cell* curr;
	T& tgt_fld;
	bool before_start = true;
    public:
	explicit Iterator(Tuple& tgt) : tgt_fld(tgt.hd) {}
	void migrate(const Cell& cell) {
	    curr = &cell;
	    before_start = true;
	}
	bool next() {
	    if (!before_start) {
		return false;
	    }
	    before_start = false;
	    if ((bool) curr->val_) {
		tgt_fld = curr->val_.get();
		return true;
	    }
	    return false;
	}
    };
};

template<class Tag, class T> class Bag {
public:
    class Iterator;
    friend Iterator;
    typedef typename std::deque<T>::const_iterator ConstTopIter;
    typedef NamedTuple<Tag,T,Nil> Tuple;
private:
    std::deque<T> store_;
public:
    explicit Bag() {}
    // TODO: Only works if T is copy-constructible.
    Bag(const Bag&) = default;
    Bag(Bag&&) = default;
    Bag& operator=(const Bag&) = delete;
    friend void swap(Bag& a, Bag& b) {
	using std::swap;
	swap(a.store_, b.store_);
    }
    void clear() {
        Bag temp;
        swap(*this, temp);
    }
    Bag& of() {
        return *this;
    }
    const Bag& find() const {
        return *this;
    }
    bool insert(const T& val) {
	store_.push_back(val);
        return true;
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd);
    }
    ConstTopIter begin() const {
	return store_.cbegin();
    }
    ConstTopIter end() const {
	return store_.cend();
    }
    Iterator iter(Tuple& tgt) const {
	Iterator it(tgt);
	it.migrate(*this);
	return it;
    }
    bool empty() const {
	return store_.empty();
    }
    bool contains(const T& val) const {
        return std::find(store_.begin(), store_.end(), val) != store_.end();
    }
    bool contains(const Tuple& tuple) const {
	return contains(tuple.hd);
    }
    unsigned int size() const {
	return store_.size();
    }
public:

    class Iterator {
    private:
	typename std::deque<T>::const_iterator curr;
	typename std::deque<T>::const_iterator end;
	T& tgt_fld;
	bool before_start = true;
    public:
        explicit Iterator(Tuple& tgt) : tgt_fld(tgt.hd) {}
	void migrate(const Bag& bag) {
	    curr = bag.store_.cbegin();
	    end = bag.store_.cend();
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
    Index(const Index&) = default;
    Index(Index&&) = default;
    Index& operator=(const Index&) = delete;
    friend void swap(Index& a, Index& b) {
	using std::swap;
	swap(a.map, b.map);
    }
    void clear() {
        Index temp;
        swap(*this, temp);
    }
    Index& of() {
        return *this;
    }
    template<class... Rest>
    auto& of(const Key& key, const Rest&... rest) {
	return map[key].of(rest...);
    }
    const Sub& operator[](const Key& key) const {
	auto it = map.find(key);
	return (it == map.cend()) ? dummy : it->second;
    }
    const Index& find() const {
        return *this;
    }
    template<class... Rest>
    const auto& find(const Key& key, const Rest&... rest) const {
        return (*this)[key].find(rest...);
    }
    template<class... Rest>
    bool insert(const Key& key, const Rest&... rest) {
	return of(key).insert(rest...);
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd, tuple.tl);
    }
    template<class Other>
    void sec_insert(const Other& other) {
	of(other.template get<Tag>()).sec_insert(other);
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

template<class Pri, class... Sec> class MultiIndex {
public:
    class Iterator;
    friend Iterator;
    typedef typename Pri::Tuple Tuple;
private:
    Pri pri_;
    std::tuple<Sec...> sec_;
public:
    explicit MultiIndex() {}
    MultiIndex(const MultiIndex&) = default;
    MultiIndex(MultiIndex&&) = default;
    MultiIndex& operator=(const MultiIndex&) = delete;
    friend void swap(MultiIndex& a, MultiIndex& b) {
	using std::swap;
	swap(a.pri_, b.pri_);
	swap(a.sec_, b.sec_);
    }
    void clear() {
        MultiIndex temp;
        swap(*this, temp);
    }
    const Pri& pri() const {
	return pri_;
    }
    template<int I>
    const typename pack_elem<I,Sec...>::type& sec() const {
	return std::get<I>(sec_);
    }
    MultiIndex& of() {
        return *this;
    }
    const MultiIndex& find() const {
        return *this;
    }
    template<class... Flds>
    bool insert(const Flds&... flds) {
	return insert(Tuple(flds...));
    }
    bool insert(const Tuple& tuple) {
	if (pri_.insert(tuple)) {
	    // Only insert on secondary index if tuple wasn't already present.
	    detail::insert_all(sec_, tuple);
	    return true;
	}
	// TODO: Check that tuple is also present on sec.
	return false;
    }
    template<class Other>
    void sec_insert(const Other& other) {
	pri_.sec_insert(other);
	detail::insert_all(sec_, other);
    }
    Iterator iter(Tuple& tgt) const {
	Iterator it(tgt);
	it.migrate(*this);
	return it;
    }
    bool empty() const {
	return pri_.empty();
    }
    template<class... Flds>
    bool contains(const Flds&... flds) const {
	return pri_.contains(flds...);
    }
    unsigned int size() const {
	return pri_.size();
    }
public:

    class Iterator {
    private:
	typename Pri::Iterator sub_iter;
    public:
	explicit Iterator(Tuple& tgt) : sub_iter(tgt) {}
	void migrate(const MultiIndex& idx) {
	    sub_iter.migrate(idx.pri_);
	}
	bool next() {
	    return sub_iter.next();
	}
    };
};

// SPECIALIZED CONTAINERS =====================================================

template<class Tag, class K, const typename KeyTraits<K>::SizeHint& Hint,
	 class S> class FlatIndex {
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
    explicit FlatIndex() : array(KeyTraits<Key>::extract_size(Hint)) {}
    FlatIndex(const FlatIndex&) = default;
    FlatIndex(FlatIndex&&) = default;
    FlatIndex& operator=(const FlatIndex&) = delete;
    friend void swap(FlatIndex& a, FlatIndex& b) {
	using std::swap;
	swap(a.array, b.array);
    }
    void clear() {
        FlatIndex temp;
        swap(*this, temp);
    }
    FlatIndex& of() {
        return *this;
    }
    template<class... Rest>
    auto& of(const Key& key, const Rest&... rest) {
	return const_cast<Sub&>((*this)[key]).of(rest...);
    }
    const Sub& operator[](const Key& key) const {
	unsigned int i = KeyTraits<Key>::extract_idx(key);
#ifdef NDEBUG
	return array[i];
#else
	return array.at(i);
#endif
    }
    const FlatIndex& find() const {
        return *this;
    }
    template<class... Rest>
    const auto& find(const Key& key, const Rest&... rest) const {
        return (*this)[key].find(rest...);
    }
    template<class... Rest>
    bool insert(const Key& key, const Rest&... rest) {
	return of(key).insert(rest...);
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd, tuple.tl);
    }
    template<class Other>
    void sec_insert(const Other& other) {
	of(other.template get<Tag>()).sec_insert(other);
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
					 std::pair<const Key,Sub&> > {
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
			       std::pair<const Key,const Sub&> > {
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

template<class Tag, class T, const typename KeyTraits<T>::SizeHint& Hint>
class BitSet {
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
    explicit BitSet() {
	EXPECT(sizeof(Store) * 8 >= KeyTraits<T>::extract_size(Hint));
    }
    BitSet(const BitSet&) = default;
    BitSet(BitSet&&) = default;
    BitSet& operator=(const BitSet&) = delete;
    friend void swap(BitSet& a, BitSet& b) {
	using std::swap;
	swap(a.bits, b.bits);
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
    template<class Other>
    void sec_insert(const Other& other) {
	insert(other.template get<Tag>());
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
    typedef std::forward_list<std::pair<const K,S> > List;
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
    bool search(const Key& key, typename List::const_iterator& pos) const {
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
    LightIndex(const LightIndex&) = default;
    LightIndex(LightIndex&&) = default;
    LightIndex& operator=(const LightIndex&) = delete;
    friend void swap(LightIndex& a, LightIndex& b) {
	using std::swap;
	swap(a.list, b.list);
    }
    void clear() {
        LightIndex temp;
        swap(*this, temp);
    }
    LightIndex& of() {
        return *this;
    }
    template<class... Rest>
    auto& of(const Key& key, const Rest&... rest) {
	typename List::const_iterator cpos;
	typename List::iterator pos =
	    search(key, cpos)
	    // HACK: Convert a List::const_iterator to a List::iterator.
	    ? list.insert_after(cpos, list.cend(), list.cend())
	    : list.emplace_after(cpos, key, Sub());
	return pos->second.of(rest...);
    }
    const Sub& operator[](const Key& key) const {
	typename List::const_iterator pos;
	return search(key, pos) ? pos->second : dummy;
    }
    const LightIndex& find() const {
        return *this;
    }
    template<class... Rest>
    const auto& find(const Key& key, const Rest&... rest) const {
        return (*this)[key].find(rest...);
    }
    template<class... Rest>
    bool insert(const Key& key, const Rest&... rest) {
	return of(key).insert(rest...);
    }
    bool insert(const Tuple& tuple) {
	return insert(tuple.hd, tuple.tl);
    }
    template<class Other>
    void sec_insert(const Other& other) {
	of(other.template get<Tag>()).sec_insert(other);
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
	return search(key, pos) ? pos->second.contains(rest...) : false;
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

// REGISTRY FOR NAMED TUPLE KEYS ==============================================

// Interface differences from main case:
// - We provide read-only access to the underlying index.
// - Instead of supplying a single Key, the client code must specify the fields
//   of the (NamedTuple-typed) key as separate arguments.
// - Not allowing T's constructor to have additional arguments besides the key
//   pointer.
// - No merge() callback is necessary (since there are never any additional
//   arguments to merge).

// TODO:
// - Provide efficient access to the ref cell, and allow adding to it.
// - Only using mi::Index to perform NamedTuple indexing. Could allow client
//   code to specify the indexing structure to use (default to nested Index's).
// - REF is a reserved tag name, with no static string identifier.
// - Allow constructor arguments besides the key pointer.

struct REF {};

namespace detail {

template<class T, class Curr> struct IndexSynthHelper;

template<class T>
struct IndexSynthHelper<T, mi::Nil> {
    typedef mi::Cell<REF, Ref<T> > Type;
};

template<class T, class Tag, class Hd, class Tl>
struct IndexSynthHelper<T, mi::NamedTuple<Tag,Hd,Tl> > {
    typedef mi::Index<Tag, Hd, typename IndexSynthHelper<T,Tl>::Type> Type;
};

template<class T, class Tuple>
struct IndexSynth : public IndexSynthHelper<T,Tuple> {};

} // namespace detail

template<class T, class Tag, class Hd, class Tl>
class Registry<T, mi::NamedTuple<Tag,Hd,Tl> > {
public:
    typedef mi::NamedTuple<Tag,Hd,Tl> Key;
    typedef typename detail::IndexSynth<T,Key>::Type Idx;
private:
    Idx obj2ref_;
    std::vector<T> ref2obj_;
public:
    explicit Registry() {}
    Registry(const Registry& rhs) = delete;
    Registry(Registry&& rhs) = default;
    Registry& operator=(const Registry& rhs) = delete;
    friend void swap(Registry& a, Registry& b) {
	using std::swap;
	swap(a.obj2ref_, b.obj2ref_);
	swap(a.ref2obj_, b.ref2obj_);
    }
    void clear() {
        Registry temp;
        swap(*this, temp);
    }
    const Idx& index() const {
        return obj2ref_;
    }
    T& operator[](Ref<T> ref) {
	EXPECT(ref.valid());
	return ref2obj_.at(ref.value());
    }
    const T& operator[](Ref<T> ref) const {
	EXPECT(ref.valid());
	return ref2obj_.at(ref.value());
    }
    template<typename... Flds>
    T& make(const Flds&... flds) {
        mi::Cell<REF, Ref<T> >& ref_cell = obj2ref_.of(flds...);
        EXPECT(ref_cell.empty());
        Ref<T> new_ref(ref2obj_.size());
        ref_cell.insert(new_ref);
        Key key(flds...);
        ref2obj_.emplace_back(&key, new_ref);
        return ref2obj_.back();
    }
    template<typename... Flds>
    T& add(const Flds&... flds) {
        const mi::Cell<REF, Ref<T> >& ref_cell = obj2ref_.find(flds...);
        if (ref_cell.empty()) {
            return make(flds...);
        }
        return (*this)[ref_cell.get()];
    }
    T& mktemp() {
        Ref<T> new_ref(ref2obj_.size());
        ref2obj_.emplace_back(nullptr, new_ref);
        return ref2obj_.back();
    }
    template<typename... Flds>
    T& find(const Flds&... flds) {
        const mi::Cell<REF, Ref<T> >& ref_cell = obj2ref_.find(flds...);
        return (*this)[ref_cell.get()];
    }
    template<typename... Flds>
    const T& find(const Flds&... flds) const {
        const mi::Cell<REF, Ref<T> >& ref_cell = obj2ref_.find(flds...);
        return (*this)[ref_cell.get()];
    }
    template<typename... Flds>
    bool contains(const Flds&... flds) const {
        const mi::Cell<REF, Ref<T> >& ref_cell = obj2ref_.find(flds...);
	return !ref_cell.empty();
    }
    unsigned int size() const {
	return ref2obj_.size();
    }
    bool empty() const {
	return ref2obj_.empty();
    }
    const T& first() const {
	return ref2obj_.front();
    }
    const T& last() const {
	return ref2obj_.back();
    }
    typename std::vector<T>::iterator begin() {
	return ref2obj_.begin();
    }
    typename std::vector<T>::iterator end() {
	return ref2obj_.end();
    }
    typename std::vector<T>::const_iterator begin() const {
	return ref2obj_.cbegin();
    }
    typename std::vector<T>::const_iterator end() const {
	return ref2obj_.cend();
    }
};

#endif
