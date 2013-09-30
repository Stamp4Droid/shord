class Class
{

	public java.lang.String name = new java.lang.String();

	public static java.lang.Class<?> forName(java.lang.String className) throws java.lang.ClassNotFoundException {
        	java.lang.Class k = new java.lang.Class();
		k.name = className;
        	return k;
	}
}
