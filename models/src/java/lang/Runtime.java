class Runtime
{
	public  java.lang.Process exec(java.lang.String[] progArray) throws java.io.IOException { return new FakeProcess(); }
	public  java.lang.Process exec(java.lang.String[] progArray, java.lang.String[] envp) throws java.io.IOException { return new FakeProcess(); }
	public  java.lang.Process exec(java.lang.String[] progArray, java.lang.String[] envp, java.io.File directory) throws java.io.IOException { return new FakeProcess(); }
	public  java.lang.Process exec(java.lang.String prog) throws java.io.IOException { return new FakeProcess(); }
	public  java.lang.Process exec(java.lang.String prog, java.lang.String[] envp) throws java.io.IOException { return new FakeProcess(); }
	public  java.lang.Process exec(java.lang.String prog, java.lang.String[] envp, java.io.File directory) throws java.io.IOException { return new FakeProcess(); }
}
