package stamp.missingmodels.util.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;

public class AliasModelsTraceReader {
	public static class Variable {
		public final String method;
		public final int offset;
		public Variable(String method, int offset) {
			this.method = method;
			this.offset = offset;
		}
		@Override
		public int hashCode() { return 31*this.method.hashCode() + this.offset; }
		@Override
		public boolean equals(Object obj) {
			Variable other = (Variable)obj;
			return this.method.equals(other.method) && this.offset == other.offset;
		}
		@Override
		public String toString() { return this.method + "#" + this.offset; }
	}
	
	public static class Parameter {
		public final String method;
		public final int index;
		public Parameter(String method, int index) {
			this.method = method;
			this.index = index;
		}
		@Override
		public int hashCode() { return 31*this.method.hashCode() + this.index; }
		@Override
		public boolean equals(Object obj) {
			Parameter other = (Parameter)obj;
			return this.method.equals(other.method) && this.index == other.index;
		}
		@Override
		public String toString() { return this.method + "[" + this.index + "]"; }
	}
	
	// method:offset
	private static Variable getVariable(String representation) {
		String[] varTokens = representation.split(":");
		// Get the variable name
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<varTokens.length-1; i++) {
			sb.append(varTokens[i]).append(":");
		}
		String method = sb.substring(0, sb.length()-1);
		// Get the dalvik offset
		int offset = Integer.parseInt(varTokens[varTokens.length-1]);
		return new Variable(method, offset);
	}
	
	// x <- X() => x
	public final MultivalueMap<Variable,Integer> variablesToAbstractObjects = new MultivalueMap<Variable,Integer>();
	// y <- f(x) => x, y
	public final MultivalueMap<Variable,Integer> argsToAbstractObjects = new MultivalueMap<Variable,Integer>();
	public final Map<Variable,Integer> argsToIndex = new HashMap<Variable,Integer>();
	public final MultivalueMap<Variable,Integer> retsToAbstractObjects = new MultivalueMap<Variable,Integer>();
	// f(x) { ... } => x
	public final MultivalueMap<Parameter,Integer> paramsToAbstractObjects = new MultivalueMap<Parameter,Integer>();
	// abstract object sets (app allocated, method arg/ret/param observed, observed + not app)
	public final Map<Integer,Variable> appAbstractObjectsToAllocations = new HashMap<Integer,Variable>();
	public final Set<Integer> observedAbstractObjects = new HashSet<Integer>();
	public final Set<Integer> frameworkAbstractObjects = new HashSet<Integer>();
	public final Map<Pair<Variable,Integer>,Integer> retAbstractObjectPairsToCounts = new HashMap<Pair<Variable,Integer>,Integer>();
	public final MultivalueMap<Integer,Variable> abstractObjectsToRets = new MultivalueMap<Integer,Variable>();
	
	public AliasModelsTraceReader(String filename) {
		System.out.println("Reading file: " + new File(filename).getAbsolutePath());
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			int count = -1;
			while((line = br.readLine()) != null) {
				count++;
				String[] tokens = line.split("\t");
				if(tokens.length == 4) {
					this.appAbstractObjectsToAllocations.put(Integer.parseInt(tokens[3]), getVariable(tokens[2]));
					this.variablesToAbstractObjects.add(getVariable(tokens[2]), Integer.parseInt(tokens[3]));
				} else if(tokens.length == 5) {
					if(tokens[1].equals("METHCALLARG")) {
						int index = Integer.parseInt(tokens[3]);
						int abstractObject = Integer.parseInt(tokens[4]);
						this.observedAbstractObjects.add(abstractObject);
						if(index == -1) {
							Variable variable = getVariable(tokens[2]);
							this.retsToAbstractObjects.add(variable, abstractObject);
							this.abstractObjectsToRets.add(abstractObject, variable);
							Pair<Variable,Integer> pair = new Pair<Variable,Integer>(variable, abstractObject);
							if(!this.retAbstractObjectPairsToCounts.containsKey(pair)) {
								this.retAbstractObjectPairsToCounts.put(pair, count);
							}
						} else {
							this.argsToIndex.put(getVariable(tokens[2]), index);
							this.argsToAbstractObjects.add(getVariable(tokens[2]), abstractObject);
						}
					} else if(tokens[1].equals("METHPARAM")) {
						if(Integer.parseInt(tokens[3]) < 0) {
							br.close();
							throw new RuntimeException("Invalid METHPARAM parameter index: " + tokens[3]);
						}
						int abstractObject = Integer.parseInt(tokens[4]);
						this.observedAbstractObjects.add(abstractObject);
						this.paramsToAbstractObjects.add(new Parameter(tokens[2], Integer.parseInt(tokens[3])), abstractObject);
					} else {
						br.close();
						throw new RuntimeException("Invalid identifier: " + tokens[1]);
					}
				} else if(!line.matches("\\s+")) {
					br.close();
					throw new RuntimeException("Invalid line: " + line);
				}
			}
			System.out.println("ALIAS MODELS TRACE TOTAL COUNT: " + (count + 1));
			br.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		// Abstract objects in framework vs. in app
		for(int abstractObject : this.observedAbstractObjects) {
			if(!this.appAbstractObjectsToAllocations.keySet().contains(abstractObject)) {
				this.frameworkAbstractObjects.add(abstractObject);
			}
		}
	}
	
	public void printStatistics() {
		// Some basic statistics
		System.out.println(this.appAbstractObjectsToAllocations.size());
		System.out.println(this.frameworkAbstractObjects.size());
		System.out.println(this.observedAbstractObjects.size());
	}
	
	public static void main(String[] args) {
		String filename = "../alias_models/alias_models_traces/SMSBot.trace";
		System.out.println(new File(filename).getAbsolutePath());
		AliasModelsTraceReader processor = new AliasModelsTraceReader(filename);
		MultivalueMap<Variable,Variable> ptDynRetToApp = new MultivalueMap<Variable,Variable>();
		for(Variable variable : processor.retsToAbstractObjects.keySet()) {
			for(int abstractObjectId : processor.retsToAbstractObjects.get(variable)) {
				if(processor.appAbstractObjectsToAllocations.containsKey(abstractObjectId)) {
					Variable abstractObject = processor.appAbstractObjectsToAllocations.get(abstractObjectId);
					ptDynRetToApp.add(variable, abstractObject);
				}
			}
		}
		int counter = 0;
		for(Variable variable : ptDynRetToApp.keySet()) {
			for(Variable abstractObject : ptDynRetToApp.get(variable)) {
				System.out.println(variable + " -> " + abstractObject);
				counter++;
			}
		}
		System.out.println(counter);
	}
}
