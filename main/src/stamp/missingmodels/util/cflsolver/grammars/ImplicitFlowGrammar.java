package stamp.missingmodels.util.cflsolver.grammars;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;

public class ImplicitFlowGrammar extends TaintGrammar {
	public ImplicitFlowGrammar() {
		this.addUnaryProduction("Ref2RefT", "ref2RefImp");
		this.addUnaryProduction("Ref2PrimT", "ref2PrimImp");
		this.addUnaryProduction("Prim2RefT", "prim2RefImp");
		this.addUnaryProduction("Prim2PrimT", "prim2PrimImp");
	}
	
	public static class NegligibleImplicitFlowGrammar extends ContextFreeGrammar {
		private static final Set<String> forms = new HashSet<String>();
		private static final Map<String,String> formsToSuffixes = new HashMap<String,String>();
		private static final Set<String> edgesT2p = new HashSet<String>();
		private static final Set<String> edgesp2p = new HashSet<String>();
		private static final Map<String,String> backwards = new HashMap<String,String>();
		private static final Map<String,String> productions = new HashMap<String,String>();
		static {
			forms.add("T2T");
			forms.add("T2p");
			forms.add("p2p");
			
			formsToSuffixes.put("T2T", "");
			formsToSuffixes.put("T2p", "_T2p");
			formsToSuffixes.put("p2p", "_p2p");
			
			edgesT2p.add("assignPrim");
			edgesT2p.add("ref2RefT");
			edgesT2p.add("ref2PrimT");
			edgesT2p.add("prim2RefT");
			edgesT2p.add("prim2PrimT");
			
			edgesp2p.add("ref2RefImp");
			edgesp2p.add("ref2PrimImp");
			edgesp2p.add("prim2RefImp");
			edgesp2p.add("prim2PrimImp");
			
			backwards.put("T2T", "T2T");
			backwards.put("T2p", "p2p");
			backwards.put("p2p", "p2p");
			
			productions.put("T2T,T2T", "T2T");
			productions.put("T2T,T2p", "T2p");
			productions.put("T2T,p2p", "p2p");
			productions.put("T2p,T2T", "T2p");
			productions.put("T2p,T2p", "T2p");
			productions.put("T2p,p2p", "T2p");
			productions.put("p2p,T2T", "p2p");
			productions.put("p2p,T2p", "p2p");
			productions.put("p2p,p2p", "p2p");
		}
		
		// these make sure we don't add multiple productions
		private final HashSet<String> unaryProductionStrings = new HashSet<String>();
		private final HashSet<String> binaryProductionStrings = new HashSet<String>();
		
		private String toString(String target, String input, boolean isInputBackwards, boolean ignoreFields) {
			return target + "," + input + "," + isInputBackwards + "," + ignoreFields;
		}
		
		private String toString(String target, String firstInput, String secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards, boolean ignoreFields) {
			return target + "," + firstInput + "," + secondInput + "," + isFirstInputBackwards + "," + isSecondInputBackwards + "," + ignoreFields;			
		}
		
		private boolean process(UnaryProduction production, MultivalueMap<String,String> formsToSymbols) {
			boolean processed = false;
			for(String form : forms) {
				if(formsToSymbols.get(form).contains(production.input.symbol)) {
					String inputForm = production.isInputBackwards ? backwards.get(form) : form;
					String targetForm = inputForm;
					if(!formsToSymbols.get(targetForm).contains(production.target.symbol)) {
						formsToSymbols.get(targetForm).add(production.target.symbol);
						processed = true;
					}

					String target = production.target.symbol + formsToSuffixes.get(targetForm);
					String input = production.input.symbol + formsToSuffixes.get(form);
					String productionString = toString(target, input, production.isInputBackwards, production.ignoreFields);
					if(!this.unaryProductionStrings.contains(productionString)) {
						this.addUnaryProduction(target, input, production.isInputBackwards, production.ignoreFields);
						this.unaryProductionStrings.add(productionString);
					}
				}
			}
			return processed;
		}
		
		private boolean process(BinaryProduction production, MultivalueMap<String,String> formsToSymbols) {
			boolean processed = false;
			for(String firstForm : forms) {
				for(String secondForm : forms) {
					if(formsToSymbols.get(firstForm).contains(production.firstInput.symbol) && formsToSymbols.get(secondForm).contains(production.secondInput.symbol)) {
						String firstInputForm = production.isFirstInputBackwards ? backwards.get(firstForm) : firstForm;
						String secondInputForm = production.isSecondInputBackwards ? backwards.get(secondForm) : secondForm;
						String targetForm = productions.get(firstInputForm + "," + secondInputForm);
						if(!formsToSymbols.get(targetForm).contains(production.target.symbol)) {
							formsToSymbols.get(targetForm).add(production.target.symbol);
							processed = true;
						}

						String target = production.target.symbol + formsToSuffixes.get(targetForm);
						String firstInput = production.firstInput.symbol + formsToSuffixes.get(firstForm);
						String secondInput = production.secondInput.symbol + formsToSuffixes.get(secondForm);
						String productionString = toString(target, firstInput, secondInput, production.isFirstInputBackwards, production.isSecondInputBackwards, production.ignoreFields);
						if(!this.binaryProductionStrings.contains(productionString)) {
							this.addBinaryProduction(target, firstInput, secondInput, production.isFirstInputBackwards, production.isSecondInputBackwards, production.ignoreFields);
							this.binaryProductionStrings.add(productionString);
						}
					}
				}
			}
			return processed;
		}
		
		private static Set<String> getTerminals(ContextFreeGrammar grammar) {
			Set<String> nonTerminals = new HashSet<String>();
			Set<String> allSymbols = new HashSet<String>();
			for(int i=0; i<grammar.getSymbols().getNumSymbols(); i++) {
				for(UnaryProduction production : grammar.unaryProductionsByTarget.get(i)) {
					nonTerminals.add(production.target.symbol);
					allSymbols.add(production.target.symbol);
					allSymbols.add(production.input.symbol);
				}
				for(BinaryProduction production : grammar.binaryProductionsByTarget.get(i)) {
					nonTerminals.add(production.target.symbol);
					allSymbols.add(production.target.symbol);
					allSymbols.add(production.firstInput.symbol);
					allSymbols.add(production.secondInput.symbol);
				}
			}
			Set<String> terminals = new HashSet<String>();
			for(String symbol : allSymbols) {
				if(!nonTerminals.contains(symbol)) {
					terminals.add(symbol);
				}
			}
			return terminals;
		}
		
		public NegligibleImplicitFlowGrammar() {
			// STEP 0: Set up implicit flow grammar and make sure it doesn't have aux productions
			ImplicitFlowGrammar grammar = new ImplicitFlowGrammar();
			for(int i=0; i<grammar.getSymbols().getNumSymbols(); i++) {
				if(!grammar.auxProductionsByInput.get(i).isEmpty()) {
					throw new RuntimeException("Currently not handling aux productions!");
				}
			}
			
			// STEP 1: We construct the set of base symbols that are T=>p, p=>p, or T=>T
			MultivalueMap<String,String> formsToSymbols = new MultivalueMap<String,String>();
			formsToSymbols.ensure("T2p").addAll(edgesT2p);
			formsToSymbols.ensure("p2p").addAll(edgesp2p);
			for(String terminal : getTerminals(grammar)) {
				if(!edgesT2p.contains(terminal) && !edgesp2p.contains(terminal)) {
					formsToSymbols.ensure("T2T").add(terminal);
				}
			}
			
			// STEP 2: Add productions to get T2p and p2p edges (since T2T suffix is "", don't need to worry about it)
			for(String symbol : formsToSymbols.get("T2p")) {
				this.addUnaryProduction(symbol + formsToSuffixes.get("T2p"), symbol);
			}
			for(String symbol : formsToSymbols.get("p2p")) {
				this.addUnaryProduction(symbol + formsToSuffixes.get("p2p"), symbol);				
			}
			
			// STEP 3: Add productions to get Src2Sink edges
			this.addUnaryProduction("Src2Sink", "Src2Sink" + formsToSuffixes.get("T2p"));
			
			// STEP 4: Setup the worklist of edges to process
			LinkedHashSet<String> worklist = new LinkedHashSet<String>();
			worklist.addAll(formsToSymbols.get("T2p"));
			worklist.addAll(formsToSymbols.get("p2p"));
			worklist.addAll(formsToSymbols.get("T2T"));
			
			// STEP 5: For each edge, for each production using the edge, add the appropriate forms to the target and corresponding productions
			while(!worklist.isEmpty()) {
				String symbol = worklist.iterator().next();
				worklist.remove(symbol);
				int symbolInt = grammar.getSymbol(symbol).id;
				for(UnaryProduction production : grammar.unaryProductionsByInput.get(symbolInt)) {
					if(process(production, formsToSymbols)) {
						worklist.add(production.target.symbol);
					}
				}
				for(BinaryProduction production : grammar.binaryProductionsByFirstInput.get(symbolInt)) {
					if(process(production, formsToSymbols)) {
						worklist.add(production.target.symbol);
					}
				}
				for(BinaryProduction production : grammar.binaryProductionsBySecondInput.get(symbolInt)) {
					if(process(production, formsToSymbols)) {
						worklist.add(production.target.symbol);
					}
				}
			}
		}
	}
}
