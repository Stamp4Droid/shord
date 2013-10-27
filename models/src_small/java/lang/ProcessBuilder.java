class ProcessBuilder
{
	public  ProcessBuilder(java.lang.String... command) { }

	public  ProcessBuilder(java.util.List<java.lang.String> command) { }
	
	public  java.lang.ProcessBuilder command(java.lang.String... command) { return this; }

	public  java.lang.ProcessBuilder command(java.util.List<java.lang.String> command) { return this; }

    public java.lang.Process start() throws java.io.IOException {
		return new FakeProcess();
    }

}
