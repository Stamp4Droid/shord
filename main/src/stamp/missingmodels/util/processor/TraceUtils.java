package stamp.missingmodels.util.processor;

import java.util.ArrayList;
import java.util.List;

public class TraceUtils {
	
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
	
	/*
	public static void main(String[] args) {
		String methodSignature = "android/database/DatabaseUtils.longForQuery(Landroid/database/sqlite/SQLiteStatement;[Ljava/lang/String;)J";
		System.out.println(convert(methodSignature));
	}
	*/
}
