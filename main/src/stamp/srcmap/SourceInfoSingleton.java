package stamp.srcmap;

import stamp.srcmap.javasource.JavaSourceInfo;

public class SourceInfoSingleton {
	public static enum SourceInfoType {
		JIMPLE, JAVA;

		public SourceInfo getSourceInfo() {
			switch(this) {
			case JAVA:
				return new JavaSourceInfo();
			default:
				throw new RuntimeException("Source info " + this.toString() + " not implemented!");
			}
		}
	}

	private static SourceInfo s = null;
	
	public static void setSourceInfoType(SourceInfoType type) {
		s = type.getSourceInfo();
	}
	
	public static SourceInfo v() {
		if(s == null) {
			s = new JavaSourceInfo();
		}
		return s;
	}
}
