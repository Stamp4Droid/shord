package stamp.missingmodels.util.cflsolver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import stamp.missingmodels.util.cflsolver.util.CallgraphUtils.Callgraph;

public class TraceUtils {
	public static class TraceReader {
		private Map<Integer,Stack<String>> stackByThread;
		private Callgraph callgraph;
		private boolean isProcessingTrace;

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
			String method = convert(tokens[3].substring(stackDepth) + tokens[4]);
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

		public Callgraph process(File traceFile) {
			try {
				this.callgraph = new Callgraph();
				this.stackByThread = new HashMap<Integer,Stack<String>>();
				this.isProcessingTrace = false;
				BufferedReader traceReader = new BufferedReader(new FileReader(traceFile));
				while(this.processLine(traceReader.readLine()));
				traceReader.close();
				return this.callgraph;
			} catch(IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Error reading file!");
			}
		}
	}

	public static Callgraph read(String traceDir, String apkName) {
		String traceFileName = apkName + ".traceout";
		System.out.println("Looking for trace file: " + traceFileName);
		try {
			System.out.println("In directory: " + new File(traceDir).getCanonicalPath());
			for(File traceFile : new File(traceDir).listFiles()) {
				System.out.println("Comparing to file: " + traceFile.getCanonicalPath());
				if(traceFile.getName().endsWith(traceFileName)) {
					System.out.println("Found trace file: " + traceFile.getName());
					return new TraceReader().process(traceFile);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("ERROR: no trace file found for: " + apkName);
		return new Callgraph();
	}

	private static String convertClass(String className) {
		return className.replace('/', '.');
	}

	private static String convertBasicType(char type) {
		switch(type) {
		case 'V':
			return "void";
		case 'B':
			return "byte";
		case 'S':
			return "short";
		case 'I':
			return "int";
		case 'J':
			return "long";
		case 'F':
			return "float";
		case 'D':
			return "double";
		case 'Z':
			return "boolean";
		case 'C':
			return "char";
		}
		throw new RuntimeException("Unrecognized basic type: " + type);
	}

	private static String convertArrayType(String type) {
		return convertType(type.substring(1)) + "[]";
	}

	private static String convertType(String type) {
		if(type.startsWith("[")) {
			return convertArrayType(type);
		} else if(type.startsWith("L")) {
			return convertClass(type.substring(1));
		} else {
			if(type.length() != 1) {
				throw new RuntimeException("Invalid type: " + type);
			}
			return convertBasicType(type.charAt(0));
		}
	}

	private static String getShordSignature(String enclosingClass, String methodName, List<String> argTypes, String returnType) {
		StringBuilder sb = new StringBuilder();
		sb.append("<");
		sb.append(enclosingClass).append(": ");
		sb.append(returnType).append(" ");
		sb.append(methodName).append("(");
		for(int i=0; i<argTypes.size()-1; i++) {
			sb.append(argTypes.get(i)).append(",");
		}
		if(argTypes.size()>0) {
			sb.append(argTypes.get(argTypes.size()-1));
		}
		sb.append(")>");
		return sb.toString();
	}

	private static String buildArg(String arg, int arrayDepth) {
		StringBuilder sb = new StringBuilder();
		sb.append(arg);
		for(int i=0; i<arrayDepth; i++) {
			sb.append("[]");
		}
		return sb.toString();
	}

	private static List<String> parseArgs(String args) {
		int arrayDepth = 0; // represents the number of '[' characters encountered
		int classStart = -1; // represents the beginning of parsing a 'L' object

		List<String> newArgs = new ArrayList<String>();
		char[] argsArray = args.toCharArray();
		for(int i=0; i<argsArray.length; i++) {
			char c = argsArray[i];
			if(classStart >= 0) {
				if(c == ';') {
					// build the arg and reset the state
					newArgs.add(buildArg(convertClass(args.substring(classStart, i)), arrayDepth));
					arrayDepth = 0;
					classStart = -1;
				}
			} else {
				switch(c) {
				case '[':
					arrayDepth++; // increment the number of '[' encountered
					break;
				case 'L':
					classStart = i+1; // class name starts after the 'L'
					break;
				default:
					// build the arg and reset the state
					newArgs.add(buildArg(convertBasicType(c), arrayDepth));
					arrayDepth = 0;
				}
			}
		}
		return newArgs;
	}

	public static String convert(String methodSignature) {
		if(methodSignature == null) {
			return null;
		}

		// get enclosing class
		String[] classTokens = methodSignature.split("\\.");
		if(classTokens.length != 2) {
			throw new RuntimeException("Class tokens length not 2: " + methodSignature);
		}
		String enclosingClass = convertClass(classTokens[0]);

		// get method name
		String[] methodTokens = classTokens[1].split("\\(");
		if(methodTokens.length != 2) {
			throw new RuntimeException("Method tokens length not 2: " + methodSignature);
		}
		String methodName = methodTokens[0];

		// get arguments
		String[] argTokens = methodTokens[1].split("\\)");
		if(argTokens.length != 2) {
			throw new RuntimeException("Arg tokens length not 2: " + methodSignature);
		}
		List<String> argTypes = parseArgs(argTokens[0]);

		// get return type
		String returnType = convertType(argTokens[1].split(";")[0]);

		// convert to shord signature
		return getShordSignature(enclosingClass, methodName, argTypes, returnType);
	}
}
