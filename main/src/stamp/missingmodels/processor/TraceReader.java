package stamp.missingmodels.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import stamp.missingmodels.util.Util.MultivalueMap;

public class TraceReader {
	//private Map<Integer,Integer> stackDepthByThread = new HashMap<Integer,Integer>();
	private final Map<Integer,Stack<String>> stackByThread = new HashMap<Integer,Stack<String>>();
	private boolean isProcessingTrace = false;
	private final MultivalueMap<String,String> callgraph = new MultivalueMap<String,String>();
	
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
			this.callgraph.add(stack.peek(), method);
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
	
	private void process(BufferedReader traceReader) throws IOException {
		while(this.processLine(traceReader.readLine()));
	}

	private MultivalueMap<String,String> getCallgraph(BufferedReader traceReader) throws IOException {
		this.process(traceReader);
		return this.callgraph;
	}
	
	public MultivalueMap<String,String> getCallgraph(String traceDir, String apkName) {
		String traceFileName = apkName + ".traceout";
		try {
			for(File traceFile : new File(traceDir).listFiles()) {
				if(traceFile.getName().endsWith(traceFileName)) {
					System.out.println("Found trace file: " + traceFile.getName());
					return this.getCallgraph(new BufferedReader(new FileReader(traceFile)));
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		throw new RuntimeException("No trace file found for: " + apkName);
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
		String traceDir= "../profiler/traceouts/";
		String apkName = "0ac54ec80dc63f5f6d0334d5eca8bb59.apk";
		printCallGraph(new TraceReader().getCallgraph(traceDir, apkName), new PrintWriter(System.out));
	}
}
