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

template<class C, class... Hints>
void test_ordering(const Hints&... hints) {
    std::vector<C> sets;
    sets.emplace_back(hints...); C& s_01_11 = sets.back();
    s_01_11.insert(0, 1);
    s_01_11.insert(1, 1);
    sets.emplace_back(hints...); C& s_00 = sets.back();
    s_00.insert(0, 0);
    sets.emplace_back(hints...);
    sets.emplace_back(hints...); C& s_10_11 = sets.back();
    s_10_11.insert(1, 0);
    s_10_11.insert(1, 1);
    sets.emplace_back(hints...); C& s_00_01_10 = sets.back();
    s_00_01_10.insert(0, 0);
    s_00_01_10.insert(0, 1);
    s_00_01_10.insert(1, 0);
    sets.emplace_back(hints...); C& s_10 = sets.back();
    s_10.insert(1, 0);
    std::cout << "Original sets:" << std::endl;
    for (const auto& s : sets) {
	std::cout << "  Set:" << std::endl;
	FOR(tup, s) {
	    std::cout << "    " << tup << std::endl;
	}
    }
    sort(sets.begin(), sets.end());
    std::cout << "Sorted sets:" << std::endl;
    for (const auto& s : sets) {
	std::cout << "  Set:" << std::endl;
	FOR(tup, s) {
	    std::cout << "    " << tup << std::endl;
	}
    }
    std::cout << std::endl;
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
    nidx.copy(nidx[false], true);
    std::cout << "All false tuples copied to true:" << std::endl;
    FOR(res, nidx) {
	std::cout <<  "  " << res << std::endl;
    }
    std::cout << "All entries for bbb:" << std::endl;
    FOR_CNSTR(res, nidx, mi::any, bars.find("bbb").ref) {
	std::cout << "  " <<  res << std::endl;
    }
    std::cout << std::endl;

    mi::Index<first, char,
	      mi::Uniq<mi::BiRel<second, third, int>>> tabs;
    tabs.insert('a', 1, 2); tabs.insert('a', 1, 3);
    tabs.insert('b', 2, 1); tabs.insert('b', 3, 1);
    tabs.insert('b', 3, 4); tabs.insert('b', 4, 4);
    tabs.insert('c', 5, 5);
    tabs.copy(join(tabs['a'], tabs['b']), 'c');
    mi::NamedTuple<second,int,mi::NamedTuple<third,int,mi::Nil>> c_tup;
    auto c_it = tabs['c'].iter(c_tup);
    std::cout << "Should see (1,1) (1,4) (5,5):" << std::endl;
    while (c_it.next()) {
	std::cout << "  (" << c_tup.get<second>() << ","
		  << c_tup.get<third>() << ")" << std::endl;
    }
    std::cout << std::endl;

    test_ordering<mi::Index<a,int,mi::Table<b,int>>>();
    test_ordering<mi::FlatIndex<a,unsigned int,
				mi::BitSet<b,unsigned int>>>(2, 2);
    test_ordering<mi::Uniq<mi::LightIndex<a,unsigned int,
					  mi::Table<b,unsigned int>>>>();

    mi::Index<first, int, mi::Table<second, int>> int_tab;
    mi::Uniq<decltype(int_tab)> uniq_tab;
    for (unsigned int i = 0; i < 5; i++) {
	int_tab.insert(i, i+40);
	uniq_tab.insert(i, i+40);
    }
    std::cout << "Regular table contents:" << std::endl;
    FOR(tup, int_tab) {
	std::cout << "  " << tup << std::endl;
    }
    std::cout << "Contains (1,41): " << int_tab.contains(1, 41) << std::endl;
    std::cout << "Contains (1,43): " << int_tab.contains(1, 43) << std::endl;
    std::cout << "Contains (6,46): " << int_tab.contains(6, 46) << std::endl;
    std::cout << "Uniq'd table contents:" << std::endl;
    FOR(tup, uniq_tab) {
	std::cout << "  " << tup << std::endl;
    }
    std::cout << "Contains (1,41): " << uniq_tab.contains(1, 41) << std::endl;
    std::cout << "Contains (1,43): " << uniq_tab.contains(1, 43) << std::endl;
    std::cout << "Contains (6,46): " << uniq_tab.contains(6, 46) << std::endl;
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

    Worklist<int,false,Table<int>> wl2;
    wl2.enqueue(1);
    wl2.dequeue();
    wl2.enqueue(1);
    assert(wl2.empty());

    return 0;
}
