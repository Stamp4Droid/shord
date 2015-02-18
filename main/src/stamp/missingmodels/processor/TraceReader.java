package stamp.missingmodels.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.Util.Pair;

public class TraceReader {
	//private Map<Integer,Integer> stackDepthByThread = new HashMap<Integer,Integer>();
	private final Map<Integer,Stack<String>> stackByThread = new HashMap<Integer,Stack<String>>();
	private boolean isProcessingTrace = false;
	private final MultivalueMap<String,String> callgraph = new MultivalueMap<String,String>();
	private final List<Pair<String,String>> orderedCallgraph = new ArrayList<Pair<String,String>>();
	
	private final Set<String> methodSet = new HashSet<String>();
	private final List<String> methodList = new ArrayList<String>();
	
	private Stack<String> getStack(int thread) {
		Stack<String> stack = this.stackByThread.get(thread);
		if(stack == null) {
			stack = new Stack<String>();
			this.stackByThread.put(thread, stack);
		}
		return stack;
	}
	
	private void enterMethod(int thread, String method) {
		Stack<String> stack = this.getStack(thread);
		if(!stack.isEmpty()) {
			String caller = stack.peek();
			this.callgraph.add(caller, method);
			this.orderedCallgraph.add(new Pair<String,String>(caller, method));
			if(!this.methodSet.contains(method)) {
				this.methodList.add(method);
				this.methodSet.add(method);
			}
		}
		stack.push(method);
	}
	
	private String exitMethod(int thread) {
		Stack<String> stack = this.getStack(thread);
		return stack.isEmpty() ? null : stack.pop();
	}

	// returns false if line == null (EOF)
	private boolean processLine(String line) {
		if(line == null) {
			return false;
		}
		
		if(!this.isProcessingTrace) {
			if(line.startsWith("Trace")) {
				this.isProcessingTrace = true;
			}
			return true;
		}
		
		// extract info
		String[] tokens = line.trim().split("\\s+");
		if(tokens.length != 5) {
			return true;
		}
		
		//System.out.println(tokens[0] + " " + tokens[1] + " " + tokens[2]);
		int thread = Integer.parseInt(tokens[0]);
		String direction = tokens[1];
		int stackDepth = 0;
		while(tokens[3].charAt(stackDepth) == '.') {
			stackDepth++;
		}
		String method = TraceUtils.convert(tokens[3].substring(stackDepth) + tokens[4]);
		//String method = tokens[3].substring(stackDepth) + tokens[4];
		
		// set data
		if(direction.equals("ent")) {
			this.enterMethod(thread, method);
		} else if(direction.equals("xit") || direction.equals("unr")) {
			String popMethod = this.exitMethod(thread);
			if(popMethod != null && !popMethod.equals(method)) {
				throw new RuntimeException("Mismatched call return: popped " + popMethod + " but exited " + method);
			}
		} else {
			throw new RuntimeException("Invalid ent/xit/unr: " + direction);
		}
		
		return true;
	}
	
	private TraceReader process(BufferedReader traceReader) throws IOException {
		while(this.processLine(traceReader.readLine()));
		return this;
	}
	
	private static TraceReader read(String traceDir, String apkName) {
		String traceFileName = apkName + ".traceout";
		System.out.println("Looking for trace file: " + traceFileName);
		try {
			System.out.println("In directory: " + new File(traceDir).getCanonicalPath());
			for(File traceFile : new File(traceDir).listFiles()) {
				System.out.println("Comparing to file: " + traceFile.getCanonicalPath());
				if(traceFile.getName().endsWith(traceFileName)) {
					System.out.println("Found trace file: " + traceFile.getName());
					return new TraceReader().process(new BufferedReader(new FileReader(traceFile)));
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("ERROR: no trace file found for: " + apkName);
		return new TraceReader();
	}
	
	public static MultivalueMap<String,String> getCallgraph(String traceDir, String apkName) {
		return read(traceDir, apkName).callgraph;
	}
	
	public static List<Pair<String,String>> getOrderedCallgraph(String traceDir, String apkName) {
		return read(traceDir, apkName).orderedCallgraph;
	}
	
	public static List<String> getMethodList(String traceDir, String apkName) {
		return read(traceDir, apkName).methodList;
	}
	
	public static List<String> getReachableMethods(List<String> methodList) {		
		ProgramRel relReachableM = (ProgramRel)ClassicProject.g().getTrgt("reachableM");
		relReachableM.load();
		List<String> result = new ArrayList<String>();
		for(Object tuple : relReachableM.getAry1ValTuples()) {
			if(methodList.contains(tuple.toString())) {
				//System.out.println("Reached reachable method: " + tuple.toString());
				result.add(tuple.toString());
			} else {
				//System.out.println("Unreached reachable method: " + tuple.toString());
			}
		}
		relReachableM.close();
		return result;
	}
	
	public static List<String> getReachableMethods(String traceDir, String apkName) {
		return getReachableMethods(getMethodList(traceDir, apkName));
	}
	
	public static void printCallGraph(MultivalueMap<String,String> callgraph, PrintWriter pw) {
		int callgraphSize = 0;
		for(String caller : callgraph.keySet()) {
			callgraphSize += callgraph.get(caller).size();
			for(String callee : callgraph.get(caller)) {
				System.out.println(caller + " -> " + callee);
			}
		}
		System.out.println("callgraph size: " + callgraphSize);
	}
	
	public static void main(String[] args) throws IOException {
		String traceDir = "../profiler/traceouts/";
		String apkName = "0ac54ec80dc63f5f6d0334d5eca8bb59.apk";
		printCallGraph(getCallgraph(traceDir, apkName), new PrintWriter(System.out));
	}
}