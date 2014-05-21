#include <iostream>
#include "CNode.h"
#include "Constraint.h"
#include "term.h"
 
#include <string>
#include <map>

using namespace std;

int main(int c, char** argv) {
  Term* x = VariableTerm::make("x");
  Term* y = VariableTerm::make("y");
  Term* z = VariableTerm::make("z");

  map<Term*, long int> elems1;
  elems1[x] = 1;
  elems1[y] = 1;
  Term* t1 = ArithmeticTerm::make(elems1, 0);

  map<Term*, long int> elems2;
  elems2[x] = 1;
  elems2[y] = 1;
  elems2[z] = 1;
  Term* t2 = ArithmeticTerm::make(elems2, 0);

  Constraint c1(t1, ConstantTerm::make(0), ATOM_GT);
  Constraint c2(t2, ConstantTerm::make(5), ATOM_LT);
  Constraint c3 = (c1 | c2);

  map<VariableTerm*, int> costs;
  costs[(VariableTerm*)x] = 1;
  costs[(VariableTerm*)y] = 1;
  costs[(VariableTerm*)z] = 5;


  map<Term*, SatValue> min_assign;
  set<Constraint> bg;
  int msa_cost = c3.msa(min_assign, bg, costs);
  for(auto it = min_assign.begin(); it!= min_assign.end(); it++) {
    Term* t = it->first;
    SatValue sv = it->second;
    cout << t->to_string() << ":" << sv.to_string() << "\t";

  }
  cout << endl;
  /*

  map<Term*, SatValue> min_assign;
  int min_vars = c3.msa(min_assign);
  for(auto it = min_assign.begin(); it!= min_assign.end(); it++) {
    Term* t = it->first;
    SatValue sv = it->second;
    cout << t->to_string() << ":" << sv.to_string() << "\t";
  }
  cout << endl;
  */
}

