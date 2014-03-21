#include <list>

#include "util.hpp"

// Unit tests for the 'util' library.

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
    mi::FlatIndex<
	bool,mi::FlatIndex<
		 Ref<Bar>,mi::Index<
			      int,
			      mi::Table<float,double>>>> nidx(nullptr, bars);
    for (const Bar& bar : bars) {
	int x = ++counter;
	float y = ++counter + 0.33;
	double z = ++counter + 0.66;
	nidx.insert(bar.flag, bar.ref, x, y, z);
    }
    struct {
	bool flag;
	Ref<Bar> ref;
	int a;
	float b;
	double c;
    } res;
    std::cout << "Should see " << nidx.size() << " entries:" << std::endl;
    auto it1 = nidx.iter(res.flag, res.ref, res.a, res.b, res.c);
    while (it1.next()) {
	std::cout << "  " << res.flag << " " << res.ref << " "
		  << res.a << " " << res.b << " " << res.c << std::endl;
    }
    std::cout << "Should see " << nidx[true].size() << " entries:" << std::endl;
    auto it2 = nidx[true].iter(res.ref, res.a, res.b, res.c);
    while (it2.next()) {
	std::cout << "  " <<  res.ref << " "
		  << res.a << " " << res.b << " " << res.c << std::endl;
    }
    std::cout << "Should see " << nidx[false][bars.find("bbb").ref].size()
	      << " entries:" << std::endl;
    auto it3 = nidx[false][bars.find("bbb").ref].iter(res.a, res.b, res.c);
    while (it3.next()) {
	std::cout << "  " <<  res.a << " " << res.b << " " << res.c
		  << std::endl;
    }
    nidx.copy(nidx[false], true);
    std::cout << "All false tuples copied to true:" << std::endl;
    auto it4 = nidx.iter(res.flag, res.ref, res.a, res.b, res.c);
    while (it4.next()) {
	std::cout <<  "  " << res.flag << " " << res.ref << " "
		  << res.a << " " << res.b << " " << res.c << std::endl;
    }
    std::cout << std::endl;

    mi::Index<char,mi::Index<int,mi::Table<int>>> tabs;
    tabs.insert('a', 1, 2); tabs.insert('a', 1, 3);
    tabs.insert('b', 2, 1); tabs.insert('b', 3, 1);
    tabs.insert('b', 3, 4); tabs.insert('b', 4, 4);
    tabs.insert('c', 5, 5);
    tabs.join(tabs['a'], tabs['b'], 'c');
    std::tuple<int,int> c_tup;
    auto c_it = tabs['c'].iter(c_tup);
    std::cout << "Should see (1,1) (1,4) (5,5):" << std::endl;
    while (c_it.next()) {
	std::cout << "  " << std::get<0>(c_tup) << " " << std::get<1>(c_tup)
		  << std::endl;
    }
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
