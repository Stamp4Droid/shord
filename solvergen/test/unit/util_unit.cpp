#include <list>

#include "util.hpp"

// Unit tests for the 'util' library.

// Simple structs used as tags
TUPLE_TAG(flag);
TUPLE_TAG(ref);
TUPLE_TAG(a);
TUPLE_TAG(b);
TUPLE_TAG(c);
TUPLE_TAG(first);
TUPLE_TAG(second);
TUPLE_TAG(third);

struct Foo {
    const int x;
    const int y;
    const int z;
public:
    Foo(int x, int y, int z) : x(x), y(y), z(z) {}
    bool operator<(const Foo& other) const {
	return std::tie(x, y, z) < std::tie(other.x, other.y, other.z);
    }
    friend std::ostream& operator<<(std::ostream& os, const Foo& obj);
};

std::ostream& operator<<(std::ostream& os, const Foo& obj) {
    os << "Foo(" << obj.x << ',' << obj.y << ',' << obj.z << ")";
    return os;
}

class Bar {
    friend Registry<Bar>;
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Bar> ref;
    const bool flag;
private:
    explicit Bar(const std::string& name, Ref<Bar> ref, bool flag)
	: name(name), ref(ref), flag(flag) {}
    bool merge(bool flag) const {
	assert(flag == this->flag);
	return false;
    }
public:
    friend std::ostream& operator<<(std::ostream& os, const Bar& obj);
};

std::ostream& operator<<(std::ostream& os, const Bar& obj) {
    os << "Bar[" << obj.name << ',' << obj.flag << "]";
    return os;
}

namespace mi {

template<> struct KeyTraits<unsigned int> {
    typedef unsigned int SizeHint;
    static unsigned int extract_size(unsigned int max) {
	return max;
    }
    static unsigned int extract_idx(unsigned int val) {
	return val;
    }
    static unsigned int from_idx(unsigned int idx) {
	return idx;
    }
};

} // namespace mi

typedef FuzzyStack<int,3> Stack;

void print_stack(const Stack& s) {
    std::cout << "size: " << s.size()
	      << ", exact: " << s.is_exact()
	      << ", contents: ";
    print(std::cout, s, false);
    std::cout << std::endl;
}

template<class T> void print_relation(const T& lhs, const T& rhs) {
    switch (compare(lhs, rhs)) {
    case 0:
	std::cout << "equal to:" << std::endl;
	break;
    case -1:
	std::cout << "less than:" << std::endl;
	break;
    case 1:
	std::cout << "greater than:" << std::endl;
	break;
    default:
	assert(false);
    }
}

int main() {
    std::cout << std::boolalpha;

    for (const boost::filesystem::path& p : Directory("/boot")) {
	std::cout << p << std::endl;
    }
    std::cout << std::endl;

    Directory dir("/boot");
    std::list<boost::filesystem::path> files(dir.begin(), dir.end());
    files.sort();
    for (const boost::filesystem::path& fpath : files) {
	std::cout << fpath << std::endl;
    }
    std::cout << std::endl;

    Registry<Bar> bars;
    bars.make("aaa", true);
    bars.make("bbb", false);
    bars.add("aaa", true);
    bars.make("ccc", false);
    std::cout << "Should see aaa,bbb,ccc" << std::endl;
    for (const Bar& b : bars) {
	std::cout << b.ref << ":" << b << std::endl;
    };
    std::cout << std::endl;

    MultiIndex<Index<Index<Table<Foo>,int,&Foo::y>,int,&Foo::x>,
	       Index<Index<PtrTable<Foo>,int,&Foo::z>,int,&Foo::y>,
	       Index<Index<PtrTable<Foo>,int,&Foo::x>,int,&Foo::y>> idx;
    idx.insert(Foo(1,2,3));
    idx.insert(Foo(-10,0,0));
    idx.insert(Foo(1,5,-11));
    idx.insert(Foo(1,2,-11));
    idx.insert(Foo(-10,-1,24));
    idx.insert(Foo(1,2,3));
    idx.insert(Foo(4,5,-11));
    for (const Foo& obj : idx.primary()[1][2]) {
	std::cout << obj << std::endl;
    }
    std::cout << std::endl;
    for (const Foo& obj : idx.secondary<0>()[5][-11]) {
	std::cout << obj << std::endl;
    }
    std::cout << std::endl;
    for (const Foo& obj : idx.secondary<1>()[2][1]) {
	std::cout << obj << std::endl;
    }
    std::cout << std::endl;
    for (const Foo& obj : idx) {
	std::cout << obj << std::endl;
    }
    std::cout << std::endl;

    int counter = 0;
    mi::FlatIndex<flag, bool,
	mi::FlatIndex<ref, Ref<Bar>,
	    mi::Index<a, int,
		mi::Index<b, float,
		    mi::Table<c, double>>>>> nidx(boost::none, bars);
    for (const Bar& bar : bars) {
	int x = ++counter;
	float y = ++counter + 0.33;
	double z = ++counter + 0.66;
	nidx.insert(bar.flag, bar.ref, x, y, z);
    }
    std::cout << "All " << nidx.size() << " entries:" << std::endl;
    FOR(res, nidx) {
	std::cout << "  " << res << std::endl;
    }
    std::cout << "All the ref's for false:" << std::endl;
    for (const auto& p : nidx[false]) {
	std::cout << "  " << p.first << std::endl;
    }
    std::cout << "All true entries:" << std::endl;
    FOR(res, nidx[true]) {
	std::cout << "  " <<  res << std::endl;
    }
    std::cout << "All false entries for bbb:" << std::endl;
    FOR(res, nidx[false][bars.find("bbb").ref]) {
	std::cout << "  " <<  res << std::endl;
    }
    nidx.of(true).copy(nidx[false]);
    std::cout << "All false tuples copied to true:" << std::endl;
    FOR(res, nidx) {
	std::cout <<  "  " << res << std::endl;
    }
    std::cout << std::endl;

    mi::Index<first, int, mi::Table<second, int>> int_tab;
    for (unsigned int i = 0; i < 5; i++) {
	int_tab.insert(i, i+40);
    }
    std::cout << "Regular table contents:" << std::endl;
    FOR(tup, int_tab) {
	std::cout << "  " << tup << std::endl;
    }
    std::cout << "Contains (1,41): " << int_tab.contains(1, 41) << std::endl;
    std::cout << "Contains (1,43): " << int_tab.contains(1, 43) << std::endl;
    std::cout << "Contains (6,46): " << int_tab.contains(6, 46) << std::endl;
    std::cout << std::endl;

    Worklist<int,true> wl1;
    wl1.enqueue(1);
    wl1.enqueue(2);
    wl1.enqueue(1);
    wl1.enqueue(3);
    std::cout << "Should see 123" << std::endl;
    while (!wl1.empty()) {
	std::cout << wl1.dequeue();
    }
    std::cout << std::endl;
    wl1.enqueue(1);
    assert(!wl1.empty());
    std::cout << std::endl;

    Worklist<int,false,Table<int>> wl2;
    wl2.enqueue(1);
    wl2.dequeue();
    wl2.enqueue(1);
    assert(wl2.empty());

    auto s321 = Stack().push(1).push(2).push(3);
    print_stack(s321);
    auto s54 = Stack().push(4).push(5);
    print_relation(s321, s54);
    print_stack(s54);
    auto s54321 = s321.append(s54);
    print_relation(s54, s54321);
    print_stack(s54321);
    auto s5454321 = s54321.append(s54);
    print_relation(s54321, s5454321);
    print_stack(s5454321);
    auto s5432154 = s54.append(s54321);
    print_relation(s5454321, s5432154);
    print_stack(s5432154);

    return 0;
}
