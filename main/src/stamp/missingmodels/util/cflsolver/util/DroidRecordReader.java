package stamp.missingmodels.util.cflsolver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;

public class DroidRecordReader {
	private final MultivalueMap<String,String> callgraph = new MultivalueMap<String,String>();
	private final List<Pair<String,String>> callgraphList = new ArrayList<Pair<String,String>>();

	// returns false if line == null (EOF)
	private boolean processLine(String line) {
		if(line == null) {
			return false;
		}
		
		String[] tokens = line.split("#");
		if(tokens.length != 3) {
			System.out.println("Error: unexpected number of tokens: " + line);
		} else {
			if(!this.callgraph.get(tokens[1]).contains(tokens[2])) {
				this.callgraph.add(tokens[1], tokens[2]);
				this.callgraphList.add(new Pair<String,String>(tokens[1], tokens[2]));
			}
		}
		return true;
	}
	
	private DroidRecordReader process(BufferedReader traceReader) throws IOException {
		while(this.processLine(traceReader.readLine()));
		return this;
	}
	
	private static DroidRecordReader read(String droidRecordDir, String apkName) {
		String droidRecordFileName = apkName + ".txt";
		try {
			for(File droidRecordFile : new File(droidRecordDir).listFiles()) {
				if(droidRecordFile.getName().endsWith(droidRecordFileName)) {
					System.out.println("Found droid record file: " + droidRecordFile.getName());
					return new DroidRecordReader().process(new BufferedReader(new FileReader(droidRecordFile)));
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("ERROR: no droid record file found for: " + apkName);
		return new DroidRecordReader();
	}
	
	public static MultivalueMap<String,String> getCallgraph(String traceDir, String apkName) {
		return read(traceDir, apkName).callgraph;
	}
	
	public static List<Pair<String,String>> getCallgraphList(String traceDir, String apkName) {
		return read(traceDir, apkName).callgraphList;
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
