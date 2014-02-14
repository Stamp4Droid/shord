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
public:
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

int main() {
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
	       PtrIndex<PtrIndex<PtrTable<Foo>,int,&Foo::z>,int,&Foo::y>,
	       PtrIndex<PtrIndex<PtrTable<Foo>,int,&Foo::x>,int,&Foo::y>> idx;
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
    std::function<bool(const Foo&)> pred = [](const Foo& f){return f.x < 0;};
    std::function<int(const Foo&)> mod = [](const Foo& f){return f.y;};
    Table<int> fm = filter_map(idx, pred, mod);
    for (int i : fm) {
	std::cout << i << std::endl;
    }
    std::cout << std::endl;

    Worklist<int> wl1(true);
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

    Worklist<int> wl2(false);
    wl2.enqueue(1);
    wl2.dequeue();
    wl2.enqueue(1);
    assert(wl2.empty());

    return 0;
}
