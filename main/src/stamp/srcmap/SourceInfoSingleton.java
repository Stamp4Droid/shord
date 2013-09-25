package stamp.srcmap;

import stamp.srcmap.sourceinfo.SourceInfo;
import stamp.srcmap.sourceinfo.javasource.JavaSourceInfo;

public class SourceInfoSingleton {
	public static enum SourceInfoType {
		JIMPLE, JAVA;
	}

	private static JavaSourceInfo javaSourceInfo = null;
	private static SourceInfoType sourceInfoType = SourceInfoType.JAVA;
	
	public static void setSourceInfoType(SourceInfoType type) {
		sourceInfoType = type;
	}
	
	public static JavaSourceInfo getJavaSourceInfo() {
		if(javaSourceInfo == null) {
			javaSourceInfo = new JavaSourceInfo();
		}
		return javaSourceInfo;
	}
	
	public static SourceInfo v() {
		switch(sourceInfoType) {
		case JAVA:
			return getJavaSourceInfo();
		default:
			throw new RuntimeException("Source info " + sourceInfoType.toString() + " not implemented!");
		}
	}
}
