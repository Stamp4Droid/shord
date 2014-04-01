#include "util.hpp"

TUPLE_TAG(A);
TUPLE_TAG(B);
TUPLE_TAG(C);

namespace mi {

template<> struct KeyTraits<unsigned int> {
    typedef unsigned int SizeHint;
    static unsigned int extract_size(unsigned int hint) {
	return hint;
    }
    static unsigned int extract_idx(unsigned int val) {
	return val;
    }
    static unsigned int from_idx(unsigned int idx) {
	return idx;
    }
};

} // namespace mi

template<class Idx>
void copy_test(unsigned int times, const Idx& from) {
    unsigned int t_start = current_time();
    for (unsigned i = 0; i < times; i++) {
	Idx to;
	to.copy(from);
    }
    std::cout << "Done in " << current_time() - t_start << "ms" << std::endl;
}

int main() {
    typedef mi::Index<A, unsigned int,
		      mi::Index<B, unsigned int,
				mi::Table<C, unsigned int>>> RegIdx;
    RegIdx reg = mi::identity<RegIdx>(100);
    std::cout << "No uniquing:" << std::endl;
    copy_test(1000, reg);

    mi::Uniq<RegIdx> uniqd;
    FOR(tup, reg) {
	uniqd.insert(tup);
    }
    std::cout << "With uniquing:" << std::endl;
    copy_test(1000, uniqd);
}
