class ProcessBuilder
{
	@STAMP(flows={@Flow(from="command",to="!PROCESS")})
	public  ProcessBuilder(java.lang.String... command) { }

	@STAMP(flows={@Flow(from="command",to="!PROCESS")})
	public  ProcessBuilder(java.util.List<java.lang.String> command) { }
	
	@STAMP(flows={@Flow(from="command",to="!PROCESS")})
	public  java.lang.ProcessBuilder command(java.lang.String... command) { return null; }

	@STAMP(flows={@Flow(from="command",to="!PROCESS")})
	public  java.lang.ProcessBuilder command(java.util.List<java.lang.String> command) { return null; }

    public java.lang.Process start() throws java.io.IOException {
		return new FakeProcess();
    }

}
