#include <iostream>
#include "CNode.h"
#include "Constraint.h"
#include "term.h"
 
#include <iostream>
#include <fstream>
#include <string>
#include <map>

#include <sstream>
#include <algorithm>
#include <iterator>

/*
 * Compile as: g++ -I.. -I../cnode -I../solver -I../numeric-lib -I../term/ -std=c++0x -lgmp example.cpp ../build/libmistral.a ../build/parser/libparser.a -o my_project -lgmp
 * The, run ./my_project
 */

using namespace std;

Constraint con = Constraint(true);
map<string, VariableTerm* > variableTerms;
map<VariableTerm*, int> costs;

VariableTerm* getTerm(string name) {
  VariableTerm* term = variableTerms[name];
  if(term == NULL) {
    term = (VariableTerm*)VariableTerm::make(name);
    variableTerms[name] = term;

    costs[getTerm(name)] = 0;
  }
  return term;
}

void addBaseEdge(string name) {
  costs[getTerm(name)] = 1;
}

void addBinaryProduction(string targetName, string firstInputName, string secondInputName) {
  // STEP 1: Make the term
  map<Term*, long int> elems1;
  elems1[getTerm(targetName)] = 1;
  elems1[getTerm(firstInputName)] = -1;
  Term* term1 = ArithmeticTerm::make(elems1, 0);

  map<Term*, long int> elems2;
  elems2[getTerm(targetName)] = 1;
  elems2[getTerm(secondInputName)] = -1;
  Term* term2 = ArithmeticTerm::make(elems2, 0);

  // STEP 2: Make the constraint
  con = con & (Constraint(term1, ConstantTerm::make(0), ATOM_LEQ) | Constraint(term2, ConstantTerm::make(0), ATOM_LEQ));
}

void addUnaryProduction(string targetName, string inputName) {
  // STEP 1: Make the term
  map<Term*, long int> elems;
  elems[getTerm(targetName)] = 1;
  elems[getTerm(inputName)] = -1;

  Term* term = ArithmeticTerm::make(elems, 0);

  // STEP 2: Make the constraint
  con = con & Constraint(term, ConstantTerm::make(0), ATOM_LEQ);
}

void addInitialCut(string targetName) {
  // STEP 1: Make the term
  map<Term*, long int> elems;
  elems[getTerm(targetName)] = 1;
  
  Term* term = ArithmeticTerm::make(elems, 0);
  
  // STEP 2: Make the constraint
  con = con & Constraint(term, ConstantTerm::make(1), ATOM_GEQ);
}

vector<string> split(string str) {
  istringstream iss(str);
  vector<string> tokens{istream_iterator<string>{iss}, istream_iterator<string>{}};
  return tokens;
}

int main(int c, char** argv) {

  string filename = "input.txt";

  // Input from file
  cout << "Reading from file" << endl;
  string line;
  ifstream input("constraints.txt");
  if(!input.is_open()) {
    return 0;
  }
  while(getline(input, line)) {
    vector<string> s = split(line);
    if(s.size() == 2) {
      if(s[0].compare("base") == 0) {
	addBaseEdge(s[1]);
      } else if(s[0].compare("cut") == 0) {
	addInitialCut(s[1]);
      } else {
	addUnaryProduction(s[0], s[1]);
      }
    } else if(s.size() == 3) {
      addBinaryProduction(s[0], s[1], s[2]);
    }
  }
  input.close();

  // Get min assignment
  cout << "Getting msa for:" << endl;
  cout << con << endl << endl;
  map<Term*, SatValue> min_assign;
  set<Constraint> bg;
  int min_vars = con.msa(min_assign, bg, costs);
  
  cout << "Done" << endl;

  // Print stuff out
  for(auto it = min_assign.begin(); it != min_assign.end(); it++) {
    Term* t = it->first;
    SatValue sv = it->second;
    cout << t->to_string() << ":" << sv.to_string() << "\t";
  }
  cout << endl;
  return 0;
}
