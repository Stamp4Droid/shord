#include <iostream>
#include "util.hpp"

TUPLE_TAG(FOO);
TUPLE_TAG(BAR);
TUPLE_TAG(QUX);
TUPLE_TAG(LOO);

int main() {
    std::cout << std::boolalpha;

    mi::MultiIndex<
        mi::Index<FOO, int,
                  mi::Index<BAR, bool,
                            mi::Table<QUX, float> > >,
        mi::Index<BAR, bool,
                  mi::Table<FOO, int> > > tab;
    typedef typename decltype(tab)::Tuple Tuple;

    tab.insert(1, true, 1.0);
    tab.insert(2, true, 2.0);
    tab.insert(3, false, 3.0);
    tab.insert(2, true, 4.0);

    std::cout << "FOO BAR QUX" << std::endl;
    FOR(t, tab) {
        std::cout << t.get<FOO>() << " " << t.get<BAR>() << " "
                  << t.get<QUX>() << std::endl;
    }
    std::cout << std::endl;
    std::cout << "FOO BAR" << std::endl;
    FOR(t, tab.sec<0>()) {
        std::cout << t.get<FOO>() << " " << t.get<BAR>() << std::endl;
    }
    std::cout << std::endl;
    std::cout << "FOO BAR" << std::endl;
    {
        mi::Index<BAR, bool,
                  mi::Table<FOO, int> > proj;
        mi::project(proj, tab.pri());
        FOR(t, proj) {
            std::cout << t.get<FOO>() << " " << t.get<BAR>() << std::endl;
        }
    }
    std::cout << std::endl;
    std::cout << "BAR QUX" << std::endl;
    {
        mi::Index<QUX, float,
                  mi::Table<BAR, bool> > proj;
        mi::project(proj, tab.pri());
        FOR(t, proj) {
            std::cout << t.get<BAR>() << " " << t.get<QUX>() << std::endl;
        }
    }

    // tab.merge<BAR>(true, false);

    // FOR(t, tab) {
    //     std::cout << t.get<FOO>() << " " << t.get<BAR>() << " "
    //               << t.get<QUX>() << std::endl;
    // }
    // std::cout << std::endl;
    // FOR(t, tab.sec<0>()) {
    //     std::cout << t.get<FOO>() << " " << t.get<BAR>() << " "
    //               << t.get<QUX>() << std::endl;
    // }
    // std::cout << std::endl;

    // tab.erase(Tuple(3, 1, 3.0));

    // std::cout <<
    //     mi::TupleIsSubset<
    //         mi::NamedTuple<BAR, bool,
    //             mi::NamedTuple<FOO, int, mi::Nil> >,
    //         Tuple>::value << std::endl;

    tab.erase(mi::NamedTuple<BAR, bool, mi::Nil>(true));

    std::cout << std::endl;
    std::cout << "After erasing:" << std::endl;
    std::cout << std::endl;

    std::cout << "FOO BAR QUX" << std::endl;
    FOR(t, tab) {
        std::cout << t.get<FOO>() << " " << t.get<BAR>() << " "
                  << t.get<QUX>() << std::endl;
    }
    std::cout << std::endl;
    std::cout << "FOO BAR" << std::endl;
    FOR(t, tab.sec<0>()) {
        std::cout << t.get<FOO>() << " " << t.get<BAR>() << std::endl;
    }
    std::cout << std::endl;
    std::cout << "FOO BAR" << std::endl;
    {
        mi::Index<BAR, bool,
                  mi::Table<FOO, int> > proj;
        mi::project(proj, tab.pri());
        FOR(t, proj) {
            std::cout << t.get<FOO>() << " " << t.get<BAR>() << std::endl;
        }
    }
    std::cout << std::endl;
    std::cout << "BAR QUX" << std::endl;
    {
        mi::Index<QUX, float,
                  mi::Table<BAR, bool> > proj;
        mi::project(proj, tab.pri());
        FOR(t, proj) {
            std::cout << t.get<BAR>() << " " << t.get<QUX>() << std::endl;
        }
    }
}
