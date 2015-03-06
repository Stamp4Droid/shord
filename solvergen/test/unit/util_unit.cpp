#include <list>

#include "util.hpp"

// Unit tests for the 'util' library.

// Simple structs used as tags
TUPLE_TAG(flag);
TUPLE_TAG(ref);
TUPLE_TAG(b);
TUPLE_TAG(c);
TUPLE_TAG(id);
TUPLE_TAG(first);
TUPLE_TAG(second);
TUPLE_TAG(third);

extern const boost::none_t NONE = boost::none;

class Bar {
public:
    typedef std::string Key;
public:
    const std::string name;
    const Ref<Bar> ref;
    const bool flag;
public:
    explicit Bar(const std::string* name, Ref<Bar> ref, bool flag)
	: name(*name), ref(ref), flag(flag) {}
    bool merge(bool flag) const {
	assert(flag == this->flag);
	return false;
    }
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

Registry<Bar> bars;

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

    bars.make("aaa", true);
    bars.make("bbb", false);
    bars.add("aaa", true);
    bars.make("ccc", false);
    std::cout << "Should see aaa,bbb,ccc" << std::endl;
    for (const Bar& b : bars) {
	std::cout << b.ref << ":" << b << std::endl;
    };
    std::cout << std::endl;

    mi::MultiIndex<
        mi::FlatIndex<flag, bool, NONE,
            mi::FlatIndex<ref, Ref<Bar>, bars,
                mi::LightIndex<b, std::string,
                    mi::Index<c, char,
                        mi::Cell<id, int> > > > >,
        mi::Index<c, char,
            mi::MultiIndex<
                mi::BitSet<flag, bool, NONE >,
                mi::Table<b, std::string> > > > nidx;
    nidx.insert(true,  bars.find("aaa").ref, "zero",  'z', 0);
    nidx.insert(true,  bars.find("bbb").ref, "one",   'o', 1);
    nidx.insert(true,  bars.find("ccc").ref, "two",   't', 2);
    nidx.insert(false, bars.find("aaa").ref, "three", 't', 3);
    nidx.insert(false, bars.find("bbb").ref, "four",  'f', 4);
    nidx.insert(false, bars.find("ccc").ref, "five",  'f', 5);
    std::cout << "All " << nidx.size() << " entries:" << std::endl;
    FOR(res, nidx) {
        std::cout << "  ";
	res.print(std::cout);
        std::cout << std::endl;
    }
    std::cout << "Inserting duplicate id entry:" << std::endl;
    nidx.insert(true,  bars.find("aaa").ref, "zero",  'z', 6);
    FOR(res, nidx) {
        std::cout << "  ";
	res.print(std::cout);
        std::cout << std::endl;
    }
    std::cout << "All the ref's for false:" << std::endl;
    for (const auto& p : nidx.pri()[false]) {
	std::cout << "  " << p.first << std::endl;
    }
    std::cout << "All true entries:" << std::endl;
    FOR(res, nidx.pri()[true]) {
        std::cout << "  ";
	res.print(std::cout);
        std::cout << std::endl;
    }
    std::cout << "All true entries for bbb:" << std::endl;
    FOR(res, nidx.pri()[true][bars.find("bbb").ref]) {
        std::cout << "  ";
	res.print(std::cout);
        std::cout << std::endl;
    }
    std::cout << "All (c,flag) combinations:" << std::endl;
    FOR(res, nidx.sec<0>()) {
        std::cout << "  ";
	res.print(std::cout);
        std::cout << std::endl;
    }
    std::cout << "All b's for c = 't':" << std::endl;
    for (const std::string& b : nidx.sec<0>()['t'].sec<0>()) {
        std::cout << "  " << b << std::endl;
    }
    std::cout << std::endl;

    mi::Index<first, int, mi::Table<second, int>> int_tab;
    for (unsigned int i = 0; i < 5; i++) {
	int_tab.insert(i, i+40);
    }
    std::cout << "Regular table contents:" << std::endl;
    FOR(tup, int_tab) {
        std::cout << "  ";
	tup.print(std::cout);
        std::cout << std::endl;
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

    return 0;
}
